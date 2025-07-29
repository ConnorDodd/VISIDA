namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class MoreParticipantDetails : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.HouseholdMembers", "PregnancyTrimester", c => c.String());
            AddColumn("dbo.HouseholdMembers", "Weight", c => c.Single(nullable: false));
            AddColumn("dbo.HouseholdMembers", "Height", c => c.Single(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.HouseholdMembers", "Height");
            DropColumn("dbo.HouseholdMembers", "Weight");
            DropColumn("dbo.HouseholdMembers", "PregnancyTrimester");
        }
    }
}
