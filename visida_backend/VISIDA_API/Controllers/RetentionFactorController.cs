using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using VISIDA_API.Models;
using VISIDA_API.Models.FCT;

namespace VISIDA_API.Controllers
{
    [Authorize(Roles = "admin,analyst,coordinator")]
    public class RetentionFactorController : ApiController
    {
        private VISIDA_APIContext db = new VISIDA_APIContext();

        // GET: api/RetentionFactor
        public IEnumerable<RetentionFactor> Get()
        {
            return db.RetentionFactors;
        }
    }
}
