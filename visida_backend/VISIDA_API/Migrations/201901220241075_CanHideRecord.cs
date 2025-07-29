namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class CanHideRecord : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.ImageRecords", "Hidden", c => c.Boolean(nullable: false));
            AddColumn("dbo.EatRecords", "Hidden", c => c.Boolean(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.EatRecords", "Hidden");
            DropColumn("dbo.ImageRecords", "Hidden");
        }
    }
}
