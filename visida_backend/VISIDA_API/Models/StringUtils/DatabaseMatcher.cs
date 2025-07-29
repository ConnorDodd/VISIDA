using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;
using VISIDA_API.Models.ExternalObjects;
using static VISIDA_API.Models.StringUtils.MatchGroup;

namespace VISIDA_API.Models.StringUtils
{
    public class DatabaseMatcher
    {
        private static string[] excludeList = { "made", "with", "to", "of", "and", "or", "in", "a", "no", "not", "from", "on", "all", "only", "at", "as", "some", "an", "any" };
        //private static string[] measureList = { "cup", "cups", "kilograms", "grams", "splash", "splashes", "kilogram", "dash" };
        private static string[] numerics = { "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty", "thirty", "forty", "fourty", "fifty", "sixty", "seventy", "eighty", "ninety", "hundred", "thousand", "million", "billion", "half", "quarter", "1/2", "1/4" };
        private static double[] numericd = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18, 18, 19, 20, 30, 40, 40, 50, 60, 70, 80, 90, 100, 1000, 1000000, 1000000000, 0.5, 0.25, 0.5, 0.25 };

        private enum QuantityTypes { Weight, Volume };
        private static Tuple<string, double, QuantityTypes>[] measures = new Tuple<string, double, QuantityTypes>[]
        {
            Tuple.Create("gram", 1d, QuantityTypes.Weight),
            Tuple.Create("kilogram", 1000d, QuantityTypes.Weight),
            Tuple.Create("cup", 250d, QuantityTypes.Volume),
            Tuple.Create("splash", 30d, QuantityTypes.Volume),
            Tuple.Create("dash", 30d, QuantityTypes.Volume),
            Tuple.Create("tablespoon", 20d, QuantityTypes.Volume),
            Tuple.Create("tbps", 20d, QuantityTypes.Volume),
            Tuple.Create("tbs", 20d, QuantityTypes.Volume),
            Tuple.Create("teaspoon", 5d, QuantityTypes.Volume),
            Tuple.Create("tsp", 5d, QuantityTypes.Volume),
        };

        public static List<MatchGroup> FindMatches(string input, EFoodComposition[] data)
        {
            var cleanString = Regex.Replace(input, "[\n \\/]", " ");
            cleanString = Regex.Replace(cleanString, "[^a-zA-Z ]", "");
            cleanString = cleanString.ToLower();

            var words = cleanString.Split(' ');
            words = words.Where(x => x.Length > 0).ToArray();
            List<MatchGroup> groups = new List<MatchGroup>();
            MatchGroup current = null;
            for (int i = words.Length - 1; i >= 0; i--)
            {
                var word = words[i].ToLower();

                if (excludeList.Contains(word.ToLower()))
                {
                    if (current != null)
                        current.Key = word + " " + current.Key;
                    else if (groups.Count > 0)
                        groups[0].Key = word + " " + groups[0].Key;
                    else
                    {
                        var group = new MatchGroup() { Key = word, Keys = new List<string>() { word }, Index = i };
                        groups.Insert(0, group);
                    }
                    continue;
                }

                var measure = measures.FirstOrDefault(x => Regex.Match(word, "^" + x.Item1 + "s?$").Success);
                if (measure != null)
                {
                    if (current != null)
                    {
                        current.Measure = measure.Item3 == QuantityTypes.Weight ? "g" : "mL";
                        current.QuantityMod = measure.Item2;
                        current.Key = word += " " + current.Key;
                    }
                    //else if (groups.Count > 0)
                    //    groups[0].Key = word + " " + groups[0].Key;
                    else
                    {
                        var group = new MatchGroup() { Key = word, Keys = new List<string>() { word }, Index = i };
                        group.Measure = measure.Item3 == QuantityTypes.Weight ? "g" : "mL";
                        group.QuantityMod = measure.Item2;
                        groups.Insert(0, group);
                        current = group;
                    }
                    continue;
                }

                var numericIndex = Array.IndexOf(numerics, word);
                if (numericIndex >= 0)
                {
                    if (current != null)
                    {
                        current.Key = word += " " + current.Key;
                        current.Quantity += numericd[numericIndex];
                    }
                    //else if (groups.Count > 0)
                    //    groups[0].Key = word + " " + groups[0].Key;
                    else
                    {
                        var group = new MatchGroup() { Key = word, Keys = new List<string>() { word }, Index = i };
                        group.Quantity = numericd[numericIndex];
                        groups.Insert(0, group);
                        current = group;
                    }
                    continue;
                }

                double quantity;
                if (double.TryParse(word, out quantity))
                {
                    if (current != null)
                    {
                        current.Key = word += " " + current.Key;
                        current.Quantity += quantity;
                    }
                    else
                    {
                        var group = new MatchGroup() { Key = word, Keys = new List<string>() { word }, Index = i };
                        group.Quantity = quantity;
                        groups.Insert(0, group);
                        current = group;
                    }
                    continue;
                }

                if (current != null)
                {
                    var intersect = new List<MatchNode>();
                    foreach (var dr in current.Nodes)
                    {
                        var dm = Regex.Match(dr.Value, "\\b" + word, RegexOptions.IgnoreCase);
                        if (dm.Success)
                        {
                            intersect.Add(new MatchNode()
                            {
                                Distance = Math.Min(dm.Index, dr.Distance),
                                Value = dr.Value,
                                Id = dr.Id
                            });
                        }
                    }
                    if (intersect.Count > 0)
                    {
                        current.Nodes = intersect;
                        current.Keys.Insert(0, word);
                        current.Key = word + " " + current.Key;
                    }
                    else
                    {
                        current = null;
                        i++;
                    }
                }
                else
                {
                    var group = new MatchGroup() { Key = word, Keys = new List<string>() { word }, Index = i };
                    foreach (var dr in data)
                    {
                        var dm = Regex.Match(dr.Name, "\\b" + word, RegexOptions.IgnoreCase);
                        if (dm.Success)
                            group.Nodes.Add(new MatchNode()
                            {
                                Distance = dm.Index,
                                Value = dr.Name,
                                Id = dr.Id
                            });
                    }
                    if (group.Nodes.Count > 0)
                    {
                        current = group;
                        groups.Insert(0, group);
                    }
                    else if (groups.Count > 0)
                    {
                        groups[0].Key = word + " " + groups[0].Key;
                    }
                    else
                    {
                        current = group;
                        groups.Insert(0, group);
                    }
                }
                EndBigLoop:;
            }

            foreach (var group in groups)
            {
                group.Nodes = group.Nodes.OrderBy(x => x.Distance).ToList();
                group.Quantity *= group.QuantityMod;
            }

            return groups;
        }

        public static List<MatchGroup> FindMatchesOld(string input, EFoodComposition[] data)
        {
            List<MatchGroup> matches = new List<MatchGroup>();
            //List<string> match = new List<string>(input.ToLower().Split(' '));
            var cleanString = Regex.Replace(input, "[\n \\/]", " ");
            cleanString = Regex.Replace(cleanString, "[\t\r]", "");
            //cleanString = Regex.Replace(cleanString, "[^a-zA-Z0-9 ]", "");
            cleanString = cleanString.ToLower();
            List<string> match = Regex.Split(cleanString, "[ ,]").ToList();
            match.RemoveAll(x => x.Length == 0);
            foreach (string s in excludeList)   
                match.RemoveAll(x => x.Equals(s));
            match.RemoveAll(x => Regex.IsMatch(x, @"\d"));

            foreach (string word in match)
            {
                var group = new MatchGroup()
                {
                    Key = word
                };
                string regWord = Regex.Replace(word, "[^0-9a-zA-Z]+", "");
                var pluralTest = new Regex("(y|es|ies|s)\\b").Match(regWord);
                if (pluralTest.Success)
                    regWord = regWord.Substring(0, pluralTest.Index);

                Regex regex = new Regex("(\\b" + regWord + ".*)", RegexOptions.IgnoreCase);

                foreach (var row in data)
                {
                    var regMatch = regex.Match(row.Name);
                    if (regMatch.Success)
                    {
                        group.Nodes.Add(new MatchNode()
                        {
                            Id = row.Id,
                            Value = row.Name,
                            Distance = regMatch.Index
                        });
                    }
                }
                if (group.Nodes.Count > 0)
                    matches.Add(group);
            }

            if (matches.Count <= 0)
                return null;
            int count = 0;
            List<MatchGroup> results = new List<MatchGroup>();
            do
            {
                var row = matches[count];
                MatchGroup testGroup = new MatchGroup()
                {
                    Nodes = row.Nodes
                };
                testGroup.Keys.Add(row.Key);

                if (count == matches.Count)
                {
                    results.Add(testGroup);
                    break;
                }

                for (; count < matches.Count; count++)
                {
                    var overlap = testGroup.Nodes.Intersect(matches[count].Nodes, new MatchNodeComparer()).ToList();
                    if (overlap.Count > 0)
                    {
                        testGroup.Nodes = overlap;
                        testGroup.Keys.Add(matches[count].Key);
                    }
                    else if (testGroup.Keys.Count > 1)
                    {
                        count--;
                        break;
                    }
                }
                results.Add(testGroup);
                count++;
            }
            while (count < matches.Count);

            foreach (MatchGroup group in results)
            {
                input = Regex.Replace(input, "[\n \\/]", " ");
                int index1 = input.IndexOf(group.Keys[0], StringComparison.InvariantCultureIgnoreCase);
                int index2 = input.IndexOf(group.Keys[group.Keys.Count - 1], index1, StringComparison.InvariantCultureIgnoreCase);
                index2 = input.IndexOf(' ', index2);
                if (index2 <= 0)
                    index2 = input.Length;
                group.Key = input.Substring(index1, index2 - index1);
                MatchNodeDistanceComparer comparer = new MatchNodeDistanceComparer();
                group.Nodes.Sort(comparer);
                if (group.Nodes.Count > 20)
                    group.Nodes.RemoveRange(20, group.Nodes.Count - 20);
            }

            return results;
        }

        public static List<MatchGroup> FindMatchesNative(string input, EFoodComposition[] data)
        {
            List<MatchGroup> matches = new List<MatchGroup>();
            input = Regex.Replace(input, "[^A-Za-z ]", "");
            //List<string> match = new List<string>(input.ToLower().Split(' '));

            foreach(var comp in data)
            {
                if (string.IsNullOrEmpty(comp.AlternateName))
                    continue;
                try
                {
                string[] split = Regex.Replace(comp.AlternateName, "[^A-Za-z ]", "").Split(new char[0], StringSplitOptions.RemoveEmptyEntries);
                string containsOR = string.Format("({0})", string.Join("|", split));
                Regex contains = new Regex(containsOR, RegexOptions.IgnoreCase);
                var reg = contains.Matches(input);

                
                foreach (var b in reg)
                {
                    var rm = (Match)b;


                    MatchGroup mg;
                    mg = matches.FirstOrDefault(x => x.Key.Equals(rm.Value));
                    if (mg == null)
                    {
                        mg = new MatchGroup() { Key = rm.Value };
                        mg.Nodes.Add(new MatchNode()
                        {
                            Id = comp.Id,
                            Value = comp.AlternateName,
                            Distance = rm.Index,

                        });
                        matches.Add(mg);
                    }
                    else
                    {
                        mg.Nodes.Add(new MatchNode()
                        {
                            Id = comp.Id,
                            Value = comp.AlternateName,
                            Distance = rm.Index
                        });
                    }
                }
                } catch (Exception e)
                {
                    continue;
                }
            }
            return matches;
        }
    }
}