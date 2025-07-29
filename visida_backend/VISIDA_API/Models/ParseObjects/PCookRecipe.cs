using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.ParseObjects
{
    public class PCookRecipe
    {
        public int RecipeId { get; set; }

        public string FinalImageUrl { get; set; }
        public string RecipeNameAudioUrl { get; set; }
        public string RecipeNameText { get; set; }
        public DateTime CaptureTime { get; set; }
        public virtual List<PCookIngredient> Ingredients { get; set; }
        public string RecipeName { get; set; }
        public AnnotatedStatuses AnnotatedStatus { get; set; }
        public int[] CommentIds { get; set; } = new int[0];

        public CookRecipe ToInternal(string hhId, int recipeCount, PHousehold pHousehold)
        {
            var fcName = "";
            if (!string.IsNullOrEmpty(this.RecipeName))
                fcName = string.Format("{0} {1}", this.RecipeName, this.CaptureTime.ToString("dd/MM/yyy"));
            else
                fcName = String.Format("{0}_Recipe_{1} {2}", hhId, recipeCount, CaptureTime.ToString("dd-MM-yyyy"));
            CookRecipe recipe = new CookRecipe
            {
                OriginId = this.RecipeId,
                ImageName = this.FinalImageUrl,
                AudioName = this.RecipeNameAudioUrl,
                TextDescription = this.RecipeNameText ?? this.RecipeName,
                Name = this.RecipeName,
                Ingredients = this.Ingredients.Select(x => x.ToInternal(pHousehold)).ToList(),
                CaptureTime = this.CaptureTime,
                FoodComposition = new FCT.FoodComposition()
                {
                    Name = fcName,
                    ModifiedDate = DateTime.Now
                },
                AnnotatedStatus = this.AnnotatedStatus,
                Comments = new List<Comment>()
            };

            foreach (var id in this.CommentIds)
            {
                foreach (var comment in pHousehold.Comments)
                {
                    if (comment.CommentId == id)
                        recipe.Comments.Add(new Comment()
                        {
                            CreatedTime = comment.CaptureTime,
                            Text = comment.Text,
                            Flag = Comment.FlagTypes.FromApp,
                            HighPriority = comment.HighPriority
                        });
                }
            }

            return recipe;
        }
    }
}