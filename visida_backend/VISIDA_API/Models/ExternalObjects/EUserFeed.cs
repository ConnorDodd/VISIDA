using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EUserFeed
    {
        public List<EComment> Comments { get; set; } = new List<EComment>();
        public List<ELoginUser> Users { get; set; }
        public List<AdminMessage> Messages { get; set; }
    }
}