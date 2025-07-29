using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using VISIDA_API.Models.ExternalObjects;
using VISIDA_API.Models.FCT;

namespace VISIDA_API.Controllers
{
    public class RDAController : AuthController
    {
        // GET: api/RDA
        public IHttpActionResult Get()
        {
            return Ok(db.RDAModels.ToList().Select(x => (ERDAModel)x).ToList());
        }

        // POST: api/RDA
        public IHttpActionResult Post([FromBody] ERDAModel data)
        {
            RDAModel model = new RDAModel()
            {
                Name = data.Name
            };

            db.RDAModels.Add(model);
            try
            {
            db.SaveChanges();
                
            } catch (Exception e)
            {
                var me = e.Message;
                return InternalServerError();
            }

            return Ok(model);
        }

        // PUT: api/RDA/5
        public void Put(int id, [FromBody]ERDAModel model)
        {
            var dbModel = db.RDAModels.Find(id);
            if (dbModel == null)
                return;

            //dbModel.RDAs.Clear();
            db.RDAs.RemoveRange(dbModel.RDAs.ToArray());
            dbModel.RDAs = model.RDAs.Select(x => (RDA)x).ToList();
            db.SaveChanges();
        }

        // DELETE: api/RDA/5
        public void Delete(int id)
        {
        }
    }
}
