using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EEatImageAudio
    {
        public int Id { get; set; }
        public string AudioName { get; set; }
        public string AudioUrl { get; set; }
        public string Format { get; set; }

        public static implicit operator EEatImageAudio(EatImageAudio a)
        {
            return new EEatImageAudio()
            {
                Id = a.Id,
                AudioName = a.AudioName,
                AudioUrl = a.AudioUrl,
                Format = a.Format
            };
        }
    }
}