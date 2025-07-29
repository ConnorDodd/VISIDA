namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class AddCommentReplies : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.Comments", "ReplyTo_Id", c => c.Int());
            CreateIndex("dbo.Comments", "ReplyTo_Id");
            AddForeignKey("dbo.Comments", "ReplyTo_Id", "dbo.Comments", "Id");
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.Comments", "ReplyTo_Id", "dbo.Comments");
            DropIndex("dbo.Comments", new[] { "ReplyTo_Id" });
            DropColumn("dbo.Comments", "ReplyTo_Id");
        }
    }
}
