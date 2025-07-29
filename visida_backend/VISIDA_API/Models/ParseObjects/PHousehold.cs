using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.ParseObjects
{
    public class PHousehold
    {
        [Required, MaxLength(36)]
        public string HouseholdId { get; set; }

        [Required]
        public int StudyId { get; set; }

        [MaxLength(32)]
        public string Country { get; set; }

        public List<PHouseholdMember> HouseholdMembers { get; set; }
        public List<PCookRecipe> HouseholdRecipes { get; set; } = new List<PCookRecipe>();
        public List<HouseholdMeal> HouseholdMeals { get; set; } = new List<HouseholdMeal>();
        public List<HouseholdGuestInfo> HouseholdGuestInformation { get; set; } = new List<HouseholdGuestInfo>();
        public List<PComment> Comments { get; set; } = new List<PComment>();

        public DateTime StartDate { get; set; }
        public DateTime EndDate { get; set; }

        public string Latitude { get; set; }
        public string Longitude { get; set; }

        //Hold a static list for the duration of the parse
        //public static List<ImageRecord> EatImageRecords { get; set; }
        //public static List<HouseholdMeal> StaticMeals { get; set; }
        //public static List<HouseholdGuestInfo> StaticGuests { get; set; }
        //public static List<PComment> StaticComments { get; set; }

        public static implicit operator Household (PHousehold p)
        {
            //Clean EatImageRecords to new list
            //EatImageRecords = new List<ImageRecord>();
            //StaticMeals = p.HouseholdMeals;
            //StaticGuests = p.HouseholdGuestInformation;
            //StaticComments = p.Comments;

            int recipeCount = 0;
            var troubleDate = new DateTime(2066, 06, 06);

            string participantId = p.HouseholdMembers.FirstOrDefault()?.ParticipantHouseholdId;
            Household h = new Household()
            {
                Study_Id = p.StudyId,
                ParticipantId = participantId.Trim(),
                //Country = p.Country,
                StartDate = troubleDate,//p.StartDate,
                EndDate = troubleDate,//p.EndDate,
                Guid = p.HouseholdId,
                Latitude = p.Latitude,
                Longitude = p.Longitude
            };

            h.ImageRecords = new List<ImageRecord>();
            
            h.HouseholdRecipes = p.HouseholdRecipes?.Select(x => x.ToInternal(participantId, ++recipeCount, p)).ToList();
            h.HouseholdMeals = p.HouseholdMeals;
            h.HouseholdGuestInfo = p.HouseholdGuestInformation;
            h.HouseholdMembers = p.HouseholdMembers.Select(x => x.ToInternal(h, p)).ToList();

                //HouseholdMembers = p.HouseholdMembers.Select(x => (HouseholdMember)x).ToList(),
                //HouseholdMeals = StaticMeals,
                //HouseholdGuestInfo = StaticGuests,
                //ImageRecords = EatImageRecords
            //};

            h.HouseholdRecipes.SelectMany(x => x.Ingredients.Select(y => y.ImageRecord)).ToList().ForEach(x => x.Household = h);

            return h;
        }
    }
}