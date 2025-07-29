using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.User;

namespace VISIDA_API.Models.InternalObjects
{
    public class WorkAssignation
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }

        public virtual LoginUser LoginUser { get; set; }
        public virtual Study Study { get; set; }

        public enum AccessLevels { Identify, Quantify, Both, View, Coordinator }
        public AccessLevels AccessLevel { get; set; }
    }
}