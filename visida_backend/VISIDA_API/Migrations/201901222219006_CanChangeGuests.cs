namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class CanChangeGuests : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.HouseholdMeals", "ModAdultFemaleGuests", c => c.Int());
            AddColumn("dbo.HouseholdMeals", "ModAdultMaleGuests", c => c.Int());
            AddColumn("dbo.HouseholdMeals", "ModChildGuests", c => c.Int());
        }
        
        public override void Down()
        {
            DropColumn("dbo.HouseholdMeals", "ModChildGuests");
            DropColumn("dbo.HouseholdMeals", "ModAdultMaleGuests");
            DropColumn("dbo.HouseholdMeals", "ModAdultFemaleGuests");
        }
    }
}
