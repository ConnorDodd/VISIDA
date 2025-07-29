namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class CommentOverhaul : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.Comments", "HighPriority", c => c.Boolean(nullable: false));
            AddColumn("dbo.Comments", "TaskCompleted", c => c.DateTime());
            AddColumn("dbo.LoginUsers", "LastSeen", c => c.DateTime());
            DropColumn("dbo.Comments", "Seen");
        }
        
        public override void Down()
        {
            AddColumn("dbo.Comments", "Seen", c => c.Boolean(nullable: false));
            DropColumn("dbo.LoginUsers", "LastSeen");
            DropColumn("dbo.Comments", "TaskCompleted");
            DropColumn("dbo.Comments", "HighPriority");
        }
    }
}
