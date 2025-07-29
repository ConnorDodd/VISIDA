using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using System.Web.Http.Description;
using VISIDA_API.Models;
using VISIDA_API.Models.ExternalObjects;
using VISIDA_API.Models.InternalObjects;
using VISIDA_API.Models.InternalObjects.Testing;

namespace VISIDA_API.Controllers
{
    [Authorize(Roles = "admin,coordinator")]
    public class ReliabilityController : ApiController
    {
        private VISIDA_APIContext db = new VISIDA_APIContext();

        [ResponseType(typeof(EReliabilityTest))]
        public IHttpActionResult GetReliabilityTest(int id)
        {
            ReliabilityTest test = db.ReliabilityTests.Find(id);
            if (test == null)
                return NotFound();

            var e = (EReliabilityTest)test;
            e.ImageRecord = (EImageRecord)test.ImageRecord;
            e.KnownRecord = (EImageRecord)test.KnownRecord;

            var b = db.Timings.Where(x => x.FoodItem.ImageRecordId == test.ImageRecord.Id);
            e.Timings = b.ToList().Select(x => (ETiming)x).ToList();

            return Ok(e);
        }

        // POST: api/Reliability
        [ResponseType(typeof(ReliabilityTest))]
        public IHttpActionResult PostReliabilityTest(EReliabilityTest rt)
        {
            var known = db.ImageRecords.Find(rt.KnownRecordId);
            var user = db.Users.Find(rt.UserId);
            if (known == null || user == null)
                return BadRequest("Known record or user was not valid");
            if (known.RecordType != ImageRecord.RecordTypes.EatRecord)
                return BadRequest("Known record must not be ingredient or leftover");

            ImageRecord record = null;
            try
            {
                var now = DateTime.Now;
                record = new ImageRecord()
                {
                    ImageName = string.Format("ReliabilityTest_{0}_{1}", user.Id, now.ToString("dd/MM/yy_HH:mm")),
                    ImageUrl = known.ImageUrl,
                    AudioUrl = known.AudioUrl,
                    CaptureTime = now,
                    Homography = new ImageHomography(known.Homography),
                    RecordType = ImageRecord.RecordTypes.Test,
                    NTranscript = known.NTranscript,
                    Transcript = known.Transcript,
                    TextDescription = known.TextDescription,
                    IsFiducialPresent = known.IsFiducialPresent,
                };

                if (rt.TestType == ReliabilityTest.TestTypes.Quantify)
                {
                    record.FoodItems = new List<FoodItem>();
                    foreach (var item in known.FoodItems)
                    {
                        record.FoodItems.Add(new FoodItem()
                        {
                            CreateStart = now,
                            CreateEnd = now,
                            FoodCompositionId = item.FoodCompositionId,
                            Name = item.Name,
                            TagXPercent = item.TagXPercent,
                            TagYPercent = item.TagYPercent
                        });
                    }
                }

                var test = new ReliabilityTest()
                {
                    KnownRecord = known,
                    ImageRecord = record,
                    AssignedTo = user,
                    AssignedTime = rt.AssignedTime ?? DateTime.Now,
                    TestType = rt.TestType
                };

                db.ReliabilityTests.Add(test);
                db.SaveChanges();

                return CreatedAtRoute("DefaultApi", new { id = test.Id }, test.Id);
            }
            catch (Exception e)
            {
                //if (record != null && record.Id > 0)
                //    db.ImageRecords.Remove(record);

                throw e;
            }
        }

        ////PUT: api/Reliability/5
        //[ResponseType(typeof(ReliabilityTest))]
        //public IHttpActionResult PutReliabilityTest([FromUri]int id, [FromBody] EReliabilityTest rt)
        //{
        //    var test = db.ReliabilityTests.Find(id);
        //    if (test == null)
        //        return BadRequest();

            
        //}

        // DELETE: api/Reliability/5
        [ResponseType(typeof(ReliabilityTest))]
        public IHttpActionResult DeleteReliabilityTest(int id)
        {
            ReliabilityTest reliabilityTest = db.ReliabilityTests.Find(id);
            if (reliabilityTest == null)
            {
                return NotFound();
            }
            db.ReliabilityTests.Remove(reliabilityTest);
            db.SaveChanges();

            return Ok(reliabilityTest);
        }

        [Route("api/Reliability/Rules"), HttpPost]
        public IHttpActionResult PostReliabilityTestRule(ReliabilityTestRule r)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            r.StartDate = r.StartDate.Date;
            if (r.RepeatDate.HasValue)
                r.RepeatDate = r.RepeatDate.Value.Date;

            db.ReliabilityTestRules.Add(r);
            db.SaveChanges();

            return Ok(r.Id);
        }

        [Route("api/Reliability/Rules/{id}"), HttpDelete]
        public IHttpActionResult DeleteReliabilityTestRule(int id)
        {
            var rule = db.ReliabilityTestRules.Find(id);
            if (rule == null)
                return NotFound();

            db.ReliabilityTestRules.Remove(rule);
            db.SaveChanges();

            return Ok(rule);
        }

        [Route("api/Reliability/{id}/Score"), HttpPost]
        public IHttpActionResult PostTestScore([FromUri]int id, [FromBody] ReliabilityTestScore score)
        {
            var test = db.ReliabilityTests.Find(id);
            if (test == null || !(score.FoodItemId > 0))
                return BadRequest();

            if (score.Id > 0)
            {
                db.Entry(score).State = EntityState.Modified;
            }
            else
            {
                test.Scores.Add(score);
            }
            test.RecalculateAccuracy();

            db.SaveChanges();

            return Ok(score);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                db.Dispose();
            }
            base.Dispose(disposing);
        }

        private bool ReliabilityTestExists(int id)
        {
            return db.ReliabilityTests.Count(e => e.Id == id) > 0;
        }
    }
}