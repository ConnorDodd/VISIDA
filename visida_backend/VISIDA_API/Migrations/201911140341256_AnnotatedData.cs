namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class AnnotatedData : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.ImageRecords", "AnnotationStatus", c => c.Int(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.ImageRecords", "AnnotationStatus");
        }
    }
}
