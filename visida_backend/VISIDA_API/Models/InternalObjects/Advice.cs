using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.FCT;

namespace VISIDA_API.Models.InternalObjects
{
    public class Advice
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }

        [ForeignKey("Table")]
        public int TableId { get; set; }
        public FoodCompositionTable Table { get; set; }

        [Column(TypeName = "VARCHAR"), StringLength(512)]
        public string Description { get; set; }
        public string IssueDescription { get; set; }
        public string SolutionDescription { get; set; }
        [JsonIgnore]
        public string SuggestionsInternal { get; set; }

        [NotMapped]
        public string[] Suggestions
        {
            get
            {
                if (SuggestionsInternal == null)
                    return new string[0];
                return SuggestionsInternal.Split('|');
            }
            set
            {
                if (value == null)
                    SuggestionsInternal = null;
                else
                    SuggestionsInternal = string.Join("|", value);
            }
        }
    }
}