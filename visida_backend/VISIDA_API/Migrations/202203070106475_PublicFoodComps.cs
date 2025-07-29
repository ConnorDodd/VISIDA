namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class PublicFoodComps : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.FoodCompositionTables", "IsPublic", c => c.Boolean(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.FoodCompositionTables", "IsPublic");
        }
    }
}
