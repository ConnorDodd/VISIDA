using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.ExternalObjects
{
    public class ESuggestion
    {
        public string Name { get; set; }

        public enum SuggestionSources { Leftover, Recipe }
        [JsonConverter(typeof(StringEnumConverter))]
        public SuggestionSources Source { get; set; }

        public int ImageRecordId { get; set; }

        public string ImageUrl { get; set; }
        public int? FoodCompositionId { get; set; }
    }
}