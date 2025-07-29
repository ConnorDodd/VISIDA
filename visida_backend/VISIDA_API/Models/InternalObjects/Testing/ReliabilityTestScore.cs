using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.InternalObjects.Testing
{
    public class ReliabilityTestScore
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }

        public virtual ReliabilityTest Test { get; set; }
        public int FoodItemId { get; set; }

        public enum ScoreTypes { Identification, Quantification };
        public ScoreTypes Type { get; set; }

        public enum AccuracyLevels { Perfect, Close, Tolerable, Unusable };
        public AccuracyLevels Accuracy { get; set; }
    }
}