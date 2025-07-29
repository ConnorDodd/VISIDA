namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class PasswordResetting : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.ResetPasswordRequests",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Key = c.String(maxLength: 32, unicode: false),
                        Used = c.Boolean(nullable: false),
                        CreatedTime = c.DateTime(nullable: false),
                        LoginUser_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.LoginUsers", t => t.LoginUser_Id)
                .Index(t => t.Key, unique: true)
                .Index(t => t.LoginUser_Id);
            
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.ResetPasswordRequests", "LoginUser_Id", "dbo.LoginUsers");
            DropIndex("dbo.ResetPasswordRequests", new[] { "LoginUser_Id" });
            DropIndex("dbo.ResetPasswordRequests", new[] { "Key" });
            DropTable("dbo.ResetPasswordRequests");
        }
    }
}
