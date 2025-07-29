using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;
using VISIDA_API.Models.FCT;
using VISIDA_API.Models.User;

namespace VISIDA_API.Models.InternalObjects
{
    public class Study
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }

        [Required, Column(TypeName = "VARCHAR"), StringLength(256), Index(IsUnique = true)]
        public string Name { get; set; }

        public virtual ICollection<WorkAssignation> Assignees { get; set; }
        public virtual ICollection<Household> Households { get; set; }
        public virtual RDAModel RDAModel { get; set; }

        [ForeignKey("FoodCompositionTable")]
        public int? FoodCompositionTable_Id { get; set; }
        public virtual FoodCompositionTable FoodCompositionTable { get; set; }

        public string CountryCode { get; set; }
        public bool Transcribe { get; set; }
        public bool Translate { get; set; }
        public bool Gestalt { get; set; }
        public int GestaltMax { get; set; }

        public DateTime? DeletedTime { get; set; }

        public virtual ICollection<Timing> Timings { get; set; }

        //public int? DeletedBy_Id { get; set; }
    }
}