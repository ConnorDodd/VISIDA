using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EHouseholdMeal
    {
        public int Id { get; set; }

        //public int AdultFemaleGuests { get; set; }
        //public int AdultMaleGuests { get; set; }
        //public int ChildGuests { get; set; }

        //public int? AdultFemaleGuestsOrig { get; set; }
        //public int? AdultMaleGuestsOrig { get; set; }
        //public int? ChildGuestsOrig { get; set; }

        public bool Finalized { get; set; }
        public bool GuestInfoCaptured { get; set; }
        public DateTime StartTime { get; set; }

        //public int TotalHeads { get { return AdultFemaleGuests + AdultMaleGuests + ChildGuests; } }

        public static implicit operator EHouseholdMeal(HouseholdMeal m)
        {
            if (m == null)
                return null;
            return new EHouseholdMeal()
            {
                Id = m.Id,
                //AdultFemaleGuests = m.ModAdultFemaleGuests.HasValue ? m.ModAdultFemaleGuests.Value : m.AdultFemaleGuests,
                //AdultMaleGuests = m.ModAdultMaleGuests.HasValue ? m.ModAdultMaleGuests.Value : m.AdultMaleGuests,
                //ChildGuests = m.ModChildGuests.HasValue ? m.ModChildGuests.Value : m.ChildGuests,
                //AdultFemaleGuestsOrig = m.AdultFemaleGuests,
                //AdultMaleGuestsOrig = m.AdultMaleGuests,
                //ChildGuestsOrig = m.ChildGuests,
                Finalized = m.Finalized,
                GuestInfoCaptured = m.GuestInfoCaptured,
                StartTime = m.StartTime
            };
        }
    }
}