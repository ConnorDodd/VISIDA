namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class CanModifyImages : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.ImageRecords", "ImageUrlUpdated", c => c.String(maxLength: 256, unicode: false));
            AddColumn("dbo.CookRecipes", "ImageUrlUpdated", c => c.String(maxLength: 256, unicode: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.CookRecipes", "ImageUrlUpdated");
            DropColumn("dbo.ImageRecords", "ImageUrlUpdated");
        }
    }
}
