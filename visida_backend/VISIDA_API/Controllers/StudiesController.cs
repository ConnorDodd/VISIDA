using SendGrid;
using SendGrid.Helpers.Mail;
using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Data.Entity.Infrastructure;
using System.Data.SqlClient;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using System.Timers;
using System.Web.Http;
using System.Web.Http.Description;
using VISIDA_API.Models;
using VISIDA_API.Models.ExternalObjects;
using VISIDA_API.Models.FCT;
using VISIDA_API.Models.InternalObjects;
using VISIDA_API.Models.User;
using VISIDA_API.Providers;

namespace VISIDA_API.Controllers
{
    [Authorize(Roles = "admin,coordinator,analyst")]
    [TimingActionFilter]
    public class StudiesController : AuthController
    {
        // GET: api/Studies
        public ICollection<EStudy> GetStudies()
        {
            List<Study> studies = new List<Study>();
            //studies = db.Studies
            //        .Include(x => x.Assignees).Include(x => x.Assignees.Select(y => y.LoginUser)).Include(x => x.Households).Include(x => x.Households.Select(y => y.HouseholdMembers))
            //        .Include(x => x.FoodCompositionTable).Include(x => x.RDAModel)
            //        .Where(x => x.DeletedTime == null).ToList();

            if (AccessLevel == EnumRole.Coordinator)
                studies = db.WorkAssignations.Include(x => x.LoginUser).Include(x => x.Study).Include(x => x.Study.Assignees).Include(x => x.Study.Assignees.Select(y => y.LoginUser))
                    .Include(x => x.Study.Households).Include(x => x.Study.Households.Select(y => y.HouseholdMembers)).Include(x => x.Study.FoodCompositionTable)
                    .Include(x => x.Study.RDAModel)
                    .Where(x => x.LoginUser.Id == user.Id && x.AccessLevel == WorkAssignation.AccessLevels.Coordinator && x.Study.DeletedTime == null).Select(x => x.Study).ToList();
            //studies = user.Assignments.Where(x => x.AccessLevel == WorkAssignation.AccessLevels.Coordinator && x.Study.DeletedTime == null).Select(x => x.Study).ToList();
            else
                studies = db.Studies
                    .Include(x => x.Assignees).Include(x => x.Assignees.Select(y => y.LoginUser)).Include(x => x.Households).Include(x => x.Households.Select(y => y.HouseholdMembers))
                    .Include(x => x.FoodCompositionTable).Include(x => x.RDAModel)
                    .Where(x => x.DeletedTime == null).ToList();


            List<EStudy> toreturn = new List<EStudy>();
            timer.Start();
            foreach (var s in studies)
            {
                toreturn.Add(new EStudy()
                {
                    Id = s.Id,
                    Name = s.Name,
                    Analysts = s.Assignees.Select(x => (EWorkAssignation)x).ToList(),// Select(x => x.LoginUser).Select(x => new ELoginUser() { Id = x.Id, UserName = x.UserName, x. }).ToList(),
                    Households = s.Households.Select(x => new EHousehold()
                    {
                        Id = x.Id,
                        Guid = x.Guid,
                        ParticipantId = x.ParticipantId,
                        Country = x.Country,
                        HouseholdMembers = x.HouseholdMembers.Select(y => (EHouseholdMember)y).ToList(),
                    }).ToList(),
                    FoodCompositionTable = s.FoodCompositionTable == null ? null : new EFoodCompositionTable()
                    {
                        Id = s.FoodCompositionTable.Id,
                        Name = s.FoodCompositionTable.Name
                    },
                    DeletedTime = s.DeletedTime,
                    CountryCode = s.CountryCode,
                    Transcribe = s.Transcribe,
                    Translate = s.Translate,
                    Gestalt = s.Gestalt,
                    RDAModel = s.RDAModel == null ? null : new ERDAModel()
                    {
                        Description = s.RDAModel.Description,
                        Id = s.RDAModel.Id,
                        FieldData = s.RDAModel.FieldData,
                        Name = s.RDAModel.Name,
                    },
                    RDAModelId = s.RDAModel?.Id
                });
            }
            //timings.Add(timer.ElapsedMilliseconds); timer.Restart();
            //foreach (Study s in studies)
            //{
            //    toreturn.Add(EStudy.ToShallowEStudy(s));
            //    timings.Add(timer.ElapsedMilliseconds);
            //    timer.Restart();
            //}
            return toreturn;
        }

        // GET: api/Studies/5
        [Authorize(Roles = "admin,coordinator,analyst")]
        [ResponseType(typeof(EStudy))]
        public IHttpActionResult GetStudy(int id)
        {
            Study study = db.Studies
                .Include(x => x.Assignees).Include(x => x.Assignees.Select(y => y.LoginUser))
                .Include(x => x.Households).Include(x => x.Households.Select(y => y.HouseholdMembers))
                .Include(x => x.Households.Select(y => y.ImageRecords)).Include(x => x.Households.Select(y => y.ImageRecords.Select(z => z.FoodItems)))
                .Include(x => x.Households.Select(y => y.HouseholdAssignments)).Include(x => x.Households.Select(y => y.HouseholdAssignments.Select(z => z.LoginUser)))
                .Include(x => x.FoodCompositionTable)
                .Include(x => x.RDAModel).Include(x => x.RDAModel.RDAs)
                .FirstOrDefault(x => x.Id == id);
            if (study == null || study.DeletedTime != null) //if not there or deleted
            {
                return NotFound();
            }

            EStudy e = study;

            return Ok(e);
        }

        // PUT: api/Studies/5
        [Authorize(Roles = "admin,coordinator")]
        [ResponseType(typeof(void))]
        public IHttpActionResult PutStudy(int id, EStudy study)
        {
            Study dbstudy = db.Studies.FirstOrDefault(x => x.Id == id);
            if (dbstudy == null)
                return BadRequest();

            if (AccessLevel == EnumRole.Admin)
            {
                //Change name
                if (db.Studies.Count(x => x.Name.Equals(study.Name) && x.Id != study.Id) > 0)
                    return BadRequest("A study with that name already exists.");
                dbstudy.Name = study.Name;

                dbstudy.CountryCode = study.CountryCode;
                dbstudy.Transcribe = study.Transcribe;
                dbstudy.Translate = study.Translate;
                dbstudy.Gestalt = study.Gestalt;
                dbstudy.GestaltMax = study.GestaltMax;
                if (study.RDAModelId == null)
                    dbstudy.RDAModel = null;
                else
                    dbstudy.RDAModel = db.RDAModels.Find(study.RDAModelId);
            }
            else if (AccessLevel == EnumRole.Coordinator)
            {
                //Coord can't rename


                //Only set if currently null
                if (dbstudy.CountryCode == null)
                    dbstudy.CountryCode = study.CountryCode;

                //These can be changed at will as it doesn't modify existing data
                dbstudy.Transcribe = study.Transcribe;
                dbstudy.Translate = study.Translate;
                dbstudy.Gestalt = study.Gestalt;
                dbstudy.GestaltMax = study.GestaltMax;
                if (study.RDAModelId != null)
                    dbstudy.RDAModel = db.RDAModels.Find(study.RDAModelId);
            }

            //Can only set once
            if (study.FoodCompositionTable != null && dbstudy.FoodCompositionTable_Id == null)
            {
                AssignFoodCompositionDatabase(dbstudy, study.FoodCompositionTable.Id);
            }

            try
            {
                db.SaveChanges();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!StudyExists(id))
                {
                    return NotFound();
                }
                else
                {
                    throw;
                }
            }

            return StatusCode(HttpStatusCode.NoContent);
        }

        private void AssignFoodCompositionDatabase(Study study, int copyId)
        {
            FoodCompositionTable database = null;
            if (copyId > 0)
                database = db.FoodCompositionTables.Include(x => x.FoodCompositions).FirstOrDefault(x => x.Id == copyId);
            else
                database = new FoodCompositionTable() { Id = 0, Name = "Custom Database", FoodCompositions = new List<FoodComposition>() };

            var name = string.Format("{0} - {1}", database.Name, study.Name);
            FoodCompositionTable table = new FoodCompositionTable()
            {
                Name = name,
                FoodCompositions = new List<FoodComposition>(),
                StandardMeasures = new List<StandardMeasure>(),
                ReferenceImages = new List<ReferenceImageType>()
            };

            foreach (var fc in database.FoodCompositions)
            {
                FoodComposition nfc = fc.Clone();
                nfc.Table_Id = null;
                nfc.Table = null;
                table.FoodCompositions.Add(nfc);
            }

            foreach (var sm in database.StandardMeasures)
            {
                StandardMeasure measure = new StandardMeasure()
                {
                    MLs = sm.MLs,
                    Name = sm.Name
                };
                table.StandardMeasures.Add(measure);
            }

            List<ReferenceImageType> references = new List<ReferenceImageType>();
            var dbReferences = db.ReferenceImageTypes.Where(x => x.Table.Id == copyId);
            foreach (var rit in dbReferences)
            {
                var r = new ReferenceImageType()
                {
                    Name = rit.Name,
                    Table = null,
                    Images = new List<ReferenceImage>()
                };

                foreach (var ri in rit.Images)
                {
                    r.Images.Add(new ReferenceImage()
                    {
                        Description = ri.Description,
                        ImageUrl = ri.ImageUrl,
                        OriginId = ri.OriginId,
                        SizeGrams = ri.SizeGrams
                    });
                }

                table.ReferenceImages.Add(r);
            }

            study.FoodCompositionTable = table;
        }

        // POST: api/Studies
        [Authorize(Roles = "admin,coordinator")]
        [ResponseType(typeof(Study))]
        public IHttpActionResult PostStudy(Study study)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }
            if (db.Studies.Count(x => x.Name.Equals(study.Name)) > 0)
                return BadRequest("A study with that name already exists.");

            db.Studies.Add(study);
            //if (AccessLevel == EnumRole.Coordinator) 
            //{
            //    user.Assignments.Add(new WorkAssignation()
            //    {
            //        AccessLevel = WorkAssignation.AccessLevels.Coordinator,
            //        Study = study
            //    });
            //}
            db.SaveChanges();
            if (AccessLevel == EnumRole.Coordinator)
            {
                //If the user is a coordinator, they must be assigned to their own study.
                study.Assignees = new List<WorkAssignation>();
                study.Assignees.Add(new WorkAssignation()
                {
                    AccessLevel = WorkAssignation.AccessLevels.Coordinator,
                    LoginUser = user
                });
                db.SaveChanges();
            }

            return CreatedAtRoute("DefaultApi", new { id = study.Id }, new { study.Id });
        }

        // DELETE: api/Studies/5
        [ResponseType(typeof(Study)), Authorize(Roles = "admin")]
        public IHttpActionResult DeleteStudy(int id)
        {
            Study study = db.Studies.Find(id);
            if (study == null)
                return NotFound();


            study.DeletedTime = DateTime.Now;
            string delStr = "_DELETED_" + study.DeletedTime?.ToString("HH:mm:ss");
            study.Name += delStr;
            //study.DeletedBy = user;

            db.WorkAssignations.RemoveRange(study.Assignees);

            foreach (var hh in study.Households)
            {
                hh.Guid += delStr;
                hh.ParticipantId += delStr;
            }

            foreach (var ir in study.Households.SelectMany(x => x.ImageRecords))
            {
                ir.ImageName += delStr;
                ir.AudioName += delStr;
            }
            db.SaveChanges();

            return Ok(study);
        }

        [Route("api/Studies/Progress"), HttpGet]
        [Authorize(Roles = "admin,coordinator")]
        public async Task<IHttpActionResult> GetStudiesProgress()
        {
            Stopwatch timer = new Stopwatch();
            timer.Start();
            List<long> timers = new List<long>();
               
            List<Study> studies = new List<Study>();
            //studies = db.Studies
            //        .Include(x => x.Assignees).Include(x => x.Assignees.Select(y => y.LoginUser)).Include(x => x.Households).Include(x => x.Households.Select(y => y.HouseholdMembers))
            //        .Include(x => x.FoodCompositionTable).Include(x => x.RDAModel)
            //        .Where(x => x.DeletedTime == null).ToList();

            if (AccessLevel == EnumRole.Coordinator)
            {
                int[] studyIds = user.Assignments.Select(x => x.Study.Id).ToArray();

                studies = db.Studies
                    .Include(x => x.Assignees).Include(x => x.Assignees.Select(y => y.LoginUser))
                    .Include(x => x.Households)
                    .Include(x => x.FoodCompositionTable)
                    .Where(x => x.DeletedTime == null && studyIds.Contains(x.Id)).ToList();
            }
            else
                studies = db.Studies.Where(x => x.DeletedTime == null)
                    .Include(x => x.Assignees).Include(x => x.Assignees.Select(y => y.LoginUser))
                    .Include(x => x.Households)
                    .Include(x => x.FoodCompositionTable)
                    //.Include(x => x.Households.Select(y => y.ImageRecords)).Include(x => x.Households.Select(y => y.ImageRecords.Select(z => z.FoodItems)))
                    .ToList();
                //studies = db.Studies
                //    .Include(x => x.Households)
                //    .Include(x => x.Households.Select(y => y.HouseholdMembers))
                //    .Include(x => x.FoodCompositionTable).Include(x => x.RDAModel)
                //    .Include(x => x.Households.Select(y => y.ImageRecords)).Include(x => x.Households.Select(y => y.ImageRecords.Select(z => z.FoodItems)))
                //    .Where(x => x.DeletedTime == null).ToList();

            timers.Add(timer.ElapsedMilliseconds);
            timer.Restart();

            List<EStudy> toreturn = new List<EStudy>();
            foreach (var s in studies)
            {

                toreturn.Add(new EStudy()
                {
                    Id = s.Id,
                    Name = s.Name,
                    Analysts = s.Assignees.Select(x => (EWorkAssignation)x).ToList(),// Select(x => x.LoginUser).Select(x => new ELoginUser() { Id = x.Id, UserName = x.UserName, x. }).ToList(),
                    Households = s.Households.Select(x => new EHousehold()
                    {
                        Id = x.Id,
                        Guid = x.Guid,
                        ParticipantId = x.ParticipantId,
                        Country = x.Country,
                        //HouseholdMembers = x.HouseholdMembers.Select(y => (EHouseholdMember)y).ToList(),
                        //WorkDone = x.WorkDone,
                        //RecordTotal = x.RecordTotal,
                        //RecordNotStarted = x.RecordNotStarted,
                        //IdentifyInProgress = x.IdentifyInProgress,
                        //IdentifyCompleted = x.IdentifyCompleted,
                        //PortionInProgress = x.PortionInProgress,
                        //PortionCompleted = x.PortionCompleted
                    }).ToList(),
                    FoodCompositionTable = s.FoodCompositionTable == null ? null : new EFoodCompositionTable()
                    {
                        Id = s.FoodCompositionTable.Id,
                        Name = s.FoodCompositionTable.Name
                    },
                    DeletedTime = s.DeletedTime,
                    CountryCode = s.CountryCode,
                    Transcribe = s.Transcribe,
                    Translate = s.Translate,
                    Gestalt = s.Gestalt,
                    //RDAModel = s.RDAModel == null ? null : new ERDAModel()
                    //{
                    //    Description = s.RDAModel.Description,
                    //    Id = s.RDAModel.Id,
                    //    FieldData = s.RDAModel.FieldData,
                    //    Name = s.RDAModel.Name,
                    //},
                    //RDAModelId = s.RDAModel?.Id
                });

                timers.Add(timer.ElapsedMilliseconds);
                timer.Restart();
            }

            var connection = (System.Data.SqlClient.SqlConnection)db.Database.Connection;
            if (connection != null && connection.State == ConnectionState.Closed)
            {
                connection.Open();
            }

            string sql = "SELECT h.*, a.total, b.notStarted, c.identifyCompleted " +
"FROM Households h " +
"CROSS APPLY " +
"( " +
"	SELECT COUNT(*) AS 'Total' " +
"	FROM ImageRecords r " +
"	WHERE r.Household_Id = h.Id " +
"	AND r.Hidden = 0 " +
") AS a " +
"CROSS APPLY " +
"( " +
"	SELECT COUNT(*) AS 'NotStarted' " +
"	FROM ImageRecords r " +
"	WHERE r.Household_Id = h.Id " +
"	AND r.IsCompleted = 0 " +
"	AND r.Hidden = 0 " +
"	AND (SELECT COUNT(*) FROM FoodItems f WHERE f.ImageRecordId = r.Id) = 0 " +
") AS b " +
"CROSS APPLY " +
"( " +
"	SELECT COUNT(*) AS 'IdentifyCompleted' " +
"	FROM ImageRecords r " +
"	WHERE r.Household_Id = h.Id " +
"	AND r.IsCompleted = 1 " +
"	AND r.Hidden = 0 " +
") AS c ";

            using (SqlCommand command = new SqlCommand(sql, connection))
            {
                command.CommandType = CommandType.Text;

                using (SqlDataReader results = command.ExecuteReader())
                {
                    while (results.Read())
                    {
                        int sid = results["Study_Id"] as int? ?? 0,
                            hid = results["Id"] as int? ?? 0;
                        foreach (var s in toreturn)
                        {
                            if (s.Id != sid)
                                continue;
                            foreach (var h in s.Households)
                            {
                                if (h.Id == hid)
                                {
                                    h.RecordTotal = results["Total"] as int? ?? 0;
                                    h.RecordNotStarted = results["NotStarted"] as int? ?? 0;
                                    h.IdentifyCompleted = results["IdentifyCompleted"] as int? ?? 0;
                                    h.IdentifyInProgress = h.RecordTotal - h.RecordNotStarted - h.IdentifyCompleted;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
            }

                timers.Add(timer.ElapsedMilliseconds);
            return Ok(toreturn);
        }

        [Route("api/Studies/{studyid}/Assign")]
        [Authorize(Roles = "admin,coordinator")]
        public async Task<IHttpActionResult> PostAssignUser(int studyid, [FromBody] UserAssignment assignment)
        {
            var study = db.Studies.Find(studyid);
            LoginUser target = null;
            if (assignment.Id > 0)
                target = db.Users.Find(assignment.Id);
            else if (assignment.Email != null)
                target = db.Users.FirstOrDefault(x => x.Email.Equals(assignment.Email));
            if (target == null && assignment.Email != null)
            {
                var regex = new Regex(@"(?<![\w\d])[\w\d]{1}");
                string username = regex.Replace(assignment.Email, x => x.Value.ToUpper());
                username = username.Substring(0, username.IndexOf('@'));
                username = Regex.Replace(username, "[^a-zA-Z0-9]", "");
                target = new LoginUser()
                {
                    Email = assignment.Email,
                    UserName = username,
                    Role = db.UserRoles.FirstOrDefault(x => x.Role.Equals("analyst"))
                };
                await new AccountController().Register(target);
                //user = db.Users.FirstOrDefault(x => x.Email.Equals(assignment.Email));
            }
            if (study == null || target == null)
                return BadRequest();
            var existingUser = study.Assignees.FirstOrDefault(x => x.LoginUser.Id == assignment.Id || (x.LoginUser.Email?.Equals(assignment.Email) ?? false));

            //WorkAssignation.AccessLevels accessEnum;
            //Enum.TryParse(accessLevel, out accessEnum);

            if (existingUser != null)
                existingUser.AccessLevel = assignment.AccessLevel;
            else
            {
                study.Assignees.Add(new WorkAssignation()
                {
                    AccessLevel = assignment.AccessLevel,
                    LoginUser = target//db.Users.FirstOrDefault(x => assignment.Email.Equals(x.Email))
                });

                if (target.Email != null)
                {
                    string html = EmailManager.GetEmailTemplate("study_invite_email.html");
                    string url = EmailManager.ActiveHost + "/#!/login";
                    html = html.Replace("{{url}}", url);
                    html = html.Replace("{{study}}", study.Name);
                    html = html.Replace("{{from}}", user.UserName);
                    html = html.Replace("{{role}}", Enum.GetName(typeof(WorkAssignation.AccessLevels), assignment.AccessLevel));

                    string fallback = "You have been invited to the VISIDA study " + study.Name + " by " + user.UserName + ". Follow this link to view study: " + url;
                    Response response = await EmailManager.SendEmail(target.Email, "Study Invite: " + study.Name + " - VISIDA", html, fallback);
                }
            }

            try
            {
                db.SaveChanges();

            }
            catch (Exception ex)
            {
                return Ok();
            }
            return Ok(new { target.Id, target.UserName });
        }
        public class UserAssignment
        {
            public string Email { get; set; }
            public int Id { get; set; }
            public WorkAssignation.AccessLevels AccessLevel { get; set; }
        }

        [Route("api/Studies/{studyid}/AssignHousehold")]
        [Authorize(Roles = "admin,coordinator")]
        public IHttpActionResult PostAssignUserHousehold(int studyid, [FromBody] HouseholdAssignment assignment)
        {
            LoginUser loginUser = db.Users.Find(assignment.AnalystId);
            Study study = db.Studies.Find(studyid);
            // Check they have priveleges to make the changes
            bool authorized = false;
            if (AccessLevel == EnumRole.Admin)
            {
                authorized = true;
            }
            else if (AccessLevel == EnumRole.Coordinator)
            {
                // Check that they are assigned to the relevant study
                foreach (var studyAssignees in study.Assignees)
                {
                    if (user.Assignments.Contains(studyAssignees))
                    {
                        authorized = true;
                    }
                }
                    
            }
            if (!authorized)
            {
                return Unauthorized();
            }
            EHouseholdAssignation eHouseholdAssignation = null;
            // Might be multiple HHIDs that match the Household Name
            // Make sure the household is assigned to this study and also that the household has matching participant id (name)
            int[] householdIds = db.Households.Where(x => x.ParticipantId == assignment.ParticipantId && x.Study_Id == studyid).Select(x => x.Id).ToArray();
            Household[] households = db.Households.Where(x => x.ParticipantId == assignment.ParticipantId && x.Study_Id == studyid).ToArray();
            // New Assignment scenario
            if (assignment.Assign)
            {
                for (int i = 0; i < households.Length; i++)
                {
                    HouseholdAssignation householdAssignation = new HouseholdAssignation()
                    {
                        LoginUser = loginUser
                    };
                    households[i].HouseholdAssignments.Add(householdAssignation);
                    eHouseholdAssignation = householdAssignation;
                }
            } else
            {
                // Delete all household assignations that match this criteria
                db.HouseholdAssignations.RemoveRange(db.HouseholdAssignations.Where(x => x.LoginUser.Id == assignment.AnalystId && householdIds.Contains(x.Household.Id)));
            }
            try
            {
                db.SaveChanges();
            }
            catch (Exception ex)
            {
                return InternalServerError(ex);
            }
            return Ok(eHouseholdAssignation);
        }

        public class HouseholdAssignment
        {
            public int HouseholdId { get; set; }
            public string ParticipantId { get; set; }
            public int AnalystId { get; set; }
            public bool Assign { get; set; }
        }

        [Route("api/Studies/{studyid}/Reinvite/{userid}")]
        [Authorize(Roles = "admin,coordinator")]
        public async Task<IHttpActionResult> GetReinviteUser(int studyid, int userid)
        {
            var study = db.Studies.Find(studyid);
            var user = db.Users.Find(userid);
            if (study == null || user == null)
                return BadRequest();

            if (string.IsNullOrEmpty(user.Email))
                return BadRequest("User has no email");

            var assignment = study.Assignees.FirstOrDefault(x => x.LoginUser.Id == userid);
            string html = EmailManager.GetEmailTemplate("study_invite_email.html");
            string url = EmailManager.ActiveHost + "/#!/login";
            html = html.Replace("{{url}}", url);
            html = html.Replace("{{study}}", study.Name);
            html = html.Replace("{{from}}", user.UserName);
            html = html.Replace("{{role}}", Enum.GetName(typeof(WorkAssignation.AccessLevels), assignment.AccessLevel));

            string fallback = "You have been invited to the VISIDA study " + study.Name + " by " + user.UserName + ". Follow this link to view study: " + url;
            Response response = await EmailManager.SendEmail(user.Email, "Study Invite: " + study.Name + " - VISIDA", html, fallback);

            return Ok();
        }

        [Route("api/Studies/{studyid}/UnAssign/{userid}")]
        [Authorize(Roles = "admin,coordinator")]
        public IHttpActionResult PostUnAssignUser(int studyid, int userid)
        {
            var study = db.Studies.Find(studyid);
            var user = db.Users.Find(userid);
            if (study == null || user == null)
                return BadRequest();

            var assignation = study.Assignees.FirstOrDefault(x => x.LoginUser.Id == userid);
            //study.Assignees.Remove(assignation);
            //user.Assignments.Remove(assignation);
            db.WorkAssignations.Remove(assignation);

            db.SaveChanges();
            return Ok();
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                db.Dispose();
            }
            base.Dispose(disposing);
        }

        private bool StudyExists(int id)
        {
            return db.Studies.Count(e => e.Id == id) > 0;
        }
    }
}