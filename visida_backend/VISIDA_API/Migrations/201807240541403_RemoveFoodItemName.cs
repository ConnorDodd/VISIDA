namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class RemoveFoodItemName : DbMigration
    {
        public override void Up()
        {
            DropColumn("dbo.FoodItems", "Name");
        }
        
        public override void Down()
        {
            AddColumn("dbo.FoodItems", "Name", c => c.String(nullable: false, maxLength: 256));
        }
    }
}
