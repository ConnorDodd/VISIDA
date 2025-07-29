using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using VISIDA_API.Models;
using VISIDA_API.Models.ExternalObjects;
using VISIDA_API.Models.InternalObjects;
using VISIDA_API.Models.InternalObjects.Portion;
using VISIDA_API.Models.User;
using System.Data.Entity;
using static VISIDA_API.Controllers.AuthController;

namespace VISIDA_API.Controllers
{
    [Authorize(Roles = "admin,analyst,coordinator,appuser")]
    public class PortionController : ApiController
    {
        protected VISIDA_APIContext db = new VISIDA_APIContext();
        protected LoginUser user;
        protected EnumRole AccessLevel;

        public PortionController()
        {
            if (string.IsNullOrEmpty(User.Identity.Name))
                throw new Exception("Session has expired. Please log in again");
            int id = int.Parse(User.Identity.Name);
            user = db.Users.Include(x => x.Role).FirstOrDefault(x => x.Id == id);

            string role = user.Role.Role;
            if (role.Equals("admin"))
                AccessLevel = EnumRole.Admin;
            else if (role.Equals("coordinator"))
                AccessLevel = EnumRole.Coordinator;
            else if (role.Equals("analyst"))
                AccessLevel = EnumRole.Analyst;
            else if (role.Equals("appuser"))
                AccessLevel = EnumRole.AppUser;
        }

        // GET: api/Portion
        [HttpGet, Authorize(Roles = "admin,analyst,coordinator,appuser")]
        public IHttpActionResult Get()
        {
            var studies = db.WorkAssignations.Include(x => x.LoginUser).Include(x => x.Study).Include(x => x.Study.Households).Include(x => x.Study.Households.Select(y => y.ImageRecords))
                .Include(x => x.Study.Households.Select(y => y.ImageRecords.Select(z => z.FoodItems)))
                .Include(x => x.Study.Households.Select(y => y.ImageRecords.Select(z => z.FoodItems.Select(zz => zz.FoodComposition))))
                .Where(x => x.LoginUser.Id == user.Id).Select(x => x.Study);
            //var studies = user.Assignments.Where(x => x.AccessLevel != WorkAssignation.AccessLevels.View).Select(x => x.Study);
            var items = studies.SelectMany(x => x.Households).SelectMany(x => x.ImageRecords).Where(x => x.ImageUrl != null && x.IsFiducialPresent).SelectMany(x => x.FoodItems)
                .Where(x => x.Priority > 100 && x.FoodCompositionId.HasValue && x.FoodComposition.Density.HasValue).ToList();
            var a = studies.ToList();
            var b = studies.SelectMany(x => x.Households).SelectMany(x => x.ImageRecords).Where(x => x.ImageUrl != null && x.IsFiducialPresent).ToList();
            int count = items?.Count() ?? 0;
            if (count > 0)
            {
                var index = new Random().Next(count);
                var portion = (EPortion)items.ElementAt(index);
                portion.Url = portion.Url.Replace("http://", "https://");

                return Ok(portion);
            }
            return BadRequest("Couldn't find a valid food item");
        }

        // GET: api/Portion/id
        [HttpGet, Route("api/Portion/{id}")]
        [Authorize(Roles = "admin,analyst,coordinator,appuser")]
        public IHttpActionResult Get(int id)
        {
            //IEnumerable<string> headerValues;
            //if (!Request.Headers.TryGetValues("Authorization", out headerValues))
            //    return Unauthorized();
            //var token = headerValues.FirstOrDefault();
            //if (token == null || !token.Equals("bearer 5a9881ff2c264dd0a7b9e564c7226db9"))
            //    return Unauthorized();

            var item = db.FoodItems.Find(id);
            if (item == null)
                return BadRequest();

            var portion = (EPortion)item;
            portion.Url = portion.Url.Replace("http://", "https://");
            return Ok(portion);
        }

        // PUT: api/Portion
        [Authorize(Roles = "admin,analyst,coordinator,appuser")]
        public IHttpActionResult Put([FromBody]EPortion portion)
        {
            FoodItem fi = db.FoodItems.Include(x => x.ImageRecord).Include(x => x.ImageRecord.Household).Include(x => x.ImageRecord.Household.Study).Include(x => x.ImageRecord.Updates)
                .FirstOrDefault(x => x.Id == portion.FoodItemId);
            if (fi == null)
                return BadRequest("Id is invalid");

            WorkAssignation rights = user.Assignments.FirstOrDefault(x => x.Study == fi.ImageRecord.Household.Study);
            if (rights == null || rights.AccessLevel == WorkAssignation.AccessLevels.View)
            {
                return Unauthorized();
            }

            var quantityGrams = Math.Round(portion.Volume * (fi.FoodComposition.Density ?? 0), 2);

            //var update = new PortionUpdate() { FoodItemId = fi.Id, UserId = user.Id, Volume = portion.Volume, Message = string.Format("User {0} set the volume of {1} to {2}", user.UserName, portion.FoodItem, portion.Volume) };
            //fi.ImageRecord.PortionUpdates.Add(update);
            fi.ImageRecord.Updates.Add(new RecordHistory()
            {
                Action = RecordHistory.UpdateTypes.Quantify,
                FoodItemId = fi.Id,
                User = user,
                QuantityGrams = portion.Volume,
                ToolMeasure = portion.Volume,
                ToolSource = "Volume input (mL)"
            });

            if (fi.ImageRecord.Household.Study.Gestalt && AccessLevel == EnumRole.Analyst)
            {
                var record = fi.ImageRecord;
                //Average all the attempts so far
                fi.QuantityGrams = record.Updates.Where(x => x.FoodItemId == fi.Id && x.Action == RecordHistory.UpdateTypes.Quantify && x.QuantityGrams > 0).Average(x => x.QuantityGrams ?? 0);
                double total = 0;
                int count = 0;
                List<int> done = new List<int>();
                foreach (var h in record.Updates.OrderByDescending(x => x.Time))
                {
                    if (h.FoodItemId == fi.Id && h.Action == RecordHistory.UpdateTypes.Quantify && h.QuantityGrams > 0)
                    {
                        if (!done.Contains(h.User.Id))
                        {
                            total += h.QuantityGrams ?? 0;
                            count++;
                            done.Add(h.User.Id);
                        }
                    }
                }
                fi.QuantityGrams = total / count;
            }
            else
                fi.QuantityGrams = quantityGrams;


            fi.MeasureType = "Volume input (mL)";
            fi.MeasureCount = portion.Volume;

            fi.ToolMeasure = portion.ToolVolume > 0 ? portion.ToolVolume : portion.Volume;
            fi.ToolSource = portion.ToolName ?? "External Quantification";

            var hm = fi.ImageRecord.Homography;
            hm.TopLeftX = portion.TopLeftX;
            hm.TopLeftY = portion.TopLeftY;
            hm.TopRightX = portion.TopRightX;
            hm.TopRightY = portion.TopRightY;
            hm.BottomLeftX = portion.BottomLeftX;
            hm.BottomLeftY = portion.BottomLeftY;
            hm.BottomRightX = portion.BottomRightX;
            hm.BottomRightY = portion.BottomRightY;

            db.SaveChanges();

            return Ok();
        }

        public enum RejectReasons { None, Unsure, ItemMissing, FiducialMissing };
        //DELETE: api/Portion/{id}?reason=
        [Authorize(Roles = "admin,analyst,coordinator,appuser")]
        [HttpDelete, Route("api/Portion/{id}")]
        public IHttpActionResult Delete([FromUri] int id, [FromUri] string reason)
        {
            var foodItem = db.FoodItems.Find(id);
            if (foodItem == null)
                return BadRequest("Id is invalid or missing");

            WorkAssignation rights = user.Assignments.FirstOrDefault(x => x.Study == foodItem.ImageRecord.Household.Study);
            if (rights == null || rights.AccessLevel == WorkAssignation.AccessLevels.View)
            {
                return Unauthorized();
            }

            RejectReasons rejectReason;
            if (string.IsNullOrEmpty(reason) || !Enum.TryParse(reason, out rejectReason))
                return BadRequest("Reject reason is invalid or missing. <url>?reason=None|Unsure|ItemMissing|FiducialMissing");

            //var update = new PortionUpdate() { FoodItemId = foodItem.Id, UserId = user.Id, Volume = 0, Message = string.Format("User {0} rejected the item {1} for reason {2}", user.UserName, foodItem.Name, reason) };
            //foodItem.ImageRecord.PortionUpdates.Add(update);
            foodItem.ImageRecord.Updates.Add(new RecordHistory()
            {
                Action = RecordHistory.UpdateTypes.Delete,
                Time = DateTime.Now,
                User = user,
                RejectReason = rejectReason,
                FoodItemId = foodItem.Id
            });

            switch (rejectReason)
            {
                case RejectReasons.ItemMissing:
                    foodItem.Priority = -1;
                    db.SaveChanges();
                    break;
                case RejectReasons.FiducialMissing:
                    var imageRecord = foodItem.ImageRecord;
                    imageRecord.IsFiducialPresent = false;
                    db.SaveChanges();
                    break;
                case RejectReasons.Unsure:
                    db.SaveChanges();
                    break;
                default:
                    break;
            }

            return Ok();
        }

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
