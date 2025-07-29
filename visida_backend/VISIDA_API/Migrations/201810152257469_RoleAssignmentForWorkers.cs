namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class RoleAssignmentForWorkers : DbMigration
    {
        public override void Up()
        {
            DropForeignKey("dbo.StudyLoginUsers", "Study_Id", "dbo.Studies");
            DropForeignKey("dbo.StudyLoginUsers", "LoginUser_Id", "dbo.LoginUsers");
            DropIndex("dbo.StudyLoginUsers", new[] { "Study_Id" });
            DropIndex("dbo.StudyLoginUsers", new[] { "LoginUser_Id" });
            CreateTable(
                "dbo.WorkAssignations",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        AccessLevel = c.Int(nullable: false),
                        LoginUser_Id = c.Int(),
                        Study_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.LoginUsers", t => t.LoginUser_Id)
                .ForeignKey("dbo.Studies", t => t.Study_Id)
                .Index(t => t.LoginUser_Id)
                .Index(t => t.Study_Id);
            
            DropTable("dbo.StudyLoginUsers");
        }
        
        public override void Down()
        {
            CreateTable(
                "dbo.StudyLoginUsers",
                c => new
                    {
                        Study_Id = c.Int(nullable: false),
                        LoginUser_Id = c.Int(nullable: false),
                    })
                .PrimaryKey(t => new { t.Study_Id, t.LoginUser_Id });
            
            DropForeignKey("dbo.WorkAssignations", "Study_Id", "dbo.Studies");
            DropForeignKey("dbo.WorkAssignations", "LoginUser_Id", "dbo.LoginUsers");
            DropIndex("dbo.WorkAssignations", new[] { "Study_Id" });
            DropIndex("dbo.WorkAssignations", new[] { "LoginUser_Id" });
            DropTable("dbo.WorkAssignations");
            CreateIndex("dbo.StudyLoginUsers", "LoginUser_Id");
            CreateIndex("dbo.StudyLoginUsers", "Study_Id");
            AddForeignKey("dbo.StudyLoginUsers", "LoginUser_Id", "dbo.LoginUsers", "Id", cascadeDelete: true);
            AddForeignKey("dbo.StudyLoginUsers", "Study_Id", "dbo.Studies", "Id", cascadeDelete: true);
        }
    }
}
