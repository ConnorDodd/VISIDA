namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class AddHouseholdAssignations_Merge : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.HouseholdAssignations",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Household_Id = c.Int(),
                        LoginUser_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.Households", t => t.Household_Id)
                .ForeignKey("dbo.LoginUsers", t => t.LoginUser_Id)
                .Index(t => t.Household_Id)
                .Index(t => t.LoginUser_Id);
            
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.HouseholdAssignations", "LoginUser_Id", "dbo.LoginUsers");
            DropForeignKey("dbo.HouseholdAssignations", "Household_Id", "dbo.Households");
            DropIndex("dbo.HouseholdAssignations", new[] { "LoginUser_Id" });
            DropIndex("dbo.HouseholdAssignations", new[] { "Household_Id" });
            DropTable("dbo.HouseholdAssignations");
        }
    }
}
