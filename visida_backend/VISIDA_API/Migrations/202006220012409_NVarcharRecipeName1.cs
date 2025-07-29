namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class NVarcharRecipeName1 : DbMigration
    {
        public override void Up()
        {
            AlterColumn("dbo.FoodCompositions", "Name", c => c.String(maxLength: 256));
        }
        
        public override void Down()
        {
            AlterColumn("dbo.FoodCompositions", "Name", c => c.String(maxLength: 256, unicode: false));
        }
    }
}
