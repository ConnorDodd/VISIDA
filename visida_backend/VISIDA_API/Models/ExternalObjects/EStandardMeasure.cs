using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.FCT;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EStandardMeasure
    {
        public int Id { get; set; }
        public string Name { get; set; }
        public double MLs { get; set; }

        public static implicit operator EStandardMeasure(StandardMeasure s)
        {
            return new EStandardMeasure()
            {
                Id = s.Id,
                Name = s.Name,
                MLs = s.MLs
            };
        }
    }

}