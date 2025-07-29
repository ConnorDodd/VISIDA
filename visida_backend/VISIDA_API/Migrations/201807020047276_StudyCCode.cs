namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class StudyCCode : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.Studies", "CountryCode", c => c.String());
            AddColumn("dbo.Studies", "Transcribe", c => c.Boolean(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.Studies", "Transcribe");
            DropColumn("dbo.Studies", "CountryCode");
        }
    }
}
