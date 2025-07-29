using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.FCT
{
    public class ReferenceImage
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [ForeignKey("Type")]
        public int FoodTypeId { get; set; }
        [Column(TypeName = "VARCHAR"), StringLength(256)]
        public string OriginId { get; set; }
        public ReferenceImageType Type { get; set; }
        [Column(TypeName = "VARCHAR"), StringLength(256)]
        public string Description { get; set; }
        [Column(TypeName = "VARCHAR"), StringLength(256), Required]
        public string ImageUrl { get; set; }
        public double SizeGrams { get; set; }
    }
}