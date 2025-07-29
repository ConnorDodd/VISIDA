namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class CommentData : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.Comments", "Flag", c => c.Int(nullable: false));
            AddColumn("dbo.Comments", "Seen", c => c.Boolean(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.Comments", "Seen");
            DropColumn("dbo.Comments", "Flag");
        }
    }
}
