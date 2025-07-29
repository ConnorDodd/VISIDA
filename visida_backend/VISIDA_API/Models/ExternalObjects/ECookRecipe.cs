using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.FCT;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.ExternalObjects
{
    public class ECookRecipe
    {
        public int Id { get; set; }
        public string HouseholdParticipantId { get; set; }
        public string Name { get; set; }

        public string ImageName { get; set; }
        public string ImageUrl { get; set; }
        public string ImageThumbUrl { get; set; }
        public bool IsFiducialPresent { get; set; }
        public ImageHomography Homography { get; set; }

        public string AudioName { get; set; }
        public string AudioUrl { get; set; }

        public DateTime CaptureTime { get; set; }
        public string TextDescription { get; set; }
        public bool Hidden { get; set; }
        public string Transcript { get; set; }
        public string NTranscript { get; set; }

        public int FoodCompositionId { get; set; }
        public FoodComposition FoodComposition { get; set; }
        public ICollection<ECookIngredient> Ingredients { get; set; }
        public ICollection<EComment> Comments { get; set; }

        public ICollection<ERecipeUsage> Usages { get; set; }
        public ICollection<YieldFactor> MatchingFactors { get; set; }

        public double? YieldFactor { get; set; }
        public string YieldFactorSource { get; set; }
        public double TotalCookedGrams { get; set; }

        public int? UsageCount { get; set; }
        public EWorkAssignation Assignation { get; set; }
        public int? StudyId { get; set; }
        public bool IsSource { get; set; }

        public static ECookRecipe ToShallow(CookRecipe c)
        {
            ECookRecipe recipe = new ECookRecipe()
            {
                Id = c.Id,
                Hidden = c.Hidden,
                HouseholdParticipantId = c.Household.ParticipantId,
                Name = c.Name,
                ImageName = c.ImageName,
                ImageUrl = c.ImageUrlUpdated ?? c.ImageUrl,
                IsFiducialPresent = c.IsFiducialPresent,
                //Homography = c.Homography,
                AudioName = c.AudioUrlUpdated ?? c.AudioName,
                AudioUrl = c.AudioUrl,
                CaptureTime = c.CaptureTime,
                TextDescription = c.TextDescription,
                Ingredients = new List<ECookIngredient>(),
                //Ingredients = c.Ingredients.Select(x => (ECookIngredient)x).ToList(),
                YieldFactor = c.YieldFactor,
                YieldFactorSource = c.YieldFactorSource,
                ImageThumbUrl = c.ImageThumbUrl,
                TotalCookedGrams = c.TotalCookedGrams,
                Comments = c.Comments.Select(x => (EComment)x).ToList(),
                NTranscript = c.NTranscript,
                Transcript = c.Transcript
            };
            return recipe;
        }

        public static implicit operator ECookRecipe(CookRecipe c)
        {
            ECookRecipe e = new ECookRecipe
            {
                Id = c.Id,
                Hidden = c.Hidden,
                HouseholdParticipantId = c.Household.ParticipantId,
                Name = c.Name,
                ImageName = c.ImageName,
                ImageUrl = c.ImageUrlUpdated ?? c.ImageUrl,
                IsFiducialPresent = c.IsFiducialPresent,
                Homography = c.Homography,
                AudioName = c.AudioName,
                AudioUrl = c.AudioUrlUpdated ?? c.AudioUrl,
                CaptureTime = c.CaptureTime,
                TextDescription = c.TextDescription,
                FoodCompositionId = c.FoodComposition.Id,
                FoodComposition = c.FoodComposition,
                Ingredients = c.Ingredients.Select(x => (ECookIngredient)x).ToList(),
                YieldFactor = c.YieldFactor,
                YieldFactorSource = c.YieldFactorSource,
                Comments = c.Comments.Select(x => (EComment)x).ToList(),
                ImageThumbUrl = c.ImageThumbUrl,
                TotalCookedGrams = c.TotalCookedGrams,
                NTranscript = c.NTranscript,
                Transcript = c.Transcript
            };

            return e;
        }

        public class ERecipeUsage
        {
            public int Id { get; set; }
            public DateTime RecordTime { get; set; }
        }
    }
}