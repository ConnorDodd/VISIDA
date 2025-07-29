namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class AddHouseholdMeals : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.HouseholdMeals",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        AdultFemaleGuests = c.Int(nullable: false),
                        AdultMaleGuests = c.Int(nullable: false),
                        ChildGuests = c.Int(nullable: false),
                        Finalized = c.Boolean(nullable: false),
                        GuestInfoCaptured = c.Boolean(nullable: false),
                        StartTime = c.DateTime(nullable: false),
                        Household_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.Households", t => t.Household_Id)
                .Index(t => t.Household_Id);
            
            AddColumn("dbo.ImageRecords", "Meal_Id", c => c.Int());
            CreateIndex("dbo.ImageRecords", "Meal_Id");
            AddForeignKey("dbo.ImageRecords", "Meal_Id", "dbo.HouseholdMeals", "Id");
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.ImageRecords", "Meal_Id", "dbo.HouseholdMeals");
            DropForeignKey("dbo.HouseholdMeals", "Household_Id", "dbo.Households");
            DropIndex("dbo.HouseholdMeals", new[] { "Household_Id" });
            DropIndex("dbo.ImageRecords", new[] { "Meal_Id" });
            DropColumn("dbo.ImageRecords", "Meal_Id");
            DropTable("dbo.HouseholdMeals");
        }
    }
}
