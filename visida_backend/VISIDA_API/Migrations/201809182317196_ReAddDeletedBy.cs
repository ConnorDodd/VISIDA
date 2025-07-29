namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class ReAddDeletedBy : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.Studies", "DeletedBy_Id", c => c.Int(nullable: true));
            CreateIndex("dbo.Studies", "DeletedBy_Id");
            AddForeignKey("dbo.Studies", "DeletedBy_Id", "dbo.LoginUsers", "Id", cascadeDelete: false);
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.Studies", "DeletedBy_Id", "dbo.LoginUsers");
            DropIndex("dbo.Studies", new[] { "DeletedBy_Id" });
            DropColumn("dbo.Studies", "DeletedBy_Id");
        }
    }
}
