namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class ConversionHistory : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.ConversionHistories",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Household_Id = c.Int(),
                        ImageRecord_Id = c.Int(),
                        Recipe_Id = c.Int(),
                        CreatedTime = c.DateTime(nullable: false),
                        ConversionType = c.Int(nullable: false),
                        CreatedBy_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.LoginUsers", t => t.CreatedBy_Id)
                .ForeignKey("dbo.Households", t => t.Household_Id)
                .ForeignKey("dbo.ImageRecords", t => t.ImageRecord_Id)
                .ForeignKey("dbo.CookRecipes", t => t.Recipe_Id)
                .Index(t => t.Household_Id)
                .Index(t => t.ImageRecord_Id)
                .Index(t => t.Recipe_Id)
                .Index(t => t.CreatedBy_Id);
            
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.ConversionHistories", "Recipe_Id", "dbo.CookRecipes");
            DropForeignKey("dbo.ConversionHistories", "ImageRecord_Id", "dbo.ImageRecords");
            DropForeignKey("dbo.ConversionHistories", "Household_Id", "dbo.Households");
            DropForeignKey("dbo.ConversionHistories", "CreatedBy_Id", "dbo.LoginUsers");
            DropIndex("dbo.ConversionHistories", new[] { "CreatedBy_Id" });
            DropIndex("dbo.ConversionHistories", new[] { "Recipe_Id" });
            DropIndex("dbo.ConversionHistories", new[] { "ImageRecord_Id" });
            DropIndex("dbo.ConversionHistories", new[] { "Household_Id" });
            DropTable("dbo.ConversionHistories");
        }
    }
}
