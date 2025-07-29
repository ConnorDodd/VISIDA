namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class CookRecipeSources : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.CookRecipes", "IsSource", c => c.Boolean(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.CookRecipes", "IsSource");
        }
    }
}
