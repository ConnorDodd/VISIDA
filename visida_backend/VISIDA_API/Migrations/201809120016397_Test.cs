namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class Test : DbMigration
    {
        public override void Up()
        {
            DropForeignKey("dbo.Measures", "FoodTypeId", "dbo.ExampleFoodCompositions");
            DropIndex("dbo.Measures", new[] { "FoodTypeId" });
            RenameColumn(table: "dbo.Measures", name: "FoodTypeId", newName: "FoodType_Id");
            AlterColumn("dbo.Measures", "FoodType_Id", c => c.Int());
            AlterColumn("dbo.Measures", "Description", c => c.String(maxLength: 256, unicode: false));
            AlterColumn("dbo.Measures", "ImageUrl", c => c.String(maxLength: 256, unicode: false));
            CreateIndex("dbo.Measures", "FoodType_Id");
            AddForeignKey("dbo.Measures", "FoodType_Id", "dbo.ExampleFoodCompositions", "Id");
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.Measures", "FoodType_Id", "dbo.ExampleFoodCompositions");
            DropIndex("dbo.Measures", new[] { "FoodType_Id" });
            AlterColumn("dbo.Measures", "ImageUrl", c => c.String(nullable: false, maxLength: 256, unicode: false));
            AlterColumn("dbo.Measures", "Description", c => c.String(nullable: false, maxLength: 256, unicode: false));
            AlterColumn("dbo.Measures", "FoodType_Id", c => c.Int(nullable: false));
            RenameColumn(table: "dbo.Measures", name: "FoodType_Id", newName: "FoodTypeId");
            CreateIndex("dbo.Measures", "FoodTypeId");
            AddForeignKey("dbo.Measures", "FoodTypeId", "dbo.ExampleFoodCompositions", "Id", cascadeDelete: true);
        }
    }
}
