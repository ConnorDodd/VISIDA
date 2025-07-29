using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.InternalObjects
{
    public class HouseholdMember
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [Required, Column(TypeName = "VARCHAR"), StringLength(36)]//, Index(IsUnique = true)]
        public string ParticipantId { get; set; }

        [Required, ForeignKey("Household")]
        public int HouseholdId { get; set; }
        public virtual Household Household { get; set; }

        public virtual ICollection<EatOccasion> EatingOccasions { get; set; }

        [Required]
        public float Age { get; set; }

        public bool IsMother { get; set; } = false;
        public bool IsBreastfed { get; set; } = false;
        public bool IsFemale { get; set; } = false;
        public string LifeStage { get; set; } = "NONE";

        public string PregnancyTrimester { get; set; } = "NA";
        public float Weight { get; set; } //Kgs
        public float Height { get; set; } //Cms
    }
}