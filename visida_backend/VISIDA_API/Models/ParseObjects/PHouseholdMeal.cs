using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

//Doesn't actually get used, gets parsed directly to internal model with unmapped attributes

namespace VISIDA_API.Models.ParseObjects
{
    public class PHouseholdMeal
    {
        public int MealId { get; set; }

        //public int AdultFemaleGuests { get; set; }
        //public int AdultMaleGuests { get; set; }
        //public int ChildGuests { get; set; }

        public bool Finalized { get; set; }
        public bool GuestInfoCaptured { get; set; }
        public DateTime StartTime { get; set; }

        public int[] RecipeIds { get; set; }
    }
}