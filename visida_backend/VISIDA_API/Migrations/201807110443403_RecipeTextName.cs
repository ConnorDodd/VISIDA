namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class RecipeTextName : DbMigration
    {
        public override void Up()
        {
            DropIndex("dbo.ImageRecords", new[] { "AudioName" });
            DropIndex("dbo.CookRecipes", new[] { "AudioName" });
            AddColumn("dbo.CookRecipes", "Description", c => c.String(maxLength: 256, unicode: false));
            AlterColumn("dbo.CookRecipes", "AudioName", c => c.String(maxLength: 256, unicode: false));
        }
        
        public override void Down()
        {
            AlterColumn("dbo.CookRecipes", "AudioName", c => c.String(nullable: false, maxLength: 256, unicode: false));
            DropColumn("dbo.CookRecipes", "Description");
            CreateIndex("dbo.CookRecipes", "AudioName", unique: true);
            CreateIndex("dbo.ImageRecords", "AudioName", unique: true);
        }
    }
}
