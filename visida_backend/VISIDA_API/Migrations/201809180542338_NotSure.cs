namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class NotSure : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.LoginUsers", "LastFeedRefresh", c => c.DateTime());
        }
        
        public override void Down()
        {
            DropColumn("dbo.LoginUsers", "LastFeedRefresh");
        }
    }
}
