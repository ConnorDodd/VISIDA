namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class UpdateExampleMeasures : DbMigration
    {
        public override void Up()
        {
            AlterColumn("dbo.ExampleFoodCompositions", "Name", c => c.String(nullable: false, maxLength: 256));
            AlterColumn("dbo.Measures", "Description", c => c.String(maxLength: 256, unicode: false));
        }
        
        public override void Down()
        {
            AlterColumn("dbo.Measures", "Description", c => c.String(nullable: false, maxLength: 256, unicode: false));
            AlterColumn("dbo.ExampleFoodCompositions", "Name", c => c.String(maxLength: 256, unicode: false));
        }
    }
}
