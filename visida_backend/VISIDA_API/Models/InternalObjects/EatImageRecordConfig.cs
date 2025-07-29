using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.InternalObjects
{
    public class EatImageRecordConfig
    {
        public List<string> Countries { get; set; }
        public List<HouseholdConfig> Households { get; set; }
        public List<StudyConfig> Studies { get; set; }
    }

    public class StudyConfig
    {
        public int Id { get; set; }
        public string Name { get; set; }
        public List<HouseholdConfig> Households { get; set; }
    }

    public class HouseholdConfig
    {
        public int Id { get; set; }
        public string Name { get; set; }
        public DateTime[] Days { get; set; }
        public IEnumerable<string> HouseholdMembers { get; set; }
    }

    public class HouseholdConfigComparer : IEqualityComparer<HouseholdConfig>
    {
        public bool Equals(HouseholdConfig x, HouseholdConfig y)
        {
            return x.Name?.Equals(y.Name) ?? false;
            //return x.Id == y.Id;
        }

        public int GetHashCode(HouseholdConfig obj)
        {
            return obj.GetHashCode();
        }
    }
}