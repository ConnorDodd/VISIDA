namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class RecipeHasFC : DbMigration
    {
        public override void Up()
        {
            DropForeignKey("dbo.FoodCompositions", "Table_Id", "dbo.FoodCompositionTables");
            DropIndex("dbo.FoodCompositions", new[] { "Table_Id" });
            AddColumn("dbo.CookRecipes", "CaptureTime", c => c.DateTime(nullable: false));
            AddColumn("dbo.CookRecipes", "FoodComposition_Id", c => c.Int());
            AlterColumn("dbo.FoodCompositions", "Table_Id", c => c.Int());
            CreateIndex("dbo.FoodCompositions", "Table_Id");
            CreateIndex("dbo.CookRecipes", "FoodComposition_Id");
            AddForeignKey("dbo.CookRecipes", "FoodComposition_Id", "dbo.FoodCompositions", "Id");
            AddForeignKey("dbo.FoodCompositions", "Table_Id", "dbo.FoodCompositionTables", "Id");
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.FoodCompositions", "Table_Id", "dbo.FoodCompositionTables");
            DropForeignKey("dbo.CookRecipes", "FoodComposition_Id", "dbo.FoodCompositions");
            DropIndex("dbo.CookRecipes", new[] { "FoodComposition_Id" });
            DropIndex("dbo.FoodCompositions", new[] { "Table_Id" });
            AlterColumn("dbo.FoodCompositions", "Table_Id", c => c.Int(nullable: false));
            DropColumn("dbo.CookRecipes", "FoodComposition_Id");
            DropColumn("dbo.CookRecipes", "CaptureTime");
            CreateIndex("dbo.FoodCompositions", "Table_Id");
            AddForeignKey("dbo.FoodCompositions", "Table_Id", "dbo.FoodCompositionTables", "Id", cascadeDelete: true);
        }
    }
}
