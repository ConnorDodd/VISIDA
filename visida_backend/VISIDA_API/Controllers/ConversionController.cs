using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using System.Data.Entity;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Controllers
{
    public class ConversionController : AuthController
    {

        #region AddRecords

        [Route("api/Conversion/CreateEatRecord"), HttpPost]
        public IHttpActionResult CreateEatRecord([FromBody]AddEatRecordRequest request)
        {
            Household h = db.Households.Include(x => x.HouseholdMembers).Include(x => x.HouseholdMembers.Select(y => y.EatingOccasions))
                .Include(x => x.HouseholdMembers.Select(y => y.EatingOccasions.Select(z => z.EatRecords))).FirstOrDefault(x => x.Id == request.HouseholdId);
            ImageRecord record = new ImageRecord();
            record.AnnotatedStatus = AnnotatedStatuses.Created;
            record.CaptureTime = request.CaptureTime;
            record.TextDescription = request.TextDescription;
            record.Household = h;

            CreateRecordFromRecord(record, request);

            db.ConversionHistories.Add(new ConversionHistory()
            {
                Household = h,
                CreatedBy = user,
                ImageRecord = record,
                ConversionType = ConversionHistory.ConversionTypes.IRCreate
            });

            db.SaveChanges();
            return Ok();
        }

        [Route("api/Conversion/CreateRecipe"), HttpPost]
        public IHttpActionResult CreateRecipe([FromBody]AddEatRecordRequest request)
        {
            Household h = db.Households.FirstOrDefault(x => x.Id == request.HouseholdId);

            string fcName = String.Format("{0}_Recipe_{1} {2}", h.ParticipantId, h.HouseholdRecipes.Count, request.CaptureTime.ToString("dd-MM-yyyy"));
            CookRecipe recipe = new CookRecipe()
            {
                Name = request.Name,
                AnnotatedStatus = AnnotatedStatuses.Created,
                FoodComposition = new Models.FCT.FoodComposition()
                {
                    Name = fcName,
                    ModifiedDate = DateTime.Now
                },
                CaptureTime = request.CaptureTime,
                TextDescription = request.TextDescription
            };
            h.HouseholdRecipes.Add(recipe);

            db.ConversionHistories.Add(new ConversionHistory()
            {
                Household = h,
                CreatedBy = user,
                Recipe = recipe,
                ConversionType = ConversionHistory.ConversionTypes.CRCreate
            });

            db.SaveChanges();
            return Ok();
        }

        [Route("api/Conversion/CreateIngredient"), HttpPost]
        public IHttpActionResult CreateIngredient([FromBody]AddEatRecordRequest request)
        {
            Household h = db.Households.FirstOrDefault(x => x.Id == request.HouseholdId);
            CookRecipe r = db.CookRecipes.FirstOrDefault(x => x.Id == request.DestinationId);

            ImageRecord ir = new ImageRecord()
            {
                AnnotatedStatus = AnnotatedStatuses.Created,
                CaptureTime = request.CaptureTime,
                Household = h,
                RecordType = ImageRecord.RecordTypes.Ingredient,
                TextDescription = request.TextDescription
            };
            r.Ingredients.Add(new CookIngredient()
            {
                AnnotatedStatus = AnnotatedStatuses.Created,
                ImageRecord = ir
            });

            db.ConversionHistories.Add(new ConversionHistory()
            {
                Household = h,
                CreatedBy = user,
                ImageRecord = ir,
                ConversionType = ConversionHistory.ConversionTypes.CICreate
            });

            db.ConversionHistories.Add(new ConversionHistory()
            {
                Household = h,
                CreatedBy = user,
                Recipe = r,
                ConversionType = ConversionHistory.ConversionTypes.CRAdd
            });

            db.SaveChanges();
            return Ok();
        }

        public class AddEatRecordRequest : ConversionRequest
        {
            public int HouseholdId { get; set; }
            public DateTime CaptureTime { get; set; }
            public string Name { get; set; }
        }

        #endregion

        #region Change Image

        [Route("api/Conversion/ChangeImage"), HttpPost]
        public IHttpActionResult ChangeImage([FromBody]ConversionRequest request)
        {
            var source = db.ImageRecords.Find(request.SourceId);
            if (source == null || request.NewImageUrl == null)
                return BadRequest();
            if (!string.IsNullOrEmpty(request.NewImageUrl))
            {
                var dest = db.ImageRecords.FirstOrDefault(x => request.NewImageUrl.Equals(x.ImageUrl));
                if (dest != null)
                    source.ImageThumbUrl = dest.ImageThumbUrl;
                else
                {
                    var dest2 = db.CookRecipes.FirstOrDefault(x => request.NewImageUrl.Equals(x.ImageUrl));
                    if (dest2 != null)
                        source.ImageThumbUrl = dest2.ImageThumbUrl;
                }
            }
            source.ImageUrlUpdated = request.NewImageUrl;
            if (!string.IsNullOrEmpty(request.NewAudioUrl))
                source.AudioUrlUpdated = request.NewAudioUrl;
            source.NTranscript = request.Transcript ?? source.NTranscript;
            string textDescription = request.TextDescription;
            if (!string.IsNullOrEmpty(textDescription))
                textDescription += Environment.NewLine + Environment.NewLine;
            textDescription += source.TextDescription;
            source.TextDescription = textDescription;

            db.ConversionHistories.Add(new ConversionHistory()
            {
                Household = source.Household,
                CreatedBy = user,
                ImageRecord = source,
                ConversionType = ConversionHistory.ConversionTypes.ChangeImage
            });

            db.SaveChanges();
            return Ok();
        }

        [Route("api/Conversion/ChangeRecipeImage"), HttpPost]
        public IHttpActionResult ChangeRecipeImage([FromBody]ConversionRequest request)
        {
            var source = db.CookRecipes.Find(request.SourceId);
            if (source == null || request.NewImageUrl == null)
                return BadRequest();
            var dest = db.ImageRecords.FirstOrDefault(x => request.NewImageUrl.Equals(x.ImageUrl));
            if (!string.IsNullOrEmpty(request.NewImageUrl))
            {
                if (dest != null)
                    source.ImageThumbUrl = dest.ImageThumbUrl;
                else
                {
                    var dest2 = db.CookRecipes.FirstOrDefault(x => request.NewImageUrl.Equals(x.ImageUrl));
                    if (dest2 != null)
                        source.ImageThumbUrl = dest2.ImageThumbUrl;
                }
            }
            source.ImageUrlUpdated = request.NewImageUrl;
            if (!string.IsNullOrEmpty(request.NewAudioUrl))
                source.AudioUrlUpdated = request.NewAudioUrl;
            source.NTranscript = request.Transcript ?? source.NTranscript;
            string textDescription = request.TextDescription;
            if (!string.IsNullOrEmpty(textDescription))
                textDescription += Environment.NewLine + Environment.NewLine;
            textDescription += source.TextDescription;
            source.TextDescription = textDescription;

            db.ConversionHistories.Add(new ConversionHistory()
            {
                Household = source.Household,
                CreatedBy = user,
                Recipe = source,
                ConversionType = ConversionHistory.ConversionTypes.ChangeImage
            });

            db.SaveChanges();
            return Ok();
        }

        #endregion

        #region Convert Record to *

        [Route("api/Conversion/ConvertRecordToRecord"), HttpPost]
        public IHttpActionResult ConvertRecordToRecord([FromBody]ConversionRequest request)
        {
            var source = db.ImageRecords.Find(request.SourceId);
            if (source == null || request.NewParticipants.Length == 0)
                return BadRequest();

            RemoveOldCopy(source);

            CreateRecordFromRecord(source, request);

            db.ConversionHistories.Add(new ConversionHistory()
            {
                Household = source.Household,
                CreatedBy = user,
                ImageRecord = source,
                ConversionType = ConversionHistory.ConversionTypes.IRFromCI
            });

            db.SaveChanges();
            return Ok();
        }

        [Route("api/Conversion/ConvertRecordToLeftovers"), HttpPost]
        public IHttpActionResult ConvertRecordToLeftovers([FromBody]ConversionRequest request)
        {
            var source = db.ImageRecords.Find(request.SourceId);
            var dest = db.ImageRecords.Find(request.DestinationId);
            if (source == null || dest == null)
                return BadRequest();

            var dGuestInfo = dest.GuestInfo;
            var dMeal = dest.Meal;

            var destRecords = db.EatRecords
                .Include(x => x.Leftovers).Include(x => x.Leftovers.FoodItems).Include(x => x.Leftovers.Comments).Include(x => x.Leftovers.Updates)
                .Where(x => x.ImageRecord.Id == request.DestinationId).ToList();
            if (destRecords.Count() > 0)
            {
                var leftovers = destRecords[0].Leftovers;
                if (leftovers != null)
                {
                    if (request.Switch)
                    {
                        leftovers.RecordType = source.RecordType;
                        leftovers.IsLeftovers = source.IsLeftovers;
                        leftovers.GuestInfo = source.GuestInfo;
                        leftovers.Meal = source.Meal;

                        if (source.RecordType == ImageRecord.RecordTypes.EatRecord)
                        {
                            var sourceRecords = db.EatRecords.Where(x => x.ImageRecord.Id == source.Id);
                            foreach (var r in sourceRecords)
                                r.ImageRecord = leftovers;
                        }
                        else if (source.RecordType == ImageRecord.RecordTypes.Leftovers)
                        {
                            var sourceRecords = db.EatRecords.Where(x => x.Leftovers.Id == source.Id);
                            foreach (var r in sourceRecords)
                                r.Leftovers = leftovers;
                        }
                        else if (source.RecordType == ImageRecord.RecordTypes.Ingredient)
                        {
                            var ing = db.CookIngredients.FirstOrDefault(x => x.ImageRecord.Id == source.Id);
                            ing.ImageRecord = leftovers;
                            ing.Recipe.UpdateFoodComposition(db);
                        }
                    }
                    else
                    {
                        db.FoodItems.RemoveRange(leftovers.FoodItems);
                        db.Comments.RemoveRange(leftovers.Comments);
                        db.RecordHistories.RemoveRange(leftovers.Updates);
                        db.ImageRecords.Remove(leftovers);
                    }
                }
                else
                    request.Switch = false;
            }

            if (!request.Switch)
                RemoveOldCopy(source);
            source.RecordType = Models.InternalObjects.ImageRecord.RecordTypes.Leftovers;
            source.IsLeftovers = true;
            source.GuestInfo = dGuestInfo;
            source.Meal = dMeal;
            foreach (var r in destRecords)
            {
                r.Leftovers = source;
            }

            db.ConversionHistories.Add(new ConversionHistory()
            {
                Household = source.Household,
                CreatedBy = user,
                ImageRecord = source,
                ConversionType = ConversionHistory.ConversionTypes.ERLFromIR
            });

            db.SaveChanges();
            return Ok();
        }

        [Route("api/Conversion/ConvertRecordToIngredient"), HttpPost]
        public IHttpActionResult ConvertRecordToIngredient([FromBody]ConversionRequest request)
        {
            var source = db.ImageRecords.Find(request.SourceId);
            var dest = db.CookRecipes.Find(request.DestinationId);
            if (source == null || dest == null)
                return BadRequest();

            RemoveOldCopy(source);

            source.RecordType = ImageRecord.RecordTypes.Ingredient;
            source.IsLeftovers = false;
            source.GuestInfo = null;
            source.Meal = null;

            dest.Ingredients.Add(new Models.InternalObjects.CookIngredient()
            {
                AnnotatedStatus = Models.InternalObjects.AnnotatedStatuses.Created,
                ImageRecord = source
            });
            dest.UpdateFoodComposition(db);

            db.ConversionHistories.Add(new ConversionHistory()
            {
                Household = source.Household,
                CreatedBy = user,
                ImageRecord = source,
                ConversionType = ConversionHistory.ConversionTypes.CIFromIR
            });

            db.ConversionHistories.Add(new ConversionHistory()
            {
                Household = source.Household,
                CreatedBy = user,
                Recipe = dest,
                ConversionType = ConversionHistory.ConversionTypes.CRAdd
            });

            db.SaveChanges();
            return Ok();
        }

        [Route("api/Conversion/ConvertRecordToRecipe"), HttpPost]
        public IHttpActionResult ConvertRecordToRecipe([FromBody]ConversionRequest request)
        {
            var source = db.ImageRecords.Include(x => x.Household).Include(x => x.Household.HouseholdRecipes).Include(x => x.FoodItems)
                .FirstOrDefault(x => x.Id == request.SourceId);
            if (source == null)
                return BadRequest();

            RemoveOldCopy(source);

            string fcName = String.Format("{0}_Recipe_{1} {2}", source.Household.ParticipantId, source.Household.HouseholdRecipes.Count, source.CaptureTime.ToString("dd-MM-yyyy"));
            var cookRecipe = new CookRecipe()
            {
                Household = source.Household,
                AnnotatedStatus = AnnotatedStatuses.Created,
                AudioName = source.AudioName,
                AudioUrl = source.AudioUrl,
                CaptureTime = source.CaptureTime,
                Homography = source.Homography,
                ImageName = source.ImageName,
                ImageThumbUrl = source.ImageThumbUrl,
                ImageUrl = source.ImageUrl,
                ImageUrlUpdated = source.ImageUrlUpdated,
                IsFiducialPresent = source.IsFiducialPresent,
                NTranscript = source.NTranscript,
                TextDescription = request.TextDescription ?? source.TextDescription,
                Transcript = source.Transcript,
                FoodComposition = new Models.FCT.FoodComposition()
                {
                    Name = fcName,
                    ModifiedDate = DateTime.Now
                }
            };
            cookRecipe.Comments = source.Comments;
            source.Household.HouseholdRecipes.Add(cookRecipe);

            db.RecordHistories.RemoveRange(source.Updates);
            db.FoodItems.RemoveRange(source.FoodItems);
            db.ImageRecords.Remove(source);

            db.ConversionHistories.Add(new ConversionHistory()
            {
                Household = source.Household,
                CreatedBy = user,
                Recipe = cookRecipe,
                ConversionType = ConversionHistory.ConversionTypes.CRFromIR
            });

            db.SaveChanges();
            return Ok();
        }

        #endregion

        #region Convert Recipe to *

        [Route("api/Conversion/ConvertRecipeToRecord"), HttpPost]
        public IHttpActionResult ConvertRecipeToRecord([FromBody]ConversionRequest request)
        {
            var source = db.CookRecipes.Include(x => x.Household)
                .FirstOrDefault(x => x.Id == request.SourceId);

            var dest = RecordFromRecipe(source);

            var ingredients = source.Ingredients.ToArray();
            foreach (var ing in ingredients)
            {
                var ingComment = ing.ImageRecord.Comments.ToArray();
                foreach (var c in ingComment)
                    db.Comments.Remove(c);

                var foodItems = ing.ImageRecord.FoodItems.ToArray();
                foreach (var fi in foodItems)
                    ing.ImageRecord.FoodItems.Remove(fi);

                var updates = ing.ImageRecord.Updates.ToArray();
                foreach (var rh in updates)
                    ing.ImageRecord.Updates.Remove(rh);

                db.ImageRecords.Remove(ing.ImageRecord);
                db.CookIngredients.Remove(ing);
            }

            var comments = source.Comments.ToArray();
            foreach (var c in comments)
                db.Comments.Remove(c);
            db.CookRecipes.Remove(source);

            CreateRecordFromRecord(dest, request);

            db.ConversionHistories.Add(new ConversionHistory()
            {
                Household = source.Household,
                CreatedBy = user,
                ImageRecord = dest,
                ConversionType = ConversionHistory.ConversionTypes.IRFromCR
            });

            db.SaveChanges();
            return Ok();
        }

        [Route("api/Conversion/ConvertRecipeToLeftovers"), HttpPost]
        public IHttpActionResult ConvertRecipeToLeftovers([FromBody]ConversionRequest request)
        {
            var source = db.CookRecipes.Include(x => x.Household)
                .FirstOrDefault(x => x.Id == request.SourceId);
            var dest = db.ImageRecords.Find(request.DestinationId);

            var newRecord = RecordFromRecipe(source);

            //TODO same as aboce
            foreach (var ing in source.Ingredients)
            {
                db.ImageRecords.Remove(ing.ImageRecord);
                db.CookIngredients.Remove(ing);
            }
            db.CookRecipes.Remove(source);

            newRecord.RecordType = Models.InternalObjects.ImageRecord.RecordTypes.Leftovers;
            newRecord.IsLeftovers = true;
            newRecord.GuestInfo = dest.GuestInfo;
            newRecord.Meal = dest.Meal;

            var destRecords = db.EatRecords.Where(x => x.ImageRecord.Id == request.DestinationId);
            foreach (var r in destRecords)
            {
                if (r.Leftovers != null)
                    db.ImageRecords.Remove(r.Leftovers);
                r.Leftovers = newRecord;
            }

            db.ConversionHistories.Add(new ConversionHistory()
            {
                Household = source.Household,
                CreatedBy = user,
                ImageRecord = newRecord,
                ConversionType = ConversionHistory.ConversionTypes.IRFromCR
            });

            db.SaveChanges();
            return Ok();
        }

        [Route("api/Conversion/ConvertRecipeToIngredient"), HttpPost]
        public IHttpActionResult ConvertRecipeToIngredient([FromBody]ConversionRequest request)
        {
            var source = db.CookRecipes.Include(x => x.Household)
                .FirstOrDefault(x => x.Id == request.SourceId);
            var dest = db.CookRecipes.Find(request.DestinationId);

            var newRecord = RecordFromRecipe(source);

            foreach (var ing in source.Ingredients)
            {
                db.ImageRecords.Remove(ing.ImageRecord);
                db.CookIngredients.Remove(ing);
            }
            db.CookRecipes.Remove(source);

            newRecord.RecordType = ImageRecord.RecordTypes.Ingredient;
            newRecord.IsLeftovers = false;

            dest.Ingredients.Add(new Models.InternalObjects.CookIngredient()
            {
                AnnotatedStatus = Models.InternalObjects.AnnotatedStatuses.Created,
                ImageRecord = newRecord
            });

            db.ConversionHistories.Add(new ConversionHistory()
            {
                Household = source.Household,
                CreatedBy = user,
                ImageRecord = newRecord,
                ConversionType = ConversionHistory.ConversionTypes.CIFromCR
            });

            db.ConversionHistories.Add(new ConversionHistory()
            {
                Household = source.Household,
                CreatedBy = user,
                Recipe = dest,
                ConversionType = ConversionHistory.ConversionTypes.CRAdd
            });

            db.SaveChanges();
            return Ok();
        }

        #endregion

        #region Utils

        private ImageRecord RecordFromRecipe(CookRecipe source)
        {
            var dest = new ImageRecord()
            {
                Household = source.Household,
                AnnotatedStatus = AnnotatedStatuses.Created,
                AudioName = source.AudioName,
                AudioUrl = source.AudioUrl,
                CaptureTime = source.CaptureTime,
                Homography = source.Homography,
                ImageName = source.ImageName,
                ImageThumbUrl = source.ImageThumbUrl,
                ImageUrl = source.ImageUrl,
                ImageUrlUpdated = source.ImageUrlUpdated,
                IsFiducialPresent = source.IsFiducialPresent,
                NTranscript = source.NTranscript,
                TextDescription = source.TextDescription,
                Transcript = source.Transcript
            };
            return dest;
        }
        private void RemoveOldCopy(ImageRecord source)
        {
            if (source.RecordType == Models.InternalObjects.ImageRecord.RecordTypes.EatRecord)
            {
                var sourceRecords = db.EatRecords.Where(x => x.ImageRecord.Id == source.Id);
                foreach (var r in sourceRecords)
                {
                    if (r.Leftovers != null)
                        db.ImageRecords.Remove(r.Leftovers);
                    r.EatOccasion.EatRecords.Remove(r);
                    db.EatRecords.Remove(r);
                }
            }
            else if (source.RecordType == Models.InternalObjects.ImageRecord.RecordTypes.Leftovers)
            {
                var sourceRecords = db.EatRecords.Where(x => x.Leftovers.Id == source.Id);
                foreach (var r in sourceRecords)
                    r.Leftovers = null;
            }
            else if (source.RecordType == Models.InternalObjects.ImageRecord.RecordTypes.Ingredient)
            {
                var sourceRecord = db.CookIngredients.FirstOrDefault(x => x.ImageRecord.Id == source.Id);
                //sourceRecord.Recipe.ImageRecords.Remove(source);
                sourceRecord.Recipe.Ingredients.Remove(sourceRecord);
                db.CookIngredients.Remove(sourceRecord);
            }
        }

        private void CreateRecordFromRecord(ImageRecord source, ConversionRequest request)
        {
            source.RecordType = ImageRecord.RecordTypes.EatRecord;
            source.IsLeftovers = false;
            if (request.NewParticipants.Length > 1)
            {
                source.GuestInfo = new HouseholdGuestInfo()
                {
                    AdultFemaleGuests = request.ParticipantTotals[0],
                    AdultMaleGuests = request.ParticipantTotals[1],
                    ChildGuests = request.ParticipantTotals[2],
                };
            }
            else
                source.GuestInfo = null;

            var participants = db.HouseholdMembers.Include(x => x.EatingOccasions).Where(x => request.NewParticipants.Contains(x.Id));
            foreach (var p in participants)
            {
                var eo = p.EatingOccasions.FirstOrDefault(x => x.TimeStart.Date.CompareTo(source.CaptureTime.Date) == 0 && source.CaptureTime > x.TimeStart.AddMinutes(-15) && source.CaptureTime < x.TimeEnd.AddMinutes(15));
                if (eo == null)
                {
                    eo = new EatOccasion()
                    {
                        AnnotatedStatus = AnnotatedStatuses.Created,
                        EatRecords = new List<EatRecord>(),
                        TimeStart = source.CaptureTime.AddMinutes(-5),
                        TimeEnd = source.CaptureTime.AddMinutes(5),
                        Original = false
                    };
                    p.EatingOccasions.Add(eo);
                }

                eo.EatRecords.Add(new EatRecord()
                {
                    ImageRecord = source,
                    Original = false,
                    FinalizeTime = DateTime.Now
                });
            }
        }

        #endregion

        public class ConversionRequest
        {
            public int SourceId { get; set; }
            public int DestinationId { get; set; }
            public string NewImageUrl { get; set; }
            public int[] NewParticipants { get; set; }
            public int[] ParticipantTotals { get; set; }
            public bool Switch { get; set; }
            public string TextDescription { get; set; }
            public string NewAudioUrl { get; set; }
            public string Transcript { get; set; }
        }
    }
}
