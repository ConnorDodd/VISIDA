using Microsoft.AspNet.Identity;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Threading.Tasks;
using System.Web;

namespace VISIDA_API.Models.User
{
    public class UserManager<TUser, TKey> : IUserStore<LoginUser, int> where TUser : LoginUser
    {
        private VISIDA_APIContext ctx;

        public UserManager(VISIDA_APIContext ctx)
        {
            this.ctx = ctx;
        }

        public async Task CreateAsync(LoginUser user)
        {
            byte[] rng = new byte[32];
            new RNGCryptoServiceProvider().GetNonZeroBytes(rng);
            user.Salt = System.Text.Encoding.Default.GetString(rng).Normalize();
            user.Password = new PasswordHasher().HashPassword(user.Salt + user.Password);
            ctx.Users.Add(user);
            ctx.SaveChanges();
            //return Task.FromResult<object>(null);
        }

        public Task DeleteAsync(LoginUser user)
        {
            throw new NotImplementedException();
        }

        public Task<LoginUser> FindByIdAsync(int userId)
        {
            return Task.FromResult(ctx.Users.Where(x => x.Id == userId).FirstOrDefault());
        }

        public Task<LoginUser> FindByNameAsync(string userName, string email)
        {
            return Task.FromResult(ctx.Users.Where(x => x.UserName.Equals(userName) || x.Email.Equals(email)).FirstOrDefault());
        }

        public void ResetPassword(LoginUser user, string password)
        {
            byte[] rng = new byte[32];
            new RNGCryptoServiceProvider().GetNonZeroBytes(rng);
            user.Salt = System.Text.Encoding.Default.GetString(rng).Normalize();
            user.Password = new PasswordHasher().HashPassword(user.Salt + password);

            ctx.SaveChanges();
        }

        public Task UpdateAsync(LoginUser user)
        {
            ctx.SaveChanges();

            return Task.CompletedTask;
        }

        public void Dispose()
        {
            ctx.Dispose();
        }

        public Task<LoginUser> FindByNameAsync(string userName)
        {
            return Task.FromResult(ctx.Users.Where(x => userName.Equals(x.Email) || userName.Equals(x.UserName)).FirstOrDefault());
        }
    }
}