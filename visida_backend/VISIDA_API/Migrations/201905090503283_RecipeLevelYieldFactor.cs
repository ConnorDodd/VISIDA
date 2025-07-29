namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class RecipeLevelYieldFactor : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.CookRecipes", "YieldFactor", c => c.Double());
            AddColumn("dbo.CookRecipes", "YieldFactorSource", c => c.String());
        }
        
        public override void Down()
        {
            DropColumn("dbo.CookRecipes", "YieldFactorSource");
            DropColumn("dbo.CookRecipes", "YieldFactor");
        }
    }
}
