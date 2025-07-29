namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class ListOriginSources : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.CookRecipes", "OriginId", c => c.Int(nullable: false));
            AddColumn("dbo.CookIngredients", "OriginId", c => c.Int(nullable: false));
            AddColumn("dbo.EatOccasions", "OriginId", c => c.Int(nullable: false));
            AddColumn("dbo.EatRecords", "OriginId", c => c.Int(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.EatRecords", "OriginId");
            DropColumn("dbo.EatOccasions", "OriginId");
            DropColumn("dbo.CookIngredients", "OriginId");
            DropColumn("dbo.CookRecipes", "OriginId");
        }
    }
}
