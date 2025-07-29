using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Data.Entity.Infrastructure;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using System.Web.Http.Description;
using VISIDA_API.Models;
using VISIDA_API.Models.ExternalObjects;
using VISIDA_API.Models.FCT;

namespace VISIDA_API.Controllers
{

    public class FoodCompositionTablesController : AuthController
    {
        private VISIDA_APIContext db = new VISIDA_APIContext();

        // GET: api/FoodCompositionTables
        public IHttpActionResult GetFoodCompositionTables()
        {
            List<EFoodCompositionTable> tables = new List<EFoodCompositionTable>();
            if (user.Role.Role.Equals("admin"))
                tables = db.FoodCompositionTables.Where(x => !x.Deleted).ToList().Select(x => new EFoodCompositionTable(x.Id, x.Name, x.IsPublic)).ToList();
            else
                tables = user.Assignments.Select(x => x.Study?.FoodCompositionTable).ToList().Where(x => x != null).Select(x => new EFoodCompositionTable(x.Id, x.Name, x.IsPublic)).ToList();

            return Ok(tables);
        }

        [HttpGet, Route("api/FoodCompositionTables/Public")]
        public IHttpActionResult GetPublicFoodCompositionTables()
        {
            List<EFoodCompositionTable> tables = db.FoodCompositionTables.Where(x => x.IsPublic).ToList().Select(x => new EFoodCompositionTable(x.Id, x.Name, x.IsPublic)).ToList();

            return Ok(tables);
        }


        // GET: api/FoodCompositionTables/5
        [ResponseType(typeof(FoodCompositionTable))]
        public IHttpActionResult GetFoodCompositionTable(int id)
        {
            FoodCompositionTable foodCompositionTable = db.FoodCompositionTables.Find(id);
            foodCompositionTable.FoodCompositions = foodCompositionTable.FoodCompositions.OrderBy(x => x.Name).ToList();
            if (foodCompositionTable == null)
            {
                return NotFound();
            }

            return Ok(foodCompositionTable); 
        }

        // GET: api/FoodCompositionTables/5
        [ResponseType(typeof(IEnumerable<string>)), Route("api/FoodCompositionTables/{id}/Names")]
        public IHttpActionResult GetFoodCompositionTableNames(int id)
        {
            FoodCompositionTable foodCompositionTable = db.FoodCompositionTables.Find(id);
            if (foodCompositionTable == null)
            {
                return NotFound();
            }
            var names = foodCompositionTable.FoodCompositions.Select(x => x.Name).OrderBy(x => x).ToList();

            return Ok(names);
        }

        // GET: api/FoodCompositionTables/5
        [ResponseType(typeof(IEnumerable<string>)), Route("api/FoodCompositionTables/{id}/Measures")]
        public IHttpActionResult GetFoodCompositionTableMeasures(int id)
        {
            FoodCompositionTable foodCompositionTable = db.FoodCompositionTables.Include(x => x.StandardMeasures).Include(x => x.FoodCompositions).FirstOrDefault(x => x.Id == id);
            if (foodCompositionTable == null)
            {
                return NotFound();
            }
            var measures = foodCompositionTable.FoodCompositions.Select(x => new { x.Id, x.Name, x.Density, x.Measures, x.AlternateName/*, x.MeasuresML */}).OrderBy(x => x.Name).ToList();
            var standards = foodCompositionTable.StandardMeasures.Select(x => (EStandardMeasure)x).ToList();

            return Ok(new { measures, standards });
        }

        // PUT: api/FoodCompositionTables/5
        [ResponseType(typeof(void))]
        public IHttpActionResult PutFoodCompositionTable(int id, FoodCompositionTable foodCompositionTable)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            if (id != foodCompositionTable.Id)
            {
                return BadRequest();
            }

            db.Entry(foodCompositionTable).State = EntityState.Modified;

            try
            {
                db.SaveChanges();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!FoodCompositionTableExists(id))
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

        // POST: api/FoodCompositionTables
        [Authorize(Roles = "admin")]
        [ResponseType(typeof(FoodCompositionTable))]
        public IHttpActionResult PostFoodCompositionTable(FoodCompositionTable fct)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }
            string name = fct.Name;
            if (db.FoodCompositionTables.FirstOrDefault(x => x.Name.Equals(name)) != null)
                return BadRequest("There is already a table with that name");
            db.FoodCompositionTables.Add(fct);
            db.SaveChanges();

            return CreatedAtRoute("DefaultApi", new { id = fct.Id }, fct);
            //db.Configuration.AutoDetectChangesEnabled = false;
            //db.Configuration.ValidateOnSaveEnabled = false;
        }

        // DELETE: api/FoodCompositionTables/5
        [ResponseType(typeof(FoodCompositionTable))]
        public IHttpActionResult DeleteFoodCompositionTable(int id)
        {
            FoodCompositionTable foodCompositionTable = db.FoodCompositionTables.Find(id);
            if (foodCompositionTable == null)
            {
                return NotFound();
            }

            //db.FoodCompositionTables.Remove(foodCompositionTable);
            foodCompositionTable.Deleted = true;
            db.SaveChanges();

            return Ok(foodCompositionTable);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                db.Dispose();
            }
            base.Dispose(disposing);
        }

        private bool FoodCompositionTableExists(int id)
        {
            return db.FoodCompositionTables.Count(e => e.Id == id) > 0;
        }
    }
}