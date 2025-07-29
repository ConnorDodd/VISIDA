using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EEatOccasion
    {
        public int Id { get; set; }
        public IList<EEatRecord> EatRecords { get; set; } = new List<EEatRecord>();
        public int HouseholdMemberId { get; set; }
        public string HouseholdMemberParticipantId { get; set; }
        public DateTime TimeStart { get; set; }
        public DateTime TimeEnd { get; set; }
        public bool Finalized { get; set; }

        public static implicit operator EEatOccasion(EatOccasion e)
        {
            return new EEatOccasion()
            {
                Id = e.Id,
                HouseholdMemberParticipantId = e.HouseholdMember.ParticipantId,
                EatRecords = e.EatRecords.Select(x => (EEatRecord)x).ToList(),
                TimeStart = e.TimeStart,
                TimeEnd = e.TimeEnd,
                Finalized = e.Finalized,
            };
        }
    }
}