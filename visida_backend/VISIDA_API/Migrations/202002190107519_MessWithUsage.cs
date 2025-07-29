namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class MessWithUsage : DbMigration
    {
        public override void Up()
        {
            DropForeignKey("dbo.UsageLogFiles", "UsageLog_Id", "dbo.Households");
            DropIndex("dbo.UsageLogFiles", new[] { "UsageLog_Id" });
            DropPrimaryKey("dbo.UsageLogFiles");
            AddColumn("dbo.UsageLogFiles", "Id", c => c.Int(nullable: false, identity: true));
            AddColumn("dbo.UsageLogFiles", "LogCreationTime", c => c.DateTime(nullable: false));
            AddPrimaryKey("dbo.UsageLogFiles", "Id");
            DropColumn("dbo.UsageLogFiles", "UsageLog_Id");
        }
        
        public override void Down()
        {
            AddColumn("dbo.UsageLogFiles", "UsageLog_Id", c => c.Int(nullable: false));
            DropPrimaryKey("dbo.UsageLogFiles");
            DropColumn("dbo.UsageLogFiles", "LogCreationTime");
            DropColumn("dbo.UsageLogFiles", "Id");
            AddPrimaryKey("dbo.UsageLogFiles", "UsageLog_Id");
            CreateIndex("dbo.UsageLogFiles", "UsageLog_Id");
            AddForeignKey("dbo.UsageLogFiles", "UsageLog_Id", "dbo.Households", "Id");
        }
    }
}
