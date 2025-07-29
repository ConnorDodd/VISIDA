using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using static VISIDA_API.Models.InternalObjects.CookIngredient;

namespace VISIDA_API.Models.FCT
{
    public class RetentionFactor
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }

        [ForeignKey("Table")]
        public int? Table_Id { get; set; }
        [JsonIgnore]
        public virtual FoodCompositionTable Table { get; set; }
        [Column(TypeName = "VARCHAR"), StringLength(256)]
        public string Name { get; set; }

        public CookMethods RetentionType { get; set; }

        public string FoodGroupId { get; set; }

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

        public FoodComposition Multiply(FoodComposition source)
        {
            FoodComposition ret = source.Multiply(1);
            ret.EnergykCal = ret.EnergykCal * (this.EnergykCal ?? 1);
            ret.EnergyWFibre = ret.EnergyWFibre * (this.EnergyWFibre ?? 1);
            ret.EnergyWoFibre = ret.EnergyWoFibre * (this.EnergyWoFibre ?? 1);
            ret.EnergykJ = ret.EnergykJ * (this.EnergykJ ?? 1);
            ret.Moisture = ret.Moisture * (this.Moisture ?? 1);
            ret.Protein = ret.Protein * (this.Protein ?? 1);
            ret.Fat = ret.Fat * (this.Fat ?? 1);
            ret.SaturatedFat = ret.SaturatedFat * (this.SaturatedFat ?? 1);
            ret.MonounsaturatedFat = ret.MonounsaturatedFat * (this.MonounsaturatedFat ?? 1);
            ret.PolyunsaturatedFat = ret.PolyunsaturatedFat * (this.PolyunsaturatedFat ?? 1);
            ret.LinoleicAcic = ret.LinoleicAcic * (this.LinoleicAcic ?? 1);
            ret.AlphaLinolenicAcid = ret.AlphaLinolenicAcid * (this.AlphaLinolenicAcid ?? 1);
            ret.CarbohydratesWoSa = ret.CarbohydratesWoSa * (this.CarbohydratesWoSa ?? 1);
            ret.Carbohydrates = ret.Carbohydrates * (this.Carbohydrates ?? 1);
            ret.Starch = ret.Starch * (this.Starch ?? 1);
            ret.SugarTotal = ret.SugarTotal * (this.SugarTotal ?? 1);
            ret.SugarAdded = ret.SugarAdded * (this.SugarAdded ?? 1);
            ret.SugarFree = ret.SugarFree * (this.SugarFree ?? 1);
            ret.Fibre = ret.Fibre * (this.Fibre ?? 1);
            ret.Alcohol = ret.Alcohol * (this.Alcohol ?? 1);
            ret.Ash = ret.Ash * (this.Ash ?? 1);
            ret.Retinol = ret.Retinol * (this.Retinol ?? 1);
            ret.BetaCarotene = ret.BetaCarotene * (this.BetaCarotene ?? 1);
            ret.ProvitaminA = ret.ProvitaminA * (this.ProvitaminA ?? 1);
            ret.VitaminA = ret.VitaminA * (this.VitaminA ?? 1);
            ret.Thiamin = ret.Thiamin * (this.Thiamin ?? 1);
            ret.Riboflavin = ret.Riboflavin * (this.Riboflavin ?? 1);
            ret.Niacin = ret.Niacin * (this.Niacin ?? 1);
            ret.NiacinDE = ret.NiacinDE * (this.NiacinDE ?? 1);
            ret.VitaminB6 = ret.VitaminB6 * (this.VitaminB6 ?? 1);
            ret.VitaminB12 = ret.VitaminB12 * (this.VitaminB12 ?? 1);
            ret.VitaminC = ret.VitaminC * (this.VitaminC ?? 1);
            ret.AlphaTocopherol = ret.AlphaTocopherol * (this.AlphaTocopherol ?? 1);
            ret.VitaminE = ret.VitaminE * (this.VitaminE ?? 1);
            ret.Folate = ret.Folate * (this.Folate ?? 1);
            ret.FolicAcid = ret.FolicAcid * (this.FolicAcid ?? 1);
            ret.FolatesTotal = ret.FolatesTotal * (this.FolatesTotal ?? 1);
            ret.FolateDietary = ret.FolateDietary * (this.FolateDietary ?? 1);
            ret.Calcium = ret.Calcium * (this.Calcium ?? 1);
            ret.Iodine = ret.Iodine * (this.Iodine ?? 1);
            ret.Phosphorus = ret.Phosphorus * (this.Phosphorus ?? 1);
            ret.Sodium = ret.Sodium * (this.Sodium ?? 1);
            ret.Potassium = ret.Potassium * (this.Potassium ?? 1);
            ret.Iron = ret.Iron * (this.Iron ?? 1);
            ret.Magnesium = ret.Magnesium * (this.Magnesium ?? 1);
            ret.Selenium = ret.Selenium * (this.Selenium ?? 1);
            ret.Copper = ret.Copper * (this.Copper ?? 1);
            ret.Zinc = ret.Zinc * (this.Zinc ?? 1);
            ret.Caffeine = ret.Caffeine * (this.Caffeine ?? 1);
            ret.Tryptophan = ret.Tryptophan * (this.Tryptophan ?? 1);
            ret.Eicosapentaenoic = ret.Eicosapentaenoic * (this.Eicosapentaenoic ?? 1);
            ret.Docosapentaenoic = ret.Docosapentaenoic * (this.Docosapentaenoic ?? 1);
            ret.Docosahexaenoic = ret.Docosahexaenoic * (this.Docosahexaenoic ?? 1);
            ret.Omega3FattyAcid = ret.Omega3FattyAcid * (this.Omega3FattyAcid ?? 1);
            ret.TransFattyAcid = ret.TransFattyAcid * (this.TransFattyAcid ?? 1);
            ret.Cholesterol = ret.Cholesterol * (this.Cholesterol ?? 1);

            return ret;
        }
    }
}