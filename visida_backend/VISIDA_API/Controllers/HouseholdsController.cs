using VISIDA_API.Models.ExternalObjects;
using VISIDA_API.Models.InternalObjects;
using VISIDA_API.Models.ParseObjects;

namespace VISIDA_API.Controllers
{
    public class HouseholdsController : AuthController
    {

        // GET: api/Households
        public IEnumerable<Household> GetHouseholds()
        {
            return db.Households.ToList();
        }

        // GET: api/Households/5
        [ResponseType(typeof(EHousehold)), Route("api/Households/{studyid}/ByName/{name}")]
        public IHttpActionResult GetHousehold(int studyid, string name)
        {
            var households = db.Households.Include(x => x.HouseholdMembers)
                .Include(x => x.HouseholdMembers.Select(y => y.EatingOccasions))
                .Include(x => x.HouseholdMembers.Select(y => y.EatingOccasions.Select(z => z.EatRecords)))
                .Include(x => x.ImageRecords).Include(x => x.ImageRecords.Select(y => y.FoodItems))
                .Include(x => x.ImageRecords).Include(x => x.ImageRecords.Select(y => y.FoodItems.Select(z => z.FoodComposition)))
                .Include(x => x.ImageRecords).Include(x => x.ImageRecords.Select(y => y.FoodItems.Select(z => z.Updates)))
                .Include(x => x.ImageRecords).Include(x => x.ImageRecords.Select(y => y.FoodItems.Select(z => z.Updates.Select(heck => heck.User))))
                .Include(x => x.HouseholdRecipes).Include(x => x.ImageRecords.Select(y => y.Comments))
                .Include(x => x.ImageRecords.Select(y => y.Comments.Select(z => z.CreatedBy)))
                .Include(x => x.ImageRecords.Select(y => y.GuestInfo))
                .Include(x => x.HouseholdRecipes.Select(y => y.Comments)).Include(x => x.HouseholdRecipes.Select(y => y.Comments.Select(z => z.CreatedBy)))
                .Include(x => x.HouseholdRecipes.Select(y => y.Ingredients))
                .Where(x => name.Equals(x.ParticipantId) && x.Study_Id == studyid).ToList();
                //.FirstOrDefault(x => x.Id == id);
            if (households.Count == 0)
                return NotFound();

            EHousehold retHouse = new EHousehold()
            {
                Country = households[0].Country,
                Guid = households[0].Guid,
                HouseholdMembers = new List<EHouseholdMember>(),
                HouseholdRecipes = new List<ECookRecipe>(),
                AllImages = new List<Tuple<string, string>>(),
                ParticipantId = name,
            };

            var householdId = households[0].Id; //There's multiple households cause there can be multiple uploads for the same name in a single study
            List<EConversionHistory> conversions = db.ConversionHistories.Include(x => x.CreatedBy).Where(x => x.Household_Id == householdId).OrderByDescending(x => x.CreatedTime).ToList().Select(x => (EConversionHistory)x).ToList();
            retHouse.ConversionHistories = conversions;

            var allImages = new List<Tuple<DateTime, string, string>>();
            foreach (var h in households)
            {
                ParseHousehold(h, retHouse, allImages);
            }
            
            var comparer = new ImageTupleComparer();
            allImages.Sort(comparer);
            foreach (var img in allImages)
                retHouse.AllImages.Add(new Tuple<string, string>(img.Item2, img.Item3));
            retHouse.AllImages = retHouse.AllImages.Distinct().ToList();

            return Ok(retHouse);
        }

        private void ParseHousehold(Household household, EHousehold ret, List<Tuple<DateTime, string, string>> allImages)
        {
            bool gestalt = household.Study.Gestalt;

            foreach (var hm in household.HouseholdMembers)
            {
                EHouseholdMember member = ret.HouseholdMembers.FirstOrDefault(x => x.ParticipantId.Equals(hm.ParticipantId));
                if (member == null)
                {
                    member = new EHouseholdMember()
                    {
                        Id = hm.Id,
                        Age = hm.Age,
                        IsFemale = hm.IsFemale,
                        IsBreastfed = hm.IsBreastfed,
                        IsMother = hm.IsMother,
                        LifeStage = hm.LifeStage,
                        ParticipantId = hm.ParticipantId,
                        FoodRecords = new List<EFoodRecord>(),

                        PregnancyTrimester = hm.PregnancyTrimester,
                        Weight = hm.Weight,
                        Height = hm.Height
                    };
                    ret.HouseholdMembers.Add(member);
                }
                //var records = hm.EatingOccasions.GroupBy(x => x.TimeStart.Date);
                var records2 = hm.EatingOccasions.SelectMany(x => x.EatRecords).GroupBy(x => x?.ImageRecord.CaptureTime.Date ?? x.FinalizeTime.Date);
                foreach (var fr in records2)
                {
                    EFoodRecord foodRecord = member.FoodRecords.FirstOrDefault(x => x.Date.CompareTo(fr.Key) == 0);
                    if (foodRecord == null)
                    {
                        foodRecord = new EFoodRecord()
                        {
                            Date = fr.Key,
                            EatOccasions = new List<EEatOccasion>()
                        };
                        member.FoodRecords.Add(foodRecord); 
                    }

                    var frList = fr.ToList();
                    foreach (var er in frList)
                    {
                        var eatingOccasion = new EEatOccasion()
                        {
                            Finalized = er.Finalized,
                            //Id = er.Id,
                            //TimeStart = eo.TimeStart,
                            //TimeEnd = eo.TimeEnd
                        };

                        if (er.Hidden)
                            continue;
                        var eatRecord = new EEatRecord()
                        {
                            Id = er.Id,
                            FinalizeTime = er.FinalizeTime,
                            Finalized = er.Finalized
                        };
                        eatRecord.ImageRecord = EImageRecord.ForHouseholdEImageRecord(er.ImageRecord, AccessLevel == EnumRole.Analyst, user, gestalt);
                        eatRecord.Leftovers = EImageRecord.ForHouseholdEImageRecord(er.Leftovers, AccessLevel == EnumRole.Analyst, user, gestalt);

                        if (er.ImageRecord.ImageUrl != null)
                            allImages.Add(new Tuple<DateTime, string, string>(er.ImageRecord.CaptureTime, er.ImageRecord.ImageThumbUrl ?? er.ImageRecord.ImageUrl, er.ImageRecord.ImageUrl));
                        if (er.Leftovers != null && er.Leftovers.ImageUrl != null)
                            allImages.Add(new Tuple<DateTime, string, string>(er.Leftovers.CaptureTime, er.Leftovers.ImageThumbUrl ?? er.Leftovers.ImageUrl, er.Leftovers.ImageUrl));

                        eatingOccasion.EatRecords.Add(eatRecord);

                        //foreach (var er in eo.EatRecords)
                        //{
                            
                        //}

                        foodRecord.EatOccasions.Add(eatingOccasion);
                    }
                    //member.FoodRecords.Add(foodRecord);
                }
                //retHouse.HouseholdMembers.Add(member);
            }

            foreach (var r in household.HouseholdRecipes)
            {
                if (r.ImageUrl != null)
                    allImages.Add(new Tuple<DateTime, string, string>(r.CaptureTime, r.ImageThumbUrl ?? r.ImageUrl, r.ImageUrl));

                ECookRecipe recipe = ECookRecipe.ToShallow(r);

                foreach (var ing in r.Ingredients)
                {
                    recipe.Ingredients.Add(new ECookIngredient()
                    {
                        Id = ing.Id,
                        ImageRecord = EImageRecord.ForHouseholdEImageRecord(ing.ImageRecord, AccessLevel == EnumRole.Analyst, user, gestalt),
                        CookMethod = ing.CookMethod,
                        CookDescription = ing.CookDescription
                    });
                    if (ing.ImageRecord.ImageUrl != null)
                        allImages.Add(new Tuple<DateTime, string, string>(ing.ImageRecord.CaptureTime, ing.ImageRecord.ImageThumbUrl ?? ing.ImageRecord.ImageUrl, ing.ImageRecord.ImageUrl));
                }
                ret.HouseholdRecipes.Add(recipe);
            }
        }

        protected class ImageTupleComparer : IComparer<Tuple<DateTime, string, string>>
        {
            public int Compare(Tuple<DateTime, string, string> x, Tuple<DateTime, string, string> y)
            {
                return x.Item1.CompareTo(y.Item1);
            }
        }

        //Get the recipes for a household, then add them to the suggestions and table?
        [ResponseType(typeof(List<EEatOccasion>)), Route("api/Households/{name}/Breastfeeding")]
        public IHttpActionResult GetBreastfeedingOccasions(string name)
        {
            //Household hh = db.Households
            //    .Include(x => x.HouseholdMembers)
            //    .Include(x => x.HouseholdMembers.Select(y => y.EatingOccasions))
            //    .Include(x => x.HouseholdMembers.Select(y => y.EatingOccasions))
            //    .FirstOrDefault(x => x.Id == id);
            var occasions = db.Households
                .Include(x => x.HouseholdMembers)
                .Include(x => x.HouseholdMembers.Select(y => y.EatingOccasions))
                .Where(x => x.ParticipantId == name)
                .SelectMany(x => x.HouseholdMembers)
                .SelectMany(x => x.EatingOccasions)
                .Where(x => x.IsBreastfeedOccasion);

            List<EEatOccasion> toReturn = new List<EEatOccasion>();
            foreach (var eo in occasions)
            {
                toReturn.Add(new EEatOccasion()
                {
                    Id = eo.Id,
                    Finalized = eo.Finalized,
                    HouseholdMemberId = eo.HouseholdMemberId,
                    HouseholdMemberParticipantId = eo.HouseholdMember.ParticipantId,
                    TimeStart = eo.TimeStart,
                    TimeEnd = eo.TimeEnd
                });
            }

            return Ok(toReturn);
        }

        //Update a breastfeeding occasion for a household
        [Route("api/Households/Breastfeeding/{id}"), HttpPut]
        public IHttpActionResult PutBreastfeedingOccasion(int id, [FromBody] EEatOccasion update)
        {
            var occasion = db.EatOccasions.Find(update.Id);
            if (occasion == null)
                return BadRequest();

            occasion.TimeStart = update.TimeStart;
            occasion.TimeEnd = update.TimeEnd;
            occasion.Finalized = update.Finalized;

            db.SaveChanges();

            return Ok();
        }

        //Delete a breastfeeding occasion for a household
        [Route("api/Households/Breastfeeding/{id}"), HttpDelete]
        public IHttpActionResult DeleteBreastfeedingOccasion(int id)
        {
            var occasion = db.EatOccasions.Find(id);
            if (occasion == null)
                return BadRequest();
            if (occasion.EatRecords.Count > 0)
                return BadRequest("Not a valid breastfeeding occasion, there is one or more records attached to it. Cannot delete.");

            db.EatOccasions.Remove(occasion);
            db.SaveChanges();

            return Ok();
        }

        //Get the recipes for a household, then add them to the suggestions and table?
        [ResponseType(typeof(ECookRecipe)), Route("api/Households/{id}/Recipes")]
        public IHttpActionResult GetHouseholdRecipes(int id)
        {
            Household hh = db.Households.Find(id);
            if (hh == null)
                return BadRequest();

            return Ok(hh.HouseholdRecipes.Where(x => !x.Hidden).ToList().Select(x => (ECookRecipe)x));
        }

        [ResponseType(typeof(ECookRecipe)), Route("api/Households/{id}/UsageLog")]
        public IHttpActionResult GetHouseholdUsageLog(int id)
        {
            Household hh = db.Households.Find(id);
            if (hh == null)
                return BadRequest();

            return Ok(hh.UsageLog);
        }

        [HttpGet, Route("api/Households/{id}/Members")]
        public IHttpActionResult GetHouseholdMembers(int id)
        {
            Household hh = db.Households.Include(x => x.HouseholdMembers).FirstOrDefault(x => x.Id == id);
            if (hh == null)
                return BadRequest();

            var members = hh.HouseholdMembers.Select(x => (EHouseholdMember)x).ToList();
            return Ok(members);
        }

        [HttpPut, Route("api/HouseholdMembers/{id}")]
        public IHttpActionResult PutHouseholdMember([FromUri]int id, [FromBody] EHouseholdMember member)
        {
            //HouseholdMember hm = db.HouseholdMembers.Find(id);
            //List<HouseholdMember> hms = db.HouseholdMembers.Where(x => x.Id == member.Id).SelectMany(x => x.Household.HouseholdMembers).Where(x => x.ParticipantId.Equals(member.ParticipantId)).ToList();
            //if (hms.Count == 0)
            HouseholdMember dbMember = db.HouseholdMembers.FirstOrDefault(x => x.Id == member.Id);
            if (dbMember == null)
                return BadRequest();

            var hms = dbMember.Household.Study.Households.SelectMany(x => x.HouseholdMembers).Where(x => x.ParticipantId.Equals(dbMember.ParticipantId));

            foreach (var hm in hms) {
                hm.Age = member.Age;
                hm.IsFemale = member.IsFemale;
                hm.IsBreastfed = member.IsBreastfed;
                hm.LifeStage = member.LifeStage;

                hm.PregnancyTrimester = member.PregnancyTrimester;
                hm.Weight = member.Weight;
                hm.Height = member.Height;
            }

            db.SaveChanges();
            return Ok();
        }

        //[ResponseType(typeof(Household))]
        //[HttpPost,Route("api/Households/{study}")]
        //public IHttpActionResult PostHouseholdWithId([FromBody] PHousehold pHousehold, [FromUri] int study)
        //{
        //    pHousehold.StudyId = study;
        //    return PostHousehold(pHousehold);
        //}

        // POST: api/Households
        [ResponseType(typeof(Household)), HttpPost]
        //[Authorize(Roles = "admin")]
        public IHttpActionResult PostHousehold(PHousehold pHousehold, [FromUri] int study = 0)
        {
            if (study > 0)
                pHousehold.StudyId = study;
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            try
            {
                Household newHousehold = pHousehold;
                Household oldHousehold = db.Households.Include(x => x.Study).FirstOrDefault(x => x.Guid.Equals(pHousehold.HouseholdId));//db.Studies.Find(pHousehold.StudyId).Households.FirstOrDefault(x => x.Guid.Equals(pHousehold.HouseholdId));
                if (oldHousehold != null && oldHousehold.Study_Id != pHousehold.StudyId)
                    return BadRequest("This data has already been uploaded to study: " + oldHousehold.Study.Name + ". If you need to update this household, please select the correct study.");

                if (oldHousehold != null)
                {
                    db.Entry(newHousehold).State = EntityState.Detached;

                    //If the household already exists in the database, we need to iterate through the new data and only add anything that doesn't already exist.
                    foreach (var newHm in newHousehold.HouseholdMembers)
                    {
                        HouseholdMember oldHm = oldHousehold.HouseholdMembers.FirstOrDefault(x => x.ParticipantId.Equals(newHm.ParticipantId));
                        if (oldHm == null)
                        {
                            oldHousehold.HouseholdMembers.Add(newHm);
                            continue;
                        }
                        foreach (var newEo in newHm.EatingOccasions)
                        {
                            EatOccasion oldEo = oldHm.EatingOccasions.FirstOrDefault(x => x.TimeStart.CompareTo(newEo.TimeStart) == 0);
                            if (oldEo == null)
                            {
                                foreach (var er in newEo.EatRecords)
                                    er.ImageRecord.Household = oldHousehold;
                                oldHm.EatingOccasions.Add(newEo);
                                continue;
                            }
                            if (newEo.EatRecords == null)
                                continue;
                            foreach (var newEr in newEo.EatRecords)
                            {
                                EatRecord oldEr = oldEo.EatRecords.FirstOrDefault(x => (x.ImageRecord.ImageName != null && x.ImageRecord.ImageName.Equals(newEr.ImageRecord.ImageName))
                                || x.ImageRecord.CaptureTime.CompareTo(newEr.ImageRecord.CaptureTime) == 0);
                                if (oldEr == null)
                                {
                                    //Need to check if the EatRecord is just new to the one member, as the image record might already exist
                                    var existingIr = oldHousehold.ImageRecords.FirstOrDefault(x => (x.ImageName?.Equals(newEr.ImageRecord.ImageName) ?? false)
                                        || x.CaptureTime.CompareTo(newEr.ImageRecord.CaptureTime) == 0);
                                    if (existingIr != null)
                                        newEr.ImageRecord = existingIr; //Just assign the imagerecord as the new one. If it needs updating, the other instances can take care of that.
                                    //This makes sense because if it's being added here but the IR already exists in the system, it must be somewhere else
                                    //TODO if the participant has been changed this won't update, fix this

                                    newEr.ImageRecord.Household = oldHousehold;
                                    if (newEr.Leftovers != null)
                                        newEr.Leftovers.Household = oldHousehold;
                                    oldEo.EatRecords.Add(newEr);
                                    continue;
                                }

                                //This instance already exists in the database. Check if it has been updated at all, and update the existing one with new details if so.
                                ImageRecord oldIr = oldEr.ImageRecord;
                                ImageRecord newIr = newEr.ImageRecord;
                                //if (newIr.AnnotatedStatus == AnnotatedStatuses.None)
                                    //continue;

                                oldIr.AnnotationStatus = newIr.AnnotationStatus;
                                oldIr.AnnotatedStatus = newIr.AnnotatedStatus;
                                oldIr.AudioName = newIr.AudioName;
                                oldIr.ImageName = newIr.ImageName;
                                oldIr.CaptureTime = newIr.CaptureTime;
                                oldIr.TextDescription = oldIr.TextDescription ?? newIr.TextDescription; //Don't overwrite if there's already data in the system

                                //checked wiremesh priority actually works
                                if (newIr.GuestInfo == null && oldIr != null)
                                    oldIr.GuestInfo = null;
                                else if (newIr.GuestInfo != null && oldIr.GuestInfo == null)
                                    oldIr.GuestInfo = new HouseholdGuestInfo()
                                    {
                                        ChildGuests = newIr.GuestInfo.ChildGuests,
                                        AdultMaleGuests = newIr.GuestInfo.AdultMaleGuests,
                                        AdultFemaleGuests = newIr.GuestInfo.AdultFemaleGuests,
                                        GuestInfoId = newIr.GuestInfo.GuestInfoId
                                    };
                                else if (oldIr.GuestInfo != null && newIr.GuestInfo != null)
                                {
                                    oldIr.GuestInfo.AdultFemaleGuests = newIr.GuestInfo.AdultFemaleGuests;
                                    oldIr.GuestInfo.AdultMaleGuests = newIr.GuestInfo.AdultMaleGuests;
                                    oldIr.GuestInfo.ChildGuests = newIr.GuestInfo.ChildGuests;
                                }

                                foreach (var newCm in newIr.Comments)
                                {
                                    //Comment oldCm = oldIr.Comments.FirstOrDefault(x => x.CreatedTime.CompareTo(newCm.CreatedTime) == 0);
                                    Comment oldCm = oldIr.Comments.FirstOrDefault(x => x.CreatedTime.Month == newCm.CreatedTime.Month
                                        && x.CreatedTime.Day == newCm.CreatedTime.Day
                                        && x.CreatedTime.Hour == newCm.CreatedTime.Hour
                                        && x.CreatedTime.Minute == newCm.CreatedTime.Minute
                                        && x.CreatedTime.Second == newCm.CreatedTime.Second);
                                    if (oldCm == null)
                                    {
                                        oldIr.Comments.Add(newCm);
                                    }
                                }
                            }
                        }
                    }
                    foreach (var newRc in newHousehold.HouseholdRecipes)
                    {
                        CookRecipe oldRc = oldHousehold.HouseholdRecipes.FirstOrDefault(x => x.CaptureTime.CompareTo(newRc.CaptureTime) == 0);
                        if (oldRc == null)
                        {
                            CookRecipe recipe = new CookRecipe()
                            {
                                //Id = newRc.Id,
                                OriginId = newRc.OriginId,
                                Name = newRc.Name,
                                ImageName = newRc.ImageName,
                                IsFiducialPresent = newRc.IsFiducialPresent,
                                Homography = newRc.Homography,
                                AudioName = newRc.AudioName,
                                TextDescription = newRc.TextDescription,
                                CaptureTime = newRc.CaptureTime,
                                FoodComposition = newRc.FoodComposition,
                                Ingredients = new List<CookIngredient>(),
                                Comments = new List<Comment>(),//newRc.Comments,
                                YieldFactor = newRc.YieldFactor,
                                YieldFactorSource = newRc.YieldFactorSource,
                                NTranscript = newRc.NTranscript,
                                Transcript = newRc.Transcript,
                                Hidden = newRc.Hidden,
                                AnnotatedStatus = newRc.AnnotatedStatus,
                            };

                            foreach (var c in newRc.Comments)
                            {
                                recipe.Comments.Add(new Comment()
                                {
                                    Text = c.Text,
                                    CreatedTime = c.CreatedTime,
                                    Flag = c.Flag,
                                    HighPriority = c.HighPriority
                                });
                            }

                            foreach (var ni in newRc.Ingredients)
                            {
                                CookIngredient cing = new CookIngredient()
                                {
                                    //ImageRecord = newIng.ImageRecord,
                                    OriginId = ni.OriginId,
                                    CookMethod = ni.CookMethod,
                                    CookDescription = ni.CookDescription,
                                    AnnotatedStatus = ni.AnnotatedStatus
                                };
                                cing.ImageRecord = new ImageRecord()
                                {
                                    CaptureTime = ni.ImageRecord.CaptureTime,
                                    RecordType = ni.ImageRecord.RecordType,
                                    ImageName = ni.ImageRecord.ImageName,
                                    AudioName = ni.ImageRecord.AudioName,
                                    TextDescription = ni.ImageRecord.TextDescription,
                                    AnnotationStatus = ni.ImageRecord.AnnotationStatus,
                                    AnnotatedStatus = ni.ImageRecord.AnnotatedStatus,
                                    Comments = new List<Comment>(),
                                    Household = oldHousehold
                                };
                                foreach (var comment in ni.ImageRecord.Comments)
                                    cing.ImageRecord.Comments.Add(new Comment()
                                    {
                                        CreatedTime = comment.CreatedTime,
                                        Flag = comment.Flag,
                                        Text = comment.Text
                                    });
                                recipe.Ingredients.Add(cing);
                            }
                            oldHousehold.HouseholdRecipes.Add(recipe);
                            //oldHousehold.HouseholdRecipes.Add(newRc);
                            continue;
                        }

                        oldRc.AnnotatedStatus = newRc.AnnotatedStatus;
                        oldRc.CaptureTime = newRc.CaptureTime;
                        oldRc.Name = newRc.Name ?? oldRc.Name;
                        oldRc.TextDescription = newRc.TextDescription ?? oldRc.TextDescription;
                        oldRc.ImageName = newRc.ImageName;

                        foreach (var com in newRc.Comments)
                        {
                            var oldCom = oldRc.Comments.FirstOrDefault(x => x.Text?.Equals(com.Text) ?? false);
                            if (oldCom == null)
                                oldRc.Comments.Add(new Comment()
                                {
                                    CreatedTime = com.CreatedTime,
                                    Flag = com.Flag,
                                    Text = com.Text
                                });
                        }

                        foreach (var newIng in newRc.Ingredients)
                        {
                            string imageName = newIng.ImageRecord.ImageName;
                            oldRc.Ingredients.FirstOrDefault(x => (x.ImageRecord?.ImageName?.Equals(imageName) ?? false));
                            CookIngredient oldIng = null;
                            foreach (var ing in oldRc.Ingredients)
                            {
                                if (ing.ImageRecord != null && ing.ImageRecord.ImageName != null && ing.ImageRecord.ImageName.Equals(newIng.ImageRecord.ImageName))
                                {
                                    oldIng = ing;
                                    break;
                                }
                                if (ing.ImageRecord?.TextDescription?.Equals(newIng.ImageRecord?.TextDescription) ?? false)
                                {
                                    oldIng = ing;
                                    break;
                                }
                            }
                            //var oldIng = oldRc.Ingredients.FirstOrDefault(x => (x.ImageRecord?.ImageName != null && x.ImageRecord?.ImageName.Equals(newIng.ImageRecord.ImageName)
                            //    || (x.ImageRecord.TextDescription?.Equals(newIng.ImageRecord?.TextDescription) ?? false)));
                            if (oldIng == null)
                            {
                                //db.Entry(newIng.ImageRecord).State = EntityState.Detached;
                                CookIngredient cing = new CookIngredient()
                                {
                                    //ImageRecord = newIng.ImageRecord,
                                    OriginId = newIng.OriginId,
                                    CookMethod = newIng.CookMethod,
                                    CookDescription = newIng.CookDescription,
                                    AnnotatedStatus = newIng.AnnotatedStatus
                                };
                                cing.ImageRecord = new ImageRecord()
                                {
                                    CaptureTime = newIng.ImageRecord.CaptureTime,
                                    RecordType = newIng.ImageRecord.RecordType,
                                    ImageName = newIng.ImageRecord.ImageName,
                                    AudioName = newIng.ImageRecord.AudioName,
                                    TextDescription = newIng.ImageRecord.TextDescription,
                                    AnnotationStatus = newIng.ImageRecord.AnnotationStatus,
                                    AnnotatedStatus = newIng.ImageRecord.AnnotatedStatus,
                                    Comments = new List<Comment>(),
                                    Household = oldHousehold
                                };
                                foreach (var comment in newIng.ImageRecord.Comments)
                                    cing.ImageRecord.Comments.Add(new Comment()
                                    {
                                        CreatedTime = comment.CreatedTime,
                                        Flag = comment.Flag,
                                        Text = comment.Text
                                    });
                                oldRc.Ingredients.Add(cing);
                                continue;
                            }
                            oldIng.AnnotatedStatus = newIng.AnnotatedStatus;
                            oldIng.CookDescription = newIng.CookDescription;

                            oldIng.ImageRecord.CaptureTime = newIng.ImageRecord.CaptureTime;
                            oldIng.ImageRecord.TextDescription = newIng.ImageRecord.TextDescription ?? oldIng.ImageRecord.TextDescription;
                            oldIng.ImageRecord.AnnotationStatus = newIng.ImageRecord.AnnotationStatus;
                            oldIng.ImageRecord.AnnotatedStatus = newIng.ImageRecord.AnnotatedStatus;

                            for (int i = 0; i < newIng.ImageRecord.Comments.Count; i++)
//                            foreach (var com in newIng.ImageRecord.Comments)
                            {
                                var com = newIng.ImageRecord.Comments.ElementAt(i);
                                var oldCom = oldIng.ImageRecord.Comments.FirstOrDefault(x => x.Text?.Equals(com.Text) ?? false);
                                if (oldCom == null)
                                    oldIng.ImageRecord.Comments.Add(new Comment()
                                    {
                                        CreatedTime = com.CreatedTime,
                                        Flag = com.Flag,
                                        Text = com.Text
                                    });
                            }
                        }
                    }
                    foreach (var newMeal in newHousehold.HouseholdMeals)
                    {
                        HouseholdMeal oldMeal = oldHousehold.HouseholdMeals.FirstOrDefault(x => x.StartTime.CompareTo(newMeal.StartTime) == 0);
                        if (oldMeal == null)
                        {
                            oldHousehold.HouseholdMeals.Add(newMeal);
                        }
                    }
                }
                else
                {
                    db.Households.Add(newHousehold); //Just add the new one
                }

                db.SaveChanges();

                return CreatedAtRoute("DefaultApi", new { id = newHousehold.Id }, newHousehold.Guid);
            }
            catch (Exception e)
            {
                var message = e.Message;
                return BadRequest(e.Message/*new Exception("An error occurred on the server")*/);
            }
        }

        [HttpGet, Route("api/Households/{hhid}/CopyTo/{studyid}"), Authorize(Roles = "admin")]
        public IHttpActionResult CopyHousehold(int hhid, int studyid)
        {
            Household dbhh = db.Households.FirstOrDefault(x => x.Id == hhid);
            Study to = db.Studies.FirstOrDefault(x => x.Id == studyid);
            if (dbhh == null || to == null)
                return BadRequest();

            List<FoodItem> toAssignRecipes = new List<FoodItem>();

            Household hh = new Household()
            {
                Study = to,
                Guid = Guid.NewGuid().ToString(),
                ParticipantId = dbhh.ParticipantId,
                StartDate = dbhh.StartDate,
                EndDate = dbhh.EndDate,
                ImageRecords = new List<ImageRecord>(),
                HouseholdRecipes = new List<CookRecipe>(),
                HouseholdMembers = new List<HouseholdMember>(),
                HouseholdGuestInfo = new List<HouseholdGuestInfo>()
            };

            foreach (var dbir in dbhh.ImageRecords)
            {
                var ir = new ImageRecord()
                {
                    LockTimestamp = null,

                    AudioName = dbir.AudioName,
                    AudioUrl = dbir.AudioUrl,
                    AudioUrlUpdated = dbir.AudioUrlUpdated,
                    CaptureTime = dbir.CaptureTime,
                    Comments = new List<Comment>(),
                    FoodItems = new List<FoodItem>(),
                    Hidden = dbir.Hidden,
                    Id = dbir.Id,
                    ImageName = dbir.ImageName,
                    ImageThumbUrl = dbir.ImageThumbUrl,
                    ImageUrlUpdated = dbir.ImageUrlUpdated,
                    ImageUrl = dbir.ImageUrl,
                    IsCompleted = dbir.IsCompleted,
                    IsFiducialPresent = dbir.IsFiducialPresent,
                    IsLeftovers = dbir.IsLeftovers,
                    ManualTranscript = dbir.ManualTranscript,
                    NTranscript = dbir.NTranscript,
                    RecordType = dbir.RecordType,
                    TextDescription = dbir.TextDescription,
                    Transcript = dbir.Transcript,
                    Translation = dbir.Translation,
                    Updates = new List<RecordHistory>()
                };
                if (dbir.Homography != null)
                    ir.Homography = new ImageHomography(dbir.Homography);
                hh.ImageRecords.Add(ir);

                foreach (var dbfi in dbir.FoodItems)
                {
                    var fi = new FoodItem()
                    {
                        CreateStart = dbfi.CreateStart,
                        CreateEnd = dbfi.CreateEnd,
                        FoodComposition = dbfi.FoodComposition,
                        FoodCompositionId = dbfi.FoodCompositionId,
                        MeasureCount = dbfi.MeasureCount,
                        MeasureType = dbfi.MeasureType,
                        Name = dbfi.Name,
                        QuantityGrams = dbfi.QuantityGrams,
                        TagXPercent = dbfi.TagXPercent,
                        TagYPercent = dbfi.TagYPercent,
                        ToolMeasure = dbfi.ToolMeasure,
                        ToolSource = dbfi.ToolSource,
                        Updates = new List<RecordHistory>()
                    };
                    ir.FoodItems.Add(fi);
                    if (dbfi.FoodComposition != null && dbfi.FoodComposition?.Table_Id == null)
                        toAssignRecipes.Add(fi);

                    foreach (var dbrh in dbfi.Updates)
                    {
                        var rh = new RecordHistory()
                        {
                            Action = dbrh.Action,
                            FoodCompositionId = dbrh.FoodCompositionId,
                            QuantityGrams = dbrh.QuantityGrams,
                            Time = dbrh.Time,
                            ToolMeasure = dbrh.ToolMeasure,
                            ToolSource = dbrh.ToolSource,
                            User = dbrh.User,
                            FoodItem = fi
                        };
                        //fi.Updates.Add(rh);
                        ir.Updates.Add(rh);
                    }
                }

                if (dbir.GuestInfo != null)
                {
                    var gi = new HouseholdGuestInfo()
                    {
                        AdultFemaleGuests = dbir.GuestInfo.AdultFemaleGuests,
                        AdultMaleGuests = dbir.GuestInfo.AdultMaleGuests,
                        ChildGuests = dbir.GuestInfo.ChildGuests,
                        ModAdultFemaleGuests = dbir.GuestInfo.ModAdultFemaleGuests,
                        ModAdultMaleGuests = dbir.GuestInfo.ModAdultMaleGuests,
                        ModChildGuests = dbir.GuestInfo.ModChildGuests
                    };
                    ir.GuestInfo = gi;
                    hh.HouseholdGuestInfo.Add(gi);
                }

                foreach (var dbc in dbir.Comments)
                {
                    var c = new Comment()
                    {
                        CreatedBy = dbc.CreatedBy,
                        CreatedTime = dbc.CreatedTime,
                        Flag = dbc.Flag,
                        Hidden = dbc.Hidden,
                        ReplyTo = dbc.ReplyTo,
                        Text = dbc.Text,

                        //ImageRecord = ir
                    };
                    ir.Comments.Add(c);
                }
            }

            foreach (var dbhm in dbhh.HouseholdMembers)
            {
                var hm = new HouseholdMember()
                {
                    Age = dbhm.Age,
                    IsMother = dbhm.IsMother,
                    IsBreastfed = dbhm.IsBreastfed,
                    ParticipantId = dbhm.ParticipantId,
                    IsFemale = dbhm.IsFemale,
                    LifeStage = dbhm.LifeStage,
                    EatingOccasions = new List<EatOccasion>()
                };
                hh.HouseholdMembers.Add(hm);

                foreach (var dbeo in dbhm.EatingOccasions)
                {
                    var eo = new EatOccasion()
                    {
                        EatRecords = new List<EatRecord>(),
                        Finalized = dbeo.Finalized,
                        IsBreastfeedOccasion = dbeo.IsBreastfeedOccasion,
                        Original = dbeo.Original,
                        OriginId = dbeo.OriginId,
                        TimeEnd = dbeo.TimeEnd,
                        TimeStart = dbeo.TimeStart
                    };
                    hm.EatingOccasions.Add(eo);
                    
                    foreach (var dber in dbeo.EatRecords)
                    {
                        var er = new EatRecord()
                        {
                            ImageRecord = hh.ImageRecords.FirstOrDefault(x => x.Id == dber.ImageRecord.Id),

                            Finalized = dber.Finalized,
                            FinalizeTime = dber.FinalizeTime,
                            Hidden = dber.Hidden,
                            Leftovers = dber.Leftovers,
                            Original = dber.Original,
                            OriginId = dber.OriginId,
                        };
                        eo.EatRecords.Add(er);

                        if (dber.Leftovers != null)
                            er.Leftovers = hh.ImageRecords.FirstOrDefault(x => x.Id == dber.Leftovers.Id);
                    }
                }
            }

            foreach (var dbcr in dbhh.HouseholdRecipes)
            {
                var cr = new CookRecipe()
                {
                    AudioName = dbcr.AudioName,
                    AudioUrl = dbcr.AudioUrl,
                    AudioUrlUpdated = dbcr.AudioUrlUpdated,
                    CaptureTime = dbcr.CaptureTime,
                    Comments = new List<Comment>(),
                    FoodComposition = new Models.FCT.FoodComposition()
                    {
                        ModifiedDate = dbcr.FoodComposition.ModifiedDate,
                        Name = dbcr.FoodComposition.Name,
                    },
                    Hidden = dbcr.Hidden,
                    ImageName = dbcr.ImageName,
                    ImageThumbUrl = dbcr.ImageThumbUrl,
                    ImageUrl = dbcr.ImageUrl,
                    ImageUrlUpdated = dbcr.ImageUrlUpdated,
                    Ingredients = new List<CookIngredient>(),
                    IsFiducialPresent = dbcr.IsFiducialPresent,
                    Name = dbcr.Name,
                    NTranscript = dbcr.NTranscript,
                    OriginId = dbcr.OriginId,
                    TextDescription = dbcr.TextDescription,
                    TotalCookedGrams = dbcr.TotalCookedGrams,
                    Transcript = dbcr.Transcript,
                    YieldFactor = dbcr.YieldFactor,
                    YieldFactorSource = dbcr.YieldFactorSource
                };
                if (dbcr.Homography != null)
                    cr.Homography = new ImageHomography(dbcr.Homography);
                hh.HouseholdRecipes.Add(cr);

                var matches = toAssignRecipes.Where(x => x.FoodCompositionId == dbcr.FoodComposition.Id);
                foreach (var m in matches)
                {
                    m.FoodComposition = cr.FoodComposition;
                }

                foreach (var dbci in dbcr.Ingredients)
                {
                    var ci = new CookIngredient()
                    {
                        ImageRecord = hh.ImageRecords.FirstOrDefault(x => x.Id == dbci.ImageRecord.Id),

                        CookDescription = dbci.CookDescription,
                        CookMethod = dbci.CookMethod,
                        OriginId = dbci.OriginId
                    };

                    cr.Ingredients.Add(ci);
                }

                foreach (var dbc in dbcr.Comments)
                {
                    var c = new Comment()
                    {
                        CreatedBy = dbc.CreatedBy,
                        CreatedTime = dbc.CreatedTime,
                        Flag = dbc.Flag,
                        Hidden = dbc.Hidden,
                        ReplyTo = dbc.ReplyTo,
                        Text = dbc.Text,

                        //Recipe = cr
                    };
                    cr.Comments.Add(c);
                }
            }

            foreach (var ir in hh.ImageRecords)
                ir.Id = 0;

            db.Households.Add(hh);
            db.SaveChanges();

            return Ok(hh.Id);
        }

        //[HttpGet, Route("api/SaveLogfiles")]
        //public IHttpActionResult SaveLogfilesLocally()
        //{
        //    var households = db.Households.Include(x => x.UsageLog);
        //    List<string> files = new List<string>();

        //    foreach (var h in households)
        //    {
        //        if (h.UsageLog == null)
        //            continue;

        //        var text = h.UsageLog.RawData.Substring(138);

        //        var fileName = h.ParticipantId + "_" + h.Guid + "_log.txt";
        //        var filePath = "D:\\VISIDA\\UsageLogs\\" + fileName;//System.Web.HttpContext.Current.Server.MapPath("~/" + fileName);
        //        files.Add(filePath);

        //        using (System.IO.StreamWriter writer = System.IO.File.CreateText(filePath))
        //        {
        //            writer.Write(text);
        //        }
        //    }

        //    return Ok(files);
        //}

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                db.Dispose();
            }
            base.Dispose(disposing);
        }

        private bool HouseholdExists(int id)
        {
            return db.Households.Count(e => e.Id == id) > 0;
        }
    }
}