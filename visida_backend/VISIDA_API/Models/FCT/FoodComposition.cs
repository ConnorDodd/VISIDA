using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;

namespace VISIDA_API.Models.FCT
{
    public class FoodComposition : INutrientData
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }

        [ForeignKey("Table")]
        public int? Table_Id { get; set; }
        public FoodCompositionTable Table { get; set; }

        public DateTime ModifiedDate { get; set; }
        [Column(TypeName = "VARCHAR"), StringLength(256)]
        public string OriginId { get; set; }
        [Column(TypeName = "NVARCHAR"), StringLength(256)]
        public string Name { get; set; }
        [Column(TypeName = "NVARCHAR"), StringLength(256)]
        public string AlternateName { get; set; }

        [NotMapped, JsonIgnore]
        public List<Tuple<string, float>> MeasureItems
        {
            get
            {
                if (String.IsNullOrEmpty(Measures))
                    return new List<Tuple<string, float>>();
                List<Tuple<string, float>> ret = new List<Tuple<string, float>>();
                var q = Measures.Split('|');
                foreach (var j in q)
                {
                    var a = j.Split(':');
                    ret.Add(new Tuple<string, float>(a[0], float.Parse(a[1])));
                }
                return ret;
            }
        }

        public class FoodCompositionComparer : IEqualityComparer<FoodComposition>
        {
            public bool Equals(FoodComposition x, FoodComposition y)
            {
                return x.OriginId.Equals(y.OriginId);
            }

            public int GetHashCode(FoodComposition obj)
            {
                return obj.Name.GetHashCode();
            }
        }
        public string Measures { get; set; }
        //public string MeasuresML { get; set; }

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


        public double? YieldWater { get; set; }
        public double? YieldStoveTop { get; set; }
        public double? YieldOven { get; set; }

        public int FoodGroupId { get; set; }
        public bool IsRecipe { get; set; }

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

        public FoodComposition Clone()
        {
            return (FoodComposition)this.MemberwiseClone();
        }

        public void OverwriteValues(FoodComposition nfc)
        {
            Measures = nfc.Measures ?? Measures;
            //MeasuresML = nfc.MeasuresML ?? MeasuresML;
            AlternateName = nfc.AlternateName ?? AlternateName;
            OriginId = nfc.OriginId ?? OriginId;
            //Density = nfc.Density ?? Density;
            EnergykCal = nfc.EnergykCal ?? EnergykCal;
            EnergyWFibre = nfc.EnergyWFibre ?? EnergyWFibre;
            EnergyWoFibre = nfc.EnergyWoFibre ?? EnergyWoFibre;
            EnergykJ = nfc.EnergykJ ?? EnergykJ;
            Moisture = nfc.Moisture ?? Moisture;
            Protein = nfc.Protein ?? Protein;
            Fat = nfc.Fat ?? Fat;
            SaturatedFat = nfc.SaturatedFat ?? SaturatedFat;
            MonounsaturatedFat = nfc.MonounsaturatedFat ?? MonounsaturatedFat;
            PolyunsaturatedFat = nfc.PolyunsaturatedFat ?? PolyunsaturatedFat;
            LinoleicAcic = nfc.LinoleicAcic ?? LinoleicAcic;
            AlphaLinolenicAcid = nfc.AlphaLinolenicAcid ?? AlphaLinolenicAcid;
            CarbohydratesWoSa = nfc.CarbohydratesWoSa ?? CarbohydratesWoSa;
            Carbohydrates = nfc.Carbohydrates ?? Carbohydrates;
            Starch = nfc.Starch ?? Starch;
            SugarTotal = nfc.SugarTotal ?? SugarTotal;
            SugarAdded = nfc.SugarAdded ?? SugarAdded;
            SugarFree = nfc.SugarFree ?? SugarFree;
            Fibre = nfc.Fibre ?? Fibre;
            Alcohol = nfc.Alcohol ?? Alcohol;
            Ash = nfc.Ash ?? Ash;
            Retinol = nfc.Retinol ?? Retinol;
            BetaCarotene = nfc.BetaCarotene ?? BetaCarotene;
            ProvitaminA = nfc.ProvitaminA ?? ProvitaminA;
            VitaminA = nfc.VitaminA ?? VitaminA;
            Thiamin = nfc.Thiamin ?? Thiamin;
            Riboflavin = nfc.Riboflavin ?? Riboflavin;
            Niacin = nfc.Niacin ?? Niacin;
            NiacinDE = nfc.NiacinDE ?? NiacinDE;
            VitaminB6 = nfc.VitaminB6 ?? VitaminB6;
            VitaminB12 = nfc.VitaminB12 ?? VitaminB12;
            VitaminC = nfc.VitaminC ?? VitaminC;
            AlphaTocopherol = nfc.AlphaTocopherol ?? AlphaTocopherol;
            VitaminE = nfc.VitaminE ?? VitaminE;
            Folate = nfc.Folate ?? Folate;
            FolicAcid = nfc.FolicAcid ?? FolicAcid;
            FolatesTotal = nfc.FolatesTotal ?? FolatesTotal;
            FolateDietary = nfc.FolateDietary ?? FolateDietary;
            Calcium = nfc.Calcium ?? Calcium;
            Iodine = nfc.Iodine ?? Iodine;
            Phosphorus = nfc.Phosphorus ?? Phosphorus;
            Sodium = nfc.Sodium ?? Sodium;
            Potassium = nfc.Potassium ?? Potassium;
            Iron = nfc.Iron ?? Iron;
            Magnesium = nfc.Magnesium ?? Magnesium;
            Selenium = nfc.Selenium ?? Selenium;
            Copper = nfc.Copper ?? Copper;
            Zinc = nfc.Zinc ?? Zinc;
            Caffeine = nfc.Caffeine ?? Caffeine;
            Tryptophan = nfc.Tryptophan ?? Tryptophan;
            Eicosapentaenoic = nfc.Eicosapentaenoic ?? Eicosapentaenoic;
            Docosapentaenoic = nfc.Docosapentaenoic ?? Docosapentaenoic;
            Docosahexaenoic = nfc.Docosahexaenoic ?? Docosahexaenoic;
            Omega3FattyAcid = nfc.Omega3FattyAcid ?? Omega3FattyAcid;
            TransFattyAcid = nfc.TransFattyAcid ?? TransFattyAcid;
            Cholesterol = nfc.Cholesterol ?? Cholesterol;
            ADG_10 = nfc.ADG_10 ?? ADG_10;
            ADG_101 = nfc.ADG_101 ?? ADG_101;
            ADG_1011 = nfc.ADG_1011 ?? ADG_1011;
            ADG_1012 = nfc.ADG_1012 ?? ADG_1012;
            ADG_1013 = nfc.ADG_1013 ?? ADG_1013;
            ADG_1014 = nfc.ADG_1014 ?? ADG_1014;
            ADG_1015 = nfc.ADG_1015 ?? ADG_1015;
            ADG_1016 = nfc.ADG_1016 ?? ADG_1016;
            ADG_1017 = nfc.ADG_1017 ?? ADG_1017;
            ADG_1018 = nfc.ADG_1018 ?? ADG_1018;
            ADG_102 = nfc.ADG_102 ?? ADG_102;
            ADG_1021 = nfc.ADG_1021 ?? ADG_1021;
            ADG_1022 = nfc.ADG_1022 ?? ADG_1022;
            ADG_1023 = nfc.ADG_1023 ?? ADG_1023;
            ADG_1024 = nfc.ADG_1024 ?? ADG_1024;
            ADG_1025 = nfc.ADG_1025 ?? ADG_1025;
            ADG_1026 = nfc.ADG_1026 ?? ADG_1026;
            ADG_1027 = nfc.ADG_1027 ?? ADG_1027;
            ADG_1028 = nfc.ADG_1028 ?? ADG_1028;
            ADG_20 = nfc.ADG_20 ?? ADG_20;
            ADG_201 = nfc.ADG_201 ?? ADG_201;
            ADG_202 = nfc.ADG_202 ?? ADG_202;
            ADG_203 = nfc.ADG_203 ?? ADG_203;
            ADG_204 = nfc.ADG_204 ?? ADG_204;
            ADG_205 = nfc.ADG_205 ?? ADG_205;
            ADG_2051 = nfc.ADG_2051 ?? ADG_2051;
            ADG_2052 = nfc.ADG_2052 ?? ADG_2052;
            ADG_30 = nfc.ADG_30 ?? ADG_30;
            ADG_301 = nfc.ADG_301 ?? ADG_301;
            ADG_302 = nfc.ADG_302 ?? ADG_302;
            ADG_303 = nfc.ADG_303 ?? ADG_303;
            ADG_40 = nfc.ADG_40 ?? ADG_40;
            ADG_401 = nfc.ADG_401 ?? ADG_401;
            ADG_4011 = nfc.ADG_4011 ?? ADG_4011;
            ADG_4012 = nfc.ADG_4012 ?? ADG_4012;
            ADG_402 = nfc.ADG_402 ?? ADG_402;
            ADG_4021 = nfc.ADG_4021 ?? ADG_4021;
            ADG_4022 = nfc.ADG_4022 ?? ADG_4022;
            ADG_4023 = nfc.ADG_4023 ?? ADG_4023;
            ADG_4024 = nfc.ADG_4024 ?? ADG_4024;
            ADG_4025 = nfc.ADG_4025 ?? ADG_4025;
            ADG_4026 = nfc.ADG_4026 ?? ADG_4026;
            ADG_4027 = nfc.ADG_4027 ?? ADG_4027;
            ADG_4028 = nfc.ADG_4028 ?? ADG_4028;
            ADG_403 = nfc.ADG_403 ?? ADG_403;
            ADG_4031 = nfc.ADG_4031 ?? ADG_4031;
            ADG_4032 = nfc.ADG_4032 ?? ADG_4032;
            ADG_4033 = nfc.ADG_4033 ?? ADG_4033;
            ADG_4034 = nfc.ADG_4034 ?? ADG_4034;
            ADG_4035 = nfc.ADG_4035 ?? ADG_4035;
            ADG_4036 = nfc.ADG_4036 ?? ADG_4036;
            ADG_4037 = nfc.ADG_4037 ?? ADG_4037;
            ADG_4038 = nfc.ADG_4038 ?? ADG_4038;
            ADG_4039 = nfc.ADG_4039 ?? ADG_4039;
            ADG_50 = nfc.ADG_50 ?? ADG_50;
            ADG_501 = nfc.ADG_501 ?? ADG_501;
            ADG_5011 = nfc.ADG_5011 ?? ADG_5011;
            ADG_5012 = nfc.ADG_5012 ?? ADG_5012;
            ADG_502 = nfc.ADG_502 ?? ADG_502;
            ADG_5021 = nfc.ADG_5021 ?? ADG_5021;
            ADG_5022 = nfc.ADG_5022 ?? ADG_5022;
            ADG_503 = nfc.ADG_503 ?? ADG_503;
            ADG_5031 = nfc.ADG_5031 ?? ADG_5031;
            ADG_5032 = nfc.ADG_5032 ?? ADG_5032;
            ADG_504 = nfc.ADG_504 ?? ADG_504;
            ADG_5041 = nfc.ADG_5041 ?? ADG_5041;
            ADG_5042 = nfc.ADG_5042 ?? ADG_5042;
            ADG_505 = nfc.ADG_505 ?? ADG_505;
            ADG_506 = nfc.ADG_506 ?? ADG_506;
            ADG_507 = nfc.ADG_507 ?? ADG_507;
            ADG_5071 = nfc.ADG_5071 ?? ADG_5071;
            ADG_5072 = nfc.ADG_5072 ?? ADG_5072;
            ADG_508 = nfc.ADG_508 ?? ADG_508;
            ADG_60 = nfc.ADG_60 ?? ADG_60;
            ADG_70 = nfc.ADG_70 ?? ADG_70;
            ADG_701 = nfc.ADG_701 ?? ADG_701;
            ADG_702 = nfc.ADG_702 ?? ADG_702;
            ADG_703 = nfc.ADG_703 ?? ADG_703;
        }

        public FoodComposition Add(FoodComposition fc)
        {
            if (fc == null)
                return this;
            return new FoodComposition()
            {
                //Density = (this.Density + fc.Density) ?? (fc.Density ?? this.Density),
                EnergykCal = (this.EnergykCal + fc.EnergykCal) ?? (fc.EnergykCal ?? this.EnergykCal),
                EnergyWFibre = (this.EnergyWFibre + fc.EnergyWFibre) ?? (fc.EnergyWFibre ?? this.EnergyWFibre),
                EnergyWoFibre = (this.EnergyWoFibre + fc.EnergyWoFibre) ?? (fc.EnergyWoFibre ?? this.EnergyWoFibre),
                EnergykJ = (this.EnergykJ + fc.EnergykJ) ?? (fc.EnergykJ ?? this.EnergykJ),
                Moisture = (this.Moisture + fc.Moisture) ?? (fc.Moisture ?? this.Moisture),
                Protein = (this.Protein + fc.Protein) ?? (fc.Protein ?? this.Protein),
                Fat = (this.Fat + fc.Fat) ?? (fc.Fat ?? this.Fat),
                SaturatedFat = (this.SaturatedFat + fc.SaturatedFat) ?? (fc.SaturatedFat ?? this.SaturatedFat),
                MonounsaturatedFat = (this.MonounsaturatedFat + fc.MonounsaturatedFat) ?? (fc.MonounsaturatedFat ?? this.MonounsaturatedFat),
                PolyunsaturatedFat = (this.PolyunsaturatedFat + fc.PolyunsaturatedFat) ?? (fc.PolyunsaturatedFat ?? this.PolyunsaturatedFat),
                LinoleicAcic = (this.LinoleicAcic + fc.LinoleicAcic) ?? (fc.LinoleicAcic ?? this.LinoleicAcic),
                AlphaLinolenicAcid = (this.AlphaLinolenicAcid + fc.AlphaLinolenicAcid) ?? (fc.AlphaLinolenicAcid ?? this.AlphaLinolenicAcid),
                CarbohydratesWoSa = (this.CarbohydratesWoSa + fc.CarbohydratesWoSa) ?? (fc.CarbohydratesWoSa ?? this.CarbohydratesWoSa),
                Carbohydrates = (this.Carbohydrates + fc.Carbohydrates) ?? (fc.Carbohydrates ?? this.Carbohydrates),
                Starch = (this.Starch + fc.Starch) ?? (fc.Starch ?? this.Starch),
                SugarTotal = (this.SugarTotal + fc.SugarTotal) ?? (fc.SugarTotal ?? this.SugarTotal),
                SugarAdded = (this.SugarAdded + fc.SugarAdded) ?? (fc.SugarAdded ?? this.SugarAdded),
                SugarFree = (this.SugarFree + fc.SugarFree) ?? (fc.SugarFree ?? this.SugarFree),
                Fibre = (this.Fibre + fc.Fibre) ?? (fc.Fibre ?? this.Fibre),
                Alcohol = (this.Alcohol + fc.Alcohol) ?? (fc.Alcohol ?? this.Alcohol),
                Ash = (this.Ash + fc.Ash) ?? (fc.Ash ?? this.Ash),
                Retinol = (this.Retinol + fc.Retinol) ?? (fc.Retinol ?? this.Retinol),
                BetaCarotene = (this.BetaCarotene + fc.BetaCarotene) ?? (fc.BetaCarotene ?? this.BetaCarotene),
                ProvitaminA = (this.ProvitaminA + fc.ProvitaminA) ?? (fc.ProvitaminA ?? this.ProvitaminA),
                VitaminA = (this.VitaminA + fc.VitaminA) ?? (fc.VitaminA ?? this.VitaminA),
                Thiamin = (this.Thiamin + fc.Thiamin) ?? (fc.Thiamin ?? this.Thiamin),
                Riboflavin = (this.Riboflavin + fc.Riboflavin) ?? (fc.Riboflavin ?? this.Riboflavin),
                Niacin = (this.Niacin + fc.Niacin) ?? (fc.Niacin ?? this.Niacin),
                NiacinDE = (this.NiacinDE + fc.NiacinDE) ?? (fc.NiacinDE ?? this.NiacinDE),
                VitaminB6 = (this.VitaminB6 + fc.VitaminB6) ?? (fc.VitaminB6 ?? this.VitaminB6),
                VitaminB12 = (this.VitaminB12 + fc.VitaminB12) ?? (fc.VitaminB12 ?? this.VitaminB12),
                VitaminC = (this.VitaminC + fc.VitaminC) ?? (fc.VitaminC ?? this.VitaminC),
                AlphaTocopherol = (this.AlphaTocopherol + fc.AlphaTocopherol) ?? (fc.AlphaTocopherol ?? this.AlphaTocopherol),
                VitaminE = (this.VitaminE + fc.VitaminE) ?? (fc.VitaminE ?? this.VitaminE),
                Folate = (this.Folate + fc.Folate) ?? (fc.Folate ?? this.Folate),
                FolicAcid = (this.FolicAcid + fc.FolicAcid) ?? (fc.FolicAcid ?? this.FolicAcid),
                FolatesTotal = (this.FolatesTotal + fc.FolatesTotal) ?? (fc.FolatesTotal ?? this.FolatesTotal),
                FolateDietary = (this.FolateDietary + fc.FolateDietary) ?? (fc.FolateDietary ?? this.FolateDietary),
                Calcium = (this.Calcium + fc.Calcium) ?? (fc.Calcium ?? this.Calcium),
                Iodine = (this.Iodine + fc.Iodine) ?? (fc.Iodine ?? this.Iodine),
                Phosphorus = (this.Phosphorus + fc.Phosphorus) ?? (fc.Phosphorus ?? this.Phosphorus),
                Sodium = (this.Sodium + fc.Sodium) ?? (fc.Sodium ?? this.Sodium),
                Potassium = (this.Potassium + fc.Potassium) ?? (fc.Potassium ?? this.Potassium),
                Iron = (this.Iron + fc.Iron) ?? (fc.Iron ?? this.Iron),
                Magnesium = (this.Magnesium + fc.Magnesium) ?? (fc.Magnesium ?? this.Magnesium),
                Selenium = (this.Selenium + fc.Selenium) ?? (fc.Selenium ?? this.Selenium),
                Copper = (this.Copper + fc.Copper) ?? (fc.Copper ?? this.Copper),
                Zinc = (this.Zinc + fc.Zinc) ?? (fc.Zinc ?? this.Zinc),
                Caffeine = (this.Caffeine + fc.Caffeine) ?? (fc.Caffeine ?? this.Caffeine),
                Tryptophan = (this.Tryptophan + fc.Tryptophan) ?? (fc.Tryptophan ?? this.Tryptophan),
                Eicosapentaenoic = (this.Eicosapentaenoic + fc.Eicosapentaenoic) ?? (fc.Eicosapentaenoic ?? this.Eicosapentaenoic),
                Docosapentaenoic = (this.Docosapentaenoic + fc.Docosapentaenoic) ?? (fc.Docosapentaenoic ?? this.Docosapentaenoic),
                Docosahexaenoic = (this.Docosahexaenoic + fc.Docosahexaenoic) ?? (fc.Docosahexaenoic ?? this.Docosahexaenoic),
                Omega3FattyAcid = (this.Omega3FattyAcid + fc.Omega3FattyAcid) ?? (fc.Omega3FattyAcid ?? this.Omega3FattyAcid),
                TransFattyAcid = (this.TransFattyAcid + fc.TransFattyAcid) ?? (fc.TransFattyAcid ?? this.TransFattyAcid),
                Cholesterol = (this.Cholesterol + fc.Cholesterol) ?? (fc.Cholesterol ?? this.Cholesterol),
                ADG_10 = (this.ADG_10 + fc.ADG_10) ?? (fc.ADG_10 ?? this.ADG_10),
                ADG_101 = (this.ADG_101 + fc.ADG_101) ?? (fc.ADG_101 ?? this.ADG_101),
                ADG_1011 = (this.ADG_1011 + fc.ADG_1011) ?? (fc.ADG_1011 ?? this.ADG_1011),
                ADG_1012 = (this.ADG_1012 + fc.ADG_1012) ?? (fc.ADG_1012 ?? this.ADG_1012),
                ADG_1013 = (this.ADG_1013 + fc.ADG_1013) ?? (fc.ADG_1013 ?? this.ADG_1013),
                ADG_1014 = (this.ADG_1014 + fc.ADG_1014) ?? (fc.ADG_1014 ?? this.ADG_1014),
                ADG_1015 = (this.ADG_1015 + fc.ADG_1015) ?? (fc.ADG_1015 ?? this.ADG_1015),
                ADG_1016 = (this.ADG_1016 + fc.ADG_1016) ?? (fc.ADG_1016 ?? this.ADG_1016),
                ADG_1017 = (this.ADG_1017 + fc.ADG_1017) ?? (fc.ADG_1017 ?? this.ADG_1017),
                ADG_1018 = (this.ADG_1018 + fc.ADG_1018) ?? (fc.ADG_1018 ?? this.ADG_1018),
                ADG_102 = (this.ADG_102 + fc.ADG_102) ?? (fc.ADG_102 ?? this.ADG_102),
                ADG_1021 = (this.ADG_1021 + fc.ADG_1021) ?? (fc.ADG_1021 ?? this.ADG_1021),
                ADG_1022 = (this.ADG_1022 + fc.ADG_1022) ?? (fc.ADG_1022 ?? this.ADG_1022),
                ADG_1023 = (this.ADG_1023 + fc.ADG_1023) ?? (fc.ADG_1023 ?? this.ADG_1023),
                ADG_1024 = (this.ADG_1024 + fc.ADG_1024) ?? (fc.ADG_1024 ?? this.ADG_1024),
                ADG_1025 = (this.ADG_1025 + fc.ADG_1025) ?? (fc.ADG_1025 ?? this.ADG_1025),
                ADG_1026 = (this.ADG_1026 + fc.ADG_1026) ?? (fc.ADG_1026 ?? this.ADG_1026),
                ADG_1027 = (this.ADG_1027 + fc.ADG_1027) ?? (fc.ADG_1027 ?? this.ADG_1027),
                ADG_1028 = (this.ADG_1028 + fc.ADG_1028) ?? (fc.ADG_1028 ?? this.ADG_1028),
                ADG_20 = (this.ADG_20 + fc.ADG_20) ?? (fc.ADG_20 ?? this.ADG_20),
                ADG_201 = (this.ADG_201 + fc.ADG_201) ?? (fc.ADG_201 ?? this.ADG_201),
                ADG_202 = (this.ADG_202 + fc.ADG_202) ?? (fc.ADG_202 ?? this.ADG_202),
                ADG_203 = (this.ADG_203 + fc.ADG_203) ?? (fc.ADG_203 ?? this.ADG_203),
                ADG_204 = (this.ADG_204 + fc.ADG_204) ?? (fc.ADG_204 ?? this.ADG_204),
                ADG_205 = (this.ADG_205 + fc.ADG_205) ?? (fc.ADG_205 ?? this.ADG_205),
                ADG_2051 = (this.ADG_2051 + fc.ADG_2051) ?? (fc.ADG_2051 ?? this.ADG_2051),
                ADG_2052 = (this.ADG_2052 + fc.ADG_2052) ?? (fc.ADG_2052 ?? this.ADG_2052),
                ADG_30 = (this.ADG_30 + fc.ADG_30) ?? (fc.ADG_30 ?? this.ADG_30),
                ADG_301 = (this.ADG_301 + fc.ADG_301) ?? (fc.ADG_301 ?? this.ADG_301),
                ADG_302 = (this.ADG_302 + fc.ADG_302) ?? (fc.ADG_302 ?? this.ADG_302),
                ADG_303 = (this.ADG_303 + fc.ADG_303) ?? (fc.ADG_303 ?? this.ADG_303),
                ADG_40 = (this.ADG_40 + fc.ADG_40) ?? (fc.ADG_40 ?? this.ADG_40),
                ADG_401 = (this.ADG_401 + fc.ADG_401) ?? (fc.ADG_401 ?? this.ADG_401),
                ADG_4011 = (this.ADG_4011 + fc.ADG_4011) ?? (fc.ADG_4011 ?? this.ADG_4011),
                ADG_4012 = (this.ADG_4012 + fc.ADG_4012) ?? (fc.ADG_4012 ?? this.ADG_4012),
                ADG_402 = (this.ADG_402 + fc.ADG_402) ?? (fc.ADG_402 ?? this.ADG_402),
                ADG_4021 = (this.ADG_4021 + fc.ADG_4021) ?? (fc.ADG_4021 ?? this.ADG_4021),
                ADG_4022 = (this.ADG_4022 + fc.ADG_4022) ?? (fc.ADG_4022 ?? this.ADG_4022),
                ADG_4023 = (this.ADG_4023 + fc.ADG_4023) ?? (fc.ADG_4023 ?? this.ADG_4023),
                ADG_4024 = (this.ADG_4024 + fc.ADG_4024) ?? (fc.ADG_4024 ?? this.ADG_4024),
                ADG_4025 = (this.ADG_4025 + fc.ADG_4025) ?? (fc.ADG_4025 ?? this.ADG_4025),
                ADG_4026 = (this.ADG_4026 + fc.ADG_4026) ?? (fc.ADG_4026 ?? this.ADG_4026),
                ADG_4027 = (this.ADG_4027 + fc.ADG_4027) ?? (fc.ADG_4027 ?? this.ADG_4027),
                ADG_4028 = (this.ADG_4028 + fc.ADG_4028) ?? (fc.ADG_4028 ?? this.ADG_4028),
                ADG_403 = (this.ADG_403 + fc.ADG_403) ?? (fc.ADG_403 ?? this.ADG_403),
                ADG_4031 = (this.ADG_4031 + fc.ADG_4031) ?? (fc.ADG_4031 ?? this.ADG_4031),
                ADG_4032 = (this.ADG_4032 + fc.ADG_4032) ?? (fc.ADG_4032 ?? this.ADG_4032),
                ADG_4033 = (this.ADG_4033 + fc.ADG_4033) ?? (fc.ADG_4033 ?? this.ADG_4033),
                ADG_4034 = (this.ADG_4034 + fc.ADG_4034) ?? (fc.ADG_4034 ?? this.ADG_4034),
                ADG_4035 = (this.ADG_4035 + fc.ADG_4035) ?? (fc.ADG_4035 ?? this.ADG_4035),
                ADG_4036 = (this.ADG_4036 + fc.ADG_4036) ?? (fc.ADG_4036 ?? this.ADG_4036),
                ADG_4037 = (this.ADG_4037 + fc.ADG_4037) ?? (fc.ADG_4037 ?? this.ADG_4037),
                ADG_4038 = (this.ADG_4038 + fc.ADG_4038) ?? (fc.ADG_4038 ?? this.ADG_4038),
                ADG_4039 = (this.ADG_4039 + fc.ADG_4039) ?? (fc.ADG_4039 ?? this.ADG_4039),
                ADG_50 = (this.ADG_50 + fc.ADG_50) ?? (fc.ADG_50 ?? this.ADG_50),
                ADG_501 = (this.ADG_501 + fc.ADG_501) ?? (fc.ADG_501 ?? this.ADG_501),
                ADG_5011 = (this.ADG_5011 + fc.ADG_5011) ?? (fc.ADG_5011 ?? this.ADG_5011),
                ADG_5012 = (this.ADG_5012 + fc.ADG_5012) ?? (fc.ADG_5012 ?? this.ADG_5012),
                ADG_502 = (this.ADG_502 + fc.ADG_502) ?? (fc.ADG_502 ?? this.ADG_502),
                ADG_5021 = (this.ADG_5021 + fc.ADG_5021) ?? (fc.ADG_5021 ?? this.ADG_5021),
                ADG_5022 = (this.ADG_5022 + fc.ADG_5022) ?? (fc.ADG_5022 ?? this.ADG_5022),
                ADG_503 = (this.ADG_503 + fc.ADG_503) ?? (fc.ADG_503 ?? this.ADG_503),
                ADG_5031 = (this.ADG_5031 + fc.ADG_5031) ?? (fc.ADG_5031 ?? this.ADG_5031),
                ADG_5032 = (this.ADG_5032 + fc.ADG_5032) ?? (fc.ADG_5032 ?? this.ADG_5032),
                ADG_504 = (this.ADG_504 + fc.ADG_504) ?? (fc.ADG_504 ?? this.ADG_504),
                ADG_5041 = (this.ADG_5041 + fc.ADG_5041) ?? (fc.ADG_5041 ?? this.ADG_5041),
                ADG_5042 = (this.ADG_5042 + fc.ADG_5042) ?? (fc.ADG_5042 ?? this.ADG_5042),
                ADG_505 = (this.ADG_505 + fc.ADG_505) ?? (fc.ADG_505 ?? this.ADG_505),
                ADG_506 = (this.ADG_506 + fc.ADG_506) ?? (fc.ADG_506 ?? this.ADG_506),
                ADG_507 = (this.ADG_507 + fc.ADG_507) ?? (fc.ADG_507 ?? this.ADG_507),
                ADG_5071 = (this.ADG_5071 + fc.ADG_5071) ?? (fc.ADG_5071 ?? this.ADG_5071),
                ADG_5072 = (this.ADG_5072 + fc.ADG_5072) ?? (fc.ADG_5072 ?? this.ADG_5072),
                ADG_508 = (this.ADG_508 + fc.ADG_508) ?? (fc.ADG_508 ?? this.ADG_508),
                ADG_60 = (this.ADG_60 + fc.ADG_60) ?? (fc.ADG_60 ?? this.ADG_60),
                ADG_70 = (this.ADG_70 + fc.ADG_70) ?? (fc.ADG_70 ?? this.ADG_70),
                ADG_701 = (this.ADG_701 + fc.ADG_701) ?? (fc.ADG_701 ?? this.ADG_701),
                ADG_702 = (this.ADG_702 + fc.ADG_702) ?? (fc.ADG_702 ?? this.ADG_702),
                ADG_703 = (this.ADG_703 + fc.ADG_703) ?? (fc.ADG_703 ?? this.ADG_703)
            };
        }

        public FoodComposition Multiply(double by)
        {
            return new FoodComposition()
            {
                //Density = this.Density * by,
                EnergykCal = this.EnergykCal * by,
                EnergyWFibre = this.EnergyWFibre * by,
                EnergyWoFibre = this.EnergyWoFibre * by,
                EnergykJ = this.EnergykJ * by,
                Moisture = this.Moisture * by,
                Protein = this.Protein * by,
                Fat = this.Fat * by,
                SaturatedFat = this.SaturatedFat * by,
                MonounsaturatedFat = this.MonounsaturatedFat * by,
                PolyunsaturatedFat = this.PolyunsaturatedFat * by,
                LinoleicAcic = this.LinoleicAcic * by,
                AlphaLinolenicAcid = this.AlphaLinolenicAcid * by,
                CarbohydratesWoSa = this.CarbohydratesWoSa * by,
                Carbohydrates = this.Carbohydrates * by,
                Starch = this.Starch * by,
                SugarTotal = this.SugarTotal * by,
                SugarAdded = this.SugarAdded * by,
                SugarFree = this.SugarFree * by,
                Fibre = this.Fibre * by,
                Alcohol = this.Alcohol * by,
                Ash = this.Ash * by,
                Retinol = this.Retinol * by,
                BetaCarotene = this.BetaCarotene * by,
                ProvitaminA = this.ProvitaminA * by,
                VitaminA = this.VitaminA * by,
                Thiamin = this.Thiamin * by,
                Riboflavin = this.Riboflavin * by,
                Niacin = this.Niacin * by,
                NiacinDE = this.NiacinDE * by,
                VitaminB6 = this.VitaminB6 * by,
                VitaminB12 = this.VitaminB12 * by,
                VitaminC = this.VitaminC * by,
                AlphaTocopherol = this.AlphaTocopherol * by,
                VitaminE = this.VitaminE * by,
                Folate = this.Folate * by,
                FolicAcid = this.FolicAcid * by,
                FolatesTotal = this.FolatesTotal * by,
                FolateDietary = this.FolateDietary * by,
                Calcium = this.Calcium * by,
                Iodine = this.Iodine * by,
                Phosphorus = this.Phosphorus * by,
                Sodium = this.Sodium * by,
                Potassium = this.Potassium * by,
                Iron = this.Iron * by,
                Magnesium = this.Magnesium * by,
                Selenium = this.Selenium * by,
                Copper = this.Copper * by,
                Zinc = this.Zinc * by,
                Caffeine = this.Caffeine * by,
                Tryptophan = this.Tryptophan * by,
                Eicosapentaenoic = this.Eicosapentaenoic * by,
                Docosapentaenoic = this.Docosapentaenoic * by,
                Docosahexaenoic = this.Docosahexaenoic * by,
                Omega3FattyAcid = this.Omega3FattyAcid * by,
                TransFattyAcid = this.TransFattyAcid * by,
                Cholesterol = this.Cholesterol * by,
                ADG_10 = this.ADG_10 * by,
                ADG_101 = this.ADG_101 * by,
                ADG_1011 = this.ADG_1011 * by,
                ADG_1012 = this.ADG_1012 * by,
                ADG_1013 = this.ADG_1013 * by,
                ADG_1014 = this.ADG_1014 * by,
                ADG_1015 = this.ADG_1015 * by,
                ADG_1016 = this.ADG_1016 * by,
                ADG_1017 = this.ADG_1017 * by,
                ADG_1018 = this.ADG_1018 * by,
                ADG_102 = this.ADG_102 * by,
                ADG_1021 = this.ADG_1021 * by,
                ADG_1022 = this.ADG_1022 * by,
                ADG_1023 = this.ADG_1023 * by,
                ADG_1024 = this.ADG_1024 * by,
                ADG_1025 = this.ADG_1025 * by,
                ADG_1026 = this.ADG_1026 * by,
                ADG_1027 = this.ADG_1027 * by,
                ADG_1028 = this.ADG_1028 * by,
                ADG_20 = this.ADG_20 * by,
                ADG_201 = this.ADG_201 * by,
                ADG_202 = this.ADG_202 * by,
                ADG_203 = this.ADG_203 * by,
                ADG_204 = this.ADG_204 * by,
                ADG_205 = this.ADG_205 * by,
                ADG_2051 = this.ADG_2051 * by,
                ADG_2052 = this.ADG_2052 * by,
                ADG_30 = this.ADG_30 * by,
                ADG_301 = this.ADG_301 * by,
                ADG_302 = this.ADG_302 * by,
                ADG_303 = this.ADG_303 * by,
                ADG_40 = this.ADG_40 * by,
                ADG_401 = this.ADG_401 * by,
                ADG_4011 = this.ADG_4011 * by,
                ADG_4012 = this.ADG_4012 * by,
                ADG_402 = this.ADG_402 * by,
                ADG_4021 = this.ADG_4021 * by,
                ADG_4022 = this.ADG_4022 * by,
                ADG_4023 = this.ADG_4023 * by,
                ADG_4024 = this.ADG_4024 * by,
                ADG_4025 = this.ADG_4025 * by,
                ADG_4026 = this.ADG_4026 * by,
                ADG_4027 = this.ADG_4027 * by,
                ADG_4028 = this.ADG_4028 * by,
                ADG_403 = this.ADG_403 * by,
                ADG_4031 = this.ADG_4031 * by,
                ADG_4032 = this.ADG_4032 * by,
                ADG_4033 = this.ADG_4033 * by,
                ADG_4034 = this.ADG_4034 * by,
                ADG_4035 = this.ADG_4035 * by,
                ADG_4036 = this.ADG_4036 * by,
                ADG_4037 = this.ADG_4037 * by,
                ADG_4038 = this.ADG_4038 * by,
                ADG_4039 = this.ADG_4039 * by,
                ADG_50 = this.ADG_50 * by,
                ADG_501 = this.ADG_501 * by,
                ADG_5011 = this.ADG_5011 * by,
                ADG_5012 = this.ADG_5012 * by,
                ADG_502 = this.ADG_502 * by,
                ADG_5021 = this.ADG_5021 * by,
                ADG_5022 = this.ADG_5022 * by,
                ADG_503 = this.ADG_503 * by,
                ADG_5031 = this.ADG_5031 * by,
                ADG_5032 = this.ADG_5032 * by,
                ADG_504 = this.ADG_504 * by,
                ADG_5041 = this.ADG_5041 * by,
                ADG_5042 = this.ADG_5042 * by,
                ADG_505 = this.ADG_505 * by,
                ADG_506 = this.ADG_506 * by,
                ADG_507 = this.ADG_507 * by,
                ADG_5071 = this.ADG_5071 * by,
                ADG_5072 = this.ADG_5072 * by,
                ADG_508 = this.ADG_508 * by,
                ADG_60 = this.ADG_60 * by,
                ADG_70 = this.ADG_70 * by,
                ADG_701 = this.ADG_701 * by,
                ADG_702 = this.ADG_702 * by,
                ADG_703 = this.ADG_703 * by
            };
        }

        public FoodComposition Divide(double by)
        {
            return new FoodComposition()
            {
                //Density = this.Density / by,
                EnergykCal = this.EnergykCal / by,
                EnergyWFibre = this.EnergyWFibre / by,
                EnergyWoFibre = this.EnergyWoFibre / by,
                EnergykJ = this.EnergykJ / by,
                Moisture = this.Moisture / by,
                Protein = this.Protein / by,
                Fat = this.Fat / by,
                SaturatedFat = this.SaturatedFat / by,
                MonounsaturatedFat = this.MonounsaturatedFat / by,
                PolyunsaturatedFat = this.PolyunsaturatedFat / by,
                LinoleicAcic = this.LinoleicAcic / by,
                AlphaLinolenicAcid = this.AlphaLinolenicAcid / by,
                CarbohydratesWoSa = this.CarbohydratesWoSa / by,
                Carbohydrates = this.Carbohydrates / by,
                Starch = this.Starch / by,
                SugarTotal = this.SugarTotal / by,
                SugarAdded = this.SugarAdded / by,
                SugarFree = this.SugarFree / by,
                Fibre = this.Fibre / by,
                Alcohol = this.Alcohol / by,
                Ash = this.Ash / by,
                Retinol = this.Retinol / by,
                BetaCarotene = this.BetaCarotene / by,
                ProvitaminA = this.ProvitaminA / by,
                VitaminA = this.VitaminA / by,
                Thiamin = this.Thiamin / by,
                Riboflavin = this.Riboflavin / by,
                Niacin = this.Niacin / by,
                NiacinDE = this.NiacinDE / by,
                VitaminB6 = this.VitaminB6 / by,
                VitaminB12 = this.VitaminB12 / by,
                VitaminC = this.VitaminC / by,
                AlphaTocopherol = this.AlphaTocopherol / by,
                VitaminE = this.VitaminE / by,
                Folate = this.Folate / by,
                FolicAcid = this.FolicAcid / by,
                FolatesTotal = this.FolatesTotal / by,
                FolateDietary = this.FolateDietary / by,
                Calcium = this.Calcium / by,
                Iodine = this.Iodine / by,
                Phosphorus = this.Phosphorus / by,
                Sodium = this.Sodium / by,
                Potassium = this.Potassium / by,
                Iron = this.Iron / by,
                Magnesium = this.Magnesium / by,
                Selenium = this.Selenium / by,
                Copper = this.Copper / by,
                Zinc = this.Zinc / by,
                Caffeine = this.Caffeine / by,
                Tryptophan = this.Tryptophan / by,
                Eicosapentaenoic = this.Eicosapentaenoic / by,
                Docosapentaenoic = this.Docosapentaenoic / by,
                Docosahexaenoic = this.Docosahexaenoic / by,
                Omega3FattyAcid = this.Omega3FattyAcid / by,
                TransFattyAcid = this.TransFattyAcid / by,
                Cholesterol = this.Cholesterol / by,
                ADG_10 = this.ADG_10 / by,
                ADG_101 = this.ADG_101 / by,
                ADG_1011 = this.ADG_1011 / by,
                ADG_1012 = this.ADG_1012 / by,
                ADG_1013 = this.ADG_1013 / by,
                ADG_1014 = this.ADG_1014 / by,
                ADG_1015 = this.ADG_1015 / by,
                ADG_1016 = this.ADG_1016 / by,
                ADG_1017 = this.ADG_1017 / by,
                ADG_1018 = this.ADG_1018 / by,
                ADG_102 = this.ADG_102 / by,
                ADG_1021 = this.ADG_1021 / by,
                ADG_1022 = this.ADG_1022 / by,
                ADG_1023 = this.ADG_1023 / by,
                ADG_1024 = this.ADG_1024 / by,
                ADG_1025 = this.ADG_1025 / by,
                ADG_1026 = this.ADG_1026 / by,
                ADG_1027 = this.ADG_1027 / by,
                ADG_1028 = this.ADG_1028 / by,
                ADG_20 = this.ADG_20 / by,
                ADG_201 = this.ADG_201 / by,
                ADG_202 = this.ADG_202 / by,
                ADG_203 = this.ADG_203 / by,
                ADG_204 = this.ADG_204 / by,
                ADG_205 = this.ADG_205 / by,
                ADG_2051 = this.ADG_2051 / by,
                ADG_2052 = this.ADG_2052 / by,
                ADG_30 = this.ADG_30 / by,
                ADG_301 = this.ADG_301 / by,
                ADG_302 = this.ADG_302 / by,
                ADG_303 = this.ADG_303 / by,
                ADG_40 = this.ADG_40 / by,
                ADG_401 = this.ADG_401 / by,
                ADG_4011 = this.ADG_4011 / by,
                ADG_4012 = this.ADG_4012 / by,
                ADG_402 = this.ADG_402 / by,
                ADG_4021 = this.ADG_4021 / by,
                ADG_4022 = this.ADG_4022 / by,
                ADG_4023 = this.ADG_4023 / by,
                ADG_4024 = this.ADG_4024 / by,
                ADG_4025 = this.ADG_4025 / by,
                ADG_4026 = this.ADG_4026 / by,
                ADG_4027 = this.ADG_4027 / by,
                ADG_4028 = this.ADG_4028 / by,
                ADG_403 = this.ADG_403 / by,
                ADG_4031 = this.ADG_4031 / by,
                ADG_4032 = this.ADG_4032 / by,
                ADG_4033 = this.ADG_4033 / by,
                ADG_4034 = this.ADG_4034 / by,
                ADG_4035 = this.ADG_4035 / by,
                ADG_4036 = this.ADG_4036 / by,
                ADG_4037 = this.ADG_4037 / by,
                ADG_4038 = this.ADG_4038 / by,
                ADG_4039 = this.ADG_4039 / by,
                ADG_50 = this.ADG_50 / by,
                ADG_501 = this.ADG_501 / by,
                ADG_5011 = this.ADG_5011 / by,
                ADG_5012 = this.ADG_5012 / by,
                ADG_502 = this.ADG_502 / by,
                ADG_5021 = this.ADG_5021 / by,
                ADG_5022 = this.ADG_5022 / by,
                ADG_503 = this.ADG_503 / by,
                ADG_5031 = this.ADG_5031 / by,
                ADG_5032 = this.ADG_5032 / by,
                ADG_504 = this.ADG_504 / by,
                ADG_5041 = this.ADG_5041 / by,
                ADG_5042 = this.ADG_5042 / by,
                ADG_505 = this.ADG_505 / by,
                ADG_506 = this.ADG_506 / by,
                ADG_507 = this.ADG_507 / by,
                ADG_5071 = this.ADG_5071 / by,
                ADG_5072 = this.ADG_5072 / by,
                ADG_508 = this.ADG_508 / by,
                ADG_60 = this.ADG_60 / by,
                ADG_70 = this.ADG_70 / by,
                ADG_701 = this.ADG_701 / by,
                ADG_702 = this.ADG_702 / by,
                ADG_703 = this.ADG_703 / by
            };
        }
    }
}