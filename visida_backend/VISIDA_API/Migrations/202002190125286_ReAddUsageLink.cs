namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class ReAddUsageLink : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.Households", "UsageLog_Id", c => c.Int());
            CreateIndex("dbo.Households", "UsageLog_Id");
            AddForeignKey("dbo.Households", "UsageLog_Id", "dbo.UsageLogFiles", "Id");
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.Households", "UsageLog_Id", "dbo.UsageLogFiles");
            DropIndex("dbo.Households", new[] { "UsageLog_Id" });
            DropColumn("dbo.Households", "UsageLog_Id");
        }
    }
}
