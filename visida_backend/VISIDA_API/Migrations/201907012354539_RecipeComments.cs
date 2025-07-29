namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class RecipeComments : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.Comments", "Recipe_Id", c => c.Int());
            CreateIndex("dbo.Comments", "Recipe_Id");
            AddForeignKey("dbo.Comments", "Recipe_Id", "dbo.CookRecipes", "Id");
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.Comments", "Recipe_Id", "dbo.CookRecipes");
            DropIndex("dbo.Comments", new[] { "Recipe_Id" });
            DropColumn("dbo.Comments", "Recipe_Id");
        }
    }
}
