using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.ParseObjects;
using VISIDA_API.Models.User;

namespace VISIDA_API.Models.InternalObjects
{
    public class CookIngredient
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        public int OriginId { get; set; }
        public virtual CookRecipe Recipe { get; set; }
        public virtual ImageRecord ImageRecord { get; set; }
        public enum CookMethods { None, Water, Grill, Oven, Fry }
        public CookMethods CookMethod { get; set; }
        [Column(TypeName = "VARCHAR"), StringLength(48)]
        public string CookDescription { get; set; }
        public AnnotatedStatuses AnnotatedStatus { get; set; }
    }
}