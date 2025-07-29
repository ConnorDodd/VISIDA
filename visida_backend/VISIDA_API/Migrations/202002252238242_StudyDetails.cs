namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class StudyDetails : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.Studies", "Translate", c => c.Boolean(nullable: false));
            AddColumn("dbo.Studies", "Gestalt", c => c.Boolean(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.Studies", "Gestalt");
            DropColumn("dbo.Studies", "Translate");
        }
    }
}
