using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Data.Entity.Infrastructure;
using System.Diagnostics;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using System.Web.Http.Description;
using VISIDA_API.Models;
using VISIDA_API.Models.FCT;
using VISIDA_API.Models.StringUtils;
using VISIDA_API.Models.User;

namespace VISIDA_API.Controllers
{
    public class FoodCompositionsController : AuthController
    {
        [Route("api/FoodCompositions/PostFoodItems")]
        public IHttpActionResult PostFoodItems()//[FromBody] FoodCompositionUpdate update)
        {
            FoodCompositionUpdate update = null;

            try
            {
                //Read with custom serializer settings
                string json = Request.Content.ReadAsStringAsync().Result;
                update = JsonConvert.DeserializeObject<FoodCompositionUpdate>(
                    json,
                    new JsonSerializerSettings
                    {
                        NullValueHandling = NullValueHandling.Ignore,
                        //DefaultValueHandling = DefaultValueHandling.Include,
                        Error = (sender, args) =>
                        {
                            //This gets triggered if a field fails to parse, like a double as 2.00.23.
                            FoodComposition comp = args.CurrentObject as FoodComposition;
                            System.Reflection.PropertyInfo info = typeof(FoodComposition).GetProperty(args.ErrorContext.Member.ToString(), System.Reflection.BindingFlags.IgnoreCase | System.Reflection.BindingFlags.Public | System.Reflection.BindingFlags.Instance);
                            Type t = info.PropertyType;
                            var nt = Nullable.GetUnderlyingType(t);
                            //If the underlying type of the failed parse object is a double, just set it to zero
                            if (comp != null && nt == typeof(double))
                            {
                                info.SetValue(comp, null);
                                args.ErrorContext.Handled = true;
                            }
                        }
                    }
                );
            }
            catch (Exception e)
            {
                return BadRequest();
            }

            Stopwatch sw = new Stopwatch();
            sw.Start();

            update.UpdatedBy = user;
            update.UpdatedAs = user.Role.Role;
            update.Date = DateTime.Now;

            FoodCompositionTable table = db.FoodCompositionTables.FirstOrDefault(x => x.Id == update.TableId);
            if (table == null)
                return BadRequest();


            //update.FoodCompositions = update.FoodCompositions.GroupBy(x => x.Name).First().ToList(); //Remove duplicates in list
            var removedDupes = update.FoodCompositions.Distinct(new FoodComposition.FoodCompositionComparer());
            update.FoodCompositions = removedDupes.ToList();


            if (!update.Overwrite)
            {
                var ids = update.FoodCompositions.Select(u => u.OriginId).ToArray();
                var existing = table.FoodCompositions.Where(u => ids.Contains(u.OriginId));
                //Only include new rows
                string[] duplicates = existing.Select(u => u.OriginId).ToArray();
                update.FoodCompositions = update.FoodCompositions.Where(u => !duplicates.Contains(u.OriginId)).ToList();
            }

            //Set creation date of new items
            foreach (var f in update.FoodCompositions)
            {
                f.ModifiedDate = update.Date;
                f.Table_Id = table.Id; //Set this to an int instead so the reference doesn't disappear when db is cleared in the loop

                if (f.OriginId == null)
                    f.OriginId = Guid.NewGuid().ToString().Substring(0, 8);
            }

            int count = 0;
            if (update.Overwrite)
            {
                VISIDA_APIContext context = null;
                try
                {
                    context = new VISIDA_APIContext();
                    context.Configuration.AutoDetectChangesEnabled = false;

                    foreach (var fc in update.FoodCompositions)
                    {
                        ++count;
                        var dbfc = table.FoodCompositions.Where(x => (fc.OriginId != null && fc.OriginId.Equals(x.OriginId)) || (x.Name.Equals(fc.Name))).FirstOrDefault();
                            //.Where(x => x.Name.Equals(fc.Name) || (!string.IsNullOrEmpty(fc.OriginId) && !string.IsNullOrEmpty(x.OriginId) && x.OriginId.Equals(fc.OriginId))).FirstOrDefault();
                        if (dbfc != null)
                        {
                            dbfc.OverwriteValues(fc);
                        }
                        else
                        {
                            context.Set<FoodComposition>().Add(fc);
                        }

                        if (count % 100 == 0)
                        {
                            context.SaveChanges();
                            context.Dispose();
                            context = new VISIDA_APIContext();
                            context.Configuration.AutoDetectChangesEnabled = false;
                        }
                    }

                    context.SaveChanges();
                }
                finally
                {
                    if (context != null)
                        context.Dispose();
                }
                try
                {

                db.SaveChanges();


                    try
                    {
                        //foreach (var r in allRecipes)
                        //{
                        //    r.UpdateFoodComposition(db);
                        //}
                        db.SaveChanges();

                    }
                    catch (Exception e)
                    {
                        return BadRequest(e.Message);
                    }
                }
                catch (Exception e)
                {
                    throw;
                }
            }
            else
            {
                VISIDA_APIContext context = null;

                try
                {
                    context = new VISIDA_APIContext();
                    context.Configuration.AutoDetectChangesEnabled = false;

                    foreach (var fc in update.FoodCompositions)
                    {
                        ++count;
                        context.Set<FoodComposition>().Add(fc);

                        if (count % 100 == 0)
                        {
                            context.SaveChanges();
                        }
                    }

                    context.SaveChanges();
                }
                finally
                {
                    if (context != null)
                        context.Dispose();
                }
            }

            var result = sw.ElapsedMilliseconds;

            return Ok(count);
        }

        [HttpPut, Route("api/FoodCompositions/PutFoodComposition")]
        public IHttpActionResult PutFoodCompostion([FromBody] FoodComposition foodComposition)
        {
            var dbComp = db.FoodCompositions.Find(foodComposition.Id);
            if (dbComp == null)
                return BadRequest("The item could not be found");

            dbComp.OverwriteValues(foodComposition);

            //var usages = a //TODO

            db.SaveChanges();

            return Ok();
        }

        // GET: api/FoodCompositions
        public string[] GetFoodCompositions()
        {
            //return db.FoodCompositions.GroupBy(x => x.Name).Select(x => x.).OrderBy(x => x.Name).Select(x => x.Name).ToArray();
            return db.FoodCompositions.Select(x => x.Name).Distinct().ToArray();
        }

        [HttpPost, Route("api/FoodCompositionTables/{id}/GetNamesForIds")]
        public int[] GetIdsForNames([FromUri] int id, [FromBody] string[] names)
        {
            List<int> ids = new List<int>();
            var foodComps = db.FoodCompositions.Where(x => x.Table_Id == id).ToList();
            foreach (var name in names)
            {
                int fcid = -1;
                foreach(var fc in foodComps)
                {
                    if (fc.Name.Equals(name))
                    {
                        fcid = fc.Id;
                        break;
                    }
                }
                ids.Add(fcid);
            }
            return ids.ToArray();
        }

        //[Route("api/FoodCompositions/{id}/Measures"), ResponseType(typeof(ReferenceImageType))]
        //public IHttpActionResult GetFoodCompositionMeasures(int id)
        //{
        //    FoodComposition fc = db.FoodCompositions.Find(id);
        //    var words = fc.Name.Split(' ');
        //    string[] names = db.ExampleFoodItems.Select(x => x.Name).ToArray();
        //    Trie trie = new Trie();
        //    trie.Add(words);
        //    trie.Build();
        //    int best = -1;
        //    string bestName = null;
        //    foreach (string name in names)
        //    {
        //        int match = trie.Find(name).Count();
        //        if (match > best)
        //        {
        //            best = match;
        //            bestName = name;
        //        }
        //    }
        //    var example = db.ExampleFoodItems.FirstOrDefault(x => x.Name.Equals(bestName));
        //        //.Where(x => fc.Name.Contains(x.Name.Split(' '))).FirstOrDefault();

        //    if (example != null)
        //        return Ok(example);
        //    else
        //        return BadRequest();
        //}


        [Route("api/FoodCompositions/GetFoodCompositionsMatching"), ResponseType(typeof(List<FoodComposition>))]
        public IHttpActionResult GetFoodCompositionsMatching(string partial)
        {
            List<FoodComposition> foodComposition = db.FoodCompositions.Where(x => x.Name.ToLower().IndexOf(partial.ToLower()) > -1).ToList();

            return Ok(foodComposition);
        }

        [Route("api/FoodCompositions/GetFoodCompositionDensities"), ResponseType(typeof(string[]))]
        public IHttpActionResult GetFoodCompositionNames()
        {
            var types = db.FoodCompositions
                .Select(x => new { x.Name, x.Density })
                .OrderBy(x => x.Name).ToList();
            //.ToDictionary(x => x.Name, x => x.Density);

            return Ok(types);
        }

        [Route("api/FoodCompositions/FoodGroups"), ResponseType(typeof(FoodGroup[]))]
        public IHttpActionResult GetFoodGroups()
        {
            var foodGroups = db.FoodGroups.ToArray();
            return Ok(foodGroups);
        }

        // PUT: api/FoodCompositions/5
        [ResponseType(typeof(void))]
        public IHttpActionResult PutFoodComposition(int id, FoodComposition foodComposition)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);
            if (id != foodComposition.Id)
                return BadRequest();

            foodComposition.ModifiedDate = DateTime.Now;
            db.Entry(foodComposition).State = EntityState.Modified;

            try
            {
                db.SaveChanges();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!FoodCompositionExists(id))
                {
                    return NotFound();
                }
                else
                {
                    throw;
                }
            }

            return StatusCode(HttpStatusCode.NoContent);
        }

        // POST: api/FoodCompositions
        [ResponseType(typeof(FoodComposition))]
        public IHttpActionResult PostFoodComposition(FoodComposition foodComposition)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            foodComposition.ModifiedDate = DateTime.Now;

            db.FoodCompositions.Add(foodComposition);
            db.SaveChanges();

            return CreatedAtRoute("DefaultApi", new { id = foodComposition.Id }, foodComposition);
        }

        // DELETE: api/FoodCompositions/5
        [ResponseType(typeof(FoodComposition))]
        public IHttpActionResult DeleteFoodComposition(int id)
        {
            FoodComposition foodComposition = db.FoodCompositions.Find(id);
            if (foodComposition == null)
            {
                return NotFound();
            }

            db.FoodCompositions.Remove(foodComposition);
            db.SaveChanges();

            return Ok(foodComposition);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                db.Dispose();
            }
            base.Dispose(disposing);
        }

        private bool FoodCompositionExists(int id)
        {
            return db.FoodCompositions.Count(e => e.Id == id) > 0;
        }
    }
}