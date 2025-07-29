using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.FCT;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EFoodItem
    {
        public int Id { get; set; }
        public string Name { get; set; }
        public int? ImageRecordId { get; set; }

        public double QuantityGrams { get; set; }
        public double MeasureCount { get; set; }
        public string MeasureType { get; set; }

        public int? FoodCompositionId { get; set; }
        public EFoodComposition FoodCompositionDatabaseEntry { get; set; }

        public double? TagXPercent { get; set; }
        public double? TagYPercent { get; set; }

        public int Priority { get; set; }

        public string CreatedBy { get; set; }
        public bool CreatedByAdmin { get; set; }
        public bool GestaltLock { get; set; }
        public int GestaltCount { get; set; }
        public double? GestaltMinEstimate { get; set; }
        public double? GestaltMaxEstimate { get; set; }

        public RetentionFactor RetentionFactor { get; set; }

        public static implicit operator EFoodItem(FoodItem f)
        {
            return new EFoodItem()
            {
                Id = f.Id,
                Name = f.FoodComposition?.Name ?? "Not Selected",
                ImageRecordId = f.ImageRecordId,
                QuantityGrams = f.QuantityGrams,
                MeasureType = f.MeasureType,
                MeasureCount = f.MeasureCount,
                FoodCompositionDatabaseEntry = f.FoodComposition,
                FoodCompositionId = f.FoodCompositionId,
                TagXPercent = f.TagXPercent,
                TagYPercent = f.TagYPercent,
                Priority = f.Priority
                //CreatedBy = f.CreatedBy.UserName
            };
        }
    }
}