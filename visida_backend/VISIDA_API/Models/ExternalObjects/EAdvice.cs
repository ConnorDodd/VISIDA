using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EAdvice
    {
        public int Id { get; set; }
        public string Description { get; set; }
        public string IssueDescription { get; set; }
        public string SolutionDescription { get; set; }
    }
}