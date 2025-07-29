namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class AllowDuplicateParticipantIds : DbMigration
    {
        public override void Up()
        {
            DropIndex("dbo.Households", new[] { "ParticipantId" });
        }
        
        public override void Down()
        {
            CreateIndex("dbo.Households", "ParticipantId", unique: true);
        }
    }
}
