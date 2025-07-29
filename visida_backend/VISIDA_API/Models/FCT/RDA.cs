using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.ExternalObjects;

namespace VISIDA_API.Models.FCT
{
    public class RDA
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }

        [ForeignKey("Model")]
        public int? Model_Id { get; set; }
        public RDAModel Model { get; set; }

        public string Description { get; set; }
        public bool IsFemale
        {
            get
            {
                return Gender == Genders.Female;
            }
        }
        public double AgeLowerBound { get; set; }
        public double AgeUpperBound { get; set; }
        public int ChildbearingMonths { get; set; }

        public enum Genders { Child, Male, Female };
        public Genders Gender { get; set; }

        public enum LifeStages {None, Pregnant, Breastfeeding, PregnantAndBreastfeeding};
        public LifeStages Childbearing { get; set; }

        public int Weight { get; set; }

        #region Nutrients
        public double? EnergykCal { get; set; }
        public double? EnergyWFibre { get; set; }
        public double? EnergyWoFibre { get; set; }
        public double? EnergykJ { get; set; }
        public double? Moisture { get; set; }

        public double? Protein { get; set; }

        //public double? ProteinHighContent { get; set; }
        //public double? Protein80PercentContent { get; set; }
        //public double? Protein70PercentContent { get; set; }

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

        public double? VitaminD { get; set; }
        #endregion

        public static implicit operator RDA(ERDA r)
        {
            var ret = new RDA()
            {
                Model_Id = r.Model_Id,
                Description = r.Description,
                AgeLowerBound = r.AgeLowerBound,
                AgeUpperBound = r.AgeUpperBound,
                ChildbearingMonths = r.ChildbearingMonths,
                Weight = r.Weight,

                EnergykCal = r.EnergykCal,
                EnergyWFibre = r.EnergyWFibre,
                EnergyWoFibre = r.EnergyWoFibre,
                EnergykJ = r.EnergykJ,
                Moisture = r.Moisture,
                Protein = r.Protein,
                Fat = r.Fat,
                SaturatedFat = r.SaturatedFat,
                MonounsaturatedFat = r.MonounsaturatedFat,
                PolyunsaturatedFat = r.PolyunsaturatedFat,
                LinoleicAcic = r.LinoleicAcic,
                AlphaLinolenicAcid = r.AlphaLinolenicAcid,
                CarbohydratesWoSa = r.CarbohydratesWoSa,
                Carbohydrates = r.Carbohydrates,
                Starch = r.Starch,
                SugarTotal = r.SugarTotal,
                SugarAdded = r.SugarAdded,
                SugarFree = r.SugarFree,
                Fibre = r.Fibre,
                Alcohol = r.Alcohol,
                Ash = r.Ash,
                Retinol = r.Retinol,
                BetaCarotene = r.BetaCarotene,
                ProvitaminA = r.ProvitaminA,
                VitaminA = r.VitaminA,
                Thiamin = r.Thiamin,
                Riboflavin = r.Riboflavin,
                Niacin = r.Niacin,
                NiacinDE = r.NiacinDE,
                VitaminB6 = r.VitaminB6,
                VitaminB12 = r.VitaminB12,
                VitaminC = r.VitaminC,
                AlphaTocopherol = r.AlphaTocopherol,
                VitaminE = r.VitaminE,
                Folate = r.Folate,
                FolicAcid = r.FolicAcid,
                FolatesTotal = r.FolatesTotal,
                FolateDietary = r.FolateDietary,
                Calcium = r.Calcium,
                Iodine = r.Iodine,
                Phosphorus = r.Phosphorus,
                Sodium = r.Sodium,
                Potassium = r.Potassium,
                Iron = r.Iron,
                Magnesium = r.Magnesium,
                Selenium = r.Selenium,
                Copper = r.Copper,
                Zinc = r.Zinc,
                Caffeine = r.Caffeine,
                Tryptophan = r.Tryptophan,
                Eicosapentaenoic = r.Eicosapentaenoic,
                Docosapentaenoic = r.Docosapentaenoic,
                Docosahexaenoic = r.Docosahexaenoic,
                Omega3FattyAcid = r.Omega3FattyAcid,
                TransFattyAcid = r.TransFattyAcid,
                Cholesterol = r.Cholesterol,
                VitaminD = r.VitaminD
            };
            LifeStages cb;
            if (Enum.TryParse(r.Childbearing, true, out cb))
                ret.Childbearing = cb;
            else
                ret.Childbearing = LifeStages.None;

            Genders g;
            if (Enum.TryParse(r.Gender, true, out g))
                ret.Gender = g;
            else
                ret.Gender = Genders.Child;

            return ret;
        }

        //#region FoodGroups
        //public double? ADG_10 { get; set; }
        //public double? ADG_101 { get; set; }
        //public double? ADG_1011 { get; set; }
        //public double? ADG_1012 { get; set; }
        //public double? ADG_1013 { get; set; }
        //public double? ADG_1014 { get; set; }
        //public double? ADG_1015 { get; set; }
        //public double? ADG_1016 { get; set; }
        //public double? ADG_1017 { get; set; }
        //public double? ADG_1018 { get; set; }
        //public double? ADG_102 { get; set; }
        //public double? ADG_1021 { get; set; }
        //public double? ADG_1022 { get; set; }
        //public double? ADG_1023 { get; set; }
        //public double? ADG_1024 { get; set; }
        //public double? ADG_1025 { get; set; }
        //public double? ADG_1026 { get; set; }
        //public double? ADG_1027 { get; set; }
        //public double? ADG_1028 { get; set; }
        //public double? ADG_20 { get; set; }
        //public double? ADG_201 { get; set; }
        //public double? ADG_202 { get; set; }
        //public double? ADG_203 { get; set; }
        //public double? ADG_204 { get; set; }
        //public double? ADG_205 { get; set; }
        //public double? ADG_2051 { get; set; }
        //public double? ADG_2052 { get; set; }
        //public double? ADG_30 { get; set; }
        //public double? ADG_301 { get; set; }
        //public double? ADG_302 { get; set; }
        //public double? ADG_303 { get; set; }
        //public double? ADG_40 { get; set; }
        //public double? ADG_401 { get; set; }
        //public double? ADG_4011 { get; set; }
        //public double? ADG_4012 { get; set; }
        //public double? ADG_402 { get; set; }
        //public double? ADG_4021 { get; set; }
        //public double? ADG_4022 { get; set; }
        //public double? ADG_4023 { get; set; }
        //public double? ADG_4024 { get; set; }
        //public double? ADG_4025 { get; set; }
        //public double? ADG_4026 { get; set; }
        //public double? ADG_4027 { get; set; }
        //public double? ADG_4028 { get; set; }
        //public double? ADG_403 { get; set; }
        //public double? ADG_4031 { get; set; }
        //public double? ADG_4032 { get; set; }
        //public double? ADG_4033 { get; set; }
        //public double? ADG_4034 { get; set; }
        //public double? ADG_4035 { get; set; }
        //public double? ADG_4036 { get; set; }
        //public double? ADG_4037 { get; set; }
        //public double? ADG_4038 { get; set; }
        //public double? ADG_4039 { get; set; }
        //public double? ADG_50 { get; set; }
        //public double? ADG_501 { get; set; }
        //public double? ADG_5011 { get; set; }
        //public double? ADG_5012 { get; set; }
        //public double? ADG_502 { get; set; }
        //public double? ADG_5021 { get; set; }
        //public double? ADG_5022 { get; set; }
        //public double? ADG_503 { get; set; }
        //public double? ADG_5031 { get; set; }
        //public double? ADG_5032 { get; set; }
        //public double? ADG_504 { get; set; }
        //public double? ADG_5041 { get; set; }
        //public double? ADG_5042 { get; set; }
        //public double? ADG_505 { get; set; }
        //public double? ADG_506 { get; set; }
        //public double? ADG_507 { get; set; }
        //public double? ADG_5071 { get; set; }
        //public double? ADG_5072 { get; set; }
        //public double? ADG_508 { get; set; }
        //public double? ADG_60 { get; set; }
        //public double? ADG_70 { get; set; }
        //public double? ADG_701 { get; set; }
        //public double? ADG_702 { get; set; }
        //public double? ADG_703 { get; set; }
        //#endregion
    }
}