using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects.Testing;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EReliabilityTest
    {
        public int Id { get; set; }
        public int ImageRecordId { get; set; }
        public int KnownRecordId { get; set; }
        public int UserId { get; set; }
        public DateTime? AssignedTime { get; set; }
        public bool Completed { get; set; }
        public bool Deleted { get; set; }
        public ReliabilityTest.TestTypes TestType { get; set; }

        public EImageRecord ImageRecord { get; set; }
        public EImageRecord KnownRecord { get; set; }
        public ICollection<ETiming> Timings { get; set; }

        public List<EReliabilityTestScore> Scores { get; set; } = new List<EReliabilityTestScore>();
        public double IdentificationAccuracy { get; set; }
        public double QuantificationAccuracy { get; set; }

        public static implicit operator EReliabilityTest(ReliabilityTest r)
        {
            return new EReliabilityTest()
            {
                Id = r.Id,
                ImageRecordId = r.ImageRecord.Id,
                KnownRecordId = r.KnownRecord.Id,
                UserId = r.AssignedTo.Id,
                AssignedTime = r.AssignedTime,
                Completed = r.ImageRecord.IsCompleted,
                Deleted = r.Deleted,
                TestType = r.TestType,
                Scores = r.Scores.Select(x => (EReliabilityTestScore)x).ToList(),
                IdentificationAccuracy = r.IdentificationAccuracy,
                QuantificationAccuracy = r.QuantificationAccuracy
            };
        }
    }
}