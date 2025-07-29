namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class RenameReferenceImagesAndAddTable : DbMigration
    {
        public override void Up()
        {
            DropForeignKey("dbo.Measures", "FoodTypeId", "dbo.ExampleFoodCompositions");
            DropIndex("dbo.Measures", new[] { "FoodTypeId" });
            CreateTable(
                "dbo.ReferenceImages",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        FoodTypeId = c.Int(nullable: false),
                        OriginId = c.String(maxLength: 256, unicode: false),
                        Description = c.String(maxLength: 256, unicode: false),
                        ImageUrl = c.String(nullable: false, maxLength: 256, unicode: false),
                        SizeGrams = c.Double(nullable: false),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.ReferenceImageTypes", t => t.FoodTypeId, cascadeDelete: true)
                .Index(t => t.FoodTypeId);
            
            CreateTable(
                "dbo.ReferenceImageTypes",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Name = c.String(nullable: false, maxLength: 256),
                        Table_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.FoodCompositionTables", t => t.Table_Id)
                .Index(t => t.Table_Id);
            
            DropTable("dbo.ExampleFoodCompositions");
            DropTable("dbo.Measures");
        }
        
        public override void Down()
        {
            CreateTable(
                "dbo.Measures",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        FoodTypeId = c.Int(nullable: false),
                        Description = c.String(maxLength: 256, unicode: false),
                        ImageUrl = c.String(nullable: false, maxLength: 256, unicode: false),
                        SizeGrams = c.Double(nullable: false),
                    })
                .PrimaryKey(t => t.Id);
            
            CreateTable(
                "dbo.ExampleFoodCompositions",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Name = c.String(nullable: false, maxLength: 256),
                    })
                .PrimaryKey(t => t.Id);
            
            DropForeignKey("dbo.ReferenceImages", "FoodTypeId", "dbo.ReferenceImageTypes");
            DropForeignKey("dbo.ReferenceImageTypes", "Table_Id", "dbo.FoodCompositionTables");
            DropIndex("dbo.ReferenceImageTypes", new[] { "Table_Id" });
            DropIndex("dbo.ReferenceImages", new[] { "FoodTypeId" });
            DropTable("dbo.ReferenceImageTypes");
            DropTable("dbo.ReferenceImages");
            CreateIndex("dbo.Measures", "FoodTypeId");
            AddForeignKey("dbo.Measures", "FoodTypeId", "dbo.ExampleFoodCompositions", "Id", cascadeDelete: true);
        }
    }
}
