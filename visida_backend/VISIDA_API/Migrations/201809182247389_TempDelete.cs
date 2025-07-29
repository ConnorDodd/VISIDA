namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class TempDelete : DbMigration
    {
        public override void Up()
        {
            DropForeignKey("dbo.LoginUsers", "Study_Id", "dbo.Studies");
            DropForeignKey("dbo.Studies", "DeletedBy_Id", "dbo.LoginUsers");
            DropForeignKey("dbo.Studies", "LoginUser_Id", "dbo.LoginUsers");
            DropIndex("dbo.LoginUsers", new[] { "Study_Id" });
            DropIndex("dbo.Studies", new[] { "DeletedBy_Id" });
            DropIndex("dbo.Studies", new[] { "LoginUser_Id" });
            DropColumn("dbo.LoginUsers", "Study_Id");
            DropColumn("dbo.Studies", "DeletedBy_Id");
            DropColumn("dbo.Studies", "LoginUser_Id");
        }
        
        public override void Down()
        {
            AddColumn("dbo.Studies", "LoginUser_Id", c => c.Int());
            AddColumn("dbo.Studies", "DeletedBy_Id", c => c.Int());
            AddColumn("dbo.LoginUsers", "Study_Id", c => c.Int());
            CreateIndex("dbo.Studies", "LoginUser_Id");
            CreateIndex("dbo.Studies", "DeletedBy_Id");
            CreateIndex("dbo.LoginUsers", "Study_Id");
            AddForeignKey("dbo.Studies", "LoginUser_Id", "dbo.LoginUsers", "Id");
            AddForeignKey("dbo.Studies", "DeletedBy_Id", "dbo.LoginUsers", "Id");
            AddForeignKey("dbo.LoginUsers", "Study_Id", "dbo.Studies", "Id");
        }
    }
}
