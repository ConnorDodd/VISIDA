using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.User
{
    public class LoginUserRole
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }

        [Required, MaxLength(64), Index(IsUnique = true)]
        public string Role { get; set; }

        public ICollection<LoginUser> Users { get; set; }
    }
}