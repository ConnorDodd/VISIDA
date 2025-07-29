namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class AddEmail : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.LoginUsers", "Email", c => c.String(maxLength: 256));
        }
        
        public override void Down()
        {
            DropColumn("dbo.LoginUsers", "Email");
        }
    }
}
