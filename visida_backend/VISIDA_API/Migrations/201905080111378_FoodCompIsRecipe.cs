namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class FoodCompIsRecipe : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.FoodCompositions", "IsRecipe", c => c.Boolean(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.FoodCompositions", "IsRecipe");
        }
    }
}
