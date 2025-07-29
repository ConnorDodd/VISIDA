namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class TimingHasStudy : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.Timings", "Study_Id", c => c.Int());
            CreateIndex("dbo.Timings", "Study_Id");
            AddForeignKey("dbo.Timings", "Study_Id", "dbo.Studies", "Id");
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.Timings", "Study_Id", "dbo.Studies");
            DropIndex("dbo.Timings", new[] { "Study_Id" });
            DropColumn("dbo.Timings", "Study_Id");
        }
    }
}
