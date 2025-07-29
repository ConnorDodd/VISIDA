namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class LongerGuidForDelete : DbMigration
    {
        public override void Up()
        {
            DropIndex("dbo.Households", new[] { "Guid" });
            AlterColumn("dbo.Households", "Guid", c => c.String(nullable: false, maxLength: 53));
            CreateIndex("dbo.Households", "Guid", unique: true);
        }
        
        public override void Down()
        {
            DropIndex("dbo.Households", new[] { "Guid" });
            AlterColumn("dbo.Households", "Guid", c => c.String(nullable: false, maxLength: 36));
            CreateIndex("dbo.Households", "Guid", unique: true);
        }
    }
}
