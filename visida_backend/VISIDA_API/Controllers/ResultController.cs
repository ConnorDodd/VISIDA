using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using System.Web.Http.Description;
using System.Data.Entity;
using VISIDA_API.Models;
using VISIDA_API.Models.ExternalObjects;
using System.Diagnostics;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Controllers
{
    [Authorize(Roles = "admin,analyst,coordinator")]
    public class ResultController : ApiController
    {
        private VISIDA_APIContext db = new VISIDA_APIContext();

        [ResponseType(typeof(EHousehold)), Route("api/Result/Intake/{studyid}")]
        public IHttpActionResult GetIntakeStudy([FromUri] int studyid, [FromUri]string[] hhid)
        {
            if (studyid <= 0 || hhid == null || hhid.Length <= 0)
                return BadRequest();
            var hhs = db.Households.Include(x => x.HouseholdMembers)
                .Include(x => x.HouseholdMembers.Select(y => y.EatingOccasions))
                .Include(x => x.HouseholdMembers.Select(y => y.EatingOccasions.Select(z => z.EatRecords)))
                .Include(x => x.ImageRecords).Include(x => x.ImageRecords.Select(y => y.FoodItems))
                .Include(x => x.ImageRecords).Include(x => x.ImageRecords.Select(y => y.Comments))
                .Include(x => x.ImageRecords.Select(y => y.FoodItems.Select(z => z.FoodComposition)))
                .Include(x => x.HouseholdMeals).Include(x => x.HouseholdGuestInfo)
                .Where(x => x.Study_Id == studyid && hhid.Contains(x.ParticipantId));

            return Ok(ParseHouseholds(hhs));
        }

        private List<EHousehold> ParseHouseholds(IQueryable<Household> hhs)
        {
            List<EHousehold> households = new List<EHousehold>();
            foreach (var household in hhs)
            {
                EHousehold retHouse = new EHousehold()
                {
                    Country = household.Country,
                    ParticipantId = household.ParticipantId,
                    Guid = household.Guid,
                    HouseholdMembers = new List<EHouseholdMember>()
                };
                foreach (var hm in household.HouseholdMembers)
                {
                    var member = new EHouseholdMember()
                    {
                        Id = hm.Id,
                        Age = hm.Age,
                        IsFemale = hm.IsFemale,
                        IsBreastfed = hm.IsBreastfed,
                        IsMother = hm.IsMother,
                        LifeStage = hm.LifeStage,
                        ParticipantId = hm.ParticipantId,
                        EatRecords = new List<EEatRecord>()
                    };
                    var records = hm.EatingOccasions.SelectMany(x => x.EatRecords);
                    foreach (var er in records)
                    {
                        var eatRecord = new EEatRecord()
                        {
                            Id = er.Id,
                            FinalizeTime = er.FinalizeTime,
                            Finalized = er.Finalized,
                            Hidden = er.Hidden
                        };
                        eatRecord.ImageRecord = EImageRecord.ToShallowEImageRecord(er.ImageRecord);
                        eatRecord.Leftovers = EImageRecord.ToShallowEImageRecord(er.Leftovers);

                        member.EatRecords.Add(eatRecord);
                    }
                    retHouse.HouseholdMembers.Add(member);
                }

                households.Add(retHouse);
            }

            return households;
        }

        [ResponseType(typeof(EHousehold)), Route("api/Result/Recipe/{studyid}")]
        public IHttpActionResult GetRecipeStudy([FromUri] int studyId, [FromUri] string[] hhid)
        {
            if (studyId <= 0 || hhid == null || hhid.Length <= 0)
                return BadRequest();
            var recipes = db.CookRecipes
                .Include(x => x.Household).Include(x => x.FoodComposition)
                .Include(x => x.Ingredients).Include(x => x.Ingredients.Select(y => y.ImageRecord)).Include(x => x.Ingredients.Select(y => y.ImageRecord.FoodItems))
                .Include(x => x.Ingredients.Select(y => y.ImageRecord.FoodItems.Select(z => z.FoodComposition)))
                .Where(x => x.Household.Study_Id == studyId && hhid.Contains(x.Household.ParticipantId));


            var fcIds = recipes.Select(x => x.FoodComposition.Id).ToList();
            var usages = db.FoodItems.Where(x => fcIds.Contains(x.FoodCompositionId ?? 0)).Select(x => x.FoodCompositionId).ToList();

            return Ok(ParseRecipes(recipes, usages));
        }

        private List<ECookRecipe> ParseRecipes(IQueryable<CookRecipe> rows, List<int?> usages)
        {
            List<ECookRecipe> recipes = new List<ECookRecipe>();

            foreach (var c in rows)
            {
                var r = new ECookRecipe
                {
                    Id = c.Id,
                    Hidden = c.Hidden,
                    HouseholdParticipantId = c.Household.ParticipantId,
                    Name = c.Name,
                    ImageName = c.ImageName,
                    ImageUrl = c.ImageUrlUpdated ?? c.ImageUrl,
                    IsFiducialPresent = c.IsFiducialPresent,
                    AudioName = c.AudioName,
                    AudioUrl = c.AudioUrlUpdated ?? c.AudioUrl,
                    CaptureTime = c.CaptureTime,
                    TextDescription = c.TextDescription,
                    FoodCompositionId = c.FoodComposition.Id,
                    FoodComposition = c.FoodComposition,
                    Ingredients = c.Ingredients.Select(x => new ECookIngredient()
                    {
                        CookMethod = x.CookMethod,
                        CookDescription = x.CookDescription,
                        ImageRecord = new EImageRecord()
                        {
                            Id = x.ImageRecord.Id,
                            Hidden = x.ImageRecord.Hidden,
                            CaptureTime = x.ImageRecord.CaptureTime,
                            FoodItems = x.ImageRecord.FoodItems.Select(i => (EFoodItem)i).ToList(),
                            IsCompleted = x.ImageRecord.IsCompleted,
                            Is24HR = x.ImageRecord.Is24HR,
                        }
                    }).ToList(),
                    YieldFactor = c.YieldFactor,
                    YieldFactorSource = c.YieldFactorSource,
                    ImageThumbUrl = c.ImageThumbUrl,
                    TotalCookedGrams = c.TotalCookedGrams,
                    UsageCount = 0
                };

                foreach (var u in usages)
                    if (u != null && u == r.FoodCompositionId)
                        r.UsageCount++;

                recipes.Add(r);
            }
            return recipes;
        }
    }
}
