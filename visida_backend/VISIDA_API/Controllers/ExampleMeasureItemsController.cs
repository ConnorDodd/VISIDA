using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Data.Entity.Infrastructure;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Threading.Tasks;
using System.Web;
using System.Web.Http;
using System.Web.Http.Description;
using VISIDA_API.Helpers;
using VISIDA_API.Models;
using VISIDA_API.Models.FCT;

namespace VISIDA_API.Controllers
{
    [Authorize(Roles = "admin,analyst,coordinator")]
    public class ExampleMeasureItemsController : ApiController
    {
        private VISIDA_APIContext db = new VISIDA_APIContext();

        //// PUT: api/ExampleMeasureItems/5
        //[ResponseType(typeof(void))]
        //public IHttpActionResult PutMeasure(int id, Measure measure)
        //{
        //    if (!ModelState.IsValid)
        //    {
        //        return BadRequest(ModelState);
        //    }

        //    if (id != measure.Id)
        //    {
        //        return BadRequest();
        //    }

        //    db.Entry(measure).State = EntityState.Modified;

        //    try
        //    {
        //        db.SaveChanges();
        //    }
        //    catch (DbUpdateConcurrencyException)
        //    {
        //        if (!MeasureExists(id))
        //        {
        //            return NotFound();
        //        }
        //        else
        //        {
        //            throw;
        //        }
        //    }

        //    return StatusCode(HttpStatusCode.NoContent);
        //}
        [AllowAnonymous]
        [Route("api/ExampleMeasureItem/PostMeasureImage")]
        public async Task<IHttpActionResult> PostMeasureImage()
        {
            string url = "";
            try
            {
                var httpRequest = HttpContext.Current.Request;

                foreach (string fileName in httpRequest.Files)
                {
                    HttpPostedFile postedFile = httpRequest.Files[fileName];
                    if (postedFile != null && postedFile.ContentLength > 0)
                    {
                        //Check image type here
                        if (!postedFile.ContentType.Substring(0, postedFile.ContentType.IndexOf('/')).Equals("image"))
                            return BadRequest("Not a valid content type.");

                        url = await StorageHelper.UploadFileToStorage(postedFile.InputStream, postedFile.FileName, StorageHelper.MEASURE_CONTAINER);//CloudImage.UploadImage(postedFile.FileName, postedFile);
                    }
                }

                return Ok(url);
            }
            catch (Exception e)
            {
                return BadRequest("An error occurred while uploading image.");
            }                        
        }

        // POST: api/ExampleMeasureItems
        [ResponseType(typeof(ReferenceImage))]
        public IHttpActionResult PostMeasure(ReferenceImage measure)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            db.ReferenceImages.Add(measure);
            db.SaveChanges();

            return CreatedAtRoute("DefaultApi", new { id = measure.Id }, measure);
        }

        // DELETE: api/ExampleMeasures/5
        [HttpDelete, Route("api/ExampleMeasureItem/{id}"), ResponseType(typeof(ReferenceImage))]
        public IHttpActionResult DeleteMeasure(int id)
        {
            ReferenceImage measure = db.ReferenceImages.Find(id);
            if (measure == null)
                return NotFound();

            db.ReferenceImages.Remove(measure);
            db.SaveChanges();

            return Ok(measure);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                db.Dispose();
            }
            base.Dispose(disposing);
        }

        private bool MeasureExists(int id)
        {
            return db.ReferenceImages.Count(e => e.Id == id) > 0;
        }
    }
}