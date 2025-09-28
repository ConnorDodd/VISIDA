using VISIDA_API.Models;
using VISIDA_API.Models.FCT;
using VISIDA_API.Models.ExternalObjects;
using System.Web.Http;
using System.Web.Http.Description;
using System.Collections.Generic;
using System.Linq;
using System.Data.Entity;

namespace VISIDA_API.Controllers
{
    [Authorize(Roles = "admin,analyst,coordinator")]
    public class ExampleMeasuresController : ApiController
    {
        private VISIDA_APIContext db = new VISIDA_APIContext();

        // GET: api/ExampleMeasures
        [Route("api/ExampleMeasures/{tableId}")]
        public IHttpActionResult GetExampleFoodItems(int tableId)
        {
            var ret = db.ReferenceImageTypes.Include(x => x.Images).Where(x => x.Table != null && x.Table.Id == tableId).OrderBy(x => x.Name).ToList().Select(x => (EReferenceImageType)x).ToList();//.Select(x => new {x.Images, x.Name, x.Id}).ToList();
            //foreach (var e in ret)
            //    e.Images = e.Images.OrderBy(x => x.SizeGrams).ToList();

            return Ok(ret);
        }

        [HttpGet, ResponseType(typeof(ReferenceImageType)), Route("api/ExampleMeasures/Search")]
        public IHttpActionResult SearchExampleFoodComposition(string search)
        {
            //string[] examples = db.ExampleFoodItems.Select(x => x.Name).ToArray();
            search = search.ToLower().Replace(",", "");
            HashSet<string> hash = new HashSet<string>(search.Split(' '));
            ReferenceImageType match = null;
            int limit = 0;
            int distance = int.MaxValue;
            foreach (ReferenceImageType ex in db.ReferenceImageTypes)
            {
                HashSet<string> set = new HashSet<string>(ex.Name.Split(' '));
                var b = hash.Where(x => set.Contains(x));
                int count = b.Count();
                if (count >= limit)
                {
                    if (count > limit)
                        distance = int.MaxValue;
                    limit = count;
                    int tDist = int.MaxValue;
                    foreach (string s in set)
                    {
                        int index = search.IndexOf(s);
                        if (index >= 0 && index < tDist)
                            tDist = index;
                    }
                    if (tDist < distance)
                    {
                        distance = tDist;
                        match = ex;
                    }
                }
            }

            if (match == null)
                return BadRequest();
            else
                return Ok(match);
        }


        // POST: api/ExampleMeasures
        [ResponseType(typeof(ReferenceImageType))]
        public IHttpActionResult PostExampleFoodComposition(ReferenceImageType exampleFoodComposition)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            db.ReferenceImageTypes.Add(exampleFoodComposition);
            db.SaveChanges();

            return CreatedAtRoute("DefaultApi", new { id = exampleFoodComposition.Id }, exampleFoodComposition);
        }

        //// DELETE: api/ExampleMeasures/5
        //[ResponseType(typeof(ExampleFoodComposition))]
        //public IHttpActionResult DeleteExampleFoodComposition(int id)
        //{
        //    ExampleFoodComposition exampleFoodComposition = db.ExampleFoodItems.Find(id);
        //    if (exampleFoodComposition == null)
        //    {
        //        return NotFound();
        //    }

        //    db.ExampleFoodItems.Remove(exampleFoodComposition);
        //    db.SaveChanges();

        //    return Ok(exampleFoodComposition);
        //}

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                db.Dispose();
            }
            base.Dispose(disposing);
        }

        private bool ExampleFoodCompositionExists(int id)
        {
            return db.ReferenceImageTypes.Count(e => e.Id == id) > 0;
        }
    }
}