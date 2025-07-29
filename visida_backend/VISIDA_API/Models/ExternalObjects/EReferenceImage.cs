using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EReferenceImage
    {
        public int Id { get; set; }
        public string OriginId { get; set; }
        public string Description { get; set; }
        public string ImageUrl { get; set; }
        public double SizeGrams { get; set; }
    }
}