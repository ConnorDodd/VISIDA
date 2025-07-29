namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class StartPriority : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.FoodItems", "Priority", c => c.Int(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.FoodItems", "Priority");
        }
    }
}
