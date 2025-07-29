namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class AllowDuplicateImageName : DbMigration
    {
        public override void Up()
        {
            DropIndex("dbo.ImageRecords", new[] { "ImageName" });
            CreateIndex("dbo.ImageRecords", "ImageName");
        }
        
        public override void Down()
        {
            DropIndex("dbo.ImageRecords", new[] { "ImageName" });
            CreateIndex("dbo.ImageRecords", "ImageName", unique: true);
        }
    }
}
