namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class History : DbMigration
    {
        public override void Up()
        {
            DropForeignKey("dbo.PortionUpdates", "ImageRecord_Id", "dbo.ImageRecords");
            DropIndex("dbo.PortionUpdates", new[] { "ImageRecord_Id" });
            CreateTable(
                "dbo.RecordHistories",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Time = c.DateTime(nullable: false),
                        Action = c.Int(nullable: false),
                        FoodItemId = c.Int(),
                        RejectReason = c.Int(nullable: false),
                        FoodCompositionId = c.Int(),
                        QuantityGrams = c.Double(),
                        ToolMeasure = c.Double(),
                        ToolSource = c.String(),
                        User_Id = c.Int(),
                        ImageRecord_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.LoginUsers", t => t.User_Id)
                .ForeignKey("dbo.ImageRecords", t => t.ImageRecord_Id)
                .Index(t => t.User_Id)
                .Index(t => t.ImageRecord_Id);
            
            DropTable("dbo.PortionUpdates");
        }
        
        public override void Down()
        {
            CreateTable(
                "dbo.PortionUpdates",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        UserId = c.Int(nullable: false),
                        FoodItemId = c.Int(nullable: false),
                        Message = c.String(),
                        Volume = c.Double(nullable: false),
                        ImageRecord_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id);
            
            DropForeignKey("dbo.RecordHistories", "ImageRecord_Id", "dbo.ImageRecords");
            DropForeignKey("dbo.RecordHistories", "User_Id", "dbo.LoginUsers");
            DropIndex("dbo.RecordHistories", new[] { "ImageRecord_Id" });
            DropIndex("dbo.RecordHistories", new[] { "User_Id" });
            DropTable("dbo.RecordHistories");
            CreateIndex("dbo.PortionUpdates", "ImageRecord_Id");
            AddForeignKey("dbo.PortionUpdates", "ImageRecord_Id", "dbo.ImageRecords", "Id");
        }
    }
}
