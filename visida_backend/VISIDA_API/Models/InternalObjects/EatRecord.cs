using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.ExternalObjects;

namespace VISIDA_API.Models.InternalObjects
{
    public class EatRecord
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        public int OriginId { get; set; }
        public virtual EatOccasion EatOccasion { get; set; }
        public virtual ImageRecord ImageRecord { get; set; }
        public virtual ImageRecord Leftovers { get; set; }

        public bool Hidden { get; set; } = false;
        [DefaultValue(true)]
        public bool Original { get; set; } = true;

        public DateTime FinalizeTime { get; set; }
        public bool Finalized { get; set; }

        public EHouseholdMember GetParticipation()
        {
            EHouseholdMember m = EatOccasion.HouseholdMember;
            m.Original = Original;
            m.Hidden = Hidden;

            return m;
        }
    }
}