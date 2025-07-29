namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class ReliabilityTesting : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.ReliabilityTests",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        TestType = c.Int(nullable: false),
                        AssignedTime = c.DateTime(nullable: false),
                        Deleted = c.Boolean(nullable: false),
                        AssignedTo_Id = c.Int(),
                        ImageRecord_Id = c.Int(),
                        KnownRecord_Id = c.Int(),
                        Rule_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.LoginUsers", t => t.AssignedTo_Id)
                .ForeignKey("dbo.ImageRecords", t => t.ImageRecord_Id)
                .ForeignKey("dbo.ImageRecords", t => t.KnownRecord_Id)
                .ForeignKey("dbo.ReliabilityTestRules", t => t.Rule_Id)
                .Index(t => t.AssignedTo_Id)
                .Index(t => t.ImageRecord_Id)
                .Index(t => t.KnownRecord_Id)
                .Index(t => t.Rule_Id);
            
            CreateTable(
                "dbo.ReliabilityTestRules",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Study_Id = c.Int(nullable: false),
                        User_Id = c.Int(nullable: false),
                        StartDate = c.DateTime(nullable: false),
                        RepeatDate = c.DateTime(),
                        TestType = c.Int(nullable: false),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.Studies", t => t.Study_Id, cascadeDelete: true)
                .ForeignKey("dbo.LoginUsers", t => t.User_Id, cascadeDelete: true)
                .Index(t => t.Study_Id)
                .Index(t => t.User_Id);
            
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.ReliabilityTestRules", "User_Id", "dbo.LoginUsers");
            DropForeignKey("dbo.ReliabilityTests", "Rule_Id", "dbo.ReliabilityTestRules");
            DropForeignKey("dbo.ReliabilityTestRules", "Study_Id", "dbo.Studies");
            DropForeignKey("dbo.ReliabilityTests", "KnownRecord_Id", "dbo.ImageRecords");
            DropForeignKey("dbo.ReliabilityTests", "ImageRecord_Id", "dbo.ImageRecords");
            DropForeignKey("dbo.ReliabilityTests", "AssignedTo_Id", "dbo.LoginUsers");
            DropIndex("dbo.ReliabilityTestRules", new[] { "User_Id" });
            DropIndex("dbo.ReliabilityTestRules", new[] { "Study_Id" });
            DropIndex("dbo.ReliabilityTests", new[] { "Rule_Id" });
            DropIndex("dbo.ReliabilityTests", new[] { "KnownRecord_Id" });
            DropIndex("dbo.ReliabilityTests", new[] { "ImageRecord_Id" });
            DropIndex("dbo.ReliabilityTests", new[] { "AssignedTo_Id" });
            DropTable("dbo.ReliabilityTestRules");
            DropTable("dbo.ReliabilityTests");
        }
    }
}
