using Microsoft.AspNet.Identity;
using Microsoft.AspNet.Identity.EntityFramework;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Web;
using VISIDA_API.Models.InternalObjects;
using VISIDA_API.Models.InternalObjects.Testing;

namespace VISIDA_API.Models.User
{
    public class AuthRepository : IDisposable
    {
        private VISIDA_APIContext ctx;

        private UserManager<LoginUser, int> manager;

        public AuthRepository()
        {
            ctx = new VISIDA_APIContext();
            manager = new UserManager<LoginUser, int>(ctx);
        }

        public async Task<LoginUser> RegisterUserAsync(LoginUser user)
        {
            var dupe = await manager.FindByNameAsync(user.UserName, user.Email);
            if (dupe != null)
                 return null;
            user.Role = ctx.UserRoles.FirstOrDefault(x => x.Role.Equals(user.Role.Role));
            if (user.Role == null)
                return null;

            await manager.CreateAsync(user);

            return await FindUserAsync(user.UserName);
        }

        public async Task<LoginUser> FindUserAsync(int userId)
        {
            return await manager.FindByIdAsync(userId);
        }

        public async void ResetPassword(LoginUser user, string password)
        {
            manager.ResetPassword(user, password);
        }

        public async Task<string> CreatePasswordReset(string userName, bool lenientResetTime = false)
        {
            var user = await manager.FindByNameAsync(userName);
            DateTime time = DateTime.Now;
            if (lenientResetTime)
                time = time.AddYears(1);
            ResetPasswordRequest rpr = new ResetPasswordRequest()
            {
                Key = Guid.NewGuid().ToString().Replace("-", ""),
                LoginUser = user,
                CreatedTime = time
            };

            ctx.ResetPasswordRequests.Add(rpr);
            ctx.SaveChanges();
            return rpr.Key;
        }

        public async Task<LoginUser> FindUserAsync(string userName, string password)
        {
            var user = await manager.FindByNameAsync(userName);
            if (user == null)
                return null;
            if (new PasswordHasher().VerifyHashedPassword(user.Password, user.Salt + password) == PasswordVerificationResult.Success)
            {
                try
                {
                    var now = DateTime.Now;
                    user.LastLogin = now;

                    var rules = ctx.ReliabilityTestRules
                        .Where(x => x.User_Id == user.Id && now > x.StartDate).ToList();
                        //&& (x.Tests.Count == 0) || (now > x.Tests.OrderByDescending(y => y.AssignedTime).FirstOrDefault().ImageRecord.CaptureTime.AddDays(x.RepeatInterval)));

                    foreach (var rule in rules)
                    {
                        //DateTime? lastDate = null; //Create a new test if the old one is too old. Creates too many if the user only logs in sporadically
                        //if (rule.Tests != null)
                        //    lastDate = rule.Tests.OrderByDescending(x => x.AssignedTime).FirstOrDefault()?.ImageRecord.CaptureTime;
                        //var interval = (int)(rule.RepeatDate.Value - rule.StartDate).TotalDays;
                        //if (lastDate.HasValue && now < lastDate.Value.AddDays(interval))
                        //    break;

                        var lastTest = rule.Tests.OrderByDescending(x => x.AssignedTime).FirstOrDefault(x => !x.Deleted);
                        if (!lastTest.ImageRecord.IsCompleted)//Skip if there's an unfinished test for this rule
                            break;
                        var lastDate = rule.Tests?.OrderByDescending(x => x.AssignedTime).FirstOrDefault()?.AssignedTime;
                        var interval = (int)(rule.RepeatDate.Value - rule.StartDate).TotalDays;
                        if (lastDate.HasValue && now < lastDate.Value.AddDays(interval))
                            break;

                        var possibleRecords = ctx.Studies.Find(rule.Study_Id).Households.SelectMany(x => x.ImageRecords).Where(x => x.IsCompleted).ToList();
                        int count = possibleRecords.Count() - 1;
                        int rand = new Random().Next(0, count > 0 ? count : 0);
                        var known = possibleRecords.Skip(rand).Take(1).FirstOrDefault();
                        if (known == null)
                            break;

                        var record = new ImageRecord()
                        {
                            ImageName = string.Format("ReliabilityTest_{0}_{1}", user.Id, now.ToString("dd/MM/yy_HH:mm")),
                            ImageUrl = known.ImageUrl,
                            AudioUrl = known.AudioUrl,
                            CaptureTime = now,
                            Homography = new ImageHomography(known.Homography),
                            RecordType = ImageRecord.RecordTypes.Test,
                            NTranscript = known.NTranscript,
                            Transcript = known.Transcript,
                            TextDescription = known.TextDescription,
                            IsFiducialPresent = known.IsFiducialPresent,
                        };
                        if (rule.TestType == ReliabilityTest.TestTypes.Quantify)
                        {
                            record.FoodItems = new List<FoodItem>();
                            foreach (var item in known.FoodItems)
                            {
                                record.FoodItems.Add(new FoodItem()
                                {
                                    CreateStart = now,
                                    CreateEnd = now,
                                    FoodCompositionId = item.FoodCompositionId,
                                    Name = item.Name,
                                    TagXPercent = item.TagXPercent,
                                    TagYPercent = item.TagYPercent
                                });
                            }
                        }

                        var test = new ReliabilityTest()
                        {
                            KnownRecord = known,
                            ImageRecord = record,
                            AssignedTo = user,
                            AssignedTime = DateTime.Now,
                            TestType = rule.TestType
                        };

                        if (rule.Tests == null)
                            rule.Tests = new List<ReliabilityTest>();
                        rule.Tests.Add(test);
                    }
                    ctx.SaveChanges();
                }
                catch (Exception e)
                {
                }

                return user;
            }
            return null;
        }

        public async Task<LoginUser> FindUserAsync(string userName)
        {
            return await manager.FindByNameAsync(userName);
        }

        public void Dispose()
        {
            ctx.Dispose();
            manager.Dispose();
        }

    }
}