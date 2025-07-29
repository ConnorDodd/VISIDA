using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.InternalObjects
{
    public class UsageLogFile
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }

        //[Key, ForeignKey("Household")]
        //public int Household_Id { get; set; }

        //public Household Household { get; set; }

        [Column(TypeName = "TEXT")]
        public string RawData { get; set; }

        public DateTime LogCreationTime { get; set; }
    }
}