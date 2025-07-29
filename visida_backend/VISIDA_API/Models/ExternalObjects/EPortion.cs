using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EPortion
    {
        public int FoodItemId { get; set; }
        public string FoodItem { get; set; }

        public double Volume { get; set; }
        public double ToolVolume { get; set; }
        public string ToolName { get; set; }

        public double TopLeftX { get; set; }
        public double TopLeftY { get; set; }
        public double TopRightX { get; set; }
        public double TopRightY { get; set; }
        public double BottomRightX { get; set; }
        public double BottomRightY { get; set; }
        public double BottomLeftX { get; set; }
        public double BottomLeftY { get; set; }

        public double TagX { get; set; }
        public double TagY { get; set; }

        public string Url { get; set; }

        public double Confidence { get; set; }

        public static implicit operator EPortion(FoodItem fi)
        {
            var homography = fi.ImageRecord.Homography;
            EPortion port = new EPortion()
            {
                FoodItemId = fi.Id,
                FoodItem = fi.FoodComposition.Name,
                ToolName = fi.ToolSource ?? "",

                TopLeftX = homography?.TopLeftX ?? 0,
                TopLeftY = homography?.TopLeftY ?? 0,
                TopRightX = homography?.TopRightX ?? 0,
                TopRightY = homography?.TopRightY ?? 0,
                BottomRightX = homography?.BottomRightX ?? 0,
                BottomRightY = homography?.BottomRightY ?? 0,
                BottomLeftX = homography?.BottomLeftX ?? 0,
                BottomLeftY = homography?.BottomLeftY ?? 0,

                TagX = fi.TagXPercent ?? 0,
                TagY = fi.TagYPercent ?? 0,

                Url = fi.ImageRecord.ImageUrl,
            };
            if (fi.MeasureType != null && fi.MeasureType.Equals("Volume input (mL)"))
            {
                port.Volume = fi.MeasureCount;
                port.ToolVolume = fi.ToolMeasure;
            }

            return port;
        }
    }
}