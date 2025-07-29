using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.FCT;

namespace VISIDA_API.Models.ExternalObjects
{
    public class EFoodComposition
    {
        public int Id { get; set; }
        public int? Table_Id { get; set; }
        public string OriginId { get; set; }
        public string Name { get; set; }
        public string AlternateName { get; set; }

        public string Measures { get; set; }
        public string MeasuresML { get; set; }
        public int FoodGroupId { get; set; }
        public bool IsRecipe { get; set; }

        public double? YieldWater { get; set; }
        public double? YieldStoveTop { get; set; }
        public double? YieldOven { get; set; }


        public double? Density { get; set; }
        public double? EnergykCal { get; set; }
        public double? EnergyWFibre { get; set; }
        public double? EnergyWoFibre { get; set; }
        public double? EnergykJ { get; set; }
        public double? Moisture { get; set; }
        public double? Protein { get; set; }
        public double? Fat { get; set; }
        public double? SaturatedFat { get; set; }
        public double? MonounsaturatedFat { get; set; }
        public double? PolyunsaturatedFat { get; set; }
        public double? LinoleicAcic { get; set; }
        public double? AlphaLinolenicAcid { get; set; }
        public double? CarbohydratesWoSa { get; set; }
        public double? Carbohydrates { get; set; }
        public double? Starch { get; set; }
        public double? SugarTotal { get; set; }
        public double? SugarAdded { get; set; }
        public double? SugarFree { get; set; }
        public double? Fibre { get; set; }
        public double? Alcohol { get; set; }
        public double? Ash { get; set; }
        public double? Retinol { get; set; }
        public double? BetaCarotene { get; set; }
        public double? ProvitaminA { get; set; }
        public double? VitaminA { get; set; }
        public double? Thiamin { get; set; }
        public double? Riboflavin { get; set; }
        public double? Niacin { get; set; }
        public double? NiacinDE { get; set; }
        public double? VitaminB6 { get; set; }
        public double? VitaminB12 { get; set; }
        public double? VitaminC { get; set; }
        public double? AlphaTocopherol { get; set; }
        public double? VitaminE { get; set; }
        public double? Folate { get; set; }
        public double? FolicAcid { get; set; }
        public double? FolatesTotal { get; set; }
        public double? FolateDietary { get; set; }
        public double? Calcium { get; set; }
        public double? Iodine { get; set; }
        public double? Phosphorus { get; set; }
        public double? Sodium { get; set; }
        public double? Potassium { get; set; }
        public double? Iron { get; set; }
        public double? Magnesium { get; set; }
        public double? Selenium { get; set; }
        public double? Copper { get; set; }
        public double? Zinc { get; set; }
        public double? Caffeine { get; set; }
        public double? Tryptophan { get; set; }
        public double? Eicosapentaenoic { get; set; }
        public double? Docosapentaenoic { get; set; }
        public double? Docosahexaenoic { get; set; }
        public double? Omega3FattyAcid { get; set; }
        public double? TransFattyAcid { get; set; }
        public double? Cholesterol { get; set; }
        public double? ADG_10 { get; set; }
        public double? ADG_101 { get; set; }
        public double? ADG_1011 { get; set; }
        public double? ADG_1012 { get; set; }
        public double? ADG_1013 { get; set; }
        public double? ADG_1014 { get; set; }
        public double? ADG_1015 { get; set; }
        public double? ADG_1016 { get; set; }
        public double? ADG_1017 { get; set; }
        public double? ADG_1018 { get; set; }
        public double? ADG_102 { get; set; }
        public double? ADG_1021 { get; set; }
        public double? ADG_1022 { get; set; }
        public double? ADG_1023 { get; set; }
        public double? ADG_1024 { get; set; }
        public double? ADG_1025 { get; set; }
        public double? ADG_1026 { get; set; }
        public double? ADG_1027 { get; set; }
        public double? ADG_1028 { get; set; }
        public double? ADG_20 { get; set; }
        public double? ADG_201 { get; set; }
        public double? ADG_202 { get; set; }
        public double? ADG_203 { get; set; }
        public double? ADG_204 { get; set; }
        public double? ADG_205 { get; set; }
        public double? ADG_2051 { get; set; }
        public double? ADG_2052 { get; set; }
        public double? ADG_30 { get; set; }
        public double? ADG_301 { get; set; }
        public double? ADG_302 { get; set; }
        public double? ADG_303 { get; set; }
        public double? ADG_40 { get; set; }
        public double? ADG_401 { get; set; }
        public double? ADG_4011 { get; set; }
        public double? ADG_4012 { get; set; }
        public double? ADG_402 { get; set; }
        public double? ADG_4021 { get; set; }
        public double? ADG_4022 { get; set; }
        public double? ADG_4023 { get; set; }
        public double? ADG_4024 { get; set; }
        public double? ADG_4025 { get; set; }
        public double? ADG_4026 { get; set; }
        public double? ADG_4027 { get; set; }
        public double? ADG_4028 { get; set; }
        public double? ADG_403 { get; set; }
        public double? ADG_4031 { get; set; }
        public double? ADG_4032 { get; set; }
        public double? ADG_4033 { get; set; }
        public double? ADG_4034 { get; set; }
        public double? ADG_4035 { get; set; }
        public double? ADG_4036 { get; set; }
        public double? ADG_4037 { get; set; }
        public double? ADG_4038 { get; set; }
        public double? ADG_4039 { get; set; }
        public double? ADG_50 { get; set; }
        public double? ADG_501 { get; set; }
        public double? ADG_5011 { get; set; }
        public double? ADG_5012 { get; set; }
        public double? ADG_502 { get; set; }
        public double? ADG_5021 { get; set; }
        public double? ADG_5022 { get; set; }
        public double? ADG_503 { get; set; }
        public double? ADG_5031 { get; set; }
        public double? ADG_5032 { get; set; }
        public double? ADG_504 { get; set; }
        public double? ADG_5041 { get; set; }
        public double? ADG_5042 { get; set; }
        public double? ADG_505 { get; set; }
        public double? ADG_506 { get; set; }
        public double? ADG_507 { get; set; }
        public double? ADG_5071 { get; set; }
        public double? ADG_5072 { get; set; }
        public double? ADG_508 { get; set; }
        public double? ADG_60 { get; set; }
        public double? ADG_70 { get; set; }
        public double? ADG_701 { get; set; }
        public double? ADG_702 { get; set; }
        public double? ADG_703 { get; set; }

        public static implicit operator EFoodComposition(FoodComposition fc)
        {
            if (fc == null)
                return null;
            return new EFoodComposition()
            {
                Id = fc.Id,
                Table_Id = fc.Table_Id,
                OriginId = fc.OriginId,
                Name = fc.Name,
                AlternateName = fc.AlternateName,
                Measures = fc.Measures,
                //MeasuresML = fc.MeasuresML,
                YieldWater = fc.YieldWater,
                YieldStoveTop = fc.YieldStoveTop,
                YieldOven = fc.YieldOven,
                FoodGroupId = fc.FoodGroupId,
                IsRecipe = fc.IsRecipe,
                Density = fc.Density,
                EnergykCal = fc.EnergykCal,
                EnergyWFibre = fc.EnergyWFibre,
                EnergyWoFibre = fc.EnergyWoFibre,
                EnergykJ = fc.EnergykJ,
                Moisture = fc.Moisture,
                Protein = fc.Protein,
                Fat = fc.Fat,
                SaturatedFat = fc.SaturatedFat,
                MonounsaturatedFat = fc.MonounsaturatedFat,
                PolyunsaturatedFat = fc.PolyunsaturatedFat,
                LinoleicAcic = fc.LinoleicAcic,
                AlphaLinolenicAcid = fc.AlphaLinolenicAcid,
                CarbohydratesWoSa = fc.CarbohydratesWoSa,
                Carbohydrates = fc.Carbohydrates,
                Starch = fc.Starch,
                SugarTotal = fc.SugarTotal,
                SugarAdded = fc.SugarAdded,
                SugarFree = fc.SugarFree,
                Fibre = fc.Fibre,
                Alcohol = fc.Alcohol,
                Ash = fc.Ash,
                Retinol = fc.Retinol,
                BetaCarotene = fc.BetaCarotene,
                ProvitaminA = fc.ProvitaminA,
                VitaminA = fc.VitaminA,
                Thiamin = fc.Thiamin,
                Riboflavin = fc.Riboflavin,
                Niacin = fc.Niacin,
                NiacinDE = fc.NiacinDE,
                VitaminB6 = fc.VitaminB6,
                VitaminB12 = fc.VitaminB12,
                VitaminC = fc.VitaminC,
                AlphaTocopherol = fc.AlphaTocopherol,
                VitaminE = fc.VitaminE,
                Folate = fc.Folate,
                FolicAcid = fc.FolicAcid,
                FolatesTotal = fc.FolatesTotal,
                FolateDietary = fc.FolateDietary,
                Calcium = fc.Calcium,
                Iodine = fc.Iodine,
                Phosphorus = fc.Phosphorus,
                Sodium = fc.Sodium,
                Potassium = fc.Potassium,
                Iron = fc.Iron,
                Magnesium = fc.Magnesium,
                Selenium = fc.Selenium,
                Copper = fc.Copper,
                Zinc = fc.Zinc,
                Caffeine = fc.Caffeine,
                Tryptophan = fc.Tryptophan,
                Eicosapentaenoic = fc.Eicosapentaenoic,
                Docosapentaenoic = fc.Docosapentaenoic,
                Docosahexaenoic = fc.Docosahexaenoic,
                Omega3FattyAcid = fc.Omega3FattyAcid,
                TransFattyAcid = fc.TransFattyAcid,
                Cholesterol = fc.Cholesterol,
                ADG_10 = fc.ADG_10,
                ADG_101 = fc.ADG_101,
                ADG_1011 = fc.ADG_1011,
                ADG_1012 = fc.ADG_1012,
                ADG_1013 = fc.ADG_1013,
                ADG_1014 = fc.ADG_1014,
                ADG_1015 = fc.ADG_1015,
                ADG_1016 = fc.ADG_1016,
                ADG_1017 = fc.ADG_1017,
                ADG_1018 = fc.ADG_1018,
                ADG_102 = fc.ADG_102,
                ADG_1021 = fc.ADG_1021,
                ADG_1022 = fc.ADG_1022,
                ADG_1023 = fc.ADG_1023,
                ADG_1024 = fc.ADG_1024,
                ADG_1025 = fc.ADG_1025,
                ADG_1026 = fc.ADG_1026,
                ADG_1027 = fc.ADG_1027,
                ADG_1028 = fc.ADG_1028,
                ADG_20 = fc.ADG_20,
                ADG_201 = fc.ADG_201,
                ADG_202 = fc.ADG_202,
                ADG_203 = fc.ADG_203,
                ADG_204 = fc.ADG_204,
                ADG_205 = fc.ADG_205,
                ADG_2051 = fc.ADG_2051,
                ADG_2052 = fc.ADG_2052,
                ADG_30 = fc.ADG_30,
                ADG_301 = fc.ADG_301,
                ADG_302 = fc.ADG_302,
                ADG_303 = fc.ADG_303,
                ADG_40 = fc.ADG_40,
                ADG_401 = fc.ADG_401,
                ADG_4011 = fc.ADG_4011,
                ADG_4012 = fc.ADG_4012,
                ADG_402 = fc.ADG_402,
                ADG_4021 = fc.ADG_4021,
                ADG_4022 = fc.ADG_4022,
                ADG_4023 = fc.ADG_4023,
                ADG_4024 = fc.ADG_4024,
                ADG_4025 = fc.ADG_4025,
                ADG_4026 = fc.ADG_4026,
                ADG_4027 = fc.ADG_4027,
                ADG_4028 = fc.ADG_4028,
                ADG_403 = fc.ADG_403,
                ADG_4031 = fc.ADG_4031,
                ADG_4032 = fc.ADG_4032,
                ADG_4033 = fc.ADG_4033,
                ADG_4034 = fc.ADG_4034,
                ADG_4035 = fc.ADG_4035,
                ADG_4036 = fc.ADG_4036,
                ADG_4037 = fc.ADG_4037,
                ADG_4038 = fc.ADG_4038,
                ADG_4039 = fc.ADG_4039,
                ADG_50 = fc.ADG_50,
                ADG_501 = fc.ADG_501,
                ADG_5011 = fc.ADG_5011,
                ADG_5012 = fc.ADG_5012,
                ADG_502 = fc.ADG_502,
                ADG_5021 = fc.ADG_5021,
                ADG_5022 = fc.ADG_5022,
                ADG_503 = fc.ADG_503,
                ADG_5031 = fc.ADG_5031,
                ADG_5032 = fc.ADG_5032,
                ADG_504 = fc.ADG_504,
                ADG_5041 = fc.ADG_5041,
                ADG_5042 = fc.ADG_5042,
                ADG_505 = fc.ADG_505,
                ADG_506 = fc.ADG_506,
                ADG_507 = fc.ADG_507,
                ADG_5071 = fc.ADG_5071,
                ADG_5072 = fc.ADG_5072,
                ADG_508 = fc.ADG_508,
                ADG_60 = fc.ADG_60,
                ADG_70 = fc.ADG_70,
                ADG_701 = fc.ADG_701,
                ADG_702 = fc.ADG_702,
                ADG_703 = fc.ADG_703,
            };
        }
    }
}