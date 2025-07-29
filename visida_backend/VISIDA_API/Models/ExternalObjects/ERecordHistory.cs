using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;
using static VISIDA_API.Controllers.PortionController;
using static VISIDA_API.Models.InternalObjects.RecordHistory;

namespace VISIDA_API.Models.ExternalObjects
{
    public class ERecordHistory
    {
        public int Id { get; set; }
        public int UserId { get; set; }
        public string UserName { get; set; }
        public DateTime Time { get; set; } = DateTime.Now;

        [JsonConverter(typeof(StringEnumConverter))]
        public UpdateTypes Action { get; set; }
        public int? FoodItemId { get; set; }

        [JsonConverter(typeof(StringEnumConverter))]
        public RejectReasons RejectReason { get; set; }

        public int? FoodCompositionId { get; set; }

        public double? QuantityGrams { get; set; }
        public double? ToolMeasure { get; set; }
        public string ToolSource { get; set; }

        public static implicit operator ERecordHistory(RecordHistory h)
        {
            return new ERecordHistory()
            {
                Id = h.Id,
                UserId = h.User.Id,
                UserName = h.User.UserName,
                Time = h.Time,
                Action = h.Action,
                FoodItemId = h.FoodItemId,
                RejectReason = h.RejectReason,
                FoodCompositionId = h.FoodCompositionId,
                QuantityGrams = h.QuantityGrams,
                ToolMeasure = h.ToolMeasure,
                ToolSource = h.ToolSource
            };
        }
    }
}