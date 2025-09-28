using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using VISIDA_API.Models.ExternalObjects;
using VISIDA_API.Models.InternalObjects;
using VISIDA_API.Models.User;
using System.Data.Entity;
using static VISIDA_API.Models.InternalObjects.WorkAssignation;
using VISIDA_API.Models.Comparators;
using VISIDA_API.Models;


using Newtonsoft.Json;
using System.Data;
using System.Web;
using System.Diagnostics;

namespace VISIDA_API.Controllers
{
    public class FeedController : AuthController
    {
        [Route("api/User/Feed")]
        public IHttpActionResult GetUserFeed()
        {
            EUserFeed feed = new EUserFeed();

            long time = 0;
            Stopwatch stopwatch = new Stopwatch();
            stopwatch.Start();

            feed.Comments = GetComments(user) ?? new List<EComment>();

            time = stopwatch.ElapsedMilliseconds;

            if (IsAdmin)
            {
                feed.Users = db.Users.OrderByDescending(x => x.LastSeen).ToList().Select(x => (ELoginUser)x).ToList();
                //feed.Messages = GetMessages();
            }
            else if (AccessLevel == EnumRole.Coordinator)
            {
                feed.Users = user.Assignments.Where(x => x.AccessLevel == AccessLevels.Coordinator).SelectMany(x => x.Study.Assignees).Select(x => x.LoginUser)
                    .Distinct().OrderByDescending(x => x.LastSeen).ToList().Select(x => (ELoginUser)x).ToList();
            }

            HttpContext.Current.Response.Headers.Add("lastLogin", user.LastFeedRefresh?.ToString("yyyy-MM-ddTHH:mm:ss") ?? "0001-01-01");
            HttpContext.Current.Response.Headers.Add("Access-Control-Expose-Headers", "lastLogin");

            user.LastFeedRefresh = DateTime.Now;
            db.SaveChanges();

            return Ok(feed);
        }

        //[Route("api/User/AdminMessages"), Authorize(Roles = "admin,coordinator")]
        //public IHttpActionResult GetUserAdminMessages(int skip = 0, int take = 10)
        //{
        //    return Ok(GetMessages(skip, take));
        //}

        //private List<AdminMessage> GetMessages(int skip = 0, int take = 10)
        //{
        //    var list = db.AdminMessages.OrderByDescending(x => x.CreatedTime).Skip(skip).Take(take);
        //    return list.ToList();
        //}

        [Route("api/User/Comments")]
        public IHttpActionResult GetUserComments([FromUri] int skip = 0, int take = 10)
        {
            return Ok(GetComments(user, skip, take));
        }

        private List<EComment> GetComments(LoginUser user, int skip = 0, int take = 10)
        {
            string mention = "@" + user.UserName;

            IQueryable<Comment> queryComments = null;
            if (AccessLevel == EnumRole.Analyst)
            {
                queryComments = db.Comments
                    .Include(x => x.ImageRecord).Include(x => x.Recipe).Include(x => x.CreatedBy)
                    .Where(x =>
                    x.CreatedBy.Id != user.Id && //it's not by me
                    (
                        x.Text.Contains(mention) || //I am mentioned
                        x.ReplyTo.CreatedBy.Id == user.Id || //or it's a reply to me
                        x.ReplyTo.Replies.Count(y => y.CreatedBy.Id == user.Id) > 0 //or it's a reply to a comment i've interacted with
                    )
                );
            }
            else if (AccessLevel == EnumRole.Coordinator)
            {
                queryComments = db.Comments.Where(x =>
                    x.CreatedBy.Id != user.Id && //it's not by me
                    (
                        x.Text.Contains(mention) || //I am mentioned
                        x.ReplyTo.CreatedBy.Id == user.Id || //or it's a reply to me
                        x.ReplyTo.Replies.Count(y => y.CreatedBy.Id == user.Id) > 0 || //or it's a reply to a comment i've interacted with
                        (
                            (x.HighPriority || x.Flag == Comment.FlagTypes.Task) &&//or it's important
                            (
                                (x.ImageRecord != null && x.ImageRecord.Household.Study.Assignees.Count(z => z.LoginUser.Id == user.Id) > 0) || //and assigned to me
                                (x.Recipe != null && x.Recipe.Household.Study.Assignees.Count(z => z.LoginUser.Id == user.Id) > 0)
                            )
                        )
                    )
                );
            }
            //var ids = user.Assignments.Where(x => x.AccessLevel == AccessLevels.Coordinator).Select(x => x.Study.Id).ToList();
            var all = queryComments?.OrderByDescending(x => x.CreatedTime).ToList();
            var comments = all?.Select(x => (EComment)x).ToList();
            comments = comments?.Distinct(new CommentComparator()).ToList();

            return comments;
        }

        //private List<EComment> GetComments(LoginUser user, int skip = 0, int take = 10)
        //{
        //    IQueryable<Comment> allComments = db.Comments.Include(x => x.ImageRecord).Include(x => x.Recipe).Include(x => x.CreatedBy)
        //        .Include(x => x.ImageRecord.Household).Include(x => x.ImageRecord.Household.Study);

        //    var mine = allComments.Where(x => x.CreatedBy.Id == user.Id); //Get all comments I've made
        //    var replies = mine.SelectMany(x => x.Replies).ToList(); //Get all replies to comments I've made
        //    //var qsd = replies.ToList();
        //    //mine.Where(x => x.ReplyTo != null && x.ReplyTo.CreatedBy.Id != user.Id) .Where(x => x.CreatedBy.Id != user.Id)Changed from this to show own comments
        //    var chains = mine.Where(x => x.ReplyTo != null).SelectMany(x => x.ReplyTo.Replies).ToList(); //Get all replies to comments i've replied to
        //    //var qsagafd = chains.ToList();

        //    List<Comment> extras = new List<Comment>();
        //    if (AccessLevel == EnumRole.Admin)
        //        extras = allComments.Where(x => x.HighPriority).ToList();
        //    if (AccessLevel == EnumRole.Coordinator)
        //    {
        //        var ids = user.Assignments.Where(x => x.AccessLevel == AccessLevels.Coordinator).Select(x => x.Study.Id).ToList();
        //        extras = allComments.Where(x => (x.CreatedBy.Role.Id == 1 || x.HighPriority) && ids.Contains(x.ImageRecord.Household.Study.Id)).ToList();
        //    }
        //    if (AccessLevel == EnumRole.Analyst)
        //    {
        //        var ids = user.Assignments.Select(x => x.Study.Id).ToList();
        //        extras = allComments.Where(x => x.CreatedBy.Role.Id == 1 && ids.Contains(x.ImageRecord.Household.Study.Id)).ToList();
        //    }
        //    //var ajhsbda = extras.ToList();

        //    extras.AddRange(replies);
        //    extras.AddRange(chains);
        //    List<EComment> list = new List<EComment>();
        //    foreach (var comment in extras)
        //    {
        //        list.Add(comment);
        //    }
        //    list = list.Distinct(new CommentComparator()).ToList();
        //    list = list.OrderByDescending(x => x.CreatedTime).ToList(); //this should be done in the database, TODO fix this whole method

        //    return list;
        //}
    }
}
