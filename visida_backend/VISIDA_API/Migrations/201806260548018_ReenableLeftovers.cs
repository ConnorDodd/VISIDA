namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class ReenableLeftovers : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.ImageRecords", "IsLeftovers", c => c.Boolean(nullable: false));
            AddColumn("dbo.EatRecords", "Leftovers_Id", c => c.Int());
            CreateIndex("dbo.EatRecords", "Leftovers_Id");
            AddForeignKey("dbo.EatRecords", "Leftovers_Id", "dbo.ImageRecords", "Id");
            DropColumn("dbo.EatRecords", "IsLeftover");
        }
        
        public override void Down()
        {
            AddColumn("dbo.EatRecords", "IsLeftover", c => c.Boolean(nullable: false));
            DropForeignKey("dbo.EatRecords", "Leftovers_Id", "dbo.ImageRecords");
            DropIndex("dbo.EatRecords", new[] { "Leftovers_Id" });
            DropColumn("dbo.EatRecords", "Leftovers_Id");
            DropColumn("dbo.ImageRecords", "IsLeftovers");
        }
    }
}
