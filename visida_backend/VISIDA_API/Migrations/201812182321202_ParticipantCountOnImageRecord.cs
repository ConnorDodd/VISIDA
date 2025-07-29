namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class ParticipantCountOnImageRecord : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.ImageRecords", "Participants", c => c.Int(nullable: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.ImageRecords", "Participants");
        }
    }
}
