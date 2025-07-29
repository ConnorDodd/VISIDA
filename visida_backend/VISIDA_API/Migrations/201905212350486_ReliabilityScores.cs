namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class ReliabilityScores : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.ReliabilityTestScores",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        FoodItemId = c.Int(nullable: false),
                        Type = c.Int(nullable: false),
                        Accuracy = c.Int(nullable: false),
                        Test_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.ReliabilityTests", t => t.Test_Id)
                .Index(t => t.Test_Id);
            
            AddColumn("dbo.ReliabilityTests", "AccuracyPercent", c => c.Double(nullable: false));
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.ReliabilityTestScores", "Test_Id", "dbo.ReliabilityTests");
            DropIndex("dbo.ReliabilityTestScores", new[] { "Test_Id" });
            DropColumn("dbo.ReliabilityTests", "AccuracyPercent");
            DropTable("dbo.ReliabilityTestScores");
        }
    }
}
