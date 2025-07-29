namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class AudioCanUpdate : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.ImageRecords", "AudioUrlUpdated", c => c.String());
            AddColumn("dbo.CookRecipes", "AudioUrlUpdated", c => c.String());
        }
        
        public override void Down()
        {
            DropColumn("dbo.CookRecipes", "AudioUrlUpdated");
            DropColumn("dbo.ImageRecords", "AudioUrlUpdated");
        }
    }
}
