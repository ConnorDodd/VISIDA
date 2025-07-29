using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EHouseholdGuestInfo
    {
        public int Id { get; set; }

        //The word 'guests' here really representes total participants including recorded household and extra unknowns
        public int AdultFemaleGuests { get; set; }
        public int AdultMaleGuests { get; set; }
        public int ChildGuests { get; set; }

        public int AdultFemaleGuestsOrig { get; set; }
        public int AdultMaleGuestsOrig { get; set; }
        public int ChildGuestsOrig { get; set; }

        public int TotalHeads { get { return AdultFemaleGuests + AdultMaleGuests + ChildGuests; } }

        public static implicit operator EHouseholdGuestInfo(HouseholdGuestInfo h)
        {
            if (h == null)
                return null;
            return new EHouseholdGuestInfo
            {
                Id = h.Id,
                AdultFemaleGuests = h.ModAdultFemaleGuests.HasValue ? h.ModAdultFemaleGuests.Value : h.AdultFemaleGuests,
                AdultMaleGuests = h.ModAdultMaleGuests.HasValue ? h.ModAdultMaleGuests.Value : h.AdultMaleGuests,
                ChildGuests = h.ModChildGuests.HasValue ? h.ModChildGuests.Value : h.ChildGuests,
                AdultFemaleGuestsOrig = h.AdultFemaleGuests,
                AdultMaleGuestsOrig = h.AdultMaleGuests,
                ChildGuestsOrig = h.ChildGuests,
            };
        }
    }
}