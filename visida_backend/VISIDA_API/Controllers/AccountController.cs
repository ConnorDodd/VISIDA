using Microsoft.AspNet.Identity;
using SendGrid;
using SendGrid.Helpers.Mail;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Threading.Tasks;
using System.Web.Http;
using VISIDA_API.Models;
using VISIDA_API.Models.InternalObjects;
using VISIDA_API.Models.User;

namespace VISIDA_API.Controllers
{
    [RoutePrefix("api/Account")]
    public class AccountController : ApiController
    {
        private AuthRepository _repo = null;

        public AccountController()
        {
            _repo = new AuthRepository();
        }

        // POST api/Account/Register
        //Called to create a new account, runs through the AuthRepository and [required] validationg before being saved to the database.
        [Route("Register"), Authorize(Roles = "admin")]
        public async Task<IHttpActionResult> Register([FromBody]LoginUser userModel)
        {
            string newPassword = Guid.NewGuid().ToString().Substring(0, 8);
            userModel.Password = newPassword;
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }

            LoginUser result = await _repo.RegisterUserAsync(userModel);

            if (result != null)
            {
                var db = new VISIDA_APIContext();
                var html = EmailManager.GetEmailTemplate("new_user_email.html");
                string key = "";
                try
                {
                    key = await _repo.CreatePasswordReset(userModel.UserName, true);
                }
                catch (Exception e)
                {
                    throw e;
                }
                string newUrl = EmailManager.ActiveHost + "/#!/loginreset?key=" + key;
                html = html.Replace("{{url}}", newUrl);
                html = html.Replace("{{role}}", userModel.Role.Role);
                string plainText = "Welcome to VISIDA! Use this link to set password: " + newUrl;

                Response response = await EmailManager.SendEmail(userModel.Email, "Account Invite - VISIDA", html, plainText);

                return Ok();
            }
            else
                return BadRequest(ModelState);
        }

        [Route("ResetPassword"), AllowAnonymous]
        public async Task<IHttpActionResult> GetResetPassword([FromUri]string username)
        {
            try
            {
                var db = new VISIDA_APIContext();
                var user = db.Users.FirstOrDefault(x => username.Equals(x.UserName) || username.Equals(x.Email));
                if (user == null)
                    return BadRequest("Could not find user with that username.");
                if (!user.IsActive)
                    return BadRequest("User is inactive. Contact an admin if you believe this to be in error");
                if (string.IsNullOrEmpty(user.Email))
                    return BadRequest("User has no email address. Please contact an administrator to reset your password and add an email.");
            ResetPasswordRequest rpr = null;
            DateTime time = DateTime.Now;
            try
            {
                rpr = new ResetPasswordRequest()
                {
                    Key = Guid.NewGuid().ToString().Replace("-", ""),
                    LoginUser = user,
                    CreatedTime = time
                };

                db.ResetPasswordRequests.Add(rpr);
                db.SaveChanges();
            }
            catch (Exception e)
            {
                throw e;
            }
            string html = EmailManager.GetEmailTemplate("reset_password_email.html");
            string newUrl = EmailManager.ActiveHost + "/#!/loginreset?key=" + rpr.Key;
            html = html.Replace("{{url}}", newUrl);
            string expiry = time.AddHours(1).ToString("HH:mm tt");
            html = html.Replace("{{expiry}}", expiry);
            string fallback = "Use this link to reset password: " + newUrl;

            Response response = await EmailManager.SendEmail(user.Email, "Password Reset - VISIDA", html, fallback);
            return Ok(user.Email);
            }
            catch (Exception e)
            {
                throw e;
            }



        }

        [Route("ResetPasswordByKey"), HttpPost, AllowAnonymous]
        public IHttpActionResult ResetPasswordEmail([FromBody]PasswordResetRequestData data)
        {
            var db = new VISIDA_APIContext();
            var reset = db.ResetPasswordRequests.FirstOrDefault(x => x.Key.Equals(data.Key));
            if (reset == null)
                return BadRequest("Reset code was invalid, try requesting a new reset.");
            if (reset.Used)
                return BadRequest("Password has already been reset. To change again, you must request another reset");
            if (reset.CreatedTime.CompareTo(DateTime.Now.AddHours(-1)) < 0)
                return BadRequest("Reset request has expired. To change your password, you must request another reset");

            _repo.ResetPassword(reset.LoginUser, data.Password);
            reset.Used = true;
            db.SaveChanges();

            return Ok();
        }

        public class PasswordResetRequestData
        {
            public string Key { get; set; }
            public string Password { get; set; }
        }

        //changes a specified users password to a new string, which gets salted and hashed
        //Since only admins can reset passwords at the moment this should be fine
        [Route("ResetPassword/{id}"), HttpPost, Authorize(Roles = "admin")]
        public async Task<IHttpActionResult> ResetPassword([FromUri] int id, [FromBody]string password)
        {
            if (string.IsNullOrEmpty(password))
                return BadRequest("Password is required.");

            var user = await _repo.FindUserAsync(id);
            if (user == null)
                return BadRequest("User cannot be found with id " + id);

            _repo.ResetPassword(user, password);

            return Ok();
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                _repo.Dispose();
            }

            base.Dispose(disposing);
        }
    }
}
