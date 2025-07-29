using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.User;

namespace VISIDA_API.Models.InternalObjects
{
    public class Household
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        [Required, MaxLength(53), Index(IsUnique = true)]
        public string Guid { get; set; }
        [Required, MaxLength(128)]//, Index(IsUnique = true)]
        public string ParticipantId { get; set; }

        [Required, ForeignKey("Study")]
        public int Study_Id { get; set; }
        public virtual Study Study { get; set; }

        public virtual UsageLogFile UsageLog { get; set; }

        [MaxLength(32)]
        public string Country { get; set; }

        [Required]
        public DateTime StartDate { get; set; }
        public DateTime EndDate { get; set; }

        public string Latitude { get; set; }
        public string Longitude { get; set; }

        public virtual ICollection<HouseholdMember> HouseholdMembers { get; set; }
        public virtual ICollection<CookRecipe> HouseholdRecipes { get; set; }
        public virtual ICollection<ImageRecord> ImageRecords { get; set; }
        public virtual ICollection<HouseholdMeal> HouseholdMeals { get; set; }
        public virtual ICollection<HouseholdGuestInfo> HouseholdGuestInfo { get; set; }
        public virtual ICollection<HouseholdAssignation> HouseholdAssignments { get; set; }

        [NotMapped]
        public List<Work> WorkDone
        {
            get
            {
                List<Work> work = new List<Work>();
                foreach (LoginUser u in Study.Assignees.Select(x => x.LoginUser))
                {
                    //var items = (from record in Records
                    //             select record into r
                    //             from item in r.FoodItems
                    //             where item.CreatedBy != null && item.CreatedBy.UserName.Equals(u.UserName)
                    //             select item).ToList();
                    work.Add(new Work()
                    {
                        UserName = u.UserName,
                        Id = u.Id,
                        LastOnline = u.LastSeen,
                        Items = ImageRecords.Sum(x => x.FoodItems.Count(y => y.CreatedBy != null && y.CreatedBy.UserName.Equals(u.UserName)))
                    });
                }

                return work;
            }

        }


        [NotMapped]
        // Return the total number of records that aren;t hidden
        public int RecordTotal { get { return ImageRecords.Count(x => !x.Hidden); } }
        [NotMapped]
        public int RecordNotStarted { get { return ImageRecords.Count(x => x.FoodItems.Count() <= 0 && !x.IsCompleted && !x.Hidden); } }

        [NotMapped]
        public int IdentifyInProgress { get { return RecordTotal - RecordNotStarted - IdentifyCompleted; } }

        [NotMapped]
        public int IdentifyCompleted { get { return ImageRecords.Count(x => x.IsCompleted && !x.Hidden); } }
        //public int IdentifyCompleted { get { return Records.Count(x => x.FoodItems.Count() > 0 && x.FoodItems.Count(y => y.FoodCompositionId == null) == 0); } }

        [NotMapped]
        public int PortionInProgress { get { return ImageRecords.Count(x => !x.Hidden && x.FoodItems.Count(y => y.QuantityGrams == 0) > 0); } }
        [NotMapped]
        public int PortionCompleted { get { return RecordTotal - RecordNotStarted - PortionInProgress; } }
        [NotMapped]
        // The identify stage is completed and there are no food items with quantity 0
        public int IdentifyAndPortionCompleted { get { return ImageRecords.Count(x => x.IsCompleted && !x.Hidden && (x.FoodItems.Count(y => y.QuantityGrams == 0) == 0)); } }
        [NotMapped]
        // Only count if it is part of an eat record or leftover
        public int HiddenRecordTotal { get { return ImageRecords.Count(x => (x.RecordType == ImageRecord.RecordTypes.EatRecord || x.RecordType == ImageRecord.RecordTypes.Leftovers) && x.Hidden); } }
        [NotMapped]
        public int LeftOversTotal { get { return ImageRecords.Count(x => x.IsLeftovers && !x.Hidden); } }
        [NotMapped]
        public int FoodItemTotal { get { return ImageRecords.Sum(
            x => {
                // Only return a count if it is part of an eat Record or Leftover
                if ((x.RecordType == ImageRecord.RecordTypes.EatRecord || x.RecordType == ImageRecord.RecordTypes.Leftovers) && !x.Hidden)
                {
                    return x.FoodItems.Count();
                }
                else
                {
                    return 0;
                }
            }); } 
        }
        [NotMapped]
        public int RecipeTotal { get { return HouseholdRecipes.Count(x => !x.Hidden); } }
        [NotMapped]
        public int HiddenRecipeTotal { get { return HouseholdRecipes.Count(x => x.Hidden); } }
        /*
        [NotMapped]
        public int IngredientTotal { get { return HouseholdRecipes.Sum(x =>{
            return x.Ingredients.Count(); 
        }); } }*/

        public class Work
        {
            public string UserName { get; set; }
            public int Id { get; set; }
            public DateTime? LastOnline { get; set; }
            public int Items { get; set; }
        }
    }
}