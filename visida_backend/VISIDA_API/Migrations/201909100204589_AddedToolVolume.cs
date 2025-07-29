namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class AddedToolVolume : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.FoodItems", "ToolMeasure", c => c.Double(nullable: false));
            AddColumn("dbo.FoodItems", "ToolSource", c => c.String());
        }
        
        public override void Down()
        {
            DropColumn("dbo.FoodItems", "ToolSource");
            DropColumn("dbo.FoodItems", "ToolMeasure");
        }
    }
}
