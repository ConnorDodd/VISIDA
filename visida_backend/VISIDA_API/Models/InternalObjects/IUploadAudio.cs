using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.InternalObjects
{
    public interface IUploadAudio
    {
        string AudioUrl { get; set; }
        string AudioName { get; set; }
        Household Household { get; set; }
        string Transcript { get; set; }
        string NTranscript { get; set; }
    }
}