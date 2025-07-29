namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class RenameRecipeDesc : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.CookRecipes", "TextDescription", c => c.String(maxLength: 1000, unicode: false));
            DropColumn("dbo.CookRecipes", "Description");
        }
        
        public override void Down()
        {
            AddColumn("dbo.CookRecipes", "Description", c => c.String(maxLength: 256, unicode: false));
            DropColumn("dbo.CookRecipes", "TextDescription");
        }
    }
}
