namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class What : DbMigration
    {
        public override void Up()
        {
            DropForeignKey("dbo.Studies", "DeletedBy_Id", "dbo.LoginUsers");
            DropIndex("dbo.Studies", new[] { "DeletedBy_Id" });
            AlterColumn("dbo.Studies", "DeletedBy_Id", c => c.Int());
            CreateIndex("dbo.Studies", "DeletedBy_Id");
            AddForeignKey("dbo.Studies", "DeletedBy_Id", "dbo.LoginUsers", "Id");
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.Studies", "DeletedBy_Id", "dbo.LoginUsers");
            DropIndex("dbo.Studies", new[] { "DeletedBy_Id" });
            AlterColumn("dbo.Studies", "DeletedBy_Id", c => c.Int());
            CreateIndex("dbo.Studies", "DeletedBy_Id");
            AddForeignKey("dbo.Studies", "DeletedBy_Id", "dbo.LoginUsers", "Id", cascadeDelete: false);
        }
    }
}
