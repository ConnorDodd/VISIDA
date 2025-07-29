using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.ExternalObjects;

namespace VISIDA_API.Models.Comparators
{
    public class CommentComparator : IEqualityComparer<EComment>
    {
        public bool Equals(EComment x, EComment y)
        {
            return x.Id == y.Id;
        }

        public int GetHashCode(EComment obj)
        {
            return 0;
        }
    }
}