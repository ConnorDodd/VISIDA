using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Data.Entity;
using System.Data.Entity.Infrastructure;
using System.Globalization;
using System.Linq;
using System.Text.RegularExpressions;
using System.Web;
using System.Web.Http;
using System.Web.Http.Description;
using VISIDA_API.Models;
using VISIDA_API.Models.ExternalObjects;
using VISIDA_API.Models.FCT;
using VISIDA_API.Models.InternalObjects;
using VISIDA_API.Providers;
using static VISIDA_API.Controllers.ImageRecordsController;

namespace VISIDA_API.Controllers
{
    [TimingActionFilter]
    public class CookRecipesController : AuthController
    {
        private static RecordHistoryComparer comp = new RecordHistoryComparer();

        private IQueryable<CookRecipe> SearchRecords()
        {
            IQueryable<CookRecipe> records = db.CookRecipes
                .Include(x => x.Comments).Include(x => x.FoodComposition).Include(x => x.Homography).Include(x => x.Household).Include(x => x.Ingredients)
                .Include(x => x.Ingredients.Select(y => y.ImageRecord)).Include(x => x.Ingredients.Select(y => y.ImageRecord.FoodItems))
                .Include(x => x.Ingredients.Select(y => y.ImageRecord.FoodItems.Select(z => z.FoodComposition))).Include(x => x.ImageRecords.Select(y => y.Homography))
                .Where(x => x.Household.Study.DeletedTime == null);

            if (AccessLevel != EnumRole.Coordinator && AccessLevel != EnumRole.Admin)
                records = records.Where(x => !x.Hidden);

            //Get initial result set depending on user type
            if (!IsAdmin)
            {
                //If not admin, reduce results to assigned studies
                int[] assigned = user.Assignments.Select(x => x.Study).SelectMany(x => x.Households).Select(h => h.Id).ToArray();
                records = records.Where(x => assigned.Contains(x.Household.Id));
            }

            //Search by Study if exists
            string sid = HttpContext.Current.Request.Params["Study"];
            int studyId;
            if (sid != null && int.TryParse(sid, out studyId))
                records = records.Where(x => x.Household.Study_Id == studyId);

            //Search by Household if exists
            //string hid = HttpContext.Current.Request.Params["Household"];
            //int householdId;
            //if (!string.IsNullOrEmpty(hid) && int.TryParse(hid, out householdId))
            //    records = records.Where(x => x.Household.Id == householdId);

            string hh = HttpContext.Current.Request.Params["Household"];
            if (!string.IsNullOrEmpty(hh))
                records = records.Where(x => x.Household.ParticipantId.Equals(hh));

            string searchStr = HttpContext.Current.Request.Params["search"];
            if (!string.IsNullOrWhiteSpace(searchStr))
            {
                searchStr = searchStr.ToLower();
                var searchArray = searchStr.Split(' ');
                foreach (var searchTerm in searchArray)
                    records = records.Where(x => x.Ingredients.FirstOrDefault(y => y.ImageRecord.FoodItems.FirstOrDefault(f => f.FoodComposition != null && f.FoodComposition.Name.ToLower().Contains(searchTerm)) != null) != null
                        || x.Name.StartsWith(searchTerm));
            }

            string commentStr = HttpContext.Current.Request.Params["comment"];
            if (!string.IsNullOrWhiteSpace(commentStr))
            {
                commentStr = commentStr.ToLower();
                records = records.Where(x => x.Comments.FirstOrDefault(c => !c.Hidden && c.Text.Contains(commentStr)) != null);
            }

            string daysStr = HttpContext.Current.Request.Params["date"];
            if (!string.IsNullOrWhiteSpace(daysStr))
            {
                DateTime[] days = daysStr.Split(',').Select(x => DateTime.ParseExact(x, ImageRecordsController.DATE_FORMAT, CultureInfo.InvariantCulture)).ToArray();
                records = records.Where(x => days.Contains(System.Data.Entity.DbFunctions.TruncateTime(x.CaptureTime) ?? new DateTime()));
            }

            return records;
        }

        // GET: api/CookRecipes
        [Authorize(Roles = "admin,analyst,coordinator")]
        public IEnumerable<ECookRecipe> GetCookRecipes([FromUri]PagingModel paging)
        {
            var records = SearchRecords();
            records.Include(x => x.Comments).Include(x => x.Comments.Select(y => y.CreatedBy));
            records = records.OrderByDescending(x => x.CaptureTime);

            paging.TotalCount = records.Count();
            object meta = paging.GetMetadata();
            HttpContext.Current.Response.Headers.Add("paging", JsonConvert.SerializeObject(meta));
            HttpContext.Current.Response.Headers.Add("Access-Control-Expose-Headers", "paging");

            var toReturn = new List<ECookRecipe>();
            var pagedRecords = paging.GetPage(records);
            foreach (var rec in pagedRecords)
            {
                try
                {
                    var recipe = new ECookRecipe()
                    {
                        Id = rec.Id,
                        Hidden = rec.Hidden,
                        HouseholdParticipantId = rec.Household.ParticipantId,
                        Name = rec.Name,
                        ImageName = rec.ImageName,
                        ImageUrl = rec.ImageUrlUpdated ?? rec.ImageUrl,
                        ImageThumbUrl = rec.ImageThumbUrl,
                        IsFiducialPresent = rec.IsFiducialPresent,
                        AudioName = rec.AudioName,
                        AudioUrl = rec.AudioUrl,
                        CaptureTime = rec.CaptureTime,
                        TextDescription = rec.TextDescription,
                        FoodCompositionId = rec.FoodComposition.Id,
                        FoodComposition = rec.FoodComposition,
                        Comments = rec.Comments.ToList().Select(x => EComment.ToShallowEComment(x)).ToList(),
                        TotalCookedGrams = rec.TotalCookedGrams,
                        Ingredients = rec.Ingredients.Select(x => new ECookIngredient()
                        {
                            Id = x.Id,
                            ImageRecord = new EImageRecord()
                            {
                                Id = x.ImageRecord.Id,
                                Hidden = x.ImageRecord.Hidden,
                                Meal = x.ImageRecord.Meal,
                                GuestInfo = x.ImageRecord.GuestInfo,
                                CaptureTime = x.ImageRecord.CaptureTime,
                                ImageName = x.ImageRecord.ImageName,
                                ImageUrl = x.ImageRecord.ImageUrlUpdated ?? x.ImageRecord.ImageUrl,
                                ImageThumbUrl = x.ImageRecord.ImageThumbUrl,
                                AudioName = x.ImageRecord.AudioName,
                                AudioUrl = x.ImageRecord.AudioUrl,
                                TextDescription = x.ImageRecord.TextDescription ?? x.ImageRecord.Transcript,
                                //Homography = x.ImageRecord.Homography,
                                IsFiducialPresent = x.ImageRecord.IsFiducialPresent,
                                RecordType = x.ImageRecord.RecordType,
                                FoodItems = x.ImageRecord.FoodItems.Select(y => (EFoodItem)y).ToList(),
                                NTranscript = x.ImageRecord.NTranscript,
                                IsCompleted = x.ImageRecord.IsCompleted,
                                LockTimestamp = x.ImageRecord.LockTimestamp,

                                ManualTranscript = x.ImageRecord.ManualTranscript,
                                Translation = x.ImageRecord.Translation,
                                Transcript = x.ImageRecord.Transcript,
                                AnnotationStatus = x.ImageRecord.AnnotationStatus,
                            },
                            CookMethod = x.CookMethod,
                            CookDescription = x.CookDescription
                        }).ToList(),
                        YieldFactor = rec.YieldFactor,
                        YieldFactorSource = rec.YieldFactorSource,
                    };

                    if (rec.Household.Study.Gestalt && AccessLevel == EnumRole.Analyst)
                    {
                        foreach (var ing in recipe.Ingredients)
                        {
                            var imageRecord = rec.Ingredients.FirstOrDefault(x => x.Id == ing.Id).ImageRecord;
                            foreach (var fi in ing.ImageRecord.FoodItems)
                            {
                                var quantifications = imageRecord.Updates.Where(x => x.FoodItemId == fi.Id && x.Action == RecordHistory.UpdateTypes.Quantify).OrderByDescending(x => x.Time).ToList();
                                var res = from elem in quantifications group elem by elem.User.Id into groups select groups.OrderBy(x => x, comp);

                                var count = res.Count();

                                RecordHistory history = quantifications.Where(x => x.User.Id == user.Id || x.User.Role.Role.Equals("admin") || x.User.Role.Role.Equals("coordinator")).OrderByDescending(x => x.Time).FirstOrDefault();
                                if (history != null)
                                {
                                    fi.QuantityGrams = history.QuantityGrams ?? 0;
                                    fi.MeasureType = history.ToolSource;
                                    fi.MeasureCount = history.ToolMeasure ?? 0;

                                    if (history.User.Id != user.Id) // Wasn't creadted by this user i.e. was created by admin
                                        fi.CreatedByAdmin = true;
                                }
                                else if (rec.Household.Study.GestaltMax > 0 && count >= rec.Household.Study.GestaltMax)
                                {
                                    fi.MeasureType = "Gestalt max reached";
                                    fi.GestaltCount = count;
                                    fi.GestaltLock = true;
                                }
                                else
                                {
                                    fi.MeasureCount = 0;
                                    fi.MeasureType = null;
                                    fi.QuantityGrams = 0;
                                }
                            }
                        }
                    }

                    toReturn.Add(recipe);

                }
                catch (Exception e)
                {
                    throw e;
                }
            }
            //var ret = paging.GetPage(records).ToList().Select(x => (ECookRecipe)x);
            return toReturn;
        }

        // GET: api/CookRecipes/5
        [ResponseType(typeof(ECookRecipe)), Authorize(Roles = "admin,analyst,coordinator"), HttpGet, Route("api/CookRecipes/{id}")]
        public IHttpActionResult GetCookRecipe(int id)
        {
            CookRecipe cookRecipe = db.CookRecipes
                .Include(x => x.Household).Include(x => x.Comments).Include(x => x.FoodComposition).Include(x => x.Homography)
                .Include(x => x.Ingredients).Include(x => x.Ingredients.Select(y => y.ImageRecord))
                .Include(x => x.Ingredients.Select(y => y.ImageRecord.FoodItems))
                .Include(x => x.Ingredients.Select(y => y.ImageRecord.FoodItems.Select(z => z.FoodComposition)))
                .Include(x => x.Household.Study)
                .FirstOrDefault(x => x.Id == id);
            if (cookRecipe == null)
                return NotFound();

            ECookRecipe ret = new ECookRecipe()
            {
                Id = cookRecipe.Id,
                Hidden = cookRecipe.Hidden,
                HouseholdParticipantId = cookRecipe.Household.ParticipantId,
                Name = cookRecipe.Name,
                ImageName = cookRecipe.ImageName,
                ImageUrl = cookRecipe.ImageUrlUpdated ?? cookRecipe.ImageUrl,
                ImageThumbUrl = cookRecipe.ImageThumbUrl,
                IsFiducialPresent = cookRecipe.IsFiducialPresent,
                Homography = cookRecipe.Homography,
                AudioName = cookRecipe.AudioName,
                AudioUrl = cookRecipe.AudioUrl,
                CaptureTime = cookRecipe.CaptureTime,
                TextDescription = cookRecipe.TextDescription,
                FoodCompositionId = cookRecipe.FoodComposition.Id,
                FoodComposition = cookRecipe.FoodComposition,
                Ingredients = new List<ECookIngredient>(),//cookRecipe.Ingredients.Select(x => (ECookIngredient)x),
                YieldFactor = cookRecipe.YieldFactor,
                YieldFactorSource = cookRecipe.YieldFactorSource,
                Comments = cookRecipe.Comments.Select(x => (EComment)x).ToList(),
                TotalCookedGrams = cookRecipe.TotalCookedGrams,
                StudyId = cookRecipe.Household.Study_Id,
                IsSource = cookRecipe.IsSource
            };

            WorkAssignation temp = db.WorkAssignations.FirstOrDefault(x => x.LoginUser.Id == user.Id && x.Study.DeletedTime == null && x.Study.Id == cookRecipe.Household.Study_Id);
            if (temp != null)
                ret.Assignation = temp;
            else if (!IsAdmin)
                return Unauthorized();

            foreach (var ing in cookRecipe.Ingredients)
            {
                ECookIngredient ingredient = new ECookIngredient()
                {
                    Id = ing.Id,
                    CookMethod = ing.CookMethod,
                    CookDescription = ing.CookDescription
                };
                ingredient.ImageRecord = new EImageRecord()
                {
                    Id = ing.ImageRecord.Id,
                    Hidden = ing.ImageRecord.Hidden,
                    CaptureTime = ing.ImageRecord.CaptureTime,
                    ImageName = ing.ImageRecord.ImageName,
                    ImageUrl = ing.ImageRecord.ImageUrl,
                    ImageThumbUrl = ing.ImageRecord.ImageThumbUrl,
                    AudioName = ing.ImageRecord.AudioName,
                    AudioUrl = ing.ImageRecord.AudioUrl,
                    TextDescription = ing.ImageRecord.TextDescription ?? ing.ImageRecord.Transcript,
                    IsFiducialPresent = ing.ImageRecord.IsFiducialPresent,
                    RecordType = ing.ImageRecord.RecordType,
                    FoodItems = ing.ImageRecord.FoodItems.Select(x => (EFoodItem)x).ToList(),
                    NTranscript = ing.ImageRecord.NTranscript,
                    IsCompleted = ing.ImageRecord.IsCompleted,
                    LockTimestamp = ing.ImageRecord.LockTimestamp,
                    ManualTranscript = ing.ImageRecord.ManualTranscript,
                    Translation = ing.ImageRecord.Translation,
                    Transcript = ing.ImageRecord.Transcript,
                    AnnotationStatus = ing.ImageRecord.AnnotationStatus,
                };
                ret.Ingredients.Add(ingredient);
            }
            ret.Ingredients = ret.Ingredients.OrderBy(x => x.Id).OrderByDescending(x => x.ImageRecord.CaptureTime).ToList();

            var retentionFactors = db.RetentionFactors.Where(x => x.Table_Id == cookRecipe.Household.Study.FoodCompositionTable_Id).ToList();
            foreach (var ing in ret.Ingredients)
            {
                foreach (var fi in ing.ImageRecord.FoodItems)
                {
                    if ((fi.FoodCompositionDatabaseEntry?.FoodGroupId ?? 0) == 0)
                        continue;
                    string foodGroupId = fi.FoodCompositionDatabaseEntry.FoodGroupId.ToString();
                    var match = retentionFactors.Where(x => foodGroupId.StartsWith(x.FoodGroupId) && x.RetentionType == ing.CookMethod).OrderByDescending(x => x.FoodGroupId.Length).FirstOrDefault();
                    foreach (var rf in retentionFactors)
                    {
                        if (rf.RetentionType == ing.CookMethod)
                            if (foodGroupId.StartsWith(rf.FoodGroupId))
                            {
                                match = rf;
                                break;
                            }
                    }
                    if (match != null)
                        fi.RetentionFactor = match;
                }
            }

            if (cookRecipe.Household.Study.Gestalt && AccessLevel == EnumRole.Analyst)
            {
                foreach (var ing in ret.Ingredients)
                {
                    var imageRecord = cookRecipe.Ingredients.FirstOrDefault(x => x.Id == ing.Id).ImageRecord;
                    foreach (var fi in ing.ImageRecord.FoodItems)
                    {
                        var quantifications = imageRecord.Updates.Where(x => x.FoodItemId == fi.Id && x.Action == RecordHistory.UpdateTypes.Quantify).OrderByDescending(x => x.Time).ToList();
                        var res = from elem in quantifications group elem by elem.User.Id into groups select groups.OrderBy(x => x, comp);

                        var count = res.Count();

                        RecordHistory history = quantifications.Where(x => x.User.Id == user.Id || x.User.Role.Role.Equals("admin") || x.User.Role.Role.Equals("coordinator")).OrderByDescending(x => x.Time).FirstOrDefault();
                        if (history != null)
                        {
                            fi.QuantityGrams = history.QuantityGrams ?? 0;
                            fi.MeasureType = history.ToolSource;
                            fi.MeasureCount = history.ToolMeasure ?? 0;

                            if (history.User.Id != user.Id) // Wasn't creadted by this user i.e. was created by admin
                                fi.CreatedByAdmin = true;
                        }
                        else if (cookRecipe.Household.Study.GestaltMax > 0 && count >= cookRecipe.Household.Study.GestaltMax)
                        {
                            fi.MeasureType = "Gestalt max reached";
                            fi.GestaltCount = count;
                            fi.GestaltLock = true;
                        }
                        else
                        {
                            fi.MeasureCount = 0;
                            fi.MeasureType = null;
                            fi.QuantityGrams = 0;
                        }
                    }
                }
            }

            //ECookRecipe ret = cookRecipe;
            //if (!string.IsNullOrWhiteSpace(ret.Name))
            ret.MatchingFactors = FindYieldFactors(cookRecipe);
            ret.Usages = db.FoodItems.Where(x => x.FoodCompositionId == ret.FoodCompositionId).Select(x => new ECookRecipe.ERecipeUsage() { Id = x.ImageRecord.Id, RecordTime = x.ImageRecord.CaptureTime }).ToList();

            var records = SearchRecords();
            var previous = records.Where(x => x.CaptureTime > cookRecipe.CaptureTime).OrderBy(x => x.CaptureTime).FirstOrDefault();
            var next = records.Where(x => x.CaptureTime < cookRecipe.CaptureTime).OrderByDescending(x => x.CaptureTime).FirstOrDefault();
            object meta = new NearbyPages() { NextId = next != null ? next.Id : 0, PrevId = previous != null ? previous.Id : 0 };
            HttpContext.Current.Response.Headers.Add("pages", JsonConvert.SerializeObject(meta));
            HttpContext.Current.Response.Headers.Add("Access-Control-Expose-Headers", "pages");

            return Ok(ret);
        }

        //private static List<YieldFactor> FindYieldFactors(CookRecipe recipe)
        //{
        //    if (recipe.Household.Study.FoodCompositionTable_Id == null)
        //        return new List<YieldFactor>();
        //    //Return all yield factors that match the recipe name
        //    YieldFactor[] factors = recipe.Household.Study.FoodCompositionTable?.FoodCompositions.Where(x => x.IsRecipe && (x.YieldOven != null || x.YieldStoveTop != null || x.YieldWater != null)).Select(x => new YieldFactor
        //    {
        //        Id = x.Id,
        //        Name = x.Name,
        //        Factor = x.YieldWater ?? x.YieldStoveTop ?? x.YieldOven,
        //        CookMethod = x.YieldWater != null ? CookIngredient.CookMethods.Water : (x.YieldStoveTop != null ? CookIngredient.CookMethods.Fry : CookIngredient.CookMethods.Oven),
        //        Density = x.Density
        //    }).ToArray();
        //    //YieldWater = x.YieldWater, YieldStoveTop = x.YieldStoveTop, YieldOven = x.YieldOven }).ToArray();

        //    var matches = new List<YieldFactor>();
        //    Regex rgx = new Regex("[^a-zA-Z0-9 ]"); //new Regex("[^a-zA-Z0-9 -]");
        //    string search = rgx.Replace(recipe.Name, "");
        //    var terms = search.Split(' ').Where(x => x.Length > 0);

        //    CultureInfo culture = CultureInfo.CurrentCulture;
        //    //culture.CompareInfo.IndexOf(paragraph, word, CompareOptions.IgnoreCase) >= 0
        //    foreach (var term in terms)
        //        matches.AddRange(factors.Where(x => culture.CompareInfo.IndexOf(x.Name, term, CompareOptions.IgnoreCase) >= 0));

        //    matches = matches.Distinct().ToList();
        //    var sreg = new Regex(search.Replace(' ', '|'), RegexOptions.IgnoreCase);
        //    foreach (var match in matches)
        //        match.Matches = sreg.Matches(match.Name).Count;
        //    matches = matches.OrderByDescending(x => x.Matches).ToList();

        //    if (!string.IsNullOrEmpty(recipe.YieldFactorSource) && matches.FirstOrDefault(x => recipe.YieldFactorSource.Equals(x.Name)) == null)
        //    {
        //        var match = factors.FirstOrDefault(x => recipe.YieldFactorSource.Equals(x.Name));
        //        if (match != null)
        //            matches.Add(match);
        //        //else
        //        //matches.Add(new YieldFactor() { Name = recipe.YieldFactorSource, Factor = recipe.YieldFactor, CookMethod = CookIngredient.CookMethods.None });
        //    }

        //    return matches;
        //}

        private YieldFactor[] FindYieldFactors(CookRecipe recipe)
        {
            var factors = db.CookRecipes.Where(x => x.IsSource && x.Id != recipe.Id && x.Household.Study_Id == recipe.Household.Study_Id).Select(x => new YieldFactor
            {
                Id = x.Id,
                Name = x.Name,
                Factor = x.YieldFactor,
                //CookMethod = x.YieldWater != null ? CookIngredient.CookMethods.Water : (x.YieldStoveTop != null ? CookIngredient.CookMethods.Fry : CookIngredient.CookMethods.Oven),
                Density = x.FoodComposition.Density
            }).ToArray();
            return factors;
        }

        // PUT: api/CookRecipes/5
        [ResponseType(typeof(ECookRecipe)), Authorize(Roles = "admin,analyst,coordinator"), HttpPut, Route("api/CookRecipes/{id}")]
        public IHttpActionResult PutCookRecipe(int id, ECookRecipe cookRecipe)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var recipe = db.CookRecipes.Find(id);
            List<YieldFactor> matchingFactors = new List<YieldFactor>();
            if (recipe == null)
                return BadRequest();

            recipe.IsSource = cookRecipe.IsSource;
            if (recipe.IsSource && (!cookRecipe.Name.Equals(recipe.Name) || cookRecipe.YieldFactor != recipe.YieldFactor || cookRecipe.FoodComposition.Density != recipe.FoodComposition.Density)) //Check if it's changed and update similar recipes referencing this
            {
                //TODO
                var refs = db.CookRecipes.Include(x => x.FoodComposition).Where(x => x.Id != id && x.Household.Study_Id == recipe.Household.Study_Id && recipe.Name.Equals(x.YieldFactorSource)).ToList();
                foreach (var r in refs)
                {
                    r.YieldFactorSource = cookRecipe.Name;
                    r.YieldFactor = cookRecipe.YieldFactor;
                    r.FoodComposition.Density = cookRecipe.FoodComposition.Density;
                }
            }

            if (cookRecipe.Name != null && !cookRecipe.Name.Equals(recipe.Name))
            {
                recipe.Name = cookRecipe.Name;

                Regex regex = new Regex(@"(\B[^a-zA-Z]*\B)");
                var matches = regex.Matches(recipe.Name);
                string name = recipe.Name, altName = "";
                foreach (Match reg in matches)
                    if (reg.Length > 0)
                        altName += reg.Value.Trim() + " ";
                name = new Regex(@"[^a-zA-Z0-9 ]").Replace(name, "");
                name = new Regex("[ ]{2,}", RegexOptions.None).Replace(name, " ");
                recipe.FoodComposition.Name = string.Format("{0} {1}",name, recipe.CaptureTime.ToString("dd/MM/yyy"));
                if (altName.Length > 0)
                    recipe.FoodComposition.AlternateName = altName.TrimEnd();
                else
                    recipe.FoodComposition.AlternateName = null;

                var match = db.FoodCompositions.Where(x => x.Name.Contains(recipe.FoodComposition.Name));
                int count = match.Count();
                if (count > 0)
                   recipe.FoodComposition.Name += ("_" + count);

            }
            recipe.CaptureTime = cookRecipe.CaptureTime;
            if (recipe.Hidden != cookRecipe.Hidden)
            {
                recipe.Hidden = cookRecipe.Hidden;
                if (recipe.Hidden)
                    foreach (var i in recipe.Ingredients)
                        i.ImageRecord.Hidden = recipe.Hidden;
            }

            recipe.YieldFactorSource = cookRecipe.YieldFactorSource;
            if (cookRecipe.FoodComposition.Density > 0)
                recipe.FoodComposition.Density = cookRecipe.FoodComposition.Density;
            if (recipe.YieldFactor != cookRecipe.YieldFactor)
                recipe.YieldFactor = cookRecipe.YieldFactor;
            foreach (var i in recipe.Ingredients)
            {
                var m = cookRecipe.Ingredients.FirstOrDefault(x => x.Id == i.Id);
                if (i.CookMethod != m.CookMethod)
                    i.CookMethod = m.CookMethod;
            }
            recipe.UpdateFoodComposition(db);

            try
            {
                db.SaveChanges();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!CookRecipeExists(id))
                {
                    return NotFound();
                }
                else
                {
                    throw;
                }
            }
            var ret = (ECookRecipe)recipe;
            //ret.MatchingFactors = matchingFactors;
            return Ok(ret);
        }

        [ResponseType(typeof(void)), Route("api/CookRecipes/{id}"), HttpPost]
        public IHttpActionResult HideImageRecord([FromUri] int id, [FromBody] bool hide)
        {
            CookRecipe recipe = db.CookRecipes.Find(id);
            recipe.Hidden = hide;
            db.SaveChanges();
            return Ok();
        }

        // POST: api/CookRecipes
        [HttpGet, Route("RecalculateRecipes")]//Authorize(Roles = "admin,analyst,coordinator")]
        public IHttpActionResult PostCookRecipe(CookRecipe cookRecipe)
        {
            var allRecipes = db.CookRecipes.Include(x => x.Ingredients)
                .Include(x => x.Ingredients.Select(y => y.ImageRecord))
                .Include(x => x.Ingredients.Select(y => y.ImageRecord.FoodItems))
                .Include(x => x.Ingredients.Select(y => y.ImageRecord.FoodItems.Select(z => z.FoodComposition)))
                .Where(x => x.Household.Id == 463);

            try
            {
                foreach (var r in allRecipes)
                {
                    r.UpdateFoodComposition(db);
                }
                db.SaveChanges();

            } catch (Exception e)
            {
                return BadRequest(e.Message);
            }


            return Ok();
        }

        [Route("api/CookRecipes/{id}/Comments"), HttpPost, Authorize(Roles = "admin,analyst,coordinator")]
        public IHttpActionResult PostCookRecipeComment(int id, [FromBody] EComment c)
        {
            CookRecipe record = db.CookRecipes.Find(id);
            if (record == null)
                return BadRequest();
            var comment = new Comment()
            {
                Text = c.Text,
                Flag = c.Flag,
                CreatedBy = user
            };
            if (c.ReplyTo > 0)
            {
                var replyTo = db.Comments.Find(c.ReplyTo);
                if (replyTo == null)
                    return BadRequest();
                comment.ReplyTo = replyTo;
            }
            record.Comments.Add(comment);
            db.SaveChanges();

            return Ok(comment.Id);
        }

        // DELETE: api/CookRecipes/5
        //[ResponseType(typeof(CookRecipe))]
        //public IHttpActionResult DeleteCookRecipe(int id)
        //{
        //    CookRecipe cookRecipe = db.CookRecipes.Find(id);
        //    if (cookRecipe == null)
        //    {
        //        return NotFound();
        //    }

        //    db.CookRecipes.Remove(cookRecipe);
        //    db.SaveChanges();

        //    return Ok(cookRecipe);
        //}

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                db.Dispose();
            }
            base.Dispose(disposing);
        }

        private bool CookRecipeExists(int id)
        {
            return db.CookRecipes.Count(e => e.Id == id) > 0;
        }
    }
}