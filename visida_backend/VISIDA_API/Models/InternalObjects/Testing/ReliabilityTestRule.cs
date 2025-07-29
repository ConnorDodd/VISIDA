using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.User;

namespace VISIDA_API.Models.InternalObjects.Testing
{
    public class ReliabilityTestRule
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }

        [JsonIgnore]
        public virtual ICollection<ReliabilityTest> Tests { get; set; }

        [ForeignKey("Study")]
        public int Study_Id { get; set; }
        [JsonIgnore]
        public Study Study { get; set; }

        [ForeignKey("User")]
        public int User_Id { get; set; }
        [JsonIgnore]
        public LoginUser User { get; set; }

        public DateTime StartDate { get; set; }
        public DateTime? RepeatDate { get; set; }

        public ReliabilityTest.TestTypes TestType { get; set; }

        [NotMapped]
        public int RepeatInterval
        {
            get
            {
                if (!RepeatDate.HasValue)
                    return -1;
                return (int)(RepeatDate.Value - StartDate).TotalDays;
            }
        }
    }
}