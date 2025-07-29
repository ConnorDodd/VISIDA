namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class AddRDAs2 : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.RDAModels",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Name = c.String(nullable: false, maxLength: 256, unicode: false),
                        Description = c.String(),
                        FieldData = c.String(),
                    })
                .PrimaryKey(t => t.Id)
                .Index(t => t.Name, unique: true);
            
            CreateTable(
                "dbo.RDAs",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Model_Id = c.Int(),
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
                .ForeignKey("dbo.RDAModels", t => t.Model_Id)
                .Index(t => t.Model_Id);
            
            AddColumn("dbo.Studies", "RDAModel_Id", c => c.Int());
            CreateIndex("dbo.Studies", "RDAModel_Id");
            AddForeignKey("dbo.Studies", "RDAModel_Id", "dbo.RDAModels", "Id");
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.Studies", "RDAModel_Id", "dbo.RDAModels");
            DropForeignKey("dbo.RDAs", "Model_Id", "dbo.RDAModels");
            DropIndex("dbo.RDAs", new[] { "Model_Id" });
            DropIndex("dbo.RDAModels", new[] { "Name" });
            DropIndex("dbo.Studies", new[] { "RDAModel_Id" });
            DropColumn("dbo.Studies", "RDAModel_Id");
            DropTable("dbo.RDAs");
            DropTable("dbo.RDAModels");
        }
    }
}
