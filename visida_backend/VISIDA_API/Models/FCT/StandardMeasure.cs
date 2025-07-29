using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.FCT
{
    public class StandardMeasure
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        public virtual FoodCompositionTable Table { get; set; }
        [Column(TypeName = "VARCHAR"), StringLength(256), Required]
        public string Name { get; set; }
        public double MLs { get; set; }
    }
}