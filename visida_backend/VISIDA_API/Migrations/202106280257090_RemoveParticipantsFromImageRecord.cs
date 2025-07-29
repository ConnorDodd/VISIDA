namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class RemoveParticipantsFromImageRecord : DbMigration
    {
        public override void Up()
        {
            DropColumn("dbo.ImageRecords", "Participants");
        }
        
        public override void Down()
        {
            AddColumn("dbo.ImageRecords", "Participants", c => c.Int(nullable: false));
        }
    }
}
