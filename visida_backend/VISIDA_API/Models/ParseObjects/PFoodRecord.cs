using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.ParseObjects
{
    public class PFoodRecord
    {
        //public DateTime Date { get; set; }
        public ICollection<PEatOccasion> EatingOccasions { get; set; }
    }
}