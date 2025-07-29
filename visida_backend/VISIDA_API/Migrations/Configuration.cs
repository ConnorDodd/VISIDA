namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity;
    using System.Data.Entity.Migrations;
    using System.Linq;
    using VISIDA_API.Models.User;

    internal sealed class Configuration : DbMigrationsConfiguration<VISIDA_API.Models.VISIDA_APIContext>
    {
        public Configuration()
        {
            AutomaticMigrationsEnabled = false;
        }

        protected override void Seed(VISIDA_API.Models.VISIDA_APIContext context)
        {
            //  This method will be called after migrating to the latest version.

            //  You can use the DbSet<T>.AddOrUpdate() helper extension method 
            //  to avoid creating duplicate seed data.

            if (context.UserRoles.Count(x => x.Role.Equals("admin")) == 0)
                context.UserRoles.AddOrUpdate(new LoginUserRole() { Role = "admin" });
            if (context.UserRoles.Count(x => x.Role.Equals("coordinator")) == 0)
                context.UserRoles.AddOrUpdate(new LoginUserRole() { Role = "coordinator" });
            if (context.UserRoles.Count(x => x.Role.Equals("analyst")) == 0)
                context.UserRoles.AddOrUpdate(new LoginUserRole() { Role = "analyst" });
            if (context.UserRoles.Count(x => x.Role.Equals("appuser")) == 0)
                context.UserRoles.AddOrUpdate(new LoginUserRole() { Role = "appuser" });
            //if (context.UserRoles.Count(x => x.Role.Equals("dietician")) == 0)
            //    context.UserRoles.AddOrUpdate(new LoginUserRole() { Role = "dietician" });

            context.SaveChanges();

            if (context.Users.Count(x => x.UserName.Equals("admin")) == 0)
            {
                LoginUserRole adminRole = context.UserRoles.FirstOrDefault(x => x.Role.Equals("admin"));
                context.Users.AddOrUpdate(new LoginUser()
                {
                    UserName = "admin",
                    Password = "AGzT3ffdhgOUAtL6RFy1N2nmt5gHHnMHG5S7uH5a1Yp0iIzIv87ptiKoCVLQi4Bvhg==",
                    Salt = "vˆ]¾*œ?`#ÏèyJ¯nšgž=ß\"qŸ•¢™h",
                    Role = adminRole
                });

                context.SaveChanges();
            }
        }
    }
}
