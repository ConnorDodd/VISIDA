using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.InternalObjects
{
    public interface IUploadImage
    {
        bool IsFiducialPresent { get; set; }
        string ImageUrl { get; set; }
        string ImageName { get; set; }
        string ImageThumbUrl { get; set; }
        ImageHomography Homography { get; set; }
    }
}