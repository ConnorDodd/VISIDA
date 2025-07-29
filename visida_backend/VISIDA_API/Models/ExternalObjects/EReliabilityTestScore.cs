using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects.Testing;
using static VISIDA_API.Models.InternalObjects.Testing.ReliabilityTestScore;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EReliabilityTestScore
    {
        public int Id { get; set; }
        public int FoodItemId { get; set; }

        public ScoreTypes Type { get; set; }
        public AccuracyLevels Accuracy { get; set; }

        public static implicit operator EReliabilityTestScore(ReliabilityTestScore rt)
        {
            return new EReliabilityTestScore()
            {
                Id = rt.Id,
                FoodItemId = rt.FoodItemId,
                Type = rt.Type,
                Accuracy = rt.Accuracy
            };
        }
    }
}