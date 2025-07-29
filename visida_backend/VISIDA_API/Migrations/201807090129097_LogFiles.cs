namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class LogFiles : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.UsageLogFiles",
                c => new
                    {
                        UsageLog_Id = c.Int(nullable: false),
                        RawData = c.String(unicode: false, storeType: "text"),
                    })
                .PrimaryKey(t => t.UsageLog_Id)
                .ForeignKey("dbo.Households", t => t.UsageLog_Id)
                .Index(t => t.UsageLog_Id);
            
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.UsageLogFiles", "UsageLog_Id", "dbo.Households");
            DropIndex("dbo.UsageLogFiles", new[] { "UsageLog_Id" });
            DropTable("dbo.UsageLogFiles");
        }
    }
}
