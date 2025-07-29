namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class Texdescription : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.ImageRecords", "TextDescription", c => c.String(maxLength: 1024, unicode: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.ImageRecords", "TextDescription");
        }
    }
}
