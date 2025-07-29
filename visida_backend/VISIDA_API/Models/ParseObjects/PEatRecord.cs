using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;
using static VISIDA_API.Models.InternalObjects.ImageRecord;

namespace VISIDA_API.Models.ParseObjects
{
    public class PEatRecord
    {
        public int FoodItemId { get; set; }
        public string AudioUrls { get; set; }
        public string ImageUrl { get; set; }
        public string Description { get; set; }
        public int MealId { get; set; }
        public int GuestInfoId { get; set; }

        public DateTime CaptureTime { get; set; }
        public DateTime FinalizeTime { get; set; }
        public bool Finalized { get; set; }
        public bool DidntEat { get; set; }

        public string LeftoverAudioUrls { get; set; }
        public string LeftoverImageUrl { get; set; }
        public string LeftoverDescription { get; set; }

        public int[] CommentIds { get; set; } = new int[0];
        public AnnotationStatuses AnnotationStatus { get; set; }
        public AnnotatedStatuses AnnotatedStatus { get; set; }

        private static int _tempId = -1;
        private static int TempId {  get { return _tempId--; } }

        public EatRecord ToInternal(Household household, PHousehold pHousehold, PEatOccasion pEatOccasion)
        {
            EatRecord e = new EatRecord()
            {
                Id = TempId,
                OriginId = this.FoodItemId,
                Finalized = this.Finalized,
                FinalizeTime = this.FinalizeTime,
            };

            HouseholdMeal meal = null;
            if (this.MealId > 0)
            {
                meal = household.HouseholdMeals.FirstOrDefault(x => x.MealId == this.MealId);
                if (meal == null)
                {
                    meal = new HouseholdMeal()
                    {
                        StartTime = this.CaptureTime,
                        Faked = true,
                        MealId = household.HouseholdMeals.Count
                    };
                    household.HouseholdMeals.Add(meal);
                }
                //if (meal.Faked)
                //{
                //    meal.AdultFemaleGuests++;
                //}
            }

            HouseholdGuestInfo guestInfo = null;
            if (this.GuestInfoId > 0)
                guestInfo = household.HouseholdGuestInfo.FirstOrDefault(x => x.GuestInfoId == this.GuestInfoId);

            if (this.AudioUrls != null && (this.AudioUrls.Equals("NO_AUDIO") || string.IsNullOrWhiteSpace(this.AudioUrls)))
                this.AudioUrls = null;
            if (this.ImageUrl != null && (this.ImageUrl.Equals("NO_IMAGE") || string.IsNullOrWhiteSpace(this.ImageUrl)))
                this.ImageUrl = null;
            if (this.LeftoverAudioUrls != null && (this.LeftoverAudioUrls.Equals("NO_AUDIO") || string.IsNullOrWhiteSpace(this.LeftoverAudioUrls)))
                this.LeftoverAudioUrls = null;
            if (this.LeftoverImageUrl != null && (this.LeftoverImageUrl.Equals("NO_IMAGE") || string.IsNullOrWhiteSpace(this.LeftoverImageUrl)))
                this.LeftoverImageUrl = null;

            if (this.AudioUrls == null && this.ImageUrl == null && string.IsNullOrEmpty(this.Description))
                return e;
            if (this.AudioUrls == null && this.ImageUrl != null)
                this.AudioUrls = this.ImageUrl.Substring(0, this.ImageUrl.Length - 4) + ".mp3";
            if (this.ImageUrl == null && this.AudioUrls != null)
                this.ImageUrl = this.AudioUrls.Substring(0, this.AudioUrls.Length - 4) + ".jpg";

            ImageRecord imageRecord = null;
            List<CookRecipe> recipes = new List<CookRecipe>();
            if (pEatOccasion.RecipeIds != null)
                recipes = household.HouseholdRecipes.Where(x => pEatOccasion.RecipeIds.Contains(x.OriginId)).ToList();
            if (this.GuestInfoId > 0)
                imageRecord = household.ImageRecords.FirstOrDefault(x => x.GuestInfoId == this.GuestInfoId); //Find existing shared item records
            if (imageRecord == null)
            {
                imageRecord = new ImageRecord
                {
                    ImageName = this.ImageUrl,
                    AudioName = this.AudioUrls,
                    RecordType = ImageRecord.RecordTypes.EatRecord,
                    TextDescription = this.Description,
                    CaptureTime = this.CaptureTime,
                    Meal = meal,
                    GuestInfo = guestInfo,
                    AnnotationStatus = this.AnnotationStatus,
                    GuestInfoId = this.GuestInfoId,
                    Recipes = recipes,
                    AnnotatedStatus = this.AnnotatedStatus
                };
                household.ImageRecords.Add(imageRecord);
            }
            e.ImageRecord = imageRecord;

            //Add any comments
            List<Comment> comments = new List<Comment>();
            foreach (int i in this.CommentIds)
            {
                var com = pHousehold.Comments.FirstOrDefault(x => x.CommentId == i);
                if (com != null)
                    comments.Add(new Comment()
                    {
                        CreatedTime = com.CaptureTime,
                        Flag = Comment.FlagTypes.FromApp,
                        Text = com.Text,
                        HighPriority = com.HighPriority
                    });

            }
            imageRecord.Comments = comments;

            if (!String.IsNullOrEmpty(this.LeftoverImageUrl) || !String.IsNullOrEmpty(this.LeftoverAudioUrls) || !string.IsNullOrEmpty(this.LeftoverDescription))
            {
                var leftoverRecord = household.ImageRecords.FirstOrDefault(x => x.ImageName != null && x.ImageName.Equals(this.LeftoverImageUrl));
                if (leftoverRecord == null)
                {
                    leftoverRecord = new ImageRecord
                    {
                        ImageName = this.LeftoverImageUrl,// ?? this.LeftoverAudioUrls?.Substring(0, this.LeftoverAudioUrls.LastIndexOf(".")) + ".jpg",
                        AudioName = this.LeftoverAudioUrls,
                        RecordType = ImageRecord.RecordTypes.Leftovers,
                        TextDescription = this.LeftoverDescription,
                        IsLeftovers = true,
                        CaptureTime = this.CaptureTime,
                        Meal = meal,
                        GuestInfo = guestInfo,
                        Recipes = recipes,
                        AnnotatedStatus = this.AnnotatedStatus
                    };
                    if (leftoverRecord.ImageName == null && leftoverRecord.AudioName != null)
                        leftoverRecord.ImageName =  leftoverRecord.AudioName?.Substring(0, leftoverRecord.AudioName.LastIndexOf(".")) + ".jpg";

                    household.ImageRecords.Add(leftoverRecord);
                }
                e.Leftovers = leftoverRecord;
            }

            return e;
        }
    }
}