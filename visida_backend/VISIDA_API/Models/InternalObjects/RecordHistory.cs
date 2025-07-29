using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.User;
using static VISIDA_API.Controllers.PortionController;

namespace VISIDA_API.Models.InternalObjects
{
    public class RecordHistory
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        public virtual LoginUser User { get; set; }
        public DateTime Time { get; set; } = DateTime.Now;

        public enum UpdateTypes { Identify, Quantify, Delete, Reject }
        public UpdateTypes Action { get; set; }

        [ForeignKey("FoodItem")]
        public int? FoodItemId { get; set; }
        public virtual FoodItem FoodItem { get; set; }

        public RejectReasons RejectReason { get; set; }

        public int? FoodCompositionId { get; set; }

        public double? QuantityGrams { get; set; }
        public double? ToolMeasure { get; set; }
        public string ToolSource { get; set; }
    }
}