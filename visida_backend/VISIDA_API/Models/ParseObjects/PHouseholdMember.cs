using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.ParseObjects
{
    public class PHouseholdMember
    {
        public float Age { get; set; }

        public bool IsBreastfed { get; set; }
        public bool IsMother { get; set; }
        public bool IsFemale { get; set; }
        public string LifeStage { get; set; }
        public string ParticipantHouseholdId { get; set; }
        public string ParticipantHouseholdMemberId { get; set; }

        public List<PFoodRecord> FoodRecords { get; set; }

        public List<PEatOccasion> EatOccasions
        {
            get
            {
                var ret = 
                    (from redundant in FoodRecords
                    select redundant into f
                    from eat in f.EatingOccasions
                    select eat);

                return ret.ToList();
            }
        }

        public HouseholdMember ToInternal(Household household, PHousehold pHousehold)
        {
            HouseholdMember h = new HouseholdMember()
            {
                ParticipantId = this.ParticipantHouseholdMemberId.Trim(),
                Age = this.Age,
                IsBreastfed = this.IsBreastfed,
                IsMother = this.IsMother,
                IsFemale = this.IsFemale,
                LifeStage = this.LifeStage,

                EatingOccasions = this.EatOccasions.Select(x => x.ToInternal(household, pHousehold)).ToList()
            };

            return h;
        }
    }
}