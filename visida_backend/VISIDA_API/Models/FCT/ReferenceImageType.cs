using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.FCT
{
    public class ReferenceImageType
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [Column(TypeName = "NVARCHAR"), StringLength(256), Required]
        public string Name { get; set; }
        public virtual FoodCompositionTable Table { get; set; }
        public virtual ICollection<ReferenceImage> Images { get; set; }
    }
}