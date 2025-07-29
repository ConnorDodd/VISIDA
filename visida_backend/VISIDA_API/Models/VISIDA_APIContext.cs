using System;
using System.Collections.Generic;
using System.Data.Entity;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models
{
    public class VISIDA_APIContext : DbContext
    {
        // You can add custom code to this file. Changes will not be overwritten.
        // 
        // If you want Entity Framework to drop and regenerate your database
        // automatically whenever you change your model schema, please use data migrations.
        // For more information refer to the documentation:
        // http://msdn.microsoft.com/en-us/data/jj591621.aspx

        public VISIDA_APIContext() : base("name=VISIDA_APIContext")
        {
            this.Database.CommandTimeout = 60;
        }

        public DbSet<InternalObjects.Study> Studies { get; set; }
        public DbSet<InternalObjects.Household> Households { get; set; }
        public DbSet<InternalObjects.HouseholdMember> HouseholdMembers { get; set; }

        public DbSet<InternalObjects.EatOccasion> EatOccasions { get; set; }

        public DbSet<User.LoginUser> Users { get; set; }
        public DbSet<User.LoginUserRole> UserRoles { get; set; }
        public DbSet<InternalObjects.WorkAssignation> WorkAssignations { get; set; }
        public DbSet<InternalObjects.HouseholdAssignation> HouseholdAssignations { get; set; }

        public DbSet<InternalObjects.FoodItem> FoodItems { get; set; }
        public DbSet<InternalObjects.Timing> Timings { get; set; }

        public DbSet<FCT.FoodComposition> FoodCompositions { get; set; }
        public DbSet<FCT.FoodCompositionTable> FoodCompositionTables { get; set; }
        public DbSet<FCT.FoodCompositionUpdate> FoodCompositionUpdates { get; set; }
        public DbSet<FCT.FoodCompositionRecipe> FoodCompositionRecipes { get; set; }
        public DbSet<FCT.ReferenceImageType> ReferenceImageTypes { get; set; }
        public System.Data.Entity.DbSet<VISIDA_API.Models.FCT.ReferenceImage> ReferenceImages { get; set; }
        public DbSet<FCT.RetentionFactor> RetentionFactors { get; set; }
        public DbSet<FCT.FoodGroup> FoodGroups { get; set; }
        public DbSet<FCT.RDA> RDAs { get; set; }
        public DbSet<FCT.RDAModel> RDAModels { get; set; }
        public DbSet<FCT.StandardMeasure> StandardMeasures { get; set; }


        public DbSet<InternalObjects.CookRecipe> CookRecipes { get; set; }
        public DbSet<InternalObjects.CookIngredient> CookIngredients { get; set; }

        public DbSet<InternalObjects.EatRecord> EatRecords { get; set; }

        public DbSet<InternalObjects.ImageRecord> ImageRecords { get; set; }
        public DbSet<InternalObjects.Comment> Comments { get; set; }
        public DbSet<InternalObjects.RecordHistory> RecordHistories { get; set; }

        public DbSet<InternalObjects.Testing.ReliabilityTestRule> ReliabilityTestRules { get; set; }
        public DbSet<InternalObjects.Testing.ReliabilityTest> ReliabilityTests { get; set; }

        public DbSet<InternalObjects.UsageLogFile> UsageLogFiles { get; set; }
        public DbSet<InternalObjects.ConversionHistory> ConversionHistories { get; set; }



        public DbSet<InternalObjects.Advice> Advices { get; set; }
        public DbSet<InternalObjects.AdminMessage> AdminMessages { get; set; }

        public DbSet<InternalObjects.ResetPasswordRequest> ResetPasswordRequests { get; set; }

        //protected override void OnModelCreating(DbModelBuilder modelBuilder)
        //{
        //    modelBuilder.Entity<InternalObjects.UsageLogFile>()
        //        .HasRequired(u => u.Household)
        //        .WithOptional(h => h.UsageLog);
        //}
    }
}
