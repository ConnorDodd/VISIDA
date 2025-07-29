namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class MeasureData : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.ExampleFoodCompositions",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Name = c.String(maxLength: 256, unicode: false),
                    })
                .PrimaryKey(t => t.Id);
            
            CreateTable(
                "dbo.Measures",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Description = c.String(maxLength: 256, unicode: false),
                        ImageUrl = c.String(maxLength: 256, unicode: false),
                        SizeGrams = c.Double(nullable: false),
                        FoodType_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.ExampleFoodCompositions", t => t.FoodType_Id)
                .Index(t => t.FoodType_Id);
            
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.Measures", "FoodType_Id", "dbo.ExampleFoodCompositions");
            DropIndex("dbo.Measures", new[] { "FoodType_Id" });
            DropTable("dbo.Measures");
            DropTable("dbo.ExampleFoodCompositions");
        }
    }
}
