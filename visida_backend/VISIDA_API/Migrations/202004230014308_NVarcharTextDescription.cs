namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class NVarcharTextDescription : DbMigration
    {
        public override void Up()
        {
            AlterColumn("dbo.ImageRecords", "TextDescription", c => c.String(maxLength: 1024));
        }
        
        public override void Down()
        {
            AlterColumn("dbo.ImageRecords", "TextDescription", c => c.String(maxLength: 1024, unicode: false));
        }
    }
}
