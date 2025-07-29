using SendGrid;
using SendGrid.Helpers.Mail;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Web;

namespace VISIDA_API.Models
{
    public static class EmailManager
    {
        public const string ActiveHost = "https://visida.newcastle.edu.au";

        public static string GetEmailTemplate(string name)
        {
            string path = System.Web.HttpContext.Current.Request.PhysicalApplicationPath + "\\Views\\" + name;
            return System.IO.File.ReadAllText(path);
        }

        public static async Task<Response> SendEmail(string to, string subject, string message, string fallback = "")
        {
            var client = new SendGridClient("<your api key here>");
            var from = new EmailAddress("connor.dodd19@gmail.com", "VISIDA");
            var address = new EmailAddress(to, "Visida User");

            var msg = MailHelper.CreateSingleEmail(from, address, subject, fallback, message);
            var response = await client.SendEmailAsync(msg);

            //msg.AddContent(MimeType.Text, fallback);
            //msg.AddContent(MimeType.Html, message);
            
            return response;
        }
    }
}