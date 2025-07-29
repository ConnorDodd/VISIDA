namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class SplitAccuracy : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.ReliabilityTests", "IdentificationAccuracy", c => c.Double(nullable: false));
            AddColumn("dbo.ReliabilityTests", "QuantificationAccuracy", c => c.Double(nullable: false));
            DropColumn("dbo.ReliabilityTests", "AccuracyPercent");
        }
        
        public override void Down()
        {
            AddColumn("dbo.ReliabilityTests", "AccuracyPercent", c => c.Double(nullable: false));
            DropColumn("dbo.ReliabilityTests", "QuantificationAccuracy");
            DropColumn("dbo.ReliabilityTests", "IdentificationAccuracy");
        }
    }
}
