using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;
using static VISIDA_API.Models.InternalObjects.ConversionHistory;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EConversionHistory
    {
        public int Id { get; set; }
        public int? HouseholdId { get; set; }
        public int? RecordId { get; set; }
        public int? RecipeId { get; set; }
        public DateTime CreatedTime { get; set; }
        public string CreatedBy { get; set; }

        [JsonConverter(typeof(StringEnumConverter))]
        public ConversionTypes ConversionType { get; set; }

        public static implicit operator EConversionHistory(ConversionHistory history)
        {
            return new EConversionHistory()
            {
                Id = history.Id,
                HouseholdId = history.Household_Id,
                RecordId = history.ImageRecord_Id,
                RecipeId = history.Recipe_Id,
                CreatedTime = history.CreatedTime,
                ConversionType = history.ConversionType,
                CreatedBy = history.CreatedBy.UserName
            };
        }

    }
}