namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class Comments : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.Comments",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Text = c.String(),
                        CreatedTime = c.DateTime(nullable: false),
                        CreatedBy_Id = c.Int(),
                        ImageRecord_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.LoginUsers", t => t.CreatedBy_Id)
                .ForeignKey("dbo.ImageRecords", t => t.ImageRecord_Id)
                .Index(t => t.CreatedBy_Id)
                .Index(t => t.ImageRecord_Id);
            
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.Comments", "ImageRecord_Id", "dbo.ImageRecords");
            DropForeignKey("dbo.Comments", "CreatedBy_Id", "dbo.LoginUsers");
            DropIndex("dbo.Comments", new[] { "ImageRecord_Id" });
            DropIndex("dbo.Comments", new[] { "CreatedBy_Id" });
            DropTable("dbo.Comments");
        }
    }
}
