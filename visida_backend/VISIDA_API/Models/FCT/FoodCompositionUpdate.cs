using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.User;

namespace VISIDA_API.Models.FCT
{
    public class FoodCompositionUpdate
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }

        public DateTime Date { get; set; }
        [JsonIgnore]
        public LoginUser UpdatedBy { get; set; }
        [MaxLength(64)]
        public string UpdatedAs { get; set; }
        [MaxLength(1000), Required]
        public string CommitMessage { get; set; }

        [Required]
        public bool Overwrite { get; set; }

        public int TableId { get; set; }

        public List<FoodComposition> FoodCompositions { get; set; }
    }
}