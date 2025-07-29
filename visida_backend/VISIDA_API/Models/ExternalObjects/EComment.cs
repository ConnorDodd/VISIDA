using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.InternalObjects;
using static VISIDA_API.Models.InternalObjects.Comment;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EComment
    {
        public int Id { get; set; }
        public int ReplyTo { get; set; }
        public int? RecordId { get; set; }
        public int? RecipeId { get; set; }
        public string Text { get; set; }
        public string AuthorName { get; set; }
        public DateTime CreatedTime { get; set; }
        [JsonConverter(typeof(StringEnumConverter))]
        public FlagTypes Flag { get; set; }
        public bool HighPriority { get; set; }
        public bool Hidden { get; set; }
        public DateTime? TaskCompleted { get; set; }

        public static implicit operator EComment(Comment c)
        {
            return new EComment
            {
                Id = c.Id,
                ReplyTo = c.ReplyTo?.Id ?? 0,
                RecordId = c.ImageRecord?.Id,
                RecipeId = c.Recipe?.Id,
                Text = c.Text,
                AuthorName = c.CreatedBy?.UserName,
                CreatedTime = c.CreatedTime,
                Flag = c.Flag,
                Hidden = c.Hidden,
                HighPriority = c.HighPriority,
                TaskCompleted = c.TaskCompleted
            };
        }

        public static EComment ToShallowEComment(Comment c)
        {
            return new EComment()
            {
                Text = c.Text,
                AuthorName = c.CreatedBy?.UserName,
                Flag = c.Flag,
                Hidden = c.Hidden,
                HighPriority = c.HighPriority,
                TaskCompleted = c.TaskCompleted
            };
        }
    }
}