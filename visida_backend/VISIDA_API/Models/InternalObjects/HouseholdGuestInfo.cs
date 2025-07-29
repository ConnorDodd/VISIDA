using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.InternalObjects
{
    public class HouseholdGuestInfo
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }

        public virtual ICollection<ImageRecord> ImageRecords { get; set; }

        //The word 'guests' here really representes total participants including recorded household and extra unknowns
        public int AdultFemaleGuests { get; set; }
        public int AdultMaleGuests { get; set; }
        public int ChildGuests { get; set; }

        public int? ModAdultFemaleGuests { get; set; }
        public int? ModAdultMaleGuests { get; set; }
        public int? ModChildGuests { get; set; }

        [NotMapped, JsonProperty(PropertyName = "geustInfoId")]
        public int GuestInfoId { get; set; }

        [NotMapped]
        public int TotalParticipants
        {
            get
            {
                return (ModAdultFemaleGuests ?? AdultFemaleGuests) + (ModAdultMaleGuests ?? AdultMaleGuests) + (ModChildGuests ?? ChildGuests);
            }
        }
    }
}