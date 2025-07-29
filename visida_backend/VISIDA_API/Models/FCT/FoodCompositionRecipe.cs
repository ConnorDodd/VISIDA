 using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;
using VISIDA_API.Models.User;

namespace VISIDA_API.Models.FCT
{
    public class FoodCompositionRecipe
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        public virtual ICollection<FoodItem> Ingredients { get; set; }
        public virtual FoodComposition CompositionResult { get; set; }
        public string Name { get; set; }
        public DateTime CreatedDate { get; set; }
        public LoginUser CreatedBy { get; set; }
    }
}