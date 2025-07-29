namespace VISIDA_API.Migrations
{
    using System;
    using System.Data.Entity.Migrations;
    
    public partial class Initial : DbMigration
    {
        public override void Up()
        {
            CreateTable(
                "dbo.CookIngredients",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        CaptureTime = c.DateTime(nullable: false),
                        Recipe_Id = c.Int(),
                        ImageRecord_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.CookRecipes", t => t.Recipe_Id)
                .ForeignKey("dbo.ImageRecords", t => t.ImageRecord_Id)
                .Index(t => t.Recipe_Id)
                .Index(t => t.ImageRecord_Id);
            
            CreateTable(
                "dbo.ImageRecords",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        RecordType = c.Int(nullable: false),
                        ImageName = c.String(nullable: false, maxLength: 256, unicode: false),
                        ImageUrl = c.String(maxLength: 256, unicode: false),
                        AudioName = c.String(nullable: false, maxLength: 256, unicode: false),
                        AudioUrl = c.String(maxLength: 256, unicode: false),
                        NTranscript = c.String(maxLength: 1024),
                        Transcript = c.String(maxLength: 1024, unicode: false),
                        IsFiducialPresent = c.Boolean(nullable: false),
                        IsCompleted = c.Boolean(nullable: false),
                        LockTimestamp = c.DateTime(),
                        Household_Id = c.Int(),
                        Homography_Id = c.Int(),
                        LockedBy_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.Households", t => t.Household_Id)
                .ForeignKey("dbo.ImageHomographies", t => t.Homography_Id)
                .ForeignKey("dbo.LoginUsers", t => t.LockedBy_Id)
                .Index(t => t.ImageName, unique: true)
                .Index(t => t.AudioName, unique: true)
                .Index(t => t.Household_Id)
                .Index(t => t.Homography_Id)
                .Index(t => t.LockedBy_Id);
            
            CreateTable(
                "dbo.FoodItems",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Name = c.String(nullable: false, maxLength: 256),
                        QuantityGrams = c.Int(nullable: false),
                        QuantityServings = c.Int(nullable: false),
                        QuantityML = c.Int(nullable: false),
                        ImageRecordId = c.Int(),
                        FoodCompositionId = c.Int(),
                        TagXPercent = c.Double(),
                        TagYPercent = c.Double(),
                        CreatedBy_Id = c.Int(),
                        FoodCompositionRecipe_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.LoginUsers", t => t.CreatedBy_Id)
                .ForeignKey("dbo.FoodCompositions", t => t.FoodCompositionId)
                .ForeignKey("dbo.ImageRecords", t => t.ImageRecordId)
                .ForeignKey("dbo.FoodCompositionRecipes", t => t.FoodCompositionRecipe_Id)
                .Index(t => t.ImageRecordId)
                .Index(t => t.FoodCompositionId)
                .Index(t => t.CreatedBy_Id)
                .Index(t => t.FoodCompositionRecipe_Id);
            
            CreateTable(
                "dbo.LoginUsers",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        UserName = c.String(nullable: false, maxLength: 256),
                        Salt = c.String(maxLength: 32),
                        Password = c.String(nullable: false, maxLength: 100),
                        LastLogin = c.DateTime(),
                        Study_Id = c.Int(),
                        Role_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.Studies", t => t.Study_Id)
                .ForeignKey("dbo.LoginUserRoles", t => t.Role_Id)
                .Index(t => t.UserName, unique: true)
                .Index(t => t.Study_Id)
                .Index(t => t.Role_Id);
            
            CreateTable(
                "dbo.Studies",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Name = c.String(nullable: false, maxLength: 256, unicode: false),
                        FoodCompositionTable_Id = c.Int(),
                        DeletedTime = c.DateTime(),
                        DeletedBy_Id = c.Int(),
                        LoginUser_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.LoginUsers", t => t.DeletedBy_Id)
                .ForeignKey("dbo.FoodCompositionTables", t => t.FoodCompositionTable_Id)
                .ForeignKey("dbo.LoginUsers", t => t.LoginUser_Id)
                .Index(t => t.Name, unique: true)
                .Index(t => t.FoodCompositionTable_Id)
                .Index(t => t.DeletedBy_Id)
                .Index(t => t.LoginUser_Id);
            
            CreateTable(
                "dbo.FoodCompositionTables",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Name = c.String(maxLength: 256, unicode: false),
                    })
                .PrimaryKey(t => t.Id)
                .Index(t => t.Name, unique: true);
            
            CreateTable(
                "dbo.FoodCompositions",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        ModifiedDate = c.DateTime(nullable: false),
                        Origin = c.String(maxLength: 256, unicode: false),
                        Name = c.String(maxLength: 256, unicode: false),
                        AlternateName = c.String(maxLength: 256),
                        Density = c.Double(),
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
                        ADG_10 = c.Double(),
                        ADG_101 = c.Double(),
                        ADG_1011 = c.Double(),
                        ADG_1012 = c.Double(),
                        ADG_1013 = c.Double(),
                        ADG_1014 = c.Double(),
                        ADG_1015 = c.Double(),
                        ADG_1016 = c.Double(),
                        ADG_1017 = c.Double(),
                        ADG_1018 = c.Double(),
                        ADG_102 = c.Double(),
                        ADG_1021 = c.Double(),
                        ADG_1022 = c.Double(),
                        ADG_1023 = c.Double(),
                        ADG_1024 = c.Double(),
                        ADG_1025 = c.Double(),
                        ADG_1026 = c.Double(),
                        ADG_1027 = c.Double(),
                        ADG_1028 = c.Double(),
                        ADG_20 = c.Double(),
                        ADG_201 = c.Double(),
                        ADG_202 = c.Double(),
                        ADG_203 = c.Double(),
                        ADG_204 = c.Double(),
                        ADG_205 = c.Double(),
                        ADG_2051 = c.Double(),
                        ADG_2052 = c.Double(),
                        ADG_30 = c.Double(),
                        ADG_301 = c.Double(),
                        ADG_302 = c.Double(),
                        ADG_303 = c.Double(),
                        ADG_40 = c.Double(),
                        ADG_401 = c.Double(),
                        ADG_4011 = c.Double(),
                        ADG_4012 = c.Double(),
                        ADG_402 = c.Double(),
                        ADG_4021 = c.Double(),
                        ADG_4022 = c.Double(),
                        ADG_4023 = c.Double(),
                        ADG_4024 = c.Double(),
                        ADG_4025 = c.Double(),
                        ADG_4026 = c.Double(),
                        ADG_4027 = c.Double(),
                        ADG_4028 = c.Double(),
                        ADG_403 = c.Double(),
                        ADG_4031 = c.Double(),
                        ADG_4032 = c.Double(),
                        ADG_4033 = c.Double(),
                        ADG_4034 = c.Double(),
                        ADG_4035 = c.Double(),
                        ADG_4036 = c.Double(),
                        ADG_4037 = c.Double(),
                        ADG_4038 = c.Double(),
                        ADG_4039 = c.Double(),
                        ADG_50 = c.Double(),
                        ADG_501 = c.Double(),
                        ADG_5011 = c.Double(),
                        ADG_5012 = c.Double(),
                        ADG_502 = c.Double(),
                        ADG_5021 = c.Double(),
                        ADG_5022 = c.Double(),
                        ADG_503 = c.Double(),
                        ADG_5031 = c.Double(),
                        ADG_5032 = c.Double(),
                        ADG_504 = c.Double(),
                        ADG_5041 = c.Double(),
                        ADG_5042 = c.Double(),
                        ADG_505 = c.Double(),
                        ADG_506 = c.Double(),
                        ADG_507 = c.Double(),
                        ADG_5071 = c.Double(),
                        ADG_5072 = c.Double(),
                        ADG_508 = c.Double(),
                        ADG_60 = c.Double(),
                        ADG_70 = c.Double(),
                        ADG_701 = c.Double(),
                        ADG_702 = c.Double(),
                        ADG_703 = c.Double(),
                        Table_Id = c.Int(),
                        FoodCompositionUpdate_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.FoodCompositionTables", t => t.Table_Id)
                .ForeignKey("dbo.FoodCompositionUpdates", t => t.FoodCompositionUpdate_Id)
                .Index(t => t.Table_Id)
                .Index(t => t.FoodCompositionUpdate_Id);
            
            CreateTable(
                "dbo.Households",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Guid = c.String(nullable: false, maxLength: 36),
                        Study_Id = c.Int(nullable: false),
                        Country = c.String(nullable: false, maxLength: 32),
                        StartDate = c.DateTime(nullable: false),
                        EndDate = c.DateTime(nullable: false),
                        Latitude = c.String(),
                        Longitude = c.String(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.Studies", t => t.Study_Id, cascadeDelete: true)
                .Index(t => t.Guid, unique: true)
                .Index(t => t.Study_Id);
            
            CreateTable(
                "dbo.HouseholdMembers",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        HouseholdId = c.Int(nullable: false),
                        Age = c.Int(nullable: false),
                        IsMother = c.Boolean(nullable: false),
                        IsBreastfed = c.Boolean(nullable: false),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.Households", t => t.HouseholdId, cascadeDelete: true)
                .Index(t => t.HouseholdId);
            
            CreateTable(
                "dbo.EatOccasions",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        HouseholdMemberId = c.Int(nullable: false),
                        TimeStart = c.DateTime(nullable: false),
                        TimeEnd = c.DateTime(nullable: false),
                        Finalized = c.Boolean(nullable: false),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.HouseholdMembers", t => t.HouseholdMemberId, cascadeDelete: true)
                .Index(t => t.HouseholdMemberId);
            
            CreateTable(
                "dbo.EatRecords",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        IsLeftover = c.Boolean(nullable: false),
                        CaptureTime = c.DateTime(nullable: false),
                        FinalizeTime = c.DateTime(nullable: false),
                        Finalized = c.Boolean(nullable: false),
                        EatOccasion_Id = c.Int(),
                        ImageRecord_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.EatOccasions", t => t.EatOccasion_Id)
                .ForeignKey("dbo.ImageRecords", t => t.ImageRecord_Id)
                .Index(t => t.EatOccasion_Id)
                .Index(t => t.ImageRecord_Id);
            
            CreateTable(
                "dbo.CookRecipes",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Name = c.String(maxLength: 256, unicode: false),
                        ImageName = c.String(maxLength: 256, unicode: false),
                        ImageUrl = c.String(maxLength: 256, unicode: false),
                        IsFiducialPresent = c.Boolean(nullable: false),
                        AudioName = c.String(nullable: false, maxLength: 256, unicode: false),
                        AudioUrl = c.String(maxLength: 256, unicode: false),
                        Homography_Id = c.Int(),
                        Household_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.ImageHomographies", t => t.Homography_Id)
                .ForeignKey("dbo.Households", t => t.Household_Id)
                .Index(t => t.AudioName, unique: true)
                .Index(t => t.Homography_Id)
                .Index(t => t.Household_Id);
            
            CreateTable(
                "dbo.ImageHomographies",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        TopLeftX = c.Double(nullable: false),
                        TopLeftY = c.Double(nullable: false),
                        TopRightX = c.Double(nullable: false),
                        TopRightY = c.Double(nullable: false),
                        BottomLeftX = c.Double(nullable: false),
                        BottomLeftY = c.Double(nullable: false),
                        BottomRightX = c.Double(nullable: false),
                        BottomRightY = c.Double(nullable: false),
                    })
                .PrimaryKey(t => t.Id);
            
            CreateTable(
                "dbo.LoginUserRoles",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Role = c.String(nullable: false, maxLength: 64),
                    })
                .PrimaryKey(t => t.Id)
                .Index(t => t.Role, unique: true);
            
            CreateTable(
                "dbo.FoodCompositionRecipes",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Name = c.String(),
                        CreatedDate = c.DateTime(nullable: false),
                        CompositionResult_Id = c.Int(),
                        CreatedBy_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.FoodCompositions", t => t.CompositionResult_Id)
                .ForeignKey("dbo.LoginUsers", t => t.CreatedBy_Id)
                .Index(t => t.CompositionResult_Id)
                .Index(t => t.CreatedBy_Id);
            
            CreateTable(
                "dbo.FoodCompositionUpdates",
                c => new
                    {
                        Id = c.Int(nullable: false, identity: true),
                        Date = c.DateTime(nullable: false),
                        UpdatedAs = c.String(maxLength: 64),
                        CommitMessage = c.String(nullable: false, maxLength: 1000),
                        Overwrite = c.Boolean(nullable: false),
                        TableId = c.Int(nullable: false),
                        UpdatedBy_Id = c.Int(),
                    })
                .PrimaryKey(t => t.Id)
                .ForeignKey("dbo.LoginUsers", t => t.UpdatedBy_Id)
                .Index(t => t.UpdatedBy_Id);
            
        }
        
        public override void Down()
        {
            DropForeignKey("dbo.FoodCompositionUpdates", "UpdatedBy_Id", "dbo.LoginUsers");
            DropForeignKey("dbo.FoodCompositions", "FoodCompositionUpdate_Id", "dbo.FoodCompositionUpdates");
            DropForeignKey("dbo.FoodItems", "FoodCompositionRecipe_Id", "dbo.FoodCompositionRecipes");
            DropForeignKey("dbo.FoodCompositionRecipes", "CreatedBy_Id", "dbo.LoginUsers");
            DropForeignKey("dbo.FoodCompositionRecipes", "CompositionResult_Id", "dbo.FoodCompositions");
            DropForeignKey("dbo.CookIngredients", "ImageRecord_Id", "dbo.ImageRecords");
            DropForeignKey("dbo.ImageRecords", "LockedBy_Id", "dbo.LoginUsers");
            DropForeignKey("dbo.ImageRecords", "Homography_Id", "dbo.ImageHomographies");
            DropForeignKey("dbo.FoodItems", "ImageRecordId", "dbo.ImageRecords");
            DropForeignKey("dbo.FoodItems", "FoodCompositionId", "dbo.FoodCompositions");
            DropForeignKey("dbo.FoodItems", "CreatedBy_Id", "dbo.LoginUsers");
            DropForeignKey("dbo.LoginUsers", "Role_Id", "dbo.LoginUserRoles");
            DropForeignKey("dbo.Studies", "LoginUser_Id", "dbo.LoginUsers");
            DropForeignKey("dbo.Households", "Study_Id", "dbo.Studies");
            DropForeignKey("dbo.ImageRecords", "Household_Id", "dbo.Households");
            DropForeignKey("dbo.CookIngredients", "Recipe_Id", "dbo.CookRecipes");
            DropForeignKey("dbo.CookRecipes", "Household_Id", "dbo.Households");
            DropForeignKey("dbo.CookRecipes", "Homography_Id", "dbo.ImageHomographies");
            DropForeignKey("dbo.HouseholdMembers", "HouseholdId", "dbo.Households");
            DropForeignKey("dbo.EatOccasions", "HouseholdMemberId", "dbo.HouseholdMembers");
            DropForeignKey("dbo.EatRecords", "ImageRecord_Id", "dbo.ImageRecords");
            DropForeignKey("dbo.EatRecords", "EatOccasion_Id", "dbo.EatOccasions");
            DropForeignKey("dbo.Studies", "FoodCompositionTable_Id", "dbo.FoodCompositionTables");
            DropForeignKey("dbo.FoodCompositions", "Table_Id", "dbo.FoodCompositionTables");
            DropForeignKey("dbo.Studies", "DeletedBy_Id", "dbo.LoginUsers");
            DropForeignKey("dbo.LoginUsers", "Study_Id", "dbo.Studies");
            DropIndex("dbo.FoodCompositionUpdates", new[] { "UpdatedBy_Id" });
            DropIndex("dbo.FoodCompositionRecipes", new[] { "CreatedBy_Id" });
            DropIndex("dbo.FoodCompositionRecipes", new[] { "CompositionResult_Id" });
            DropIndex("dbo.LoginUserRoles", new[] { "Role" });
            DropIndex("dbo.CookRecipes", new[] { "Household_Id" });
            DropIndex("dbo.CookRecipes", new[] { "Homography_Id" });
            DropIndex("dbo.CookRecipes", new[] { "AudioName" });
            DropIndex("dbo.EatRecords", new[] { "ImageRecord_Id" });
            DropIndex("dbo.EatRecords", new[] { "EatOccasion_Id" });
            DropIndex("dbo.EatOccasions", new[] { "HouseholdMemberId" });
            DropIndex("dbo.HouseholdMembers", new[] { "HouseholdId" });
            DropIndex("dbo.Households", new[] { "Study_Id" });
            DropIndex("dbo.Households", new[] { "Guid" });
            DropIndex("dbo.FoodCompositions", new[] { "FoodCompositionUpdate_Id" });
            DropIndex("dbo.FoodCompositions", new[] { "Table_Id" });
            DropIndex("dbo.FoodCompositionTables", new[] { "Name" });
            DropIndex("dbo.Studies", new[] { "LoginUser_Id" });
            DropIndex("dbo.Studies", new[] { "DeletedBy_Id" });
            DropIndex("dbo.Studies", new[] { "FoodCompositionTable_Id" });
            DropIndex("dbo.Studies", new[] { "Name" });
            DropIndex("dbo.LoginUsers", new[] { "Role_Id" });
            DropIndex("dbo.LoginUsers", new[] { "Study_Id" });
            DropIndex("dbo.LoginUsers", new[] { "UserName" });
            DropIndex("dbo.FoodItems", new[] { "FoodCompositionRecipe_Id" });
            DropIndex("dbo.FoodItems", new[] { "CreatedBy_Id" });
            DropIndex("dbo.FoodItems", new[] { "FoodCompositionId" });
            DropIndex("dbo.FoodItems", new[] { "ImageRecordId" });
            DropIndex("dbo.ImageRecords", new[] { "LockedBy_Id" });
            DropIndex("dbo.ImageRecords", new[] { "Homography_Id" });
            DropIndex("dbo.ImageRecords", new[] { "Household_Id" });
            DropIndex("dbo.ImageRecords", new[] { "AudioName" });
            DropIndex("dbo.ImageRecords", new[] { "ImageName" });
            DropIndex("dbo.CookIngredients", new[] { "ImageRecord_Id" });
            DropIndex("dbo.CookIngredients", new[] { "Recipe_Id" });
            DropTable("dbo.FoodCompositionUpdates");
            DropTable("dbo.FoodCompositionRecipes");
            DropTable("dbo.LoginUserRoles");
            DropTable("dbo.ImageHomographies");
            DropTable("dbo.CookRecipes");
            DropTable("dbo.EatRecords");
            DropTable("dbo.EatOccasions");
            DropTable("dbo.HouseholdMembers");
            DropTable("dbo.Households");
            DropTable("dbo.FoodCompositions");
            DropTable("dbo.FoodCompositionTables");
            DropTable("dbo.Studies");
            DropTable("dbo.LoginUsers");
            DropTable("dbo.FoodItems");
            DropTable("dbo.ImageRecords");
            DropTable("dbo.CookIngredients");
        }
    }
}
