namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class TotalGramsForRecipe : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.CookRecipes", "TotalCookedGrams", c => c.Double(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.CookRecipes", "TotalCookedGrams");
        }
    }
}
