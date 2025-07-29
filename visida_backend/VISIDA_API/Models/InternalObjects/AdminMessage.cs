using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.InternalObjects
{
    public class AdminMessage
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        public string Message { get; set; }
        public enum ContentType { Test, Flag }
        public ContentType Type { get; set; } = ContentType.Test;
        public DateTime CreatedTime { get; set; } = DateTime.Now;
        public int RefId { get; set; }
    }
}