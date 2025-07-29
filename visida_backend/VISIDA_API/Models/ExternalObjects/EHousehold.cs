using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;
using static VISIDA_API.Models.InternalObjects.Household;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EHousehold
    {
        public int Id { get; set; }
        public string Guid { get; set; }
        public string ParticipantId { get; set; }
        public string Country { get; set; }
        public EStudy Study { get; set; }
        public List<EHouseholdMember> HouseholdMembers { get; set; }
        public List<ECookRecipe> HouseholdRecipes { get; set; }
        public List<EHouseholdAssignation> HouseholdAssignations { get; set; }
        public List<EConversionHistory> ConversionHistories { get; set; }
        public List<Work> WorkDone { get; set; }
        public int RecordTotal { get; set; }
        public int RecordNotStarted { get; set; }
        public int IdentifyInProgress { get; set; }
        public int IdentifyCompleted { get; set; }
        public int PortionInProgress { get; set; }
        public int PortionCompleted { get; set; }
        public int IdentifyAndPortionCompleted { get; set; }
        public int HiddenRecordTotal { get; set; }
        public int LeftOversTotal { get; set; }
        public int FoodItemTotal { get; set; }
        public int RecipeTotal { get; set; }
        public int HiddenRecipeTotal { get; set; }
        //public int IngredientTotal { get; set; }

        public List<Tuple<string, string>> AllImages { get; set; }

        public static implicit operator EHousehold(Household h)
        {
            return new EHousehold()
            {
                Id = h.Id,
                Guid = h.Guid,
                ParticipantId = h.ParticipantId,
                Country = h.Country,
                HouseholdMembers = h.HouseholdMembers.Select(x => (EHouseholdMember)x).ToList(),
                HouseholdAssignations = h.HouseholdAssignments.Select(x => (EHouseholdAssignation)x).ToList(),
                //WorkDone = h.WorkDone,
                RecordTotal = h.RecordTotal,
                RecordNotStarted = h.RecordNotStarted,
                IdentifyInProgress = h.IdentifyInProgress,
                IdentifyCompleted = h.IdentifyCompleted,
                PortionInProgress = h.PortionInProgress,
                PortionCompleted = h.PortionCompleted,
                IdentifyAndPortionCompleted = h.IdentifyAndPortionCompleted,
                HiddenRecordTotal = h.HiddenRecordTotal,
                LeftOversTotal = h.LeftOversTotal,
                FoodItemTotal = h.FoodItemTotal,
                RecipeTotal = h.RecipeTotal,
                HiddenRecipeTotal = h.HiddenRecipeTotal
                // IngredientTotal = h.IngredientTotal
            };
        }
    }
}