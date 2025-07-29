using System;
using System.Net;
using System.Threading.Tasks;
using System.Web.Http;
using SendGrid;
using VISIDA_API.Models;

namespace VISIDA_API.Controllers
{
    [Authorize]
    public class HelpController : AuthController
    {
        // Post - send an email
        [Route("api/ReportIssue")]
        public async Task<IHttpActionResult> ReportIssue([FromBody] EmailData emailData)
        {
            String html = EmailManager.GetEmailTemplate("report_issue_email.html");
            html = html.Replace("{{ user }}", user.UserName);
            html = html.Replace("{{ role }}", user.Role.Role);
            html = html.Replace("{{ page }}", emailData.Page);
            html = html.Replace("{{ issue }}", emailData.Issue);
            html = html.Replace("{{ expect }}", emailData.Expect);

            // TODO - update this email address
            Response response = await EmailManager.SendEmail("visidaproject@gmail.com", "VISIDA Help Email", html);

            // TODO - proper handling of response

            if (response.StatusCode == HttpStatusCode.Accepted)
            {
                return Ok();
            }
            else
            {
                return InternalServerError();
            }
        }

        public class EmailData
        {
            public String Page { get; set; }
            public String Issue { get; set; }
            public String Expect { get; set; }
        }
    }
}