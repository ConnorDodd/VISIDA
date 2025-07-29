namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class AnnotationStatus : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.ImageRecords", "AnnotatedStatus", c => c.Int(nullable: false));
            AddColumn("dbo.CookRecipes", "AnnotatedStatus", c => c.Int(nullable: false));
            AddColumn("dbo.CookIngredients", "AnnotatedStatus", c => c.Int(nullable: false));
            AddColumn("dbo.EatOccasions", "IsBreastfeedOccasion", c => c.Boolean(nullable: false));
            AddColumn("dbo.EatOccasions", "AnnotatedStatus", c => c.Int(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.EatOccasions", "AnnotatedStatus");
            DropColumn("dbo.EatOccasions", "IsBreastfeedOccasion");
            DropColumn("dbo.CookIngredients", "AnnotatedStatus");
            DropColumn("dbo.CookRecipes", "AnnotatedStatus");
            DropColumn("dbo.ImageRecords", "AnnotatedStatus");
        }
    }
}
