namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class AddStandardMeasures : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.StandardMeasures",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Name = c.String(nullable: false, maxLength: 256, unicode: false),
                        MLs = c.Double(nullable: false),
                        Table_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.FoodCompositionTables", t => t.Table_Id)
                .Index(t => t.Table_Id);
            
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.StandardMeasures", "Table_Id", "dbo.FoodCompositionTables");
            DropIndex("dbo.StandardMeasures", new[] { "Table_Id" });
            DropTable("dbo.StandardMeasures");
        }
    }
}
