namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class RemoveMealGuests : DbMigration
    {
        public override void Up()
        {
            DropColumn("dbo.HouseholdMeals", "AdultFemaleGuests");
            DropColumn("dbo.HouseholdMeals", "AdultMaleGuests");
            DropColumn("dbo.HouseholdMeals", "ChildGuests");
            DropColumn("dbo.HouseholdMeals", "ModAdultFemaleGuests");
            DropColumn("dbo.HouseholdMeals", "ModAdultMaleGuests");
            DropColumn("dbo.HouseholdMeals", "ModChildGuests");
        }
        
        public override void Down()
        {
            AddColumn("dbo.HouseholdMeals", "ModChildGuests", c => c.Int());
            AddColumn("dbo.HouseholdMeals", "ModAdultMaleGuests", c => c.Int());
            AddColumn("dbo.HouseholdMeals", "ModAdultFemaleGuests", c => c.Int());
            AddColumn("dbo.HouseholdMeals", "ChildGuests", c => c.Int(nullable: false));
            AddColumn("dbo.HouseholdMeals", "AdultMaleGuests", c => c.Int(nullable: false));
            AddColumn("dbo.HouseholdMeals", "AdultFemaleGuests", c => c.Int(nullable: false));
        }
    }
}
