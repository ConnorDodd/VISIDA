namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class LinkedRecipes : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.CookRecipeImageRecords",
                c => new
                    {
                        CookRecipe_Id = c.Int(nullable: false),
                        ImageRecord_Id = c.Int(nullable: false),
                    })
                .PrimaryKey(t => new { t.CookRecipe_Id, t.ImageRecord_Id })
                .ForeignKey("dbo.CookRecipes", t => t.CookRecipe_Id, cascadeDelete: true)
                .ForeignKey("dbo.ImageRecords", t => t.ImageRecord_Id, cascadeDelete: true)
                .Index(t => t.CookRecipe_Id)
                .Index(t => t.ImageRecord_Id);
            
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.CookRecipeImageRecords", "ImageRecord_Id", "dbo.ImageRecords");
            DropForeignKey("dbo.CookRecipeImageRecords", "CookRecipe_Id", "dbo.CookRecipes");
            DropIndex("dbo.CookRecipeImageRecords", new[] { "ImageRecord_Id" });
            DropIndex("dbo.CookRecipeImageRecords", new[] { "CookRecipe_Id" });
            DropTable("dbo.CookRecipeImageRecords");
        }
    }
}
