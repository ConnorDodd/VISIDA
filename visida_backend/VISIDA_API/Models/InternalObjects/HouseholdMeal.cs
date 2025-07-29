using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.InternalObjects
{
    public class HouseholdMeal
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        public virtual Household Household { get; set; }
        public virtual ICollection<ImageRecord> ImageRecords { get; set; }

        //public int AdultFemaleGuests { get; set; }
        //public int AdultMaleGuests { get; set; }
        //public int ChildGuests { get; set; }

        //public int? ModAdultFemaleGuests { get; set; }
        //public int? ModAdultMaleGuests { get; set; }
        //public int? ModChildGuests { get; set; }

        public bool Finalized { get; set; }
        public bool GuestInfoCaptured { get; set; }
        public DateTime StartTime { get; set; }

        //[NotMapped]
        //public int TotalHeads { get { return AdultFemaleGuests + AdultMaleGuests + ChildGuests; } }
        [NotMapped]
        public int MealId { get; set; }
        [NotMapped]
        public bool Faked { get; set; }
    }
}