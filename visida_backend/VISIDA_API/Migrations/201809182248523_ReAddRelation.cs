namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class ReAddRelation : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.StudyLoginUsers",
                c => new
                    {
                        Study_Id = c.Int(nullable: false),
                        LoginUser_Id = c.Int(nullable: false),
                    })
                .PrimaryKey(t => new { t.Study_Id, t.LoginUser_Id })
                .ForeignKey("dbo.Studies", t => t.Study_Id, cascadeDelete: true)
                .ForeignKey("dbo.LoginUsers", t => t.LoginUser_Id, cascadeDelete: true)
                .Index(t => t.Study_Id)
                .Index(t => t.LoginUser_Id);
            
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.StudyLoginUsers", "LoginUser_Id", "dbo.LoginUsers");
            DropForeignKey("dbo.StudyLoginUsers", "Study_Id", "dbo.Studies");
            DropIndex("dbo.StudyLoginUsers", new[] { "LoginUser_Id" });
            DropIndex("dbo.StudyLoginUsers", new[] { "Study_Id" });
            DropTable("dbo.StudyLoginUsers");
        }
    }
}
