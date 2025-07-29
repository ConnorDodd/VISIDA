namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class ImprovedTagOnHomography : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.ImageHomographies", "Improved", c => c.Boolean(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.ImageHomographies", "Improved");
        }
    }
}
