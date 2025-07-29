using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.ExternalObjects
{
    public class ECookIngredient
    {
        public int Id { get; set; }
        public EImageRecord ImageRecord { get; set; }
        public CookIngredient.CookMethods CookMethod{ get; set; }
        public string CookDescription { get; set; }

        public static implicit operator ECookIngredient(CookIngredient ci)
        {
            return new ECookIngredient
            {
                Id = ci.Id,
                ImageRecord = ci.ImageRecord,
                CookMethod = ci.CookMethod,
                CookDescription = ci.CookDescription
            };
        }
    }
}