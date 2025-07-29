using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects.Testing;
using VISIDA_API.Models.User;

namespace VISIDA_API.Models.ExternalObjects
{
    public class ELoginUser
    {
        public int Id { get; set; }
        public bool IsActive { get; set; }
        public string UserName { get; set; }
        public string Role { get; set; }
        public DateTime? LastLogin { get; set; }

        public List<EStudy> Studies { get; set; }
        public List<ETiming> Timings { get; set; }
        public List<EReliabilityTest> Tests { get; set; }
        public List<ReliabilityTestRule> TestRules { get; set; }

        public static implicit operator ELoginUser(LoginUser lu)
        {
            return new ELoginUser()
            {
                Id = lu.Id,
                IsActive = lu.IsActive,
                UserName = lu.UserName,
                Role = lu.Role.Role,
                LastLogin = lu.LastLogin
            };
        }
    }
}