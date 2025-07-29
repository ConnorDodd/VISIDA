using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;
using VISIDA_API.Models.FCT;

namespace VISIDA_API.Models.InternalObjects
{
    public class FoodItem
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        //[Required, MaxLength(256)]
        [NotMapped]
        public string Name { get; set; }

        public double QuantityGrams { get; set; }
        public double MeasureCount { get; set; }
        public string MeasureType { get; set; }
        public double ToolMeasure { get; set; }
        public string ToolSource { get; set; }

        [ForeignKey("ImageRecord")]
        public int? ImageRecordId { get; set; }
        public virtual ImageRecord ImageRecord { get; set; }

        [ForeignKey("FoodComposition")]
        public int? FoodCompositionId { get; set; }
        public virtual FoodComposition FoodComposition { get; set; }

        public double? TagXPercent { get; set; }
        public double? TagYPercent { get; set; }

        public int Priority { get; set; } = 100;
        public virtual List<RecordHistory> Updates { get; set; }


        [JsonIgnore]
        public virtual User.LoginUser CreatedBy { get; set; }
        [NotMapped]
        public DateTime CreateStart { get; set; }
        [NotMapped]
        public DateTime CreateEnd { get; set; }
    }
}