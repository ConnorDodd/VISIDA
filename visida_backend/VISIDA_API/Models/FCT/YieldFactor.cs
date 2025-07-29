using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.FCT
{
    public class YieldFactor
    {
        public int Id { get; set; }
        public int Matches { get; set; }
        public string Name { get; set; }
        //public double? YieldWater { get; set; }
        //public double? YieldStoveTop { get; set; }
        //public double? YieldOven { get; set; }

        public double? Factor { get; set; }
        public double? Density { get; set; }
        public InternalObjects.CookIngredient.CookMethods CookMethod { get; set; }
    }
}