using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.FCT
{
    public class FoodCompositionTable
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }

        [Column(TypeName = "VARCHAR"), StringLength(256), Index(IsUnique = true)]
        public string Name { get; set; }

        public bool Deleted { get; set; }

        public bool IsPublic { get; set; }

        public virtual ICollection<FoodComposition> FoodCompositions { get; set; }
        public virtual ICollection<StandardMeasure> StandardMeasures { get; set; }
        public virtual ICollection<ReferenceImageType> ReferenceImages { get; set; }
    }
}