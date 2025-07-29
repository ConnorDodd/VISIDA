namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class CanDeleteTables : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.FoodCompositionTables", "Deleted", c => c.Boolean(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.FoodCompositionTables", "Deleted");
        }
    }
}
