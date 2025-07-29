using Microsoft.AspNet.Identity;
using Microsoft.AspNet.Identity.EntityFramework;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;
using VISIDA_API.Models.InternalObjects.Testing;

namespace VISIDA_API.Models.User
{
    public class LoginUser : IUser<int>
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        public bool IsActive { get; set; } = true;

        [Required, MaxLength(256), Index(IsUnique = true)]
        public string UserName { get; set; }
        [MaxLength(256)]//, Index(IsUnique = true)]
        public string Email { get; set; }

        [MaxLength(32), JsonIgnore]
        public string Salt { get; set; }

        public virtual LoginUserRole Role { get; set; }

        [Required, DataType(DataType.Password)]
        [StringLength(100, ErrorMessage = "The {0} must be at least {2} characters long.", MinimumLength = 5)]
        public string Password { get; set; }

        //[JsonIgnore]
        //public virtual ICollection<Household> AssignedHouseholds { get; set; }

        public virtual ICollection<WorkAssignation> Assignments { get; set; }
        public virtual ICollection<HouseholdAssignation> HouseholdAssignments { get; set; }
        public virtual ICollection<Comment> Comments { get; set; }
        public virtual ICollection<ReliabilityTest> Tests { get; set; }

        public DateTime? LastSeen { get; set; }
        public DateTime? LastLogin { get; set; }
        public DateTime? LastFeedRefresh { get; set; }

        [NotMapped]
        public bool IsAdmin { get { return Role.Id == 1 || Role.Id == 4; } }
    }
}