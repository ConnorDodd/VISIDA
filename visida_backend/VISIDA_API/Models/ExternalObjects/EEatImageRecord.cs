using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EEatImageRecord
    {
        public int Id { get; set; }
        public EEatOccasion EatOccasion { get; set; }

        public string ImageName { get; set; }
        public string ImageUrl { get; set; }

        public EEatImageText[] EatImageTexts { get; set; }
        public EEatImageAudio[] EatImageAudios { get; set; }
        public EFoodItem[] FoodItems { get; set; }

        public bool IsLeftover { get; set; }
        public int? LeftoverFromId { get; set; }
        public int? LeftoverId { get; set; }

        public DateTime CaptureTime { get; set; }
        public bool Finalized { get; set; }

        public int? TableId { get; set; }

        public string LockedName { get; set; }

        public OpenCV.DoublePoint[] Homography { get; set; }

        public static implicit operator EEatImageRecord(EatImageRecord r)
        {
            EEatImageRecord e = new EEatImageRecord()
            {
                Id = r.Id,
                ImageName = r.ImageName,
                ImageUrl = r.ImageUrl,
                //EatImageTexts = r.EatImageTexts.Select(x => (EEatImageText)x).ToArray(),
                //EatImageAudios = r.EatImageAudios.Select(x => (EEatImageAudio)x).ToArray(),
                FoodItems = r.FoodItems.Select(x => (EFoodItem)x).ToArray(),
                IsLeftover = r.IsLeftover,
                LeftoverFromId = r.LeftoverFrom?.Id,
                LeftoverId = r.LeftoverId,
                CaptureTime = r.CaptureTime,
                Finalized = r.Finalized,
                TableId = r.EatOccasion.HouseholdMember.Household.Study.FoodCompositionTable_Id,
                LockedName = r.LockedBy?.UserName,
                Homography = r.Homography?.Points,
                EatOccasion = new EEatOccasion
                {
                    Id = r.EatOccasion.Id,
                    TimeStart = r.EatOccasion.TimeStart,
                    TimeEnd = r.EatOccasion.TimeEnd,
                    Finalized = r.EatOccasion.Finalized
                }
            };

            return e;
        }
    }
}