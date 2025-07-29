namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class OptionalCountry : DbMigration
    {
        public override void Up()
        {
            AlterColumn("dbo.Households", "Country", c => c.String(maxLength: 32));
        }
        
        public override void Down()
        {
            AlterColumn("dbo.Households", "Country", c => c.String(nullable: false, maxLength: 32));
        }
    }
}
