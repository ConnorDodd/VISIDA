using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.ParseObjects
{
    public class PHouseholdGuestInfo
    {
        [Required]
        public int GeustInfoId { get; set; } 

        public int AdultFemaleGuests { get; set; }
        public int AdultMaleGuests { get; set; }
        public int ChildGuests { get; set; }
    }
}