using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.InternalObjects
{
    public class EatOccasion
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        public int OriginId { get; set; }

        [Required, ForeignKey("HouseholdMember")]
        public int HouseholdMemberId { get; set; }
        public virtual HouseholdMember HouseholdMember { get; set; }
        public virtual ICollection<EatRecord> EatRecords { get; set; }

        [Required]
        public DateTime TimeStart { get; set; }
        public DateTime TimeEnd { get; set; }

        public bool Finalized { get; set; }
        public bool IsBreastfeedOccasion { get; set; }

        [DefaultValue(true)]
        public bool Original { get; set; } = true;
        public AnnotatedStatuses AnnotatedStatus { get; set; }
    }
}