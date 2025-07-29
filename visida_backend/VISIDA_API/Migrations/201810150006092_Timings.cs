namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class Timings : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.Timings",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Type = c.Int(nullable: false),
                        TimeTaken = c.Int(nullable: false),
                        CreatedTime = c.DateTime(nullable: false),
                        CreatedBy_Id = c.Int(),
                        FoodItem_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.LoginUsers", t => t.CreatedBy_Id)
                .ForeignKey("dbo.FoodItems", t => t.FoodItem_Id, cascadeDelete: true)
                .Index(t => t.CreatedBy_Id)
                .Index(t => t.FoodItem_Id);
            
            DropColumn("dbo.FoodItems", "QuantityServings");
            DropColumn("dbo.FoodItems", "QuantityML");
        }
        
        public override void Down()
        {
            AddColumn("dbo.FoodItems", "QuantityML", c => c.Int(nullable: false));
            AddColumn("dbo.FoodItems", "QuantityServings", c => c.Int(nullable: false));
            DropForeignKey("dbo.Timings", "FoodItem_Id", "dbo.FoodItems");
            DropForeignKey("dbo.Timings", "CreatedBy_Id", "dbo.LoginUsers");
            DropIndex("dbo.Timings", new[] { "FoodItem_Id" });
            DropIndex("dbo.Timings", new[] { "CreatedBy_Id" });
            DropTable("dbo.Timings");
        }
    }
}
