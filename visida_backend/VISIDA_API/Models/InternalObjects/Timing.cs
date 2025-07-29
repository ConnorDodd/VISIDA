using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.User;

namespace VISIDA_API.Models.InternalObjects
{
    public class Timing
    {
        public enum TimingType { IdentifyFoodItem, QuantifyFoodItem, IdentifyWithQuantityFoodItem }
        public TimingType Type { get; set; }

        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        public virtual LoginUser CreatedBy { get; set; }
        public int TimeTaken { get; set; }
        public DateTime CreatedTime { get; set; }
        public virtual FoodItem FoodItem { get; set; }
        public virtual Study Study { get; set; }
    }
}