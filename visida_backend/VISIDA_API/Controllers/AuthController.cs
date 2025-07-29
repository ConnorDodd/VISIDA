using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Web;
using System.Web.Http;
using VISIDA_API.Models;
using VISIDA_API.Models.User;
using System.Data.Entity;

//This just extends ApiController and provides a couple of variables I use in every controller, like retrieving the calling account from the DB and making a context

namespace VISIDA_API.Controllers
{
    [Authorize(Roles = "admin,analyst,coordinator")]
    public class AuthController : ApiController
    {
        protected VISIDA_APIContext db = new VISIDA_APIContext();
        protected LoginUser user;
        protected bool IsAdmin { get { return AccessLevel == EnumRole.Admin; } }
        public enum EnumRole { AppUser, Analyst, Coordinator, Admin }
        protected EnumRole AccessLevel;

        public Stopwatch timer = new Stopwatch();

        public AuthController()
        {
            timer.Start();
            if (string.IsNullOrEmpty(User.Identity.Name))
                throw new Exception("Session has expired. Please log in again");
            int id = int.Parse(User.Identity.Name);
            user = db.Users.Include(x => x.Role).Include(x => x.Assignments).Include(y => y.Assignments.Select(z => z.Study)).FirstOrDefault(x => x.Id == id);

            string role = user.Role.Role;
            if (role.Equals("admin"))
                AccessLevel = EnumRole.Admin;
            else if (role.Equals("coordinator"))
                AccessLevel = EnumRole.Coordinator;
            else if (role.Equals("analyst"))
                AccessLevel = EnumRole.Analyst;
            else if (role.Equals("appuser"))
                AccessLevel = EnumRole.AppUser;


            user.LastSeen = DateTime.Now;
            db.SaveChanges();
        }
    }
}