namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class MeasureForeignKey : DbMigration
    {
        public override void Up()
        {
            DropForeignKey("dbo.Measures", "FoodType_Id", "dbo.ExampleFoodCompositions");
            DropIndex("dbo.Measures", new[] { "FoodType_Id" });
            RenameColumn(table: "dbo.Measures", name: "FoodType_Id", newName: "FoodTypeId");
            AlterColumn("dbo.Measures", "Description", c => c.String(nullable: false, maxLength: 256, unicode: false));
            AlterColumn("dbo.Measures", "ImageUrl", c => c.String(nullable: false, maxLength: 256, unicode: false));
            AlterColumn("dbo.Measures", "FoodTypeId", c => c.Int(nullable: false));
            CreateIndex("dbo.Measures", "FoodTypeId");
            AddForeignKey("dbo.Measures", "FoodTypeId", "dbo.ExampleFoodCompositions", "Id", cascadeDelete: true);
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.Measures", "FoodTypeId", "dbo.ExampleFoodCompositions");
            DropIndex("dbo.Measures", new[] { "FoodTypeId" });
            AlterColumn("dbo.Measures", "FoodTypeId", c => c.Int());
            AlterColumn("dbo.Measures", "ImageUrl", c => c.String(maxLength: 256, unicode: false));
            AlterColumn("dbo.Measures", "Description", c => c.String(maxLength: 256, unicode: false));
            RenameColumn(table: "dbo.Measures", name: "FoodTypeId", newName: "FoodType_Id");
            CreateIndex("dbo.Measures", "FoodType_Id");
            AddForeignKey("dbo.Measures", "FoodType_Id", "dbo.ExampleFoodCompositions", "Id");
        }
    }
}
