using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Models.User;

namespace VISIDA_API.Models.FCT
{
    public class FoodCompositionOrig
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int VisidaId { get; set; }

        [JsonIgnore]
        public virtual FoodCompositionTable Table { get; set; }

        public DateTime ModifiedDate { get; set; }
        [JsonIgnore]
        public LoginUser ModifiedBy { get; set; }
        public DateTime? DeletedDate { get; set; }
        [JsonIgnore]
        public LoginUser DeletedBy { get; set; }
        public FoodCompositionUpdate CreatedMetadata { get; set; }

        [Column(TypeName = "VARCHAR"), StringLength(64)]//, Index(IsUnique = true)]
        public string AseanId { get; set; }
        [Column(TypeName = "VARCHAR"), StringLength(256)]
        public string Origin { get; set; }
        [Column(TypeName = "VARCHAR"), StringLength(256)]
        public string Name { get; set; }
        [Column(TypeName = "NVARCHAR"), StringLength(256)]
        public string AlternateName { get; set; }

        public double? Density { get; set; }
        public double? Energy { get; set; }
        public double? Moisture { get; set; }
        public double? Protein { get; set; }
        public double? TotalFat { get; set; }
        public double? Carbohydrate { get; set; }
        public double? Starch { get; set; }
        public double? TotalSugar { get; set; }
        public double? AddedSugar { get; set; }
        public double? FreeSugar { get; }
        public double? Fibre { get; set; }
        public double? Alcohol { get; set; }
        public double? Ash { get; set; }
        public double? Retinol { get; set; }
        public double? BetaCarotene { get; set; }
        public double? Calcium { get; set; }
        public double? Phosphorus { get; set; }
        public double? Sodium { get; set; }
        public double? Potassium { get; set; }
        public double? Iron { get; set; }
        public double? Copper { get; set; }
        public double? Zinc { get; set; }
        public double? VitaminA { get; set; }
        public double? VitaminB1 { get; set; }
        public double? VitaminB2 { get; set; }
        public double? Niacin { get; set; }
        public double? VitaminC { get; set; }
        public double? Folate { get; set; }
        public double? VitaminB6 { get; set; }
        public double? VitaminB12 { get; set; }
        public double? Alphatocopherol { get; set; }
        public double? VitaminE { get; set; }
        public double? Tryptophan { get; set; }
        public double? Iodine { get; set; }
        public double? Magnesium { get; set; } 
        public double? Selenium { get; set; }
        public double? Caffeine { get; set; }
        public double? Cholesterol { get; set; }
        public double? SaturatedFat { get; set; }
        public double? MonounsaturatedFat { get; set; }
        public double? PolyunsaturatedFat { get; set; }
        public double? LinoleicAcid { get; set; }
        public double? AlphaLinolenicAcid { get; set; }
        public double? Eicosapentaenoic { get; set; }
        public double? Docosapentaenoic { get; set; }
        public double? Docosahexaenoic { get; set; }
        public double? Omega3FattyAcid { get; set; }
        public double? TransFattyAcid { get; set; }
    }

    public class FoodCompositionComparer : IEqualityComparer<FoodCompositionOrig>
    {
        public bool Equals(FoodCompositionOrig x, FoodCompositionOrig y)
        {
            return x.Name.Equals(y.Name);
        }

        public int GetHashCode(FoodCompositionOrig obj)
        {
            return obj.Name.GetHashCode();
        }
    }
}