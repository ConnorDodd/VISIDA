namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class OptionalAudioAndIamge : DbMigration
    {
        public override void Up()
        {
            DropIndex("dbo.ImageRecords", new[] { "ImageName" });
            DropIndex("dbo.ImageRecords", new[] { "AudioName" });
            AlterColumn("dbo.ImageRecords", "ImageName", c => c.String(maxLength: 256, unicode: false));
            AlterColumn("dbo.ImageRecords", "AudioName", c => c.String(maxLength: 256, unicode: false));
            CreateIndex("dbo.ImageRecords", "ImageName", unique: true);
            CreateIndex("dbo.ImageRecords", "AudioName", unique: true);
        }
        
        public override void Down()
        {
            DropIndex("dbo.ImageRecords", new[] { "AudioName" });
            DropIndex("dbo.ImageRecords", new[] { "ImageName" });
            AlterColumn("dbo.ImageRecords", "AudioName", c => c.String(nullable: false, maxLength: 256, unicode: false));
            AlterColumn("dbo.ImageRecords", "ImageName", c => c.String(nullable: false, maxLength: 256, unicode: false));
            CreateIndex("dbo.ImageRecords", "AudioName", unique: true);
            CreateIndex("dbo.ImageRecords", "ImageName", unique: true);
        }
    }
}
