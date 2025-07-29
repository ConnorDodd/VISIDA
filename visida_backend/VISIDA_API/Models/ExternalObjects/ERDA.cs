using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using VISIDA_API.Models.FCT;
using static VISIDA_API.Models.FCT.RDA;

namespace VISIDA_API.Models.ExternalObjects
{
    public class ERDA
    {
        public int? Model_Id { get; set; }

        public string Description { get; set; }
        public bool IsFemale { get; set; }
        public double AgeLowerBound { get; set; }
        public double AgeUpperBound { get; set; }
        public int ChildbearingMonths { get; set; }
        public string Childbearing { get; set; }
        public string Gender { get; set; }

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

        public static implicit operator ERDA(RDA r)
        {
            ERDA e = new ERDA()
            {
                Model_Id = r.Model_Id,
                Description = r.Description,
                IsFemale = r.IsFemale,
                Gender = Enum.GetName(r.Gender.GetType(), r.Gender),
                AgeLowerBound = r.AgeLowerBound,
                AgeUpperBound = r.AgeUpperBound,
                ChildbearingMonths = r.ChildbearingMonths,
                Childbearing = Enum.GetName(r.Childbearing.GetType(), r.Childbearing),
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

            if (r.Id == 95)
            {
                var asd = true;

            }

            return e;
        }
    }
}