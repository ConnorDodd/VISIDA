using VISIDA_API.Models.ExternalObjects;
using VISIDA_API.Models.InternalObjects;
using VISIDA_API.Models.User;
using static VISIDA_API.Models.InternalObjects.WorkAssignation;

namespace VISIDA_API.Controllers
{
    public class UsersController : AuthController
    {
        [Route("api/GetSearchConfig")]
        public IHttpActionResult GetSearchConfig()
        {
            List<string> countries = new List<string>();
            List<HouseholdConfig> households = new List<HouseholdConfig>();
            List<StudyConfig> studies = new List<StudyConfig>();
            if (IsAdmin)
            {
                var hhs = db.Households.Where(x => x.Study.DeletedTime == null);
                households = AssignHouseholdDays(hhs);

                countries = hhs.GroupBy(r => r.Country).Select(grp => grp.FirstOrDefault()).Select(h => h.Country).ToList();
                studies = db.Studies.Include(x => x.Households).Where(x => x.DeletedTime == null)
                    .Select(x => new StudyConfig
                    {
                        Name = x.Name,
                        Id = x.Id,
                        Households = x.Households
                    .Select(y => new HouseholdConfig() { Id = y.Id, Name = y.ParticipantId }).GroupBy(y => y.Name).Select(y => y.FirstOrDefault()).ToList()
                    }).ToList();

                households = households.GroupBy(x => x.Name).Select(x => new HouseholdConfig() { Id = x.First().Id, Name = x.First().Name, Days = x.SelectMany(y => y.Days).OrderBy(y => y.Date).ToArray(), HouseholdMembers = x.SelectMany(h => h.HouseholdMembers).Distinct() }).ToList();
            }
            else
            {
                var assignedStudies = db.WorkAssignations.Where(x => x.LoginUser.Id == user.Id && x.Study.DeletedTime == null).Select(x => x.Study).Include(x => x.Households).ToList();
                //var assignedHouseholds = db.WorkAssignations.Where(x => x.LoginUser.Id == user.Id && x.Study.DeletedTime == null).SelectMany(x => x.Study.Households);
                var assignedHouseholdIds = db.HouseholdAssignations.Where(x => x.LoginUser.Id == user.Id).Select(x => x.Household.Id).ToList();
                var assignedHouseholds = db.WorkAssignations.Where(x => x.LoginUser.Id == user.Id && x.Study.DeletedTime == null).SelectMany(x => x.Study.Households.Where(y => x.AccessLevel == AccessLevels.Coordinator || assignedHouseholdIds.FirstOrDefault(z => y.Id == z) > 0));
                //var assignedHouseholds = user.HouseholdAssignments.Select(x => x.Household);
                households = AssignHouseholdDays(assignedHouseholds);
                //var query = (from ta in assignedHouseholds.SelectMany(x => x.ImageRecords)
                //             select new { id = ta.Household.Id, date = System.Data.Entity.DbFunctions.TruncateTime(ta.CaptureTime) }).Distinct();
                //var dates = query.ToList();
                //households = assignedHouseholds.Select(h => new HouseholdConfig() { Id = h.Id, Name = h.ParticipantId }).ToList();//.Distinct(new HouseholdConfigComparer()).ToList();
                //households.ForEach(h => h.Days = dates.Where(x => x.id == h.Id).Select(x => x.date).ToArray() ?? new DateTime?[0]);
                countries = assignedHouseholds.GroupBy(x => x.Country).Select(grp => grp.FirstOrDefault()).Select(x => x.Country).ToList();
                studies = assignedStudies.Select(x => new StudyConfig { Name = x.Name, Id = x.Id, Households = x.Households.Select(y => new HouseholdConfig() { Id = y.Id, Name = y.ParticipantId }).GroupBy(y => y.Name).Select(y => y.FirstOrDefault()).OrderBy(y => y.Name).ToList() }).ToList();

                households = households.GroupBy(x => x.Name).Select(x => new HouseholdConfig() { Id = x.First().Id, Name = x.First().Name, Days = x.SelectMany(y => y.Days).OrderBy(y => y.Date).ToArray(), HouseholdMembers = x.SelectMany(h => h.HouseholdMembers).Distinct() }).ToList();
                studies.ForEach(x => x.Households = x.Households.Where(y => households.FirstOrDefault(z => z.Id == y.Id) != null).ToList());
            }
            

            EatImageRecordConfig settings = new EatImageRecordConfig
            {
                Countries = countries,
                Households = households.OrderBy(x => x.Name).ToList(),
                Studies = studies,
            };

            return Ok(settings);
        }


        private static List<HouseholdConfig> AssignHouseholdDays(IQueryable<Household> dbHH)
        {
            List<HouseholdConfig> households = new List<HouseholdConfig>();
            var query = (from ta in dbHH.SelectMany(x => x.ImageRecords)
                         select new DateConfig() { Name = ta.Household.ParticipantId, Date = System.Data.Entity.DbFunctions.TruncateTime(ta.CaptureTime) ?? new DateTime() }).Distinct();
            var dates = query.ToList();

            households = dbHH.Select(h => new HouseholdConfig() { Id = h.Id, Name = h.ParticipantId, HouseholdMembers = h.HouseholdMembers.Select(hm => hm.ParticipantId) }).ToList();
            households.ForEach(h => h.Days = dates.Where(x => x.Name.Equals(h.Name)).Select(x => x.Date).ToArray());
            return households;
        }
        private class DateConfig { public string Name { get; set; } public DateTime Date { get; set; } }

        [Route("api/GetSearchUsers")]
        public IHttpActionResult GetSearchUsers()
        {
            List<ELoginUser> users = new List<ELoginUser>();
            if (IsAdmin)
                users = db.Users.Select(x => new ELoginUser() { UserName = x.UserName, Id = x.Id }).ToList();
            else
                users = user.Assignments.SelectMany(x => x.Study.Assignees).Select(x => x.LoginUser).Distinct().OrderBy(x => x.UserName).Select(x => new ELoginUser() { UserName = x.UserName, Id = x.Id }).ToList();

            return Ok(users);
        }




        [Route("api/User/{id}/Progress")]
        public IHttpActionResult GetUserProgress(int id)
        {
            var dbAssignments = db.WorkAssignations.Include(x => x.LoginUser).Include(x => x.Study).Include(x => x.Study.Households)
                .Include(x => x.Study.Households.Select(y => y.ImageRecords)).Include(x => x.Study.Households.Select(y => y.ImageRecords.Select(z => z.FoodItems)))
                .Where(x => x.LoginUser.Id == id && x.Study.DeletedTime == null);
            var assignments = new List<object>();
            foreach (var assign in dbAssignments)
            {
                var households = assign.Study.Households;

                switch (assign.AccessLevel)
                {
                    case AccessLevels.Coordinator:
                    case AccessLevels.Both:
                    case AccessLevels.Identify:
                        assignments.Add(new
                        {
                            accessLevel = Enum.GetName(typeof(AccessLevels), assign.AccessLevel),
                            studyName = assign.Study.Name,
                            studyId = assign.Study.Id,
                            total = households.Sum(x => x.RecordTotal),
                            notStarted = households.Sum(x => x.RecordNotStarted),
                            inProgress = households.Sum(x => x.IdentifyInProgress)
                        });
                        break;
                    case AccessLevels.Quantify:
                        assignments.Add(new
                        {
                            accessLevel = Enum.GetName(typeof(AccessLevels), assign.AccessLevel),
                            studyName = assign.Study.Name,
                            studyId = assign.Study.Id,
                            total = households.Sum(x => x.RecordTotal),
                            notStarted = households.Sum(x => x.RecordNotStarted),
                            inProgress = households.Sum(x => x.PortionInProgress)
                        });
                        break;
                    case AccessLevels.View:
                        break;
                    default:
                        break;
                }
            }

            return Ok(assignments);
        }

        [Route("api/User/{id}/Timings"), Authorize(Roles = "admin,coordinator")]
        public IHttpActionResult GetUserTimings(int id)
        {
            IList<ETiming> timings = null;
            if (AccessLevel == EnumRole.Coordinator)
                timings = user.Assignments.Where(x => x.AccessLevel == AccessLevels.Coordinator).SelectMany(x => x.Study.Timings.Where(y => y.CreatedBy.Id == id).Select(y => (ETiming)y)).ToList();
            else
                timings = db.Timings.Where(x => x.CreatedBy.Id == id).Select(x => (ETiming)x).ToList();
            return Ok(timings);
        }

        // GET: api/Users
        [Authorize(Roles = "admin,coordinator")]
        public IHttpActionResult GetUsers()
        {
            IEnumerable<LoginUser> users = null;
            if (AccessLevel == EnumRole.Coordinator)
                users = user.Assignments.Where(x => x.Study != null && x.LoginUser != null).Where(x => x.AccessLevel == AccessLevels.Coordinator).SelectMany(x => x.Study.Assignees.Select(y => y.LoginUser)).Distinct();
            else
                users = db.Users.Include("Role");

            var objs = users.Select(x => new { x.Id, x.UserName, x.Role.Role, x.Email, x.IsActive, x.LastLogin }).ToList(); //, x.AssignedHouseholds

            return Ok(objs);
        }

        [Route("api/Roles"), Authorize(Roles = "admin,coordinator")]
        public IHttpActionResult GetRoles()
        {
            return Ok(db.UserRoles.Select(x => new { x.Id, x.Role}).ToList());
        }

        //GET: api/Users/5
        [ResponseType(typeof(LoginUser)), Authorize(Roles = "admin,coordinator")]
        public IHttpActionResult GetLoginUser(int id)
        {
            LoginUser loginUser = db.Users
                .Include(x => x.Assignments).Include(x => x.Assignments.Select(y => y.Study))
                .Include(x => x.Assignments.Select(y => y.Study.Households))
                .Include(x => x.Assignments.Select(y => y.Study.Households.Select(z => z.ImageRecords)))
                .Include(x => x.Assignments.Select(y => y.Study.Households.Select(z => z.ImageRecords.Select(a => a.FoodItems))))
                .Include(x => x.Tests)
                .Include(x => x.Tests.Select(y => y.Scores))
                .Include(x => x.Tests.Select(y => y.ImageRecord)).Include(x => x.Tests.Select(y => y.ImageRecord.FoodItems))
                .Include(x => x.Tests.Select(y => y.KnownRecord)).Include(x => x.Tests.Select(y => y.KnownRecord.FoodItems))
                .FirstOrDefault(x => x.Id == id);
            if (loginUser == null)
                return NotFound();
            if (AccessLevel == EnumRole.Coordinator)
            {
                var coordinating = user.Assignments.Select(x => x.Study.Id);
                var assign = loginUser.Assignments.FirstOrDefault(x => coordinating.Contains(x.Study.Id));
                if (assign == null)
                    return BadRequest();
            }
                //loginUser = user.Assignments.Where(x => x.AccessLevel == AccessLevels.Coordinator).SelectMany(x => x.Study.Assignees).FirstOrDefault(x => x.LoginUser?.Id == id)?.LoginUser;
            //else
                //loginUser = db.Users.Find(id);


            ELoginUser euser = loginUser;    
            List<EStudy> studies = new List<EStudy>();
            foreach (var assign in loginUser.Assignments)
            {
                if (AccessLevel != EnumRole.Admin && user.Assignments.FirstOrDefault(x => x.Study?.Id == assign.Study?.Id) == null)
                    continue;
                //var study = (EStudy)assign.Study;
                var study = new EStudy()
                {
                    Id = assign.Study.Id,
                    Name = assign.Study.Name,
                    Households = new List<EHousehold>(),//assign.Study.Households.Select(x => (EHousehold)x).ToList(),
                    DeletedTime = assign.Study.DeletedTime,
                    CountryCode = assign.Study.CountryCode,
                    Transcribe = assign.Study.Transcribe,
                    Translate = assign.Study.Translate,
                    Gestalt = assign.Study.Gestalt,
                };
                foreach (var h in assign.Study.Households)
                {
                    study.Households.Add(new EHousehold()
                    {
                        Id = h.Id,
                        Guid = h.Guid,
                        ParticipantId = h.ParticipantId,
                        Country = h.Country,
                        RecordTotal = h.RecordTotal,
                        RecordNotStarted = h.RecordNotStarted,
                        IdentifyInProgress = h.IdentifyInProgress,
                        IdentifyCompleted = h.IdentifyCompleted,
                        PortionInProgress = h.PortionInProgress,
                        PortionCompleted = h.PortionCompleted
                    });
                }
                //study.WorkDone = assign.Study.Timings.Select(x => (ETiming)x).ToList();
                study.AccessLevel = assign.AccessLevel;
                studies.Add(study);
            }
            euser.Studies = studies;
            euser.Tests = loginUser.Tests.Select(x => (EReliabilityTest)x).OrderByDescending(x => x.AssignedTime).ToList();
            euser.TestRules = db.ReliabilityTestRules.Where(x => x.User_Id == loginUser.Id).ToList();

            return Ok(euser);
        }

        [Authorize(Roles = "admin")]
        [System.Web.Mvc.HttpPost, Route("api/Users/AssignRole")]
        public IHttpActionResult AssignRole([FromBody] Dictionary<string, string> dictionary)
        {
            int userId = int.Parse(dictionary["userId"]);
            string roleName = dictionary["role"];
            LoginUser user = db.Users.Where(x => x.Id == userId).FirstOrDefault();
            LoginUserRole role = db.UserRoles.FirstOrDefault(x => x.Role.Equals(roleName));
            if (user == null || role == null)
                return BadRequest();
            user.Role = role;
            db.SaveChanges();

            return Ok();
        }

        [Authorize(Roles = "admin")]
        [System.Web.Mvc.HttpPost, Route("api/Users/{id}/ActivateUser")]
        public IHttpActionResult ActivateUser([FromUri] int id, [FromUri] bool active)
        {
            LoginUser user = db.Users.Find(id);
            if (user == null)
                return NotFound();
            user.IsActive = active;
            db.SaveChanges();

            return Ok();
        }


        // DELETE: api/Users/5
        [Authorize(Roles = "admin")]
        [ResponseType(typeof(LoginUser))]
        public IHttpActionResult DeleteLoginUser(int id)
        {
            LoginUser loginUser = db.Users.Find(id);
            if (loginUser == null)
            {
                return NotFound();
            }

            db.Users.Remove(loginUser);
            db.SaveChanges();

            return Ok(loginUser);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                db.Dispose();
            }
            base.Dispose(disposing);
        }

        private bool LoginUserExists(int id)
        {
            return db.Users.Count(e => e.Id == id) > 0;
        }
    }
}