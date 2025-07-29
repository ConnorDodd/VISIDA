namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class CanAddEatRecords : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.EatOccasions", "Original", c => c.Boolean(nullable: false, defaultValue:true));
            AddColumn("dbo.EatRecords", "Original", c => c.Boolean(nullable: false, defaultValue: true));
        }
        
        public override void Down()
        {
            DropColumn("dbo.EatRecords", "Original");
            DropColumn("dbo.EatOccasions", "Original");
        }
    }
}
