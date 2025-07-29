namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class ThumbnailImage : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.ImageRecords", "ImageThumbUrl", c => c.String(maxLength: 256, unicode: false));
            AddColumn("dbo.CookRecipes", "ImageThumbUrl", c => c.String(maxLength: 256, unicode: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.CookRecipes", "ImageThumbUrl");
            DropColumn("dbo.ImageRecords", "ImageThumbUrl");
        }
    }
}
