using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;
using static VISIDA_API.Models.InternalObjects.WorkAssignation;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EWorkAssignation
    {
        public int Id { get; set; }
        public int UserId { get; set; }
        public string UserName { get; set; }
        [JsonConverter(typeof(StringEnumConverter))]
        public AccessLevels AccessLevel { get; set; }

        public static implicit operator EWorkAssignation(WorkAssignation w)
        {
            if (w == null)
                return null;
            return new EWorkAssignation()
            {
                Id = w.Id,
                UserId = w.LoginUser.Id,
                AccessLevel = w.AccessLevel,
                UserName = w.LoginUser?.UserName
            };
        }
    }
}