using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EEatOccasionDay
    {
        public ICollection<EEatRecord> EatRecords { get; set; }
        public int? HouseholdMemberId { get; set; }
        public string HouseholdMemberParticipantId { get; set; }
        public DateTime Date { get; set; }
        public int Meals { get; set; }
        
        public EEatOccasionDay()
        {

        }

        public EEatOccasionDay(IGrouping<DateTime, EatOccasion> e)
        {
            Date = e.Key;
            Meals = e.Count();
            EatRecords = e.SelectMany(x => x.EatRecords).Select(x => (EEatRecord)x).ToList();
            //HouseholdMemberId = e.FirstOrDefault()?.HouseholdMember.Id;
            HouseholdMemberParticipantId = e.FirstOrDefault()?.HouseholdMember.ParticipantId;
        }
    }
}