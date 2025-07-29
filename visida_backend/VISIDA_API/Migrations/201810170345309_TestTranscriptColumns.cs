namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class TestTranscriptColumns : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.ImageRecords", "ManualTranscript", c => c.String(maxLength: 1024));
            AddColumn("dbo.ImageRecords", "Translation", c => c.String(maxLength: 1024, unicode: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.ImageRecords", "Translation");
            DropColumn("dbo.ImageRecords", "ManualTranscript");
        }
    }
}
