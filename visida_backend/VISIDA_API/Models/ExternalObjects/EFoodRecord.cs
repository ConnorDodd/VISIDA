using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EFoodRecord
    {
        public DateTime Date { get; set; }
        public List<EEatOccasion> EatOccasions { get; set; }
    }
}