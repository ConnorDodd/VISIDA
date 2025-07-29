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
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Controllers
{
    public class EatOccasionsController : ApiController
    {
        private VISIDA_APIContext db = new VISIDA_APIContext();

        // GET: api/EatOccasions
        public IEnumerable<EatOccasion> GetEatOccasions()
        {
            return db.EatOccasions;
        }

        // GET: api/EatOccasions/5
        [ResponseType(typeof(EatOccasion))]
        public IHttpActionResult GetEatOccasion(int id)
        {
            EatOccasion eatOccasion = db.EatOccasions.Find(id);
            if (eatOccasion == null)
            {
                return NotFound();
            }

            return Ok(eatOccasion);
        }

        // PUT: api/EatOccasions/5
        [ResponseType(typeof(void))]
        public IHttpActionResult PutEatOccasion(int id, EatOccasion eatOccasion)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            if (id != eatOccasion.Id)
            {
                return BadRequest();
            }

            db.Entry(eatOccasion).State = EntityState.Modified;

            try
            {
                db.SaveChanges();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!EatOccasionExists(id))
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

        // POST: api/EatOccasions
        [ResponseType(typeof(EatOccasion))]
        public IHttpActionResult PostEatOccasion(EatOccasion eatOccasion)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            db.EatOccasions.Add(eatOccasion);
            db.SaveChanges();

            return CreatedAtRoute("DefaultApi", new { id = eatOccasion.Id }, eatOccasion);
        }

        // DELETE: api/EatOccasions/5
        [ResponseType(typeof(EatOccasion))]
        public IHttpActionResult DeleteEatOccasion(int id)
        {
            EatOccasion eatOccasion = db.EatOccasions.Find(id);
            if (eatOccasion == null)
            {
                return NotFound();
            }

            db.EatOccasions.Remove(eatOccasion);
            db.SaveChanges();

            return Ok(eatOccasion);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                db.Dispose();
            }
            base.Dispose(disposing);
        }

        private bool EatOccasionExists(int id)
        {
            return db.EatOccasions.Count(e => e.Id == id) > 0;
        }
    }
}