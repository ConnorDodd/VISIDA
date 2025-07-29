using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.User;

namespace VISIDA_API.Models.InternalObjects.Testing
{
    public class ReliabilityTest
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        public virtual ReliabilityTestRule Rule { get; set; }
        public virtual ICollection<ReliabilityTestScore> Scores { get; set; }

        public virtual ImageRecord KnownRecord { get; set; }
        public virtual ImageRecord ImageRecord { get; set; }
        public TestTypes TestType { get; set; }

        public virtual LoginUser AssignedTo { get; set; }
        public DateTime AssignedTime { get; set; }
        public bool Deleted { get; set; } = false;

        public enum TestTypes { Identify = 0, Quantify = 1, Both = 2 };

        public double IdentificationAccuracy { get; set; }
        public double QuantificationAccuracy { get; set; }

        public WorkAssignation.AccessLevels GetAssignationType()
        {
            switch (TestType)
            {
                case TestTypes.Both: return WorkAssignation.AccessLevels.Both;
                case TestTypes.Identify: return WorkAssignation.AccessLevels.Identify;
                case TestTypes.Quantify: return WorkAssignation.AccessLevels.Quantify;
                default: return WorkAssignation.AccessLevels.View;
            }
        }

        internal void RecalculateAccuracy()
        {
            double identAcc = 0, quantAcc = 0;
            int identCount = 0, quantCount = 0;
            var known = KnownRecord.FoodItems.ToList();
            foreach (var item in ImageRecord.FoodItems)
            {
                foreach (var k in known)
                {
                    if (item.FoodCompositionId == k.FoodCompositionId)
                    {
                        identAcc += 100;
                        identCount++;

                        if (item.QuantityGrams > 0)
                        {
                            var acc = (Math.Abs(k.QuantityGrams - item.QuantityGrams) / k.QuantityGrams) * 100;
                            quantAcc += 100 - acc;
                            quantCount++;
                        }
                    }
                }
            }

            foreach (var score in Scores)
            {
                if (score.Type == ReliabilityTestScore.ScoreTypes.Identification)
                {
                    switch (score.Accuracy)
                    {
                        case ReliabilityTestScore.AccuracyLevels.Perfect:
                            identAcc += 100;
                            break;
                        case ReliabilityTestScore.AccuracyLevels.Close:
                            identAcc += 85;
                            break;
                        case ReliabilityTestScore.AccuracyLevels.Tolerable:
                            identAcc += 60;
                            break;
                        case ReliabilityTestScore.AccuracyLevels.Unusable:
                            identAcc += 10;
                            break;
                    }
                    identCount++;
                }
                else if (score.Type == ReliabilityTestScore.ScoreTypes.Quantification)
                {
                    switch (score.Accuracy)
                    {
                        case ReliabilityTestScore.AccuracyLevels.Perfect:
                            quantAcc += 100;
                            break;
                        case ReliabilityTestScore.AccuracyLevels.Close:
                            quantAcc += 85;
                            break;
                        case ReliabilityTestScore.AccuracyLevels.Tolerable:
                            quantAcc += 60;
                            break;
                        case ReliabilityTestScore.AccuracyLevels.Unusable:
                            quantAcc += 10;
                            break;
                    }
                    quantCount++;
                }
            }

            QuantificationAccuracy = (quantAcc / quantCount);
            IdentificationAccuracy = (identAcc / identCount);
        }
    }
}