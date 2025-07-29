using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;
using static VISIDA_API.Models.InternalObjects.Timing;

namespace VISIDA_API.Models.ExternalObjects
{
    public class ETiming
    {
        public TimingType Type { get; set; }
        public int Id { get; set; }
        public int StudyId { get; set; }
        public int CreatedById { get; set; }
        public int TimeTaken { get; set; }
        public DateTime CreatedTime { get; set; }
        public int FoodItemId { get; set; }

        public static implicit operator ETiming(Timing t)
        {
            return new ETiming()
            {
                Id = t.Id,
                StudyId = t.Study?.Id ?? 0,
                Type = t.Type,
                CreatedById = t.CreatedBy?.Id ?? 0,
                TimeTaken = t.TimeTaken,
                CreatedTime = t.CreatedTime,
                FoodItemId = t.FoodItem?.Id ?? 0
            };
        }
    }
}