namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class RecipeTranscript : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.CookRecipes", "NTranscript", c => c.String(maxLength: 1024));
            AddColumn("dbo.CookRecipes", "Transcript", c => c.String(maxLength: 1024, unicode: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.CookRecipes", "Transcript");
            DropColumn("dbo.CookRecipes", "NTranscript");
        }
    }
}
