namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class FoodGroups : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.FoodGroups",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        FoodGroupId = c.String(),
                        Description = c.String(),
                    })
                .PrimaryKey(t => t.Id);
            
        }
        
        public override void Down()
        {
            DropTable("dbo.FoodGroups");
        }
    }
}
