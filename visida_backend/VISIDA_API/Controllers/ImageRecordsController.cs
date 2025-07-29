using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Data.Entity.Infrastructure;
using System.Diagnostics;
using System.Globalization;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web;
using System.Web.Http;
using System.Web.Http.Description;
using VISIDA_API.Models;
using VISIDA_API.Models.ExternalObjects;
using VISIDA_API.Models.FCT;
using VISIDA_API.Models.InternalObjects;
using VISIDA_API.Models.InternalObjects.Testing;
using VISIDA_API.Models.OpenCV;
using VISIDA_API.Models.StringUtils;
using VISIDA_API.Models.User;
using VISIDA_API.Providers;

namespace VISIDA_API.Controllers
{
    [TimingActionFilter]
    public class ImageRecordsController : AuthController
    {
        public const string DATE_FORMAT = "yyyy-MM-dd";

        private static RecordHistoryComparer comp = new RecordHistoryComparer();
        private IQueryable<ImageRecord> SearchRecords()
        {
            //Get initial result set depending on user type
            IQueryable<ImageRecord> records = null;
            if (IsAdmin)
            {
                records = db.ImageRecords
                    .Where(x => x.Household.Study.DeletedTime == null);
            }
            else
            {
                // Retrieve all the householdId's from studies that the user has been assigned to
                int[] coordWorkAssignations = db.WorkAssignations.Where(x => x.LoginUser.Id == user.Id && x.Study.DeletedTime == null && x.AccessLevel == WorkAssignation.AccessLevels.Coordinator).SelectMany(x => x.Study.Households.Select(y => y.Id)).ToArray();
                int[] nonCoordWorkAssignations = db.WorkAssignations.Where(x => x.LoginUser.Id == user.Id && x.Study.DeletedTime == null && x.AccessLevel != WorkAssignation.AccessLevels.Coordinator).SelectMany(x => x.Study.Households.Select(y => y.Id)).ToArray();
                //int[] assigned = user.Assignments.Select(x => x.Study).SelectMany(x => x.Households).Select(h => h.Id).ToArray();
                // Retrieve all the householdId's from the households that the user has been assigned to - where they are not coordinator
                int[] householdAssignations = db.HouseholdAssignations.Where(x => x.LoginUser.Id == user.Id && nonCoordWorkAssignations.Contains(x.Household.Id)).Select(x => x.Household.Id).ToArray();

                // Query the households where they are coordinator
                IQueryable<ImageRecord> records1 = db.ImageRecords
                .Where(x => coordWorkAssignations.Contains(x.Household.Id) && x.Household.Study.DeletedTime == null);

                // Query the households where they are not coordinator
                IQueryable<ImageRecord> records2 = db.ImageRecords
                .Where(x => householdAssignations.Contains(x.Household.Id) && x.Household.Study.DeletedTime == null);
                records2 = records2.Where(x => !x.Hidden);

                records = records1.Concat(records2);
            }


            string srid = HttpContext.Current.Request.Params["recipe"];
            int rid;
            if (srid != null && int.TryParse(srid, out rid))
                records = db.CookRecipes.Where(x => x.Id == rid).SelectMany(x => x.Ingredients.Select(y => y.ImageRecord));

            //Search by RecordType if exists
            string rts = HttpContext.Current.Request.Params["recordType"];
            ImageRecord.RecordTypes rt;
            if (rts != null && rts.Equals("EatRecordLeftovers"))
                records = records.Where(x => x.RecordType == ImageRecord.RecordTypes.EatRecord || x.RecordType == ImageRecord.RecordTypes.Leftovers);
            else if (rts != null && Enum.TryParse(rts, out rt))
                records = records.Where(x => x.RecordType == rt);

            //Search by Study if exists
            string sss = HttpContext.Current.Request.Params["Study"];
            int iss;
            if (sss != null && int.TryParse(sss, out iss))
                records = records.Where(x => x.Household.Study_Id == iss);

            //Search by Household if exists
            string hh = HttpContext.Current.Request.Params["Household"];
            if (hh != null)
                records = records.Where(x => x.Household.ParticipantId.Equals(hh));

            //Search by HouseholdMember if exists
            string hm = HttpContext.Current.Request.Params["Member"];
            if (hm != null)
                records = records.Where(x => x.EatRecords.FirstOrDefault(y => !y.Hidden && y.EatOccasion.HouseholdMember.ParticipantId.Equals(hm)) != null);
            //records = records.Where(x => x.Household.HouseholdMembers.FirstOrDefault(y => y.EatingOccasions.FirstOrDefault(z => z.EatRecords.FirstOrDefault(er => er.ImageRecord.Id == x.Id) != null) != null) != null);
            //records = records.Where(x => x.Household.HouseholdMembers.FirstOrDefault(y => y.ParticipantId.Equals(hm)) != null);

            //Search by Identified status
            string identStr = HttpContext.Current.Request.Params["Identified"];
            int identInt = 0;
            if (identStr != null && int.TryParse(identStr, out identInt))
            {
                switch (identInt)
                {
                    case -1: //Has no records
                        records = records.Where(x => x.FoodItems.Count == 0 && x.IsCompleted == false);
                        break;
                    case 0: //Has unfinished records
                        records = records.Where(x => x.FoodItems.Count > 0 && x.IsCompleted == false);
                        break;
                    case 1: //Has no unfinished records
                        records = records.Where(x => x.IsCompleted);
                        break;
                    case 2: //Has unfinished or no records
                        records = records.Where(x => !x.IsCompleted);
                        break;
                }
            }

            //Search by Portioned status
            string prtStr = HttpContext.Current.Request.Params["Portioned"];
            int prtInt = 0;
            if (prtStr != null && int.TryParse(prtStr, out prtInt))
            {
                //switch (prtInt)
                //{

                //    case -1: //No items have quantification
                //        if (AccessLevel == EnumRole.Analyst)
                //            records = records.Where(x => x.FoodItems.Count(f => f.Updates.Count(u => u.Action == RecordHistory.UpdateTypes.Quantify && (u.User.Id == user.Id || (u.User.Role.Id == 1 || u.User.Role.Id == 4))) > 0) == 0 &&
                //            (
                //                x.FoodItems.Count == 0 ||
                //                x.FoodItems.Count(f => f.Updates.Where(u => u.Action == RecordHistory.UpdateTypes.Quantify).GroupBy(u => u.User.Id).Count() == x.Household.Study.GestaltMax) < x.FoodItems.Count()
                //            ));
                //        else
                //            records = records.Where(x => x.FoodItems.Count(f => f.QuantityGrams > 0) == 0);//.Where(x => x.FoodItems.Count == 0 && !x.IsCompleted);
                //        break;
                //    case 0: //Has unfinished records
                //        if (AccessLevel == EnumRole.Analyst)
                //            records = records.Where(x =>
                //                //x.Updates.Count(u => u.Action == RecordHistory.UpdateTypes.Quantify && (u.User.Id == user.Id || (u.User.Role.Id == 1 || u.User.Role.Id == 4))) > 0 &&
                //                x.FoodItems.Count(f => f.QuantityGrams > 0) > 0 &&
                //                x.FoodItems.Count(f => f.Updates.Count(u => u.Action == RecordHistory.UpdateTypes.Quantify && (u.User.Id == user.Id || (u.User.Role.Id == 1 || u.User.Role.Id == 4))) > 0 ||
                //                    f.Updates.Where(u => u.Action == RecordHistory.UpdateTypes.Quantify).GroupBy(u => u.User.Id).Count() >= x.Household.Study.GestaltMax) < x.FoodItems.Count());
                //        else
                //            records = records.Where(x =>
                //                x.Updates.Count(u => u.Action == RecordHistory.UpdateTypes.Quantify) > 0 &&
                //                x.FoodItems.Count(f => f.Updates.Count(u => u.Action == RecordHistory.UpdateTypes.Quantify && (u.User.Role.Id == 1 || u.User.Role.Id == 4)) > 0 ||
                //                    f.Updates.Where(u => u.Action == RecordHistory.UpdateTypes.Quantify).GroupBy(u => u.User.Id).Count() >= x.Household.Study.GestaltMax) < x.FoodItems.Count());
                //        //records = records.Where(x => /*Add a check for NOT gestalt here*/x.Updates.Where(u => u.Action == RecordHistory.UpdateTypes.Quantify)
                //        //.GroupBy(u => u.FoodItemId).Count(u => u.GroupBy(f => f.User.Id).Count() == 1 && u.Count(f => f.User.Role.Id == 1 || f.User.Role.Id == 4) == 0) > 1);
                //        break;
                //    case 1: //Has no unfinished records
                //        if (AccessLevel == EnumRole.Analyst)
                //            records = records.Where(x =>
                //                x.FoodItems.Count() > 0 &&
                //                x.FoodItems.Count(f => f.Updates.Count(u => u.Action == RecordHistory.UpdateTypes.Quantify && (u.User.Id == user.Id || (u.User.Role.Id == 1 || u.User.Role.Id == 4))) > 0 ||
                //                    f.Updates.Where(u => u.Action == RecordHistory.UpdateTypes.Quantify).GroupBy(u => u.User.Id).Count() >= x.Household.Study.GestaltMax) == x.FoodItems.Count());
                //        else
                //            records = records.Where(x =>
                //                x.FoodItems.Count() > 0 &&
                //                x.FoodItems.Count(f => f.Updates.Count(u => u.Action == RecordHistory.UpdateTypes.Quantify && (u.User.Role.Id == 1 || u.User.Role.Id == 4)) > 0 ||
                //                    f.Updates.Where(u => u.Action == RecordHistory.UpdateTypes.Quantify).GroupBy(u => u.User.Id).Count() >= x.Household.Study.GestaltMax) == x.FoodItems.Count());
                //        break;
                //}
                switch (prtInt)
                {

                    case -1: //In progress
                        //There is 0 items, OR the number of items that I or an admin have quantified, OR reached gestalt, is < max items
                        if (AccessLevel == EnumRole.Analyst)
                        {
                            records = records.Where(x => 
                                (!x.IsCompleted && x.FoodItems.Count() == 0) ||
                                x.FoodItems.Count(f => 
                                    f.Updates.Count(u => u.Action == RecordHistory.UpdateTypes.Quantify && (u.User.Id == user.Id || (u.User.Role.Id == 1 || u.User.Role.Id == 4))) > 0 ||
                                    f.Updates.Where(u => u.Action == RecordHistory.UpdateTypes.Quantify).GroupBy(u => u.User.Id).Count() == x.Household.Study.GestaltMax
                                ) < x.FoodItems.Count()
                            );
                        }
                        else if (AccessLevel == EnumRole.Admin || AccessLevel == EnumRole.Coordinator)
                        {
                            records = records.Where(x =>
                                (!x.IsCompleted && x.FoodItems.Count() == 0) ||
                                x.FoodItems.Count(f =>
                                    f.Updates.Count(u => u.Action == RecordHistory.UpdateTypes.Quantify && (u.User.Role.Id == 1 || u.User.Role.Id == 4)) > 0 || //just ignore personal updates
                                    f.Updates.Where(u => u.Action == RecordHistory.UpdateTypes.Quantify).GroupBy(u => u.User.Id).Count() == x.Household.Study.GestaltMax
                                ) < x.FoodItems.Count()
                            );
                        }
                        break;
                    case 1: //Completed
                        if (AccessLevel == EnumRole.Analyst)
                            //There is > 1 items AND I or an admin have made a quantification, OR gestalt reached, on all items 
                            if (AccessLevel == EnumRole.Analyst)
                            {
                                records = records.Where(x =>
                                    (x.IsCompleted || x.FoodItems.Count() > 1) &&
                                    x.FoodItems.Count(f =>
                                        f.Updates.Count(u => u.Action == RecordHistory.UpdateTypes.Quantify && (u.User.Id == user.Id || (u.User.Role.Id == 1 || u.User.Role.Id == 4))) > 0 ||
                                        f.Updates.Where(u => u.Action == RecordHistory.UpdateTypes.Quantify).GroupBy(u => u.User.Id).Count() == x.Household.Study.GestaltMax
                                    ) == x.FoodItems.Count()
                                );
                            }
                            else if (AccessLevel == EnumRole.Admin || AccessLevel == EnumRole.Coordinator)
                            {
                                records = records.Where(x =>
                                    (x.IsCompleted || x.FoodItems.Count() > 1) &&
                                    x.FoodItems.Count(f =>
                                        f.Updates.Count(u => u.Action == RecordHistory.UpdateTypes.Quantify && (u.User.Role.Id == 1 || u.User.Role.Id == 4)) > 0 ||
                                        f.Updates.Where(u => u.Action == RecordHistory.UpdateTypes.Quantify).GroupBy(u => u.User.Id).Count() == x.Household.Study.GestaltMax
                                    ) == x.FoodItems.Count()
                                );
                            }
                        break;
                }
            }

            string searchStr = HttpContext.Current.Request.Params["search"];
            if (!string.IsNullOrWhiteSpace(searchStr))
            {
                searchStr = searchStr.ToLower();
                var searchArray = searchStr.Split(' ');
                //records = records.Where(x => searchArray.All(y => x.FoodItems.SelectMany(z => z.FoodComposition.Name).))
                foreach (var searchTerm in searchArray)
                    records = records.Where(x => x.FoodItems.FirstOrDefault(f => f.FoodComposition != null && f.FoodComposition.Name.ToLower().Contains(searchTerm)) != null);
            }

            string commentStr = HttpContext.Current.Request.Params["comment"];
            if (!string.IsNullOrWhiteSpace(commentStr))
            {
                commentStr = commentStr.ToLower();
                records = records.Where(x => x.Comments.FirstOrDefault(c => /*!c.Hidden &&*/ c.Text.Contains(commentStr)) != null);
            }

            string daysStr = HttpContext.Current.Request.Params["date"];
            if (!string.IsNullOrWhiteSpace(daysStr))
            {
                DateTime[] days = daysStr.Split(',').Select(x => DateTime.ParseExact(x, DATE_FORMAT, CultureInfo.InvariantCulture)).ToArray();
                records = records.Where(x => days.Contains(System.Data.Entity.DbFunctions.TruncateTime(x.CaptureTime) ?? new DateTime()));
            }

            string gvStr = HttpContext.Current.Request.Params["gv"];
            double gvDbl = 0;
            if (!string.IsNullOrEmpty(gvStr) && double.TryParse(gvStr, out gvDbl))
            {
                gvDbl = Math.Abs(gvDbl);
                gvDbl = gvDbl / 100;
                //records = records.Where(x => x.FoodItems.Count(
                //    y =>
                //    y.Updates.Min(u => u.QuantityGrams) > 0 &&
                //    (
                //        y.Updates.Where(u => u.Action == RecordHistory.UpdateTypes.Quantify).GroupBy(u => u.User.Id).Max(u => u.OrderByDescending(z => z.Time).FirstOrDefault().QuantityGrams) -  
                //        y.Updates.Where(u => u.Action == RecordHistory.UpdateTypes.Quantify).GroupBy(u => u.User.Id).Min(u => u.OrderByDescending(z => z.Time).FirstOrDefault().QuantityGrams)
                //    ) /
                //    y.Updates.Where(u => u.Action == RecordHistory.UpdateTypes.Quantify).GroupBy(u => u.User.Id).Max(u => u.OrderByDescending(z => z.Time).FirstOrDefault().QuantityGrams)
                //    > gvDbl
                //    ) > 0);
                records = records.Where(x => x.FoodItems.Count(
                    y =>
                    y.Updates.Min(u => u.QuantityGrams) > 0 &&
                    y.Updates.Count(u => u.Action == RecordHistory.UpdateTypes.Quantify && (u.User.Role.Id == 1 || u.User.Role.Id == 4)) == 0 &&
                    (
                        y.Updates.Where(u => u.Action == RecordHistory.UpdateTypes.Quantify).GroupBy(u => u.User.Id).Max(u => u.OrderByDescending(z => z.Time).FirstOrDefault().QuantityGrams) -
                        y.Updates.Where(u => u.Action == RecordHistory.UpdateTypes.Quantify).GroupBy(u => u.User.Id).Min(u => u.OrderByDescending(z => z.Time).FirstOrDefault().QuantityGrams)
                    ) /
                    (
                        (
                            y.Updates.Where(u => u.Action == RecordHistory.UpdateTypes.Quantify).GroupBy(u => u.User.Id).Max(u => u.OrderByDescending(z => z.Time).FirstOrDefault().QuantityGrams) +
                            y.Updates.Where(u => u.Action == RecordHistory.UpdateTypes.Quantify).GroupBy(u => u.User.Id).Min(u => u.OrderByDescending(z => z.Time).FirstOrDefault().QuantityGrams)
                        ) / 2
                    )
                    > gvDbl
                    ) > 0);
            }

            //Whether record was hidden
            string sHidden = HttpContext.Current.Request.Params["hidden"];
            if (!string.IsNullOrEmpty(sHidden))
            {
                if (sHidden.Equals("1"))
                    records = records.Where(x => x.Hidden == true);
                else if (sHidden.Equals("0"))
                    records = records; // Do nothing for 0, show hidden and unhidden
            }
            else
                records = records.Where(x => x.Hidden == false); //default is hide hidden

            return records;
            //.Include(x => x.FoodItems).Include(x => x.FoodItems.Select(y => y.FoodComposition)).Include(x => x.Household).Include(x => x.Household.HouseholdRecipes)
            //    .Include(x => x.Household).Include(x => x.Household.HouseholdRecipes.Select(y => y.FoodComposition)).Include(x => x.Updates)
            //    .Include(x => x.Household.Study)
        }

        // GET: api/ImageRecords

        private static List<long> _timings = new List<long>();
        private static int count = 0;
        public ICollection<EImageRecord> GetImageRecords([FromUri]PagingModel paging)
        {
            Stopwatch timer = new Stopwatch();
            timer.Start();

            IQueryable<ImageRecord> records;
            records = SearchRecords();
            records.Include(x => x.FoodItems).Include(x => x.FoodItems.Select(y => y.FoodComposition)).Include(x => x.Household).Include(x => x.Household.HouseholdRecipes)
                    .Include(x => x.Household.HouseholdRecipes.Select(y => y.FoodComposition)).Include(x => x.Updates)
                    .Include(x => x.Household.Study).Include(x => x.Comments).Include(x => x.Comments.Select(y => y.CreatedBy));//.Include(x => x.Updates.Select(y => y.User));

            //Order records
            string orderby = HttpContext.Current.Request.Params["order"];
            if ("desc".Equals(orderby))
                records = records.OrderBy(x => x.CaptureTime);
            else
                records = records.OrderByDescending(x => x.CaptureTime);

            //Return page of records with metadata
            paging.TotalCount = records.Count();
            object meta = paging.GetMetadata();
            HttpContext.Current.Response.Headers.Add("paging", JsonConvert.SerializeObject(meta));
            HttpContext.Current.Response.Headers.Add("Access-Control-Expose-Headers", "paging");// TODO Import household ids

            var tests = user.Tests.Where(x => !x.Deleted && !x.ImageRecord.IsCompleted).Select(x => (EImageRecord)x.ImageRecord).ToList();

            try
            {
                var pn = paging.PageNumber;
                var ps = paging.PageSize;
                var a = records.Skip((pn - 1) * ps).Take(ps);
                var b = a.Include(x => x.Comments).Include(x => x.Updates).Include(x => x.Updates.Select(y => y.User)).Include(x => x.Household).Include(x => x.FoodItems).Include(x => x.FoodItems.Select(y => y.FoodComposition))
                    .Include(x => x.Household.Study).Include(x => x.Updates.Select(y => y.User)).Include(x => x.Updates.Select(y => y.User.Role)).ToList();
                //var c = b.Select(x => (EImageRecord)x).ToList();
                //timings.Add(timer.ElapsedMilliseconds); timer.Restart();
                var ret = new List<EImageRecord>();
                var ids = b.Select(x => x.Id);
                var eatRecords = db.EatRecords
                    .Include(x => x.ImageRecord)
                    .Include(x => x.Leftovers)
                    .Include(x => x.EatOccasion)
                    .Include(x => x.EatOccasion.HouseholdMember)
                    .Where(x => ids.Contains(x.ImageRecord.Id) || ids.Contains(x.Leftovers.Id)).ToList();
                foreach (var r in b)
                {
                    List<EatRecord> matchingRecords = new List<EatRecord>();
                    for (int i = 0; i < eatRecords.Count; i++)
                    {
                        var erd = eatRecords[i];
                        if (erd.ImageRecord?.Id == r.Id || erd.Leftovers?.Id == r.Id)
                        {
                            matchingRecords.Add(erd);
                            eatRecords.Remove(erd);
                            i--;
                        }
                    }
                    var imageRecord = new EImageRecord()
                    {
                        Id = r.Id,
                        HouseholdParticipantId = r.Household?.ParticipantId,
                        HouseholdId = r.Household?.Id ?? 0,
                        Hidden = r.Hidden,
                        //Meal = r.Meal,
                        //GuestInfo = r.GuestInfo,
                        CaptureTime = r.CaptureTime,
                        Comments = r.Comments.Select(x => EComment.ToShallowEComment(x)).ToList(),
                        ImageName = r.ImageName,
                        Is24HR = r.Is24HR,
                        ImageUrl = r.ImageUrlUpdated ?? r.ImageUrl,
                        ImageThumbUrl = r.ImageThumbUrl,
                        AudioName = r.AudioName,
                        AudioUrl = r.AudioUrl,
                        TextDescription = r.TextDescription ?? r.Transcript,
                        //Homography = r.Homography,
                        IsFiducialPresent = r.IsFiducialPresent,
                        RecordType = r.RecordType,
                        FoodItems = r.FoodItems.Select(x => (EFoodItem)x).ToList(),
                        NTranscript = r.NTranscript,
                        IsCompleted = r.IsCompleted,
                        LockTimestamp = r.LockTimestamp,
                        ParticipantCount = r.ParticipantCount,
                        ManualTranscript = r.ManualTranscript,
                        Translation = r.Translation,
                        Transcript = r.Transcript,
                        AnnotationStatus = r.AnnotationStatus,
                        Participants = new List<EHouseholdMember>()
                    };
                    foreach (var er in matchingRecords)
                    {
                        if (er.Hidden)
                            continue;
                        imageRecord.Participants.Add(new EHouseholdMember()
                        {
                            Id = er.EatOccasion.HouseholdMember.Id,
                            ParticipantId = er.EatOccasion.HouseholdMember.ParticipantId
                        });
                    }

                    if (r.Household.Study.Gestalt)
                    {
                        imageRecord.GestaltMax = r.Household.Study.GestaltMax;
                        if (AccessLevel == EnumRole.Analyst)
                        {
                            foreach (var fi in imageRecord.FoodItems)
                            {
                                var quantifications = r.Updates.Where(x => x.FoodItemId == fi.Id && x.Action == RecordHistory.UpdateTypes.Quantify).OrderByDescending(x => x.Time).ToList();
                                var res = from elem in quantifications group elem by elem.User.Id into groups select groups.OrderBy(x => x, comp);

                                var count = res.Count();
                                if (count > 0)
                                {
                                    var test = res.Select(x => x.OrderByDescending(y => y, comp).FirstOrDefault()?.QuantityGrams).ToList();
                                    fi.GestaltMinEstimate = res.Min(x => x.OrderByDescending(y => y.Time).FirstOrDefault()?.QuantityGrams ?? 0);
                                    fi.GestaltMaxEstimate = res.Max(x => x.OrderByDescending(y => y.Time).FirstOrDefault()?.QuantityGrams ?? 0);
                                }

                                RecordHistory history = quantifications.Where(x => x.User.Id == user.Id || x.User.Role.Role.Equals("admin") || x.User.Role.Role.Equals("coordinator")).OrderByDescending(x => x.Time).FirstOrDefault();
                                if (history != null)
                                {
                                    fi.QuantityGrams = history.QuantityGrams ?? 0;
                                    fi.MeasureType = history.ToolSource;
                                    fi.MeasureCount = history.ToolMeasure ?? 0;

                                    if (imageRecord.GestaltMax > 0)
                                        fi.GestaltCount = count;
                                    if (history.User.Id != user.Id) // Wasn't creadted by this user i.e. was created by admin
                                        fi.CreatedByAdmin = true;
                                }
                                else if (r.Household.Study.GestaltMax > 0 && count >= r.Household.Study.GestaltMax)
                                {
                                    fi.MeasureType = "Gestalt max reached";
                                    fi.GestaltCount = count;
                                    fi.GestaltLock = true;
                                }
                                else
                                {
                                    fi.MeasureCount = 0;
                                    fi.MeasureType = null;
                                    fi.QuantityGrams = 0;
                                    if (imageRecord.GestaltMax > 0)
                                        fi.GestaltCount = count;
                                }
                            }
                        }
                        else //if (imageRecord.GestaltMax > 0)
                        {
                            foreach (var fi in imageRecord.FoodItems)
                            {
                                var quantifications = r.Updates.Where(x => x.FoodItemId == fi.Id && x.Action == RecordHistory.UpdateTypes.Quantify).OrderByDescending(x => x.Time).ToList();
                                var res = from elem in quantifications group elem by elem.User.Id into groups select groups.OrderByDescending(x => x.Time).ToList();

                                var count = res.Count();
                                if (count > 0)
                                {
                                    //var test = res.Select(x => x.OrderByDescending(y => y.Time).FirstOrDefault()?.QuantityGrams).ToList();
                                    fi.GestaltMinEstimate = res.Min(x => x.OrderByDescending(y => y.Time).FirstOrDefault()?.QuantityGrams ?? 0);
                                    fi.GestaltMaxEstimate = res.Max(x => x.OrderByDescending(y => y.Time).FirstOrDefault()?.QuantityGrams ?? 0);
                                }
//                                    fi.GestaltMinEstimate = res.OrderBy(x => x.OrderByDescending(y => y.Time).FirstOrDefault().QuantityGrams).FirstOrDefault().Qua;

                                if (quantifications.FirstOrDefault(x => x.User.Role.Id == 1 || x.User.Role.Id == 4) != null)
                                    fi.CreatedByAdmin = true;
                                else
                                    fi.GestaltCount = count;
                            }
                        }
                    }

                    ret.Add(imageRecord);
                }
                //var d = b.Select(x => EImageRecord.ToShallowEImageRecord(x)).ToList();
                //var ret = paging.GetPage(records).ToList().Select(x => (EImageRecord)x).ToList();

                //if (tests.Count > 0)
                //ret.Insert(0, tests[0]);

                //var ret = d;
                //timings.Add(timer.ElapsedMilliseconds); timer.Restart();
                timer.Stop();
                _timings.Add(timer.ElapsedMilliseconds);

                return ret;
            }
            catch (Exception e)
            {
                throw;
            }
        }

        public class RecordHistoryComparer : IComparer<RecordHistory>
        {
            public int Compare(RecordHistory x, RecordHistory y)
            {
                return x.Time.CompareTo(y.Time);
            }
        }

        // GET: api/ImageRecords/5
        [ResponseType(typeof(EImageRecord)), HttpGet, Route("api/ImageRecords/{id}")]
        public IHttpActionResult GetImageRecord(int id)
        {
            try
            {
                ImageRecord r = db.ImageRecords.Include(x => x.Comments).Include(x => x.LockedBy).Include(x => x.Meal).Include(x => x.Recipes).Include(x => x.Recipes.Select(y => y.FoodComposition))
                    .Include(x => x.Homography)
                    .Include(x => x.Updates).Include(x => x.Updates.Select(y => y.User)).Include(x => x.Updates.Select(y => y.User.Role)).Include(x => x.FoodItems).Include(x => x.FoodItems.Select(y => y.FoodComposition))
                    .Include(x => x.Household).Include(x => x.Household.Study).Include(x => x.Household.HouseholdRecipes).Include(x => x.Household.HouseholdRecipes.Select(y => y.FoodComposition))
                    .FirstOrDefault(x => x.Id == id);
                if (r == null)
                    return NotFound();

                //DateTime date = db.ImageRecords.Where(x => x.Id == r.Id).Select(x => DbFunctions.TruncateTime(x.CaptureTime)).FirstOrDefault() ?? new DateTime();

                EImageRecord ret = r;

                ret.HouseholdRecipeNames = r.Household.Study.Households.Where(x => x.ParticipantId.Equals(r.Household.ParticipantId)).SelectMany(x => x.HouseholdRecipes).Where(x => !x.Hidden).Select(x => new EHouseholdRecipe { Id = x.FoodComposition?.Id, Name = x.FoodComposition?.Name, AlternateName = x.FoodComposition?.AlternateName, Density = x.FoodComposition?.Density, RecipeId = x.Id }).ToArray();

                //if (AccessLevel == EnumRole.Admin || AccessLevel == EnumRole.Coordinator)
                //{
                //    foreach (var comment in r.Comments)
                //        comment.Seen = true;
                //    //db.AdminMessages.Where(x => x.Type == AdminMessage.ContentType.Flag && x.RefId == id).ToList().ForEach(x => x.see)
                //    db.SaveChanges();
                //}
                WorkAssignation temp = null;
                if (r.Household != null)
                    temp = db.WorkAssignations.FirstOrDefault(x => x.LoginUser.Id == user.Id && x.Study.DeletedTime == null && x.Study.Id == r.Household.Study_Id);
                //user.Assignments.FirstOrDefault(x => x.Study.Id == r.Household?.Study_Id);
                if (temp != null)
                    ret.Assignation = temp;
                else if (!IsAdmin && r.RecordType != ImageRecord.RecordTypes.Test)
                    return Unauthorized();
                if (user != null)
                {
                    DateTime now = DateTime.Now;
                    if (r.LockTimestamp == null || r.LockTimestamp < now.AddMinutes(-30.0))
                    {
                        db.ImageRecords
                            .Where(x => x.LockedBy.Id == user.Id)
                            .ToList()
                            .ForEach(x => { x.LockedBy = null; x.LockTimestamp = null; });
                        r.LockedBy = user;
                        r.LockTimestamp = now;
                        db.SaveChanges();
                    }

                    var records = SearchRecords();

                    ImageRecord previous, next;
                    List<ImageRecord> middling;
                    string orderby = HttpContext.Current.Request.Params["order"];
                    bool desc = "desc".Equals(orderby);
                    if (desc)
                    {
                        previous = records.Where(x => x.CaptureTime < r.CaptureTime).OrderByDescending(x => x.CaptureTime).ThenByDescending(x => x.Id).FirstOrDefault();
                        next = records.Where(x => x.CaptureTime > r.CaptureTime).OrderBy(x => x.CaptureTime).ThenBy(x => x.Id).FirstOrDefault();
                        middling = records.Where(x => x.CaptureTime == r.CaptureTime).ToList();
                    }
                    else
                    {
                        previous = records.Where(x => x.CaptureTime > r.CaptureTime).OrderBy(x => x.CaptureTime).FirstOrDefault();
                        next = records.Where(x => x.CaptureTime < r.CaptureTime).OrderByDescending(x => x.CaptureTime).FirstOrDefault();
                        middling = records.Where(x => x.CaptureTime == r.CaptureTime).ToList();
                    }
                    int len = middling.Count();
                    if (len > 1)
                    {

                        for (int i = 0; i < len; i++)
                        {
                            if (middling[i].Id == id)
                            {
                                if (i > 0)
                                    previous = middling[i - 1];
                                if (i + 1 < len)
                                    next = middling[i + 1];
                                break;
                            }
                        }
                    }

                    object meta = new NearbyPages() { NextId = next != null ? next.Id : 0, PrevId = previous != null ? previous.Id : 0 };
                    HttpContext.Current.Response.Headers.Add("pages", JsonConvert.SerializeObject(meta));
                    HttpContext.Current.Response.Headers.Add("Access-Control-Expose-Headers", "pages");
                }

                //Add components of full meal to suggestions if it's leftovers

                if (r.RecordType == ImageRecord.RecordTypes.Leftovers)
                {
                    var er = db.EatRecords.Include(x => x.ImageRecord).Include(x => x.ImageRecord.FoodItems).Include(x => x.ImageRecord.FoodItems.Select(y => y.FoodComposition))
                        .FirstOrDefault(x => x.Leftovers != null && x.Leftovers.Id == r.Id);
                    ret.FinalizedTime = er?.FinalizeTime;
                    ret.Finalized = er?.Finalized ?? false;
                    ret.ReviewDayAudio = r.ImageName == null && (r.AudioName?.Contains("AUDIOONLY") ?? false);
                    var notEaten = er?.ImageRecord;

                    if (notEaten != null)
                    {
                        ret.LeftoverFromId = notEaten.Id;
                        foreach (var fi in notEaten.FoodItems)
                        {
                            if (ret.FoodItems.FirstOrDefault(x => x.Name.Equals(fi.FoodComposition?.Name)) == null)
                            {
                                if (fi.FoodComposition != null)
                                    ret.Suggestions.Add(new ESuggestion
                                    {
                                        Name = fi.FoodComposition.Name,
                                        ImageRecordId = ret.Id,
                                        Source = ESuggestion.SuggestionSources.Leftover,
                                        FoodCompositionId = fi.FoodCompositionId
                                    });
                            }
                        }
                    }
                }
                else if (r.RecordType == ImageRecord.RecordTypes.EatRecord)
                {
                    //var er = db.EatRecords.Include(x => x.ImageRecord).Include(x => x.ImageRecord.FoodItems).Include(x => x.ImageRecord.FoodItems.Select(y => y.FoodComposition))
                    //.FirstOrDefault(x => x.ImageRecord != null && x.ImageRecord.Id == r.Id);
                    var ers = db.EatRecords
                        .Include(x => x.Leftovers).Include(x => x.EatOccasion).Include(x => x.EatOccasion.HouseholdMember).Where(x => x.ImageRecord.Id == r.Id).ToList();
                    var er = ers.FirstOrDefault();
                    ret.FinalizedTime = er?.FinalizeTime;
                    ret.Finalized = er?.Finalized ?? false;
                    ret.ReviewDayAudio = r.ImageName == null && (r.AudioName?.Contains("AUDIOONLY") ?? false);
                    var leftovers = er?.Leftovers;
                    if (leftovers != null)
                        ret.LeftoverId = leftovers.Id;

                    //If there is prelinked recipes, just use those. Otherwise display likely (recent) recipes
                    if (r.Recipes.Count > 0)
                    {
                        ret.Suggestions.AddRange(r.Recipes.Select(x => new ESuggestion
                        {
                            Name = x.FoodComposition?.Name,
                            ImageRecordId = x.Id,
                            Source = ESuggestion.SuggestionSources.Recipe,
                            ImageUrl = x.ImageUrl,
                            FoodCompositionId = x.FoodComposition.Id
                        }));
                    }
                    else
                    {
                        ret.Suggestions.AddRange(r.Household.HouseholdRecipes
                            .Where(x => x.CaptureTime > r.CaptureTime.AddDays(-1) && !x.Hidden)
                            .Select(x => new ESuggestion
                            {
                                Name = x.FoodComposition?.Name,
                                ImageRecordId = x.Id,
                                Source = ESuggestion.SuggestionSources.Recipe,
                                ImageUrl = x.ImageUrl,
                                FoodCompositionId = x.FoodComposition.Id
                            }));
                    }

                    //var participants = db.EatRecords.ToList().Select(x => x.GetParticipation()).ToList();   //.Select(x => x.EatOccasion.HouseholdMember).ToList().Select(x => (EHouseholdMember)x).ToList();
                    if (AccessLevel == EnumRole.Analyst)
                        ers = ers.Where(x => !x.Hidden).ToList();
                    var participants = ers.Select(x => x.GetParticipation()).ToList();
                    ret.Participants = participants;
                }
                else if (r.RecordType == ImageRecord.RecordTypes.Test)
                {
                    var ass = new EWorkAssignation();
                    if (IsAdmin)
                    {
                        var test = db.ReliabilityTests.FirstOrDefault(x => x.ImageRecord.Id == ret.Id);
                        ret.TableId = test.KnownRecord.Household.Study.FoodCompositionTable_Id;
                        ass.AccessLevel = WorkAssignation.AccessLevels.Both; //TODO INclude
                    }
                    else
                    {
                        var test = user.Tests.FirstOrDefault(x => x.ImageRecord.Id == ret.Id);
                        switch (test.TestType)
                        {
                            case ReliabilityTest.TestTypes.Quantify: ass.AccessLevel = WorkAssignation.AccessLevels.Quantify; break;
                            case ReliabilityTest.TestTypes.Both: ass.AccessLevel = WorkAssignation.AccessLevels.Both; break;
                            default: ass.AccessLevel = WorkAssignation.AccessLevels.Identify; break;
                        }

                        var known = test.KnownRecord;
                        ret.TableId = known.Household.Study.FoodCompositionTable_Id;
                    }
                    ret.Assignation = ass;
                }
                else if (r.RecordType == ImageRecord.RecordTypes.Ingredient)
                {
                    var recipe = db.CookRecipes.FirstOrDefault(x => x.Ingredients.Count(y => y.ImageRecord.Id == r.Id) > 0);
                    ret.RecipeId = recipe.Id;
                    ret.RecipeName = recipe.Name ?? "Unnamed: " + recipe.Id;
                }

                string transcript = ret.TextDescription ?? ret.Transcript;//ret.Transcript ?? ret.TextDescription;
                if (ret.TableId != null)
                {
                    EFoodComposition[] comps = db.FoodCompositions.Where(x => x.Table_Id == ret.TableId).Select(x => new EFoodComposition { Id = x.Id, Name = x.Name, AlternateName = x.AlternateName }).ToArray();
                    if (!String.IsNullOrEmpty(transcript))
                    {
                        //string[] names = db.FoodCompositionTables.Find(ret.TableId).FoodCompositions.Select(x => x.Name).ToArray();
                        ret.TranscriptionGroups = Models.StringUtils.DatabaseMatcher.FindMatches(transcript, comps);
                    }
                    if (!string.IsNullOrEmpty(ret.NTranscript))
                    {
                        //string[] altNames = db.FoodCompositionTables.Find(ret.TableId).FoodCompositions.Where(x => !string.IsNullOrWhiteSpace(x.AlternateName)).Select(x => x.AlternateName).ToArray();
                        ret.NTranscriptionGroups = Models.StringUtils.DatabaseMatcher.FindMatchesNative(ret.NTranscript, comps);
                    }
                }

                //Find the analysts own best guess if gestalt is enabled 
                if (r.Household.Study.Gestalt && AccessLevel == EnumRole.Analyst)
                {
                    foreach (var fi in ret.FoodItems)
                    {
                        var quantifications = r.Updates.Where(x => x.FoodItemId == fi.Id && x.Action == RecordHistory.UpdateTypes.Quantify).OrderByDescending(x => x.Time).ToList();
                        var res = from elem in quantifications group elem by elem.User.Id into groups select groups.OrderBy(x => x, comp);
                        //for (var i = 0; i < quantifications.Count; i++)
                        //{
                        //    for 
                        //}
                        //RecordHistory history = r.Updates.Where(x => (x.User.Id == user.Id || x.User.Role.Role.Equals("admin") || x.User.Role.Role.Equals("coordinator")) && x.FoodItemId == fi.Id && x.Action == RecordHistory.UpdateTypes.Quantify)
                        //    .OrderByDescending(x => x.Time).FirstOrDefault();
                        RecordHistory history = quantifications.Where(x => x.User.Id == user.Id || x.User.Role.Role.Equals("admin") || x.User.Role.Role.Equals("coordinator")).OrderByDescending(x => x.Time).FirstOrDefault();

                        if (history != null)
                        {
                            fi.QuantityGrams = history.QuantityGrams ?? 0;
                            fi.MeasureType = history.ToolSource;
                            fi.MeasureCount = history.ToolMeasure ?? 0;

                            if (history.User.Id != user.Id) // Wasn't creadted by this user i.e. was created by admin
                                fi.CreatedByAdmin = true;

                            fi.GestaltMinEstimate = res.Min(x => x.OrderByDescending(y => y.Time).FirstOrDefault()?.QuantityGrams ?? 0);
                            fi.GestaltMaxEstimate = res.Max(x => x.OrderByDescending(y => y.Time).FirstOrDefault()?.QuantityGrams ?? 0);
                        }
                        else if (r.Household.Study.GestaltMax > 0 && res.Count() >= r.Household.Study.GestaltMax)
                        {
                            //fi.QuantityGrams = 0;
                            fi.MeasureType = "Gestalt max reached";
                            //fi.MeasureCount = 0;

                            fi.GestaltLock = true;
                        }
                        else
                        {
                            fi.MeasureCount = 0;
                            fi.MeasureType = null;
                            fi.QuantityGrams = 0;
                        }
                    }
                }
                else
                {
                    foreach (var fi in ret.FoodItems)
                    {
                        var quantifications = r.Updates.Where(x => x.FoodItemId == fi.Id && x.Action == RecordHistory.UpdateTypes.Quantify).OrderByDescending(x => x.Time).ToList();
                        var res = from elem in quantifications group elem by elem.User.Id into groups select groups.OrderBy(x => x, comp);

                        if (res.Count() > 0)
                        {
                            //if (res.FirstOrDefault(x => x.FirstOrDefault(y => y.User.Role.Id == 1 || y.User.Role.Id == 4) != null) != null)
                            if (quantifications.Where(x => x.User.Role.Role.Equals("admin") || x.User.Role.Role.Equals("coordinator")).OrderByDescending(x => x.Time).Count() > 0)
                                fi.CreatedByAdmin = true;
                            fi.GestaltMinEstimate = res.Min(x => x.OrderByDescending(y => y.Time).FirstOrDefault()?.QuantityGrams ?? 0);
                            fi.GestaltMaxEstimate = res.Max(x => x.OrderByDescending(y => y.Time).FirstOrDefault()?.QuantityGrams ?? 0);
                        }
                    }
                }

                return Ok(ret);
            } catch (Exception e)
            {
                return BadRequest(JsonConvert.SerializeObject(e));
            }
        }

        // PUT: api/ImageRecords/5
        [ResponseType(typeof(void)), HttpPut, Route("api/ImageRecords/{id}")]
        public IHttpActionResult PutImageRecord(int id, EImageRecord imageRecord)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            ImageRecord dbRecord = db.ImageRecords.Include("Household").FirstOrDefault(x => x.Id == imageRecord.Id);
            if (dbRecord == null)
                return BadRequest();
            if (dbRecord.RecordType != ImageRecord.RecordTypes.Test)
            {
                WorkAssignation rights = user.Assignments.FirstOrDefault(x => x.Study == dbRecord.Household.Study);
                if (IsAdmin || rights.AccessLevel == WorkAssignation.AccessLevels.Coordinator)
                {
                    //var meal = dbRecord.Meal;
                    //if (meal != null && imageRecord.Meal != null)
                    //{
                    //    dbRecord.Meal.ModAdultFemaleGuests = imageRecord.Meal.AdultFemaleGuests;
                    //    dbRecord.Meal.ModAdultMaleGuests = imageRecord.Meal.AdultMaleGuests;
                    //    dbRecord.Meal.ModChildGuests = imageRecord.Meal.ChildGuests;
                    //}
                    var guestInfo = dbRecord.GuestInfo;
                    if (guestInfo != null && imageRecord.GuestInfo != null)
                    {
                        dbRecord.GuestInfo.ModAdultFemaleGuests = imageRecord.GuestInfo.AdultFemaleGuests;
                        dbRecord.GuestInfo.ModAdultMaleGuests = imageRecord.GuestInfo.AdultMaleGuests;
                        dbRecord.GuestInfo.ModChildGuests = imageRecord.GuestInfo.ChildGuests;
                    }

                    dbRecord.CaptureTime = imageRecord.CaptureTime;
                    
                    if (dbRecord.RecordType == ImageRecord.RecordTypes.Ingredient && dbRecord.Hidden != imageRecord.Hidden)
                    {
                        dbRecord.Hidden = imageRecord.Hidden;
                        var recipe = db.CookIngredients.Include(x => x.Recipe).Include(x => x.Recipe.Ingredients).Include(x => x.Recipe.Ingredients.Select(y => y.ImageRecord))
                            .Include(x => x.Recipe.Ingredients.Select(y => y.ImageRecord.FoodItems)).FirstOrDefault(x => x.ImageRecord.Id == dbRecord.Id).Recipe;
                        recipe.UpdateFoodComposition(db);
                    }
                    dbRecord.Hidden = imageRecord.Hidden;
                }
                dbRecord.IsFiducialPresent = imageRecord.IsFiducialPresent;

                //dbRecord.TextDescription = imageRecord.TextDescription;
                //dbRecord.ManualTranscript = imageRecord.ManualTranscript;
                //dbRecord.Translation = imageRecord.Translation;
            }
            else
            {
                var test = user.Tests.FirstOrDefault(x => x.ImageRecord.Id == imageRecord.Id);
                if (!dbRecord.IsCompleted && imageRecord.IsCompleted)
                {
                    db.AdminMessages.Add(new AdminMessage()
                    {
                        Message = "User " + user.UserName + " has completed a reliability test.",
                        RefId = test.Id,
                        Type = AdminMessage.ContentType.Test
                    });

                    test.RecalculateAccuracy();
                }
            }
            dbRecord.IsCompleted = imageRecord.IsCompleted;

            try
            {
                db.SaveChanges();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!ImageRecordExists(id))
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


        [ResponseType(typeof(void)), Route("api/ImageRecords/{id}"), HttpPost]
        public IHttpActionResult HideImageRecord([FromUri] int id, [FromBody] bool hide)
        {
            ImageRecord record = db.ImageRecords.Find(id);
            record.Hidden = hide;
            db.SaveChanges();
            return Ok();
        }


        // POST: api/ImageRecords
        [ResponseType(typeof(EImageRecord))]
        public IHttpActionResult PostImageRecord(ImageRecord imageRecord)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            db.ImageRecords.Add(imageRecord);
            db.SaveChanges();

            return CreatedAtRoute("DefaultApi", new { id = imageRecord.Id }, imageRecord);
        }

        [Route("api/ImageRecords/{id}/Comments")]
        public IHttpActionResult PostImageRecordComment(int id, [FromBody] EComment c)
        {
            ImageRecord record = db.ImageRecords.Find(id);
            if (record == null)
                return BadRequest();
            var comment = new Comment()
            {
                Text = c.Text,
                Flag = c.Flag,
                CreatedBy = user,
                HighPriority = c.HighPriority
            };
            if (c.ReplyTo > 0)
            {
                var replyTo = db.Comments.Find(c.ReplyTo);
                if (replyTo == null)
                    return BadRequest();
                comment.ReplyTo = replyTo;
            }
            record.Comments.Add(comment);
            db.SaveChanges();

            return Ok(comment.Id);
        }

        [Route("api/Comments/{id}"), HttpDelete]
        public IHttpActionResult DeleteImageRecordComment([FromUri] int id)
        {
            Comment comment = db.Comments.Find(id);
            comment.Hidden = !comment.Hidden;
            db.SaveChanges();

            return Ok();
        }
        [Route("api/Comments/{id}"), HttpPut]
        //For task updating
        public IHttpActionResult PutImageRecordComment([FromUri] int id, [FromBody] EComment update)
        {
            Comment comment = db.Comments.Find(id);
            comment.TaskCompleted = update.TaskCompleted;
            db.SaveChanges();

            return Ok();
        }

        [Route("api/ImageRecords/{id}/TextDescription")]
        public IHttpActionResult PostImageRecordDescription(int id, [FromBody] string description)
        {
            ImageRecord record = db.ImageRecords.Include(x => x.Household).Include(x => x.Household.Study)
                .FirstOrDefault(x => x.Id == id);
            if (record == null)
                return BadRequest();

            record.TextDescription = description;
            db.SaveChanges();

            List<MatchGroup> groups = new List<MatchGroup>();
            if (!string.IsNullOrWhiteSpace(description))
            {
                EFoodComposition[] comps = db.FoodCompositions.Where(x => x.Table_Id == record.Household.Study.FoodCompositionTable_Id).Select(x => new EFoodComposition { Id = x.Id, Name = x.Name, AlternateName = x.AlternateName }).ToArray();
                groups = Models.StringUtils.DatabaseMatcher.FindMatches(description, comps);
            }

            return Ok(groups);
        }

        [Route("api/ImageRecords/{id}/Transcript")]
        public IHttpActionResult PostImageRecordTranscript(int id, [FromBody] string transcript)
        {
            ImageRecord record = db.ImageRecords.Include(x => x.Household).Include(x => x.Household.Study)
                .FirstOrDefault(x => x.Id == id);
            if (record == null)
                return BadRequest();

            record.ManualTranscript = transcript;
            db.SaveChanges();

            List<MatchGroup> groups = new List<MatchGroup>();
            if (!string.IsNullOrWhiteSpace(transcript))
            {
                EFoodComposition[] comps = db.FoodCompositions.Where(x => x.Table_Id == record.Household.Study.FoodCompositionTable_Id).Select(x => new EFoodComposition { Id = x.Id, Name = x.Name, AlternateName = x.AlternateName }).ToArray();
                groups = Models.StringUtils.DatabaseMatcher.FindMatchesNative(transcript, comps);
            }

            return Ok(groups);
        }

        [HttpGet, Route("api/RecalculateHistories")]
        public IHttpActionResult RecalculateHistories()
        {
            List<ImageRecord> records = db.ImageRecords
                .Include(x => x.Updates)
                .Include(x => x.Updates.Select(y => y.User))
                .Include(x => x.FoodItems)
                .Include(x => x.FoodItems.Select(y => y.FoodComposition))
                .ToList();
            List<StandardMeasure> standards = db.StandardMeasures.ToList();
            try
            {

            foreach (var record in records)
            {
                foreach (var foodItem in record.FoodItems)
                {
                    RecalculateHistoryForItem(foodItem, standards);

                    var first = foodItem.Updates?.OrderByDescending(x => x.Time).FirstOrDefault(x => x.Action == RecordHistory.UpdateTypes.Quantify);

                    if (("Gestalt estimation (g)").Equals(foodItem.MeasureType))
                    {
                        double? quantity = null;
                        if (first != null && (first.User.Role.Id == 1 || first.User.Role.Id == 4))
                            quantity = first.QuantityGrams;

                        if (quantity.HasValue)
                            foodItem.QuantityGrams = quantity ?? 0;
                        else
                        {
                            var estimates = foodItem.Updates.OrderByDescending(x => x.Time).GroupBy(x => x.User.Id).Select(x => x.FirstOrDefault(y => y.Action == RecordHistory.UpdateTypes.Quantify));
                            quantity = estimates.Where(x => x != null).Average(x => x.QuantityGrams ?? 0);
                            foodItem.QuantityGrams = quantity ?? 0;
                                //foodItem.Updates.Where(x => x.Action == RecordHistory.UpdateTypes.Quantify).Average(x => x.QuantityGrams ?? 0);
                        }
                        foodItem.MeasureCount = quantity ?? 0;
                    }
                    else if (first != null)
                    {
                        foodItem.QuantityGrams = first.QuantityGrams ?? 0;
                    }
                    
                }
            }
            } catch (Exception e)
            {
                return BadRequest(e.Message + "\n"  + e.StackTrace);
            }

            db.SaveChanges();

             return Ok();
        }

        [HttpGet, Route("api/RecalculateHistory/{id}")]
        public IHttpActionResult RecalculateHistories(int id)
        {
            FoodItem foodItem = db.FoodItems
                .Include(x => x.Updates)
                .Include(x => x.Updates.Select(y => y.User))
                .Include(x => x.FoodComposition)
                .Include(x => x.ImageRecord.Household.Study.FoodCompositionTable)
                .FirstOrDefault(x => x.Id == id);
            List<StandardMeasure> standards = db.StandardMeasures.ToList();

            RecalculateHistoryForItem(foodItem, standards);

            var first = foodItem.Updates?.OrderByDescending(x => x.Time).FirstOrDefault(x => x.Action == RecordHistory.UpdateTypes.Quantify);

            if (("Gestalt estimation (g)").Equals(foodItem.MeasureType))
            {
                double? quantity = null;
                if (first != null && (first.User.Role.Id == 1 || first.User.Role.Id == 4))
                    quantity = first.QuantityGrams;

                if (quantity.HasValue)
                    foodItem.QuantityGrams = quantity ?? 0;
                else
                {
                    var estimates = foodItem.Updates.OrderByDescending(x => x.Time).GroupBy(x => x.User.Id).Select(x => x.FirstOrDefault(y => y.Action == RecordHistory.UpdateTypes.Quantify));
                    foodItem.QuantityGrams = estimates.Where(x => x != null).Average(x => x.QuantityGrams ?? 0);
                    //foodItem.Updates.Where(x => x.Action == RecordHistory.UpdateTypes.Quantify).Average(x => x.QuantityGrams ?? 0);
                }
            }
            else
            {
                foodItem.QuantityGrams = first.QuantityGrams ?? 0;
            }

            db.SaveChanges();

            return Ok();
        }

        public static void RecalculateFoodItem(FoodItem foodItem, VISIDA_APIContext db)
        {
            List<StandardMeasure> standards = db.StandardMeasures.ToList();
            RecalculateHistoryForItem(foodItem, standards);

            var first = foodItem.Updates?.OrderByDescending(x => x.Time).FirstOrDefault(x => x.Action == RecordHistory.UpdateTypes.Quantify);

            if (("Gestalt estimation (g)").Equals(foodItem.MeasureType))
            {
                double? quantity = null;
                if (first != null && (first.User.Role.Id == 1 || first.User.Role.Id == 4))
                    quantity = first.QuantityGrams;

                if (quantity.HasValue)
                    foodItem.QuantityGrams = quantity ?? 0;
                else
                {
                    var estimates = foodItem.Updates.OrderByDescending(x => x.Time).GroupBy(x => x.User.Id).Select(x => x.FirstOrDefault(y => y.Action == RecordHistory.UpdateTypes.Quantify));
                    foodItem.QuantityGrams = estimates.Where(x => x != null).Average(x => x.QuantityGrams ?? 0);
                    //foodItem.Updates.Where(x => x.Action == RecordHistory.UpdateTypes.Quantify).Average(x => x.QuantityGrams ?? 0);
                }
                foodItem.MeasureCount = foodItem.QuantityGrams;
            }
            else
            {
                foodItem.QuantityGrams = first?.QuantityGrams ?? 0;
            }

            //db.SaveChanges();
        }

        private static void RecalculateHistoryForItem(FoodItem foodItem, List<StandardMeasure> standards)
        {
            foreach (var update in foodItem.Updates)
            {
                if (foodItem.FoodComposition == null)
                    continue;
                if (update.Action == RecordHistory.UpdateTypes.Quantify)
                {
                    if (update.ToolSource.Equals("Raw gram input (g)") || update.ToolSource.Equals("Gestalt estimation (g)"))
                        continue;
                    var density = foodItem.FoodComposition.Density;
                    if (update.ToolSource.Equals("Volume input (mL)"))
                        update.QuantityGrams = update.ToolMeasure * density;
                    else if (foodItem.FoodComposition.Measures != null && foodItem.FoodComposition.Measures.Contains(update.ToolSource))
                    {
                        var measure = foodItem.FoodComposition.MeasureItems.FirstOrDefault(x => x.Item1.Equals(update.ToolSource));
                        if (measure != null)
                        try
                        {
                            update.QuantityGrams = update.ToolMeasure * measure.Item2;

                        } catch (Exception e)
                        {
                            continue;
                        }
                    }
                    else if (density.HasValue)
                    {
                        StandardMeasure measure = null;
                        if (foodItem.FoodComposition.Table_Id != null)
                            measure = standards.FirstOrDefault(x => x.Table.Id == foodItem.FoodComposition.Table_Id && x.Name.Equals(update.ToolSource));
                        else
                            measure = standards.FirstOrDefault(x => x.Table.Id == foodItem.ImageRecord.Household.Study.FoodCompositionTable.Id && x.Name.Equals(update.ToolSource));
                        if (measure != null)
                            try
                            {
                            update.QuantityGrams = update.ToolMeasure * density * measure.MLs;

                            } catch (Exception e)
                            {
                                continue;
                            }
                    }
                }
            }
        }

        [Route("api/ImageRecords/{id}/Homography"), HttpPut]
        public IHttpActionResult PutImageRecordHomography(int id, [FromBody] DoublePoint[] points)
        {
            var record = db.ImageRecords.Find(id);
            if (record == null)
                return BadRequest();
            if (record.Homography == null)
                record.Homography = new ImageHomography();
            record.Homography.Points = points;
            db.SaveChanges();

            return Ok();
        }

        // DELETE: api/ImageRecords/5
        [ResponseType(typeof(ImageRecord))]
        public IHttpActionResult DeleteImageRecord(int id)
        {
            ImageRecord imageRecord = db.ImageRecords.Find(id);
            if (imageRecord == null)
            {
                return NotFound();
            }

            db.ImageRecords.Remove(imageRecord);
            db.SaveChanges();

            return Ok(imageRecord);
        }

        [HttpGet, Route("api/ImageRecords/{id}/AddParticipant/{pid}")]
        public IHttpActionResult AddImageParticipant(int id, int pid)
        {
            var record = db.ImageRecords.Find(id);
            if (record == null)
                return BadRequest("That record could not be found");
            var eatRecords = db.EatRecords.Include(x => x.EatOccasion).Include(x => x.EatOccasion.HouseholdMember).Where(x => x.ImageRecord.Id == id).ToList();
            var participation = eatRecords.FirstOrDefault(x => x.EatOccasion.HouseholdMember.Id == pid);//db.EatRecords.FirstOrDefault(x => x.ImageRecord.Id == id && x.EatOccasion.HouseholdMemberId == pid);
            var hm = db.HouseholdMembers.Find(pid);
            var leftovers = eatRecords.FirstOrDefault(x => x.Leftovers != null)?.Leftovers;

            if (participation != null && !participation.Hidden)
                return BadRequest("Participant is already added to this record. Cannot add twice.");
            else if (participation != null && participation.Original)
            {
                participation.Hidden = false;
            }
            else if (participation == null)
            {
                var occS = db.EatRecords.Where(x => x.ImageRecord.Id == id).FirstOrDefault()?.EatOccasion;
                EatOccasion occ = null;
                if (occS != null)
                    occ = db.EatOccasions.FirstOrDefault(x => x.HouseholdMemberId == pid && x.TimeStart.Equals(occS.TimeStart));
                if (occ == null)
                {
                    occ = new EatOccasion()
                    {
                        OriginId = occS.OriginId,
                        EatRecords = new List<EatRecord>(),
                        TimeStart = occS.TimeStart,
                        TimeEnd = occS.TimeEnd,
                        HouseholdMember = hm,
                        Finalized = false,
                        Original = false
                    };
                    db.EatOccasions.Add(occ);
                }
                participation = new EatRecord()
                {
                    Finalized = false,
                    FinalizeTime = occS.TimeEnd,
                    Hidden = false,
                    Original = false,
                    ImageRecord = record,
                    Leftovers = leftovers
                };
                occ.EatRecords.Add(participation);
            }

            if (record.GuestInfo == null)
            {
                record.GuestInfo = new HouseholdGuestInfo()
                {
                    ChildGuests = eatRecords.Count(x => x.EatOccasion.HouseholdMember.Age < 18),
                    AdultFemaleGuests = eatRecords.Count(x => x.EatOccasion.HouseholdMember.Age >= 18 && x.EatOccasion.HouseholdMember.IsFemale),
                    AdultMaleGuests = eatRecords.Count(x => x.EatOccasion.HouseholdMember.Age >= 18 && !x.EatOccasion.HouseholdMember.IsFemale)
                };
            }

            if (hm.Age < 18)
                record.GuestInfo.ModChildGuests = (record.GuestInfo.ModChildGuests ?? record.GuestInfo.ChildGuests) + 1;
            else if (hm.IsFemale)
                record.GuestInfo.ModAdultFemaleGuests = (record.GuestInfo.ModAdultFemaleGuests ?? record.GuestInfo.AdultFemaleGuests) + 1;
            else
                record.GuestInfo.ModAdultMaleGuests = (record.GuestInfo.ModAdultMaleGuests ?? record.GuestInfo.AdultMaleGuests) + 1;

            try
            {
                db.SaveChanges();

            }
            catch (Exception e)
            {
                return BadRequest("Uncaught error");
            }

            return Ok(new Tuple<EHouseholdMember, EHouseholdGuestInfo>((EHouseholdMember)hm, (EHouseholdGuestInfo)record.GuestInfo));
        }

        [HttpGet, Route("api/ImageRecords/{id}/AddParticipantByName/{name}")]
        public IHttpActionResult AddImageParticipant(int id, string name)
        {
            var participantId = db.EatRecords.FirstOrDefault(x => x.ImageRecord.Id == id).EatOccasion.HouseholdMember.Household.HouseholdMembers.FirstOrDefault(x => x.ParticipantId.Equals(name)).Id;
            return AddImageParticipant(id, participantId);
        }

        [HttpDelete, Route("api/ImageRecords/{id}/DeleteParticipant/{pid}")]
        public IHttpActionResult DeleteEatRecord(int id, int pid)
        {
            var record = db.ImageRecords.Find(id);
            if (record == null)
                return BadRequest("That record could not be found");
            var eatRecords = db.EatRecords.Include(x => x.EatOccasion).Include(x => x.EatOccasion.HouseholdMember).Where(x => x.ImageRecord.Id == id).ToList();
            //var eat = db.EatRecords.FirstOrDefault(x => x.ImageRecord.Id == id && x.EatOccasion.HouseholdMemberId == pid);
            var participation = eatRecords.FirstOrDefault(x => x.EatOccasion.HouseholdMember.Id == pid);
            var hm = participation.EatOccasion.HouseholdMember;
            if (participation == null)
                return BadRequest("That user was not found as a participant.");

            if (eatRecords.Where(x => !x.Hidden).Count() == 1)
                return BadRequest("Cannot remove the last participant. There must be at least one participant");

            if (participation.Original)
            {
                participation.Hidden = true;
            }
            else
            {
                var occ = participation.EatOccasion;
                //db.EatRecords.Remove(eat);
                occ.EatRecords.Remove(participation);
                if (!occ.Original && occ.EatRecords.Count == 0)
                    db.EatOccasions.Remove(occ);
                db.EatRecords.Remove(participation);
            }

            if (record.GuestInfo != null)
            {
                if (hm.Age < 18)
                    record.GuestInfo.ModChildGuests = Math.Max((record.GuestInfo.ModChildGuests ?? record.GuestInfo.ChildGuests) - 1, 0);
                else if (hm.IsFemale)
                    record.GuestInfo.ModAdultFemaleGuests = Math.Max((record.GuestInfo.ModAdultFemaleGuests ?? record.GuestInfo.AdultFemaleGuests) - 1, 0);
                else
                    record.GuestInfo.ModAdultMaleGuests = Math.Max((record.GuestInfo.ModAdultMaleGuests ?? record.GuestInfo.AdultMaleGuests) - 1, 0);
            }

            db.SaveChanges();

            return Ok((EHouseholdGuestInfo)record.GuestInfo);
        }

        [HttpDelete, Route("api/ImageRecords/{id}/DeleteParticipantByName/{name}")]
        public IHttpActionResult DeleteEatRecord(int id, string name)
        {
            var participantId = db.EatRecords.FirstOrDefault(x => x.ImageRecord.Id == id).EatOccasion.HouseholdMember.Household.HouseholdMembers.FirstOrDefault(x => x.ParticipantId.Equals(name)).Id;
            return DeleteEatRecord(id, participantId);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                db.Dispose();
            }
            base.Dispose(disposing);
        }

        private bool ImageRecordExists(int id)
        {
            return db.ImageRecords.Count(e => e.Id == id) > 0;
        }
    }
}