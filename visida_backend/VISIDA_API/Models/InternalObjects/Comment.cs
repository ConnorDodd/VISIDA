using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.User;

namespace VISIDA_API.Models.InternalObjects
{
    public class Comment
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        public virtual ImageRecord ImageRecord { get; set; }
        public virtual CookRecipe Recipe { get; set; }
        public virtual LoginUser CreatedBy { get; set; }
        public string Text { get; set; }
        public DateTime CreatedTime { get; set; } = DateTime.Now;

        public Comment ReplyTo { get; set; }
        public virtual IList<Comment> Replies { get; set; }

        public enum FlagTypes {Normal, Reply, Task, FromApp};
        public FlagTypes Flag { get; set; }

        public bool HighPriority { get; set; }
        public bool Hidden { get; set; } = false;

        //Task
        [NotMapped]
        public bool IsTask { get
            {
                return Flag == FlagTypes.Task;
            }
        }
        public DateTime? TaskCompleted { get; set; }
    }
}