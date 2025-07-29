namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class MakeQuantityDouble : DbMigration
    {
        public override void Up()
        {
            AlterColumn("dbo.FoodItems", "QuantityGrams", c => c.Double(nullable: false));
        }
        
        public override void Down()
        {
            AlterColumn("dbo.FoodItems", "QuantityGrams", c => c.Int(nullable: false));
        }
    }
}
