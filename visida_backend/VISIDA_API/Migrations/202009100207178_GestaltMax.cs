namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class GestaltMax : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.Studies", "GestaltMax", c => c.Int(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.Studies", "GestaltMax");
        }
    }
}
