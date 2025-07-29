using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.User;

namespace VISIDA_API.Models.InternalObjects
{
    public class ConversionHistory
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }

        [ForeignKey("Household")]
        public int? Household_Id { get; set; }
        public virtual Household Household { get; set; }

        [ForeignKey("ImageRecord")]
        public int? ImageRecord_Id { get; set; }
        public virtual ImageRecord ImageRecord { get; set; }

        [ForeignKey("Recipe")]
        public int? Recipe_Id { get; set; }
        public virtual CookRecipe Recipe { get; set; }

        public virtual LoginUser CreatedBy { get; set; }
        public DateTime CreatedTime { get; set; } = DateTime.Now;

        public enum ConversionTypes { IRCreate, CRCreate, CICreate, CRAdd, ChangeImage, IRFromCI, CIFromIR, CRFromIR, IRFromCR, CIFromCR, ERLFromIR }
        public ConversionTypes ConversionType { get; set; }


    }
}