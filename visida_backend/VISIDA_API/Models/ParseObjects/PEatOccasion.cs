using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.ParseObjects
{
    public class PEatOccasion
    {
        public int EatingOccasionId { get; set; }
        public DateTime StartTime { get; set; }
        public DateTime EndTime { get; set; }

        public bool IsBreastfeedOccasion { get; set; }
        public bool Finalized { get; set; }

        public List<PEatRecord> FoodItems { get; set; }
        public int[] RecipeIds { get; set; } = new int[0];

        public AnnotatedStatuses AnnotatedStatus { get; set; }

        //public static List<EatRecord> Leftovers { get; set; }

        public EatOccasion ToInternal(Household household, PHousehold pHousehold)
        {
            //Leftovers = new List<EatRecord>();
            EatOccasion e = new EatOccasion()
            {
                OriginId = this.EatingOccasionId,
                TimeStart = this.StartTime,
                TimeEnd = this.EndTime,
                Finalized = this.Finalized,
                IsBreastfeedOccasion = this.IsBreastfeedOccasion,
                AnnotatedStatus = this.AnnotatedStatus,
                EatRecords = new List<EatRecord>()
                //EatRecords = this.FoodItems.Select(x => x.ToInternal(household, pHousehold, e)).ToList()
            };
            foreach (var fi in this.FoodItems)
                e.EatRecords.Add(fi.ToInternal(household, pHousehold, this));

            return e;
        }

    }
}