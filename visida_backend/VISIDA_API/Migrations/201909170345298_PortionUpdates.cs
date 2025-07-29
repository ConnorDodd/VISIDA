namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class PortionUpdates : DbMigration
    {
        public override void Up()
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
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.ImageRecords", t => t.ImageRecord_Id)
                .Index(t => t.ImageRecord_Id);
            
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.PortionUpdates", "ImageRecord_Id", "dbo.ImageRecords");
            DropIndex("dbo.PortionUpdates", new[] { "ImageRecord_Id" });
            DropTable("dbo.PortionUpdates");
        }
    }
}
