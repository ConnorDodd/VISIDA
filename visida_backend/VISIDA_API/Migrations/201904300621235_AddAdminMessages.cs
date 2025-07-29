namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class AddAdminMessages : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.AdminMessages",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Message = c.String(),
                        Type = c.Int(nullable: false),
                        CreatedTime = c.DateTime(nullable: false),
                        RefId = c.Int(nullable: false),
                    })
                .PrimaryKey(t => t.Id);
            
        }
        
        public override void Down()
        {
            DropTable("dbo.AdminMessages");
        }
    }
}
