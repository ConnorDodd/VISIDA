namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class Track24HR : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.ImageRecords", "Is24HR", c => c.Boolean(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.ImageRecords", "Is24HR");
        }
    }
}
