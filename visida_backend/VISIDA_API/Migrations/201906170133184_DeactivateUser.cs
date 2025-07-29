namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class DeactivateUser : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.LoginUsers", "IsActive", c => c.Boolean(nullable: false, defaultValue: true));
        }
        
        public override void Down()
        {
            DropColumn("dbo.LoginUsers", "IsActive");
        }
    }
}
