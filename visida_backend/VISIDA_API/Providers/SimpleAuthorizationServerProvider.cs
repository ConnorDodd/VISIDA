using Microsoft.AspNet.Identity.EntityFramework;
using Microsoft.Owin.Security;
using Microsoft.Owin.Security.OAuth;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Claims;
using System.Threading.Tasks;
using System.Web;
using VISIDA_API.Models.User;

//This is the login class, where /token requests are directed to. It's purpose is to validate login credentials, then create a token with whatever metadata it needs and returning it.

namespace VISIDA_API.Providers
{
    public class SimpleAuthorizationServerProvider : OAuthAuthorizationServerProvider
    {
        public override async Task ValidateClientAuthentication(OAuthValidateClientAuthenticationContext context)
        {
            context.Validated();
        }

        public override async Task GrantResourceOwnerCredentials(OAuthGrantResourceOwnerCredentialsContext context)
        {

            context.OwinContext.Response.Headers.Add("Access-Control-Allow-Origin", new[] { "*" });

            var identity = new ClaimsIdentity(context.Options.AuthenticationType);
            AuthenticationProperties prop = new AuthenticationProperties();
            using (AuthRepository _repo = new AuthRepository())
            {
                LoginUser user = null;
                try
                {
                     user = await _repo.FindUserAsync(context.UserName, context.Password);
                }
                catch (Exception e)
                {
                    var q = e.Message;
                    context.SetError("server_error", "The server could not process your request.");
                    return;
                }

                if (user == null)
                {
                    context.SetError("invalid_grant", "The user name or password is incorrect.");
                    return;
                }
                if (!user.IsActive)
                {
                    context.SetError("invalid_grant", "Your account has been disabled. Please contact an administrator if you believe this to be in error.");
                    return;
                }

                //identity.AddClaim(new Claim("name", user.Id.ToString()));
                identity.AddClaim(new Claim(@"http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name", user.Id.ToString()));
                identity.AddClaim(new Claim("username", user.UserName));
                //identity.AddClaim(new Claim(@"http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name", user.Email));
                prop.Dictionary.Add("id", user.Id.ToString());
                prop.Dictionary.Add("role", user.Role.Role);
                prop.Dictionary.Add("username", user.UserName);
                //prop.Dictionary.Add("email", user.Email);
                if (user.Role != null)
                {
                    if (user.Role.Role.Equals("admin"))
                    {
                        identity.AddClaim(new Claim(ClaimTypes.Role, user.Role.Role));
                        //prop.Dictionary.Add(user.Role.Role, "true");
                    }
                    else
                    {
                        //prop.Dictionary.Add("admin", "false");
                        //prop.Dictionary.Add(user.Role.Role, "true");
                        identity.AddClaim(new Claim(ClaimTypes.Role, user.Role.Role));
                    }
                }
            }

            context.Validated(new AuthenticationTicket(identity, prop));

        }

        public override Task TokenEndpoint(OAuthTokenEndpointContext context)
        {
            foreach (KeyValuePair<string, string> property in context.Properties.Dictionary)
            {
                context.AdditionalResponseParameters.Add(property.Key, property.Value);
            }

            return Task.FromResult<object>(null);
        }
    }
}