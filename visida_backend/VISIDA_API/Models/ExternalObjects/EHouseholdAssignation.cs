using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;
using static VISIDA_API.Models.InternalObjects.HouseholdAssignation;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EHouseholdAssignation
    {
        public int Id { get; set; }
        public int UserId { get; set; }
        public string UserName { get; set; }

        public static implicit operator EHouseholdAssignation(HouseholdAssignation h)
        {
            if (h == null)
                return null;
            return new EHouseholdAssignation()
            {
                Id = h.Id,
                UserId = h.LoginUser.Id,
                UserName = h.LoginUser?.UserName
            };
        }
    }
}