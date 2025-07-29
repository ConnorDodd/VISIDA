namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class HideRecipe : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.CookRecipes", "Hidden", c => c.Boolean(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.CookRecipes", "Hidden");
        }
    }
}
