namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class NVarcharTextRecipe : DbMigration
    {
        public override void Up()
        {
            AlterColumn("dbo.CookRecipes", "TextDescription", c => c.String(maxLength: 1000));
        }
        
        public override void Down()
        {
            AlterColumn("dbo.CookRecipes", "TextDescription", c => c.String(maxLength: 1000, unicode: false));
        }
    }
}
