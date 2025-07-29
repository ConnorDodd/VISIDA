using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;
using VISIDA_API.Models.InternalObjects.Portion;
using VISIDA_API.Models.StringUtils;
using VISIDA_API.Models.User;
using static VISIDA_API.Models.InternalObjects.ImageRecord;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EImageRecord
    {
        public int Id { get; set; }
        public string HouseholdParticipantId { get; set; }
        public int HouseholdId { get; set; }
        public int? TableId { get; set; }
        public int? StudyId { get; set; }
        public int? LeftoverId { get; set; }
        public int? LeftoverFromId { get; set; }
        public EHouseholdMeal Meal { get; set; }
        public EHouseholdGuestInfo GuestInfo { get; set; }
        public DateTime CaptureTime { get; set; }

        public bool Hidden { get; set; }
        public bool Finalized { get; set; }
        public DateTime? FinalizedTime { get; set; }
        public bool ReviewDayAudio { get; set; }

        public string ImageName { get; set; }
        public string ImageUrl { get; set; }
        public string ImageThumbUrl { get; set; }

        public string AudioName { get; set; }
        public string AudioUrl { get; set; }

        public string TextDescription { get; set; }
        public string NTranscript { get; set; }
        public string Transcript { get; set; }
        public List<EComment> Comments { get; set; }

        public virtual ImageHomography Homography { get; set; }
        public bool IsFiducialPresent { get; set; }

        public int? RecipeId { get; set; }
        public string RecipeName { get; set; }

        [JsonConverter(typeof(StringEnumConverter))]
        public ImageRecord.RecordTypes RecordType { get; set; }
        public ICollection<EFoodItem> FoodItems { get; set; }
        public List<ESuggestion> Suggestions { get; set; } = new List<ESuggestion>();
        public object[] HouseholdRecipeNames { get; set; }
        public List<EStandardMeasure> StandardMeasures { get; set; }
        public CookIngredient.CookMethods? CookMethod { get; set; }

        public List<MatchGroup> TranscriptionGroups { get; set; }
        public List<MatchGroup> NTranscriptionGroups { get; set; }
        //public ICollection<PortionUpdate> PortionUpdates { get; set; }
        public ICollection<ERecordHistory> Updates { get; set; }

        public EWorkAssignation Assignation { get; set; }

        public List<EHouseholdMember> Participants { get; set; }
        [JsonConverter(typeof(StringEnumConverter))]
        public AnnotationStatuses AnnotationStatus { get; set; }
        public int ParticipantCount { get; set; }

        public bool IsCompleted { get; set; }
        public bool Is24HR { get; set; }
        public int GestaltMax { get; set; }

        public string LockedBy { get; set; }
        public DateTime? LockTimestamp { get; set; }

        //Test stuff
        public string ManualTranscript { get; set; }
        public string Translation { get; set; }

        public static EImageRecord ToShallowEImageRecord(ImageRecord r)
        {
            if (r == null)
                return null;
            return new EImageRecord()
            {
                Id = r.Id,
                HouseholdParticipantId = r.Household?.ParticipantId,
                HouseholdId = r.Household?.Id ?? 0,
                Hidden = r.Hidden,
                Meal = r.Meal,
                GuestInfo = r.GuestInfo,
                CaptureTime = r.CaptureTime,
                ImageName = r.ImageName,
                ImageUrl = r.ImageUrlUpdated ?? r.ImageUrl,
                ImageThumbUrl = r.ImageThumbUrl,
                AudioName = r.AudioName,
                AudioUrl = r.AudioUrlUpdated ?? r.AudioUrl,
                TextDescription = r.TextDescription ?? r.Transcript,
                //Homography = r.Homography,
                IsFiducialPresent = r.IsFiducialPresent,
                RecordType = r.RecordType,
                FoodItems = r.FoodItems.Select(x => (EFoodItem)x).ToList(),
                NTranscript = r.NTranscript,
                IsCompleted = r.IsCompleted,
                LockTimestamp = r.LockTimestamp,
                Is24HR = r.Is24HR,
                ParticipantCount = r.ParticipantCount,
                ManualTranscript = r.ManualTranscript,
                Translation = r.Translation,
                Transcript = r.Transcript,
                AnnotationStatus = r.AnnotationStatus,

                Comments = r.Comments.Select(x => (EComment)x).ToList()
            };
        }

        public static EImageRecord ForHouseholdEImageRecord(ImageRecord record, bool analyst, LoginUser user, bool gestalt)
        {
            if (record == null)
                return null;
            var ret = new EImageRecord()
            {
                Id = record.Id,
                Hidden = record.Hidden,
                GuestInfo = record.GuestInfo,
                CaptureTime = record.CaptureTime,
                ImageName = record.ImageName,
                ImageUrl = record.ImageUrlUpdated ?? record.ImageUrl,
                ImageThumbUrl = record.ImageThumbUrl,
                AudioName = record.AudioName,
                AudioUrl = record.AudioUrlUpdated ?? record.AudioUrl,
                TextDescription = record.TextDescription ?? record.Transcript,
                RecordType = record.RecordType,
                NTranscript = record.NTranscript,
                IsCompleted = record.IsCompleted,
                Is24HR = record.Is24HR,
                AnnotationStatus = record.AnnotationStatus,
                Comments = record.Comments.Select(x => (EComment)x).ToList()
                
            };
            if (analyst && gestalt)
            {
                ret.FoodItems = new List<EFoodItem>();
                foreach (var fi in record.FoodItems)
                {
                    var update = fi.Updates.OrderByDescending(x => x.Time).FirstOrDefault(x => x.Action == RecordHistory.UpdateTypes.Quantify && (x.User.Id == user.Id || x.User.IsAdmin));
                    EFoodItem efi = new EFoodItem()
                    {
                        Id = fi.Id,
                        QuantityGrams = update?.QuantityGrams ?? 0,
                        Name = fi.FoodComposition?.Name
                    };
                    ret.FoodItems.Add(efi);
                }
            }
            else
            {
                ret.FoodItems = record.FoodItems.Select(x => new EFoodItem()
                {
                    Id = x.Id,
                    QuantityGrams = x.QuantityGrams,
                    Name = x.FoodComposition?.Name
                }).ToList();
            }
            return ret;
        }

        public static implicit operator EImageRecord(ImageRecord r)
        {
            if (r == null)
                return null;
            return new EImageRecord()
            {
                Id = r.Id,
                HouseholdParticipantId = r.Household?.ParticipantId,
                HouseholdId = r.Household?.Id ?? 0,
                Hidden = r.Hidden,
                Meal = r.Meal,
                GuestInfo = r.GuestInfo,
                CaptureTime = r.CaptureTime,
                TableId = r.Household?.Study.FoodCompositionTable_Id,
                ImageName = r.ImageName,
                ImageUrl = r.ImageUrlUpdated ?? r.ImageUrl,
                ImageThumbUrl = r.ImageThumbUrl,
                AudioName = r.AudioName,
                AudioUrl = r.AudioUrlUpdated ?? r.AudioUrl,
                TextDescription = r.TextDescription ?? r.Transcript,
                Homography = r.Homography,
                IsFiducialPresent = r.IsFiducialPresent,
                RecordType = r.RecordType,
                FoodItems = r.FoodItems.Select(x => (EFoodItem)x).ToList(),
                NTranscript = r.ManualTranscript ?? r.NTranscript,
                Comments = r.Comments.Select(x => (EComment)x).ToList(),
                IsCompleted = r.IsCompleted,
                LockedBy = r.LockedBy?.UserName,
                LockTimestamp = r.LockTimestamp,
                //HouseholdRecipeNames = r.Household?.HouseholdRecipes.Where(x => !x.Hidden).Select(x => new EHouseholdRecipe{ Id = x.FoodComposition?.Id, Name = x.FoodComposition?.Name, AlternateName = x.FoodComposition?.AlternateName, Density = x.FoodComposition?.Density, RecipeId = x.Id }).ToArray(),
                ParticipantCount = r.ParticipantCount,
                Is24HR = r.Is24HR,

                ManualTranscript = r.ManualTranscript,
                Translation = r.Translation,
                Transcript = r.Transcript,
                AnnotationStatus = r.AnnotationStatus,

                Updates = r.Updates.OrderByDescending(x => x.Time).ThenByDescending(x => x.Id).Select(x => (ERecordHistory)x).ToList(),
                StudyId = r.Household.Study_Id,
            };
        }
    }

    public class EHouseholdRecipe
    {
        public int? Id { get; set; }
        public string Name { get; set; }
        public string AlternateName { get; set; }
        public double? Density { get; set; }
        public int? RecipeId { get; set; }
    }
}