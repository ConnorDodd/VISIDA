using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using VISIDA_API.Models.InternalObjects;
using VISIDA_API.Models.ParseObjects;

namespace VISIDA_API.Controllers
{
    public class RecallController : AuthController
    {
        // POST: api/Recall
        [Route("api/Recall/{id}"), HttpPost]
        public IHttpActionResult Post([FromUri] int id, [FromBody]List<PHousehold> households)
        {
            var study = db.Studies.Find(id);
            if (study == null)
                return BadRequest();
            foreach (var hh in households)
            {
                Household household = hh;
                household.Guid = Guid.NewGuid().ToString();
                household.ParticipantId = "24HR_" + household.ParticipantId;

                foreach (var ir in household.ImageRecords)
                    ir.Is24HR = true;

                study.Households.Add(household);
            }

            db.SaveChanges();

            return Ok();
        }
    }
}
