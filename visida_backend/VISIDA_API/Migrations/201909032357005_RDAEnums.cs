namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class RDAEnums : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.RDAs", "AgeLowerBound", c => c.Double(nullable: false));
            AddColumn("dbo.RDAs", "Gender", c => c.Int(nullable: false));
            AddColumn("dbo.RDAs", "Childbearing", c => c.Int(nullable: false));
            DropColumn("dbo.RDAs", "IsFemale");
        }
        
        public override void Down()
        {
            AddColumn("dbo.RDAs", "IsFemale", c => c.Boolean(nullable: false));
            DropColumn("dbo.RDAs", "Childbearing");
            DropColumn("dbo.RDAs", "Gender");
            DropColumn("dbo.RDAs", "AgeLowerBound");
        }
    }
}
