using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.User;

namespace VISIDA_API.Models.InternalObjects
{
    public class ResetPasswordRequest
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }

        public virtual LoginUser LoginUser { get; set; }
        [Column(TypeName = "VARCHAR"), StringLength(32), Index(IsUnique = true)]
        public string Key { get; set; }
        public bool Used { get; set; }
        public DateTime CreatedTime { get; set; }
    }
}