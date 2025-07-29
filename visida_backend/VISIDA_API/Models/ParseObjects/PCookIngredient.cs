using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;

namespace VISIDA_API.Models.ParseObjects
{
    public class PCookIngredient
    {
        public int IngredientId { get; set; }
        public Household Household { get; set; }
        public string ImageUrl { get; set; }
        public string AudioUrl { get; set; }
        public DateTime CaptureTime { get; set; }
        public string Description { get; set; }
        public AnnotatedStatuses AnnotatedStatus { get; set; }
        public int[] CommentIds { get; set; } = new int[0];

        public CookIngredient ToInternal(PHousehold pHousehold)
        {
            CookIngredient ingredient = new CookIngredient
            {
                OriginId = this.IngredientId,
                AnnotatedStatus = this.AnnotatedStatus,
                ImageRecord = new ImageRecord
                {
                    ImageName = this.ImageUrl,
                    AudioName = this.AudioUrl,
                    Household = this.Household,
                    RecordType = ImageRecord.RecordTypes.Ingredient,
                    CaptureTime = this.CaptureTime,
                    AnnotatedStatus = this.AnnotatedStatus,
                    TextDescription = this.Description,
                    Comments = new List<Comment>()
                }
            };

            foreach (var id in this.CommentIds)
            {
                foreach (var comment in pHousehold.Comments)
                {
                    if (comment.CommentId == id)
                        ingredient.ImageRecord.Comments.Add(new Comment()
                        {
                            CreatedTime = comment.CaptureTime,
                            Text = comment.Text,
                            Flag = Comment.FlagTypes.FromApp,
                            HighPriority = comment.HighPriority
                        });
                }
            }

            return ingredient;
        }
    }
}