namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class AgeToDouble : DbMigration
    {
        public override void Up()
        {
            AlterColumn("dbo.HouseholdMembers", "Age", c => c.Single(nullable: false));
        }
        
        public override void Down()
        {
            AlterColumn("dbo.HouseholdMembers", "Age", c => c.Int(nullable: false));
        }
    }
}
