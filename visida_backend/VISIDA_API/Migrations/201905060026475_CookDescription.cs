namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class CookDescription : DbMigration
    {
        public override void Up()
        {
            AddColumn("dbo.CookIngredients", "CookDescription", c => c.String(maxLength: 48, unicode: false));
        }
        
        public override void Down()
        {
            DropColumn("dbo.CookIngredients", "CookDescription");
        }
    }
}
