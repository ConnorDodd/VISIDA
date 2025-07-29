namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class YieldFactors : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.FoodCompositions", "YieldWater", c => c.Double());
            AddColumn("dbo.FoodCompositions", "YieldStoveTop", c => c.Double());
            AddColumn("dbo.FoodCompositions", "YieldOven", c => c.Double());
        }
        
        public override void Down()
        {
            DropColumn("dbo.FoodCompositions", "YieldOven");
            DropColumn("dbo.FoodCompositions", "YieldStoveTop");
            DropColumn("dbo.FoodCompositions", "YieldWater");
        }
    }
}
