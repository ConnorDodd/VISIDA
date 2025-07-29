namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class AdviceHasTable : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.Advices", "TableId", c => c.Int(nullable: false));
            CreateIndex("dbo.Advices", "TableId");
            AddForeignKey("dbo.Advices", "TableId", "dbo.FoodCompositionTables", "Id", cascadeDelete: true);
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.Advices", "TableId", "dbo.FoodCompositionTables");
            DropIndex("dbo.Advices", new[] { "TableId" });
            DropColumn("dbo.Advices", "TableId");
        }
    }
}
