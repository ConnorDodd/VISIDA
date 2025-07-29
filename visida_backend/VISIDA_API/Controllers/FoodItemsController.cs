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
using VISIDA_API.Models.FCT;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Controllers
{
    public class FoodItemsController : AuthController
    {
        // GET: api/FoodItems
        public IEnumerable<FoodItem> GetFoodItems()
        {
            return db.FoodItems.ToList();
        }

        // GET: api/FoodItems/5
        [ResponseType(typeof(FoodItem))]
        public IHttpActionResult GetFoodItem(int id)
        {
            FoodItem foodItem = db.FoodItems.Find(id);
            if (foodItem == null)
            {
                return NotFound();
            }

            return Ok(foodItem);
        }

        // PUT: api/FoodItems/5
        [ResponseType(typeof(void))]
        public IHttpActionResult PutFoodItem(int id, FoodItem foodItem)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);
            if (id != foodItem.Id)
                return BadRequest();

            //Update createdby
            var original = db.FoodItems.Find(id);
            db.Entry(original).State = EntityState.Detached;
            var record = db.ImageRecords.Include(x => x.Household).Include(x => x.Household.Study).Include(x => x.Updates).FirstOrDefault(x => x.Id == foodItem.ImageRecordId);
            WorkAssignation rights = null;
            if (record.RecordType == ImageRecord.RecordTypes.Test)
            {
                rights = new WorkAssignation();
                if (IsAdmin)
                    rights.AccessLevel = WorkAssignation.AccessLevels.Coordinator;
                else
                    rights.AccessLevel = user.Tests.FirstOrDefault(x => x.ImageRecord.Id == record.Id).GetAssignationType();
            }
            else
                rights = user.Assignments.FirstOrDefault(x => x.Study == record.Household.Study);
            foodItem.CreatedBy = user;

            //temp workaround for wiremesh usability
            if (foodItem?.MeasureType?.Equals("Use wiremesh app") ?? false)
            {
                foodItem.Priority = 200;
                record.IsFiducialPresent = true;
            }
            else
                foodItem.Priority = 100;

            if (AccessLevel == EnumRole.Admin || AccessLevel == EnumRole.Coordinator)
                db.Entry(foodItem).State = EntityState.Modified;
            else
            {
                //var test = record.Updates.FirstOrDefault(x => x.FoodItemId == foodItem.Id && x.Action == RecordHistory.UpdateTypes.Quantify && (x.User.Role.Role.Equals("admin")
                //    || x.User.Role.Role.Equals("coordinator")));
                if (rights == null || rights.AccessLevel == WorkAssignation.AccessLevels.View)
                    return Unauthorized();
                else if (rights.AccessLevel == WorkAssignation.AccessLevels.Identify
                    || record.Updates.FirstOrDefault(x => x.FoodItemId == foodItem.Id && x.Action == RecordHistory.UpdateTypes.Quantify && (x.User.Role.Role.Equals("admin") 
                    || x.User.Role.Role.Equals("coordinator"))) != null) //If locked to identify or an admin has already overwritten
                {
                    foodItem.QuantityGrams = original.QuantityGrams;
                    foodItem.MeasureType = original.MeasureType;
                    foodItem.MeasureCount = original.MeasureCount;
                    db.Entry(foodItem).State = EntityState.Modified;
                }
                else if (rights.AccessLevel == WorkAssignation.AccessLevels.Quantify)
                {
                    foodItem.TagXPercent = original.TagXPercent;
                    foodItem.TagYPercent = original.TagYPercent;
                    foodItem.FoodCompositionId = original.FoodCompositionId;
                    foodItem.Name = original.Name;
                    db.Entry(foodItem).State = EntityState.Modified;
                }
                else
                    db.Entry(foodItem).State = EntityState.Modified;
            }

            if (foodItem.FoodCompositionId != original.FoodCompositionId)
                record.Updates.Add(new RecordHistory()
                {
                    Action = RecordHistory.UpdateTypes.Identify,
                    FoodItemId = foodItem.Id,
                    User = user,
                    FoodCompositionId = foodItem.FoodCompositionId
                });
            if (foodItem.QuantityGrams > 0)
            {
                if (record.Household.Study.Gestalt)
                {
                    RecordHistory history = record.Updates.Where(x => x.User.Id == user.Id && x.FoodItemId == foodItem.Id && x.Action == RecordHistory.UpdateTypes.Quantify)
                            .OrderByDescending(x => x.Time).FirstOrDefault();
                    if (history == null || (history != null && (history.QuantityGrams != foodItem.QuantityGrams || !foodItem.MeasureType.Equals(history.ToolSource))))
                    {
                        record.Updates.Add(new RecordHistory()
                        {
                            Action = RecordHistory.UpdateTypes.Quantify,
                            FoodItemId = foodItem.Id,
                            User = user,
                            QuantityGrams = foodItem.QuantityGrams,
                            ToolMeasure = foodItem.MeasureCount,
                            ToolSource = foodItem.MeasureType
                        });

                        if (AccessLevel == EnumRole.Analyst)
                        {
                            //Average all the attempts so far
                            //foodItem.QuantityGrams = record.Updates.Where(x => x.FoodItemId == foodItem.Id && x.Action == RecordHistory.UpdateTypes.Quantify && x.QuantityGrams > 0).Average(x => x.QuantityGrams ?? 0);
                            double total = 0;
                            int count = 0;
                            List<int> done = new List<int>();
                            foreach (var h in record.Updates.OrderByDescending(x => x.Time))
                            {
                                if (h.FoodItemId == foodItem.Id && h.Action == RecordHistory.UpdateTypes.Quantify && h.QuantityGrams > 0)
                                {
                                    if (!done.Contains(h.User.Id))
                                    {
                                        total += h.QuantityGrams ?? 0;
                                        count++;
                                        done.Add(h.User.Id);
                                    }
                                }
                            }
                            foodItem.QuantityGrams = total / count;
                            foodItem.MeasureCount = foodItem.QuantityGrams;
                            foodItem.MeasureType = "Gestalt estimation (g)";
                        }
                    }
                }
                else if (foodItem.QuantityGrams != original.QuantityGrams)
                {
                    record.Updates.Add(new RecordHistory()
                    {
                        Action = RecordHistory.UpdateTypes.Quantify,
                        FoodItemId = foodItem.Id,
                        User = user,
                        QuantityGrams = foodItem.QuantityGrams,
                        ToolMeasure = foodItem.MeasureCount,
                        ToolSource = foodItem.MeasureType
                    });
                }
            }

            try
            {
                UpdateRecipeFoodComp(foodItem);
                ImageRecordsController.RecalculateFoodItem(foodItem, db);

                //db.SaveChanges();
                if (foodItem.CreateStart != null && foodItem.CreateEnd != null)
                {
                    db.Timings.Add(new Timing()
                    {
                        Study = rights?.Study ?? record.Household?.Study,
                        Type = foodItem.QuantityGrams != original.QuantityGrams ? Timing.TimingType.QuantifyFoodItem : Timing.TimingType.IdentifyFoodItem, 
                        CreatedBy = user,
                        CreatedTime = foodItem.CreateStart,
                        TimeTaken = (int)foodItem.CreateEnd.Subtract(foodItem.CreateStart).TotalMilliseconds,
                        FoodItem = foodItem
                    });
                }

                db.SaveChanges();
            }
            catch (Exception e)
            {
                if (!FoodItemExists(id))
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

        // POST: api/FoodItems
        [ResponseType(typeof(FoodItem))]
        public IHttpActionResult PostFoodItem(FoodItem foodItem)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var imageRecord = db.ImageRecords.Find(foodItem.ImageRecordId);
            WorkAssignation rights;
            if (imageRecord.RecordType == ImageRecord.RecordTypes.Test)
                rights = new WorkAssignation() { AccessLevel = user.Tests.FirstOrDefault(x => x.ImageRecord.Id == imageRecord.Id)?.GetAssignationType() ?? WorkAssignation.AccessLevels.View };
            else
                rights = user.Assignments.FirstOrDefault(x => x.Study == imageRecord.Household.Study);
            foodItem.CreatedBy = user;

            if (IsAdmin)
                db.FoodItems.Add(foodItem);
            else
            {
                if (rights == null || rights.AccessLevel == WorkAssignation.AccessLevels.View || rights.AccessLevel == WorkAssignation.AccessLevels.Quantify)
                    return Unauthorized();
                else if (rights.AccessLevel == WorkAssignation.AccessLevels.Identify)
                    foodItem.QuantityGrams = 0;
                db.FoodItems.Add(foodItem);
            }

            if (foodItem.FoodCompositionId != null)
                foodItem.FoodComposition = db.FoodCompositions.Find(foodItem.FoodCompositionId);

            UpdateRecipeFoodComp(foodItem);
            if (foodItem.CreateStart != null && foodItem.CreateEnd != null)
            {
                db.Timings.Add(new Timing()
                {
                    Study = rights?.Study ?? imageRecord.Household?.Study,
                    Type = foodItem.QuantityGrams > 0 ? Timing.TimingType.IdentifyWithQuantityFoodItem : Timing.TimingType.IdentifyFoodItem,
                    CreatedBy = user,
                    CreatedTime = foodItem.CreateStart,
                    TimeTaken = (int)foodItem.CreateEnd.Subtract(foodItem.CreateStart).TotalMilliseconds,
                    FoodItem = foodItem
                });
            }

            db.SaveChanges();

            foodItem.ImageRecord.Updates.Add(new RecordHistory()
            {
                Action = RecordHistory.UpdateTypes.Identify,
                FoodCompositionId = foodItem.FoodCompositionId,
                FoodItemId = foodItem.Id,
                User = user
            });
            if (foodItem.QuantityGrams > 0)
            {
                foodItem.ImageRecord.Updates.Add(new RecordHistory()
                {
                    Action = RecordHistory.UpdateTypes.Quantify,
                    FoodItemId = foodItem.Id,
                    FoodCompositionId = foodItem.FoodCompositionId,
                    User = user,
                    QuantityGrams = foodItem.QuantityGrams,
                    ToolMeasure = foodItem.MeasureCount,
                    ToolSource = foodItem.MeasureType
                });
                if (foodItem.ImageRecord.Household.Study.Gestalt && AccessLevel == EnumRole.Analyst)
                {
                    //Average all the attempts so far
                    //No need to average cause there's only 1
                    foodItem.MeasureCount = foodItem.QuantityGrams;
                    foodItem.MeasureType = "Gestalt estimation (g)";
                }
            }

            db.SaveChanges();

            return Created("DefaultApi", new { id = foodItem.Id });
        }

        private void UpdateRecipeFoodComp(FoodItem foodItem)
        {
            db.Entry(foodItem).Reference("FoodComposition").Load();
            var imageRecord = db.ImageRecords.Find(foodItem.ImageRecordId);
            if (foodItem.FoodCompositionId != null && imageRecord != null && imageRecord.RecordType == ImageRecord.RecordTypes.Ingredient)
            {
                var recipe = db.CookRecipes.FirstOrDefault(x => x.Ingredients.Count(y => y.ImageRecord.Id == imageRecord.Id) > 0);
                if (recipe != null)
                {
                    recipe.UpdateFoodComposition(db);
                }
            }
        }

        // DELETE: api/FoodItems/5
        [ResponseType(typeof(FoodItem))]
        public IHttpActionResult DeleteFoodItem(int id)
        {
            FoodItem foodItem = db.FoodItems.Find(id);
            if (foodItem == null)
                return NotFound();

            ImageRecord record = foodItem.ImageRecord;
            if (AccessLevel != EnumRole.Admin && AccessLevel != EnumRole.Coordinator)
            {
                WorkAssignation rights;
                if (record.RecordType != ImageRecord.RecordTypes.Test)
                {
                    rights = user.Assignments.FirstOrDefault(x => x.Study == record.Household.Study);
                    if (rights.AccessLevel == WorkAssignation.AccessLevels.Quantify || rights.AccessLevel == WorkAssignation.AccessLevels.View)
                        return Unauthorized();
                }
            }

            record.Updates.Add(new RecordHistory()
            {
                Action = RecordHistory.UpdateTypes.Delete,
                FoodCompositionId = foodItem.FoodComposition?.Id,
                FoodItemId = foodItem.Id,
                User = user,
                QuantityGrams = foodItem.QuantityGrams,
                ToolMeasure = foodItem.MeasureCount,
                ToolSource = foodItem.MeasureType
            });
            db.FoodItems.Remove(foodItem);

            if (record.RecordType == ImageRecord.RecordTypes.Ingredient)
            {
                var recipe = db.CookIngredients.Include(x => x.Recipe).Include(x => x.Recipe.Ingredients).Include(x => x.Recipe.Ingredients.Select(y => y.ImageRecord))
                    .Include(x => x.Recipe.Ingredients.Select(y => y.ImageRecord.FoodItems)).FirstOrDefault(x => x.ImageRecord.Id == record.Id).Recipe;
                recipe.UpdateFoodComposition(db);
            }

            db.SaveChanges();

            return Ok(id);
        }

        [ResponseType(typeof(int)), HttpGet, Route("api/FoodItems/{id}/Flag")]
        public IHttpActionResult FlagFoodItem(int id)
        {
            var foodItem = db.FoodItems.Find(id);
            if (foodItem == null)
                return BadRequest("Item could not be found");

            db.AdminMessages.Add(new AdminMessage()
            {
                Message = user.UserName + " flagged the item " + (foodItem.FoodComposition?.Name ?? "unknown") + " as incorrect.",
                RefId = foodItem.ImageRecord.Id,
                Type = AdminMessage.ContentType.Flag
            });

            db.SaveChanges();

            return Ok(id);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                db.Dispose();
            }
            base.Dispose(disposing);
        }

        private bool FoodItemExists(int id)
        {
            return db.FoodItems.Count(e => e.Id == id) > 0;
        }
    }
}