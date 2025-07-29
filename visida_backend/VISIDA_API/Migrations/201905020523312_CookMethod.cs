namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class CookMethod : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.FoodCompositions", "FoodGroupId", c => c.Int(nullable: false));
            AddColumn("dbo.CookIngredients", "CookMethod", c => c.Int(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.CookIngredients", "CookMethod");
            DropColumn("dbo.FoodCompositions", "FoodGroupId");
        }
    }
}
