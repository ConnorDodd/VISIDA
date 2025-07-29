namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class Measures : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.FoodCompositions", "OriginId", c => c.String(maxLength: 256, unicode: false));
            AddColumn("dbo.FoodCompositions", "Measures", c => c.String());
            DropColumn("dbo.FoodCompositions", "Origin");
        }
        
        public override void Down()
        {
            AddColumn("dbo.FoodCompositions", "Origin", c => c.String(maxLength: 256, unicode: false));
            DropColumn("dbo.FoodCompositions", "Measures");
            DropColumn("dbo.FoodCompositions", "OriginId");
        }
    }
}
