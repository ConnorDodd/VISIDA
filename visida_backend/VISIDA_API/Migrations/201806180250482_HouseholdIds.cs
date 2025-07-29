namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class HouseholdIds : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.Households", "ParticipantId", c => c.String(nullable: false, maxLength: 128));
            AddColumn("dbo.HouseholdMembers", "ParticipantId", c => c.String(nullable: false, maxLength: 36, unicode: false));
            AddColumn("dbo.HouseholdMembers", "IsFemale", c => c.Boolean(nullable: false));
            AddColumn("dbo.HouseholdMembers", "LifeStage", c => c.String());
            CreateIndex("dbo.Households", "ParticipantId", unique: true);
        }
        
        public override void Down()
        {
            DropIndex("dbo.Households", new[] { "ParticipantId" });
            DropColumn("dbo.HouseholdMembers", "LifeStage");
            DropColumn("dbo.HouseholdMembers", "IsFemale");
            DropColumn("dbo.HouseholdMembers", "ParticipantId");
            DropColumn("dbo.Households", "ParticipantId");
        }
    }
}
