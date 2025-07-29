using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EEatRecord
    {
        public int Id { get; set; }
        public EImageRecord ImageRecord { get; set; }
        public EImageRecord Leftovers { get; set; }

        public DateTime CaptureTime { get; set; }
        public DateTime FinalizeTime { get; set; }
        public bool Finalized { get; set; }
        public bool Hidden { get; set; }

        public static implicit operator EEatRecord(EatRecord e)
        {
            EEatRecord record = new EEatRecord()
            {
                Id = e.Id,
                ImageRecord = e.ImageRecord,
                FinalizeTime = e.FinalizeTime,
                Finalized = e.Finalized
            };
            if (e.Leftovers != null)
                record.Leftovers = e.Leftovers;

            return record;
        }
    }
}