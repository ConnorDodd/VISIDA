namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class MovedCaptureDateToImageRecord : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.ImageRecords", "CaptureTime", c => c.DateTime(nullable: false));
            DropColumn("dbo.CookIngredients", "CaptureTime");
            DropColumn("dbo.EatRecords", "CaptureTime");
        }
        
        public override void Down()
        {
            AddColumn("dbo.EatRecords", "CaptureTime", c => c.DateTime(nullable: false));
            AddColumn("dbo.CookIngredients", "CaptureTime", c => c.DateTime(nullable: false));
            DropColumn("dbo.ImageRecords", "CaptureTime");
        }
    }
}
