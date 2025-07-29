using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.ExternalObjects;

namespace VISIDA_API.Models.FCT
{
    public class RDAModel
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [Required, Column(TypeName = "VARCHAR"), StringLength(256), Index(IsUnique = true)]
        public string Name { get; set; }
        public string Description { get; set; }

        public string FieldData { get; set; }

        public virtual ICollection<RDA> RDAs { get; set; }
    }
}