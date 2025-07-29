using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EHouseholdMember
    {
        public int Id { get; set; }
        public string ParticipantId { get; set; }

        public float Age { get; set; }

        public bool IsMother { get; set; } = false;
        public bool IsBreastfed { get; set; } = false;
        public bool IsFemale { get; set; } = false;
        public string LifeStage { get; set; } = "NONE";

        public string PregnancyTrimester { get; set; } = "NA";
        public float Weight { get; set; } //Kgs
        public float Height { get; set; } //Cms

        public bool Hidden { get; set; }
        public bool Original { get; set; }

        public List<EFoodRecord> FoodRecords { get; set; }
        public List<EEatRecord> EatRecords { get; set; }

        public static implicit operator EHouseholdMember(HouseholdMember m)
        {
            return new EHouseholdMember
            {
                Id = m.Id,
                ParticipantId = m.ParticipantId,
                Age = m.Age,
                IsMother = m.IsMother,
                IsBreastfed = m.IsBreastfed,
                IsFemale = m.IsFemale,
                LifeStage = m.LifeStage,

                PregnancyTrimester = m.PregnancyTrimester,
                Weight = m.Weight,
                Height = m.Height
            };
        }
    }
}