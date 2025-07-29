using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.StringUtils
{
    public class MatchGroup
    {
        public string Key { get; set; }
        public List<string> Keys { get; set; } = new List<string>();
        public List<MatchNode> Nodes { get; set; } = new List<MatchNode>();

        public int Index { get; set; }
        public string Measure { get; set; }
        public double Quantity { get; set; }
        public double QuantityMod { get; set; }

        public class MatchNode
        {
            public string Value { get; set; }
            public int Distance { get; set; }
            public int Id { get; set; }

            public override string ToString()
            {
                return String.Format("{0}: {1}", Distance, Value);
            }
        }

        public class MatchNodeComparer : IEqualityComparer<MatchNode>
        {
            public bool Equals(MatchNode x, MatchNode y)
            {
                return x.Value.Equals(y.Value);
            }

            public int GetHashCode(MatchNode obj)
            {
                return obj.Value.GetHashCode();
            }
        }

        public class MatchNodeDistanceComparer : IComparer<MatchNode>
        {
            public int Compare(MatchNode x, MatchNode y)
            {
                if (x.Distance > y.Distance)
                    return 1;
                if (x.Distance < y.Distance)
                    return -1;
                return 0;
            }
        }
    }
}