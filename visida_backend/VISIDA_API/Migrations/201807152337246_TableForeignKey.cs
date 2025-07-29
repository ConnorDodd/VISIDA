namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class TableForeignKey : DbMigration
    {
        public override void Up()
        {
            DropForeignKey("dbo.FoodCompositions", "Table_Id", "dbo.FoodCompositionTables");
            DropIndex("dbo.FoodCompositions", new[] { "Table_Id" });
            AlterColumn("dbo.FoodCompositions", "Table_Id", c => c.Int(nullable: false));
            CreateIndex("dbo.FoodCompositions", "Table_Id");
            AddForeignKey("dbo.FoodCompositions", "Table_Id", "dbo.FoodCompositionTables", "Id", cascadeDelete: true);
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.FoodCompositions", "Table_Id", "dbo.FoodCompositionTables");
            DropIndex("dbo.FoodCompositions", new[] { "Table_Id" });
            AlterColumn("dbo.FoodCompositions", "Table_Id", c => c.Int());
            CreateIndex("dbo.FoodCompositions", "Table_Id");
            AddForeignKey("dbo.FoodCompositions", "Table_Id", "dbo.FoodCompositionTables", "Id");
        }
    }
}
