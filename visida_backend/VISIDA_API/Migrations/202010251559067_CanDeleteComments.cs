namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class CanDeleteComments : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.Comments", "Hidden", c => c.Boolean(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.Comments", "Hidden");
        }
    }
}
