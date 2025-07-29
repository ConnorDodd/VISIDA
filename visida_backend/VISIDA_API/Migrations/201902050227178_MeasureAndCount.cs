namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class MeasureAndCount : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.FoodItems", "MeasureCount", c => c.Double(nullable: false));
            AddColumn("dbo.FoodItems", "MeasureType", c => c.String());
        }
        
        public override void Down()
        {
            DropColumn("dbo.FoodItems", "MeasureType");
            DropColumn("dbo.FoodItems", "MeasureCount");
        }
    }
}
