using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EEatImageText
    {
        public int Id { get; set; }
        public string Description { get; set; }

        public static implicit operator EEatImageText(EatImageText t)
        {
            return new EEatImageText()
            {
                Id = t.Id,
                Description = t.Description
            };
        }
    }
}