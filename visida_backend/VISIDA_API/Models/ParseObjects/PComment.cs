using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.ParseObjects
{
    public class PComment
    {
        public int CommentId { get; set; }
        public string Text { get; set; }
        public DateTime CaptureTime { get; set; }
        public string CreatedBy { get; set; }
        public bool HighPriority { get; set; }
    }
}