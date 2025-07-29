namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class AddRDAs : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.RDAs",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Study_Id = c.Int(),
                        Description = c.String(),
                        IsFemale = c.Boolean(nullable: false),
                        AgeUpperBound = c.Double(nullable: false),
                        ChildbearingMonths = c.Int(nullable: false),
                        Weight = c.Int(nullable: false),
                        EnergykCal = c.Double(),
                        EnergyWFibre = c.Double(),
                        EnergyWoFibre = c.Double(),
                        EnergykJ = c.Double(),
                        Moisture = c.Double(),
                        Protein = c.Double(),
                        Fat = c.Double(),
                        SaturatedFat = c.Double(),
                        MonounsaturatedFat = c.Double(),
                        PolyunsaturatedFat = c.Double(),
                        LinoleicAcic = c.Double(),
                        AlphaLinolenicAcid = c.Double(),
                        CarbohydratesWoSa = c.Double(),
                        Carbohydrates = c.Double(),
                        Starch = c.Double(),
                        SugarTotal = c.Double(),
                        SugarAdded = c.Double(),
                        SugarFree = c.Double(),
                        Fibre = c.Double(),
                        Alcohol = c.Double(),
                        Ash = c.Double(),
                        Retinol = c.Double(),
                        BetaCarotene = c.Double(),
                        ProvitaminA = c.Double(),
                        VitaminA = c.Double(),
                        Thiamin = c.Double(),
                        Riboflavin = c.Double(),
                        Niacin = c.Double(),
                        NiacinDE = c.Double(),
                        VitaminB6 = c.Double(),
                        VitaminB12 = c.Double(),
                        VitaminC = c.Double(),
                        AlphaTocopherol = c.Double(),
                        VitaminE = c.Double(),
                        Folate = c.Double(),
                        FolicAcid = c.Double(),
                        FolatesTotal = c.Double(),
                        FolateDietary = c.Double(),
                        Calcium = c.Double(),
                        Iodine = c.Double(),
                        Phosphorus = c.Double(),
                        Sodium = c.Double(),
                        Potassium = c.Double(),
                        Iron = c.Double(),
                        Magnesium = c.Double(),
                        Selenium = c.Double(),
                        Copper = c.Double(),
                        Zinc = c.Double(),
                        Caffeine = c.Double(),
                        Tryptophan = c.Double(),
                        Eicosapentaenoic = c.Double(),
                        Docosapentaenoic = c.Double(),
                        Docosahexaenoic = c.Double(),
                        Omega3FattyAcid = c.Double(),
                        TransFattyAcid = c.Double(),
                        Cholesterol = c.Double(),
                        VitaminD = c.Double(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.Studies", t => t.Study_Id)
                .Index(t => t.Study_Id);
            
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.RDAs", "Study_Id", "dbo.Studies");
            DropIndex("dbo.RDAs", new[] { "Study_Id" });
            DropTable("dbo.RDAs");
        }
    }
}
