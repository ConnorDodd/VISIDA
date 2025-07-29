using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Text.RegularExpressions;
using System.Web;
using System.Web.Http;
using VISIDA_API.Models;
using VISIDA_API.Models.ExternalObjects;
using VISIDA_API.Models.InternalObjects;
using VISIDA_API.Models.User;

//Advices are basically FAQs that are tangentially linked to specific Food Compositions, attached by keywords in the FCD name

namespace VISIDA_API.Controllers
{
    public class AdviceController : ApiController
    {
        private VISIDA_APIContext db = new VISIDA_APIContext();

        // GET: api/Advice
        [Authorize(Roles = "admin,analyst,coordinator")]
        public List<Advice> Get([FromUri]PagingModel paging)
        {
            try
            {
                return new List<Advice>();
                List<Advice> ret = new List<Advice>();
                var records = db.Advices.Where(x => x.Id > 0);

                string table = HttpContext.Current.Request.Params["table"];
                int tableId;
                if (table != null && int.TryParse(table, out tableId))
                    records = records.Where(x => x.TableId == tableId);

                Regex rgx = new Regex("[^a-zA-Z0-9 -]"); //Remove any weird characters so it can be compared properly
                string search = HttpContext.Current.Request.Params["search"];
                if (!string.IsNullOrEmpty(search))
                {
                    var matches = new List<Advice>();
                    search = rgx.Replace(search, "");
                    var terms = search.Split(' '); //Split search into space separated word fragments

                    foreach (var term in terms)
                    {
                        matches.AddRange(records.Where(x => x.Description.Contains(term)));
                        //records = records.Where(x => x.Description.Contains(term));
                    }

                    matches = matches.Distinct().ToList();
                    //Create a regex that matches anything starting with any of the search fragments
                    var sreg = new Regex(search.Replace(' ', '|'), RegexOptions.IgnoreCase);
                    //Order all matches by how many search terms they match
                    matches = matches.OrderByDescending(x => sreg.Matches(x.Description).Count).ToList();

                    paging.TotalCount = matches.Count;
                    ret = (List<Advice>)paging.GetPage(matches); //Page to only send a certain amount
                }
                else
                {
                    paging.TotalCount = records.Count();
                    records = records.OrderBy(x => x.Description);
                    ret = paging.GetPage(records).ToList();
                }

                object meta = paging.GetMetadata();
                HttpContext.Current.Response.Headers.Add("paging", JsonConvert.SerializeObject(meta));
                HttpContext.Current.Response.Headers.Add("Access-Control-Expose-Headers", "paging");// TODO Import household ids

                return ret;
            }
            catch (Exception e)
            {
                throw;
            }
        }

        // POST: api/Advice
        [Authorize(Roles = "admin,analyst,coordinator")]
        public IHttpActionResult Post([FromBody]Advice advice)
        {
            db.Advices.Add(advice);
            db.SaveChanges();
            return Ok();
        }

        // PUT: api/Advice/5
        //public void Put(int id, [FromBody]string value)
        //{
        //}

        //// DELETE: api/Advice/5
        //public void Delete(int id)
        //{
        //}

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                db.Dispose();
            }
            base.Dispose(disposing);
        }
    }
}
