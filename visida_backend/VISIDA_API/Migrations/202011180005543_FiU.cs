namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class FiU : DbMigration
    {
        public override void Up()
        {
            CreateIndex("dbo.RecordHistories", "FoodItemId");
            AddForeignKey("dbo.RecordHistories", "FoodItemId", "dbo.FoodItems", "Id");
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.RecordHistories", "FoodItemId", "dbo.FoodItems");
            DropIndex("dbo.RecordHistories", new[] { "FoodItemId" });
        }
    }
}
