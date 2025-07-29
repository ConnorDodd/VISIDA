using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Web;
using VISIDA_API.Controllers;
using VISIDA_API.Models.FCT;

namespace VISIDA_API.Models.InternalObjects
{
    public class CookRecipe : IUploadImage, IUploadAudio
    {
        [Key, DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        public int OriginId { get; set; } //Only used during parsing to connect objects
        public virtual Household Household { get; set; }
        [Column(TypeName = "NVARCHAR"), StringLength(256)]
        public string Name { get; set; }

        [Column(TypeName = "VARCHAR"), StringLength(256)]
        public string ImageName { get; set; }
        [Column(TypeName = "VARCHAR"), StringLength(256)]
        public string ImageUrl { get; set; }
        [Column(TypeName = "VARCHAR"), StringLength(256)]
        public string ImageThumbUrl { get; set; }
        [Column(TypeName = "VARCHAR"), StringLength(256)]
        public string ImageUrlUpdated { get; set; }

        public bool IsFiducialPresent { get; set; }
        public virtual ImageHomography Homography { get; set; }

        [Column(TypeName = "VARCHAR"), StringLength(256)]
        public string AudioName { get; set; }
        [Column(TypeName = "VARCHAR"), StringLength(256)]
        public string AudioUrl { get; set; }
        public string AudioUrlUpdated { get; set; }

        //TODO NVarchar
        [Column(TypeName = "NVARCHAR"), StringLength(1000)]
        public string TextDescription { get; set; }
        public DateTime CaptureTime { get; set; }

        public virtual FoodComposition FoodComposition { get; set; }
        public virtual ICollection<CookIngredient> Ingredients { get; set; }
        public virtual ICollection<Comment> Comments { get; set; }

        public double? YieldFactor { get; set; }
        public string YieldFactorSource { get; set; }
        public double TotalCookedGrams { get; set; }

        [Column(TypeName = "NVARCHAR"), StringLength(1024)]
        public string NTranscript { get; set; }
        [Column(TypeName = "VARCHAR"), StringLength(1024)]
        public string Transcript { get; set; }
        public bool Hidden { get; set; }
        public AnnotatedStatuses AnnotatedStatus { get; set; }

        public ICollection<ImageRecord> ImageRecords { get; set; }
        public bool IsSource { get; set; }

        public void UpdateFoodComposition(VISIDA_APIContext db)
        {
            FoodComposition result = new FoodComposition();
            double totalQuantity = 0, uncookedQuantity = 0;
            foreach (CookIngredient ingredient in Ingredients)
            {
                if (ingredient.ImageRecord.Hidden)
                    continue;
                foreach (FoodItem item in ingredient.ImageRecord.FoodItems.ToList())
                {
                    if (item.FoodComposition == null)
                        continue;
                    double mult = item.QuantityGrams / 100.0;
                    var toadd = item.FoodComposition.Multiply(mult); //db.FoodCompositions.Find(ingredient.FoodCompositionId)?.Multiply(mult);

                    double yieldFactor = 1.0;
                    RetentionFactor retentionFactor = null;
                    string foodGroupStr = item.FoodComposition.FoodGroupId.ToString();
                    //Retention factor works off first 2 figures in food group
                    string foodGroup = foodGroupStr.Length >= 2 ? foodGroupStr.Substring(0, 2) : "0";
                    //Find yield factor and retention factor for cooking method for item/foodgroup
                    switch (ingredient.CookMethod)
                    {
                        case CookIngredient.CookMethods.None:
                            uncookedQuantity += item.QuantityGrams;
                            break;
                        case CookIngredient.CookMethods.Water:
                            yieldFactor = item.FoodComposition.YieldWater ?? 1.0;
                            retentionFactor = db.RetentionFactors.FirstOrDefault(x => x.FoodGroupId == foodGroup && x.RetentionType == CookIngredient.CookMethods.Water);
                            break;
                        case CookIngredient.CookMethods.Grill:
                            yieldFactor = item.FoodComposition.YieldStoveTop ?? 1.0;
                            retentionFactor = db.RetentionFactors.FirstOrDefault(x => x.FoodGroupId == foodGroup && x.RetentionType == CookIngredient.CookMethods.Grill);
                            break;
                        case CookIngredient.CookMethods.Fry:
                            yieldFactor = item.FoodComposition.YieldStoveTop ?? 1.0;
                            retentionFactor = db.RetentionFactors.FirstOrDefault(x => x.FoodGroupId == foodGroup && x.RetentionType == CookIngredient.CookMethods.Fry);
                            break;
                        case CookIngredient.CookMethods.Oven:
                            yieldFactor = item.FoodComposition.YieldOven ?? 1.0;
                            retentionFactor = db.RetentionFactors.FirstOrDefault(x => x.FoodGroupId == foodGroup && x.RetentionType == CookIngredient.CookMethods.Oven);
                            break;
                        default:
                            break;
                    }
                    //Increase total cooked weight
                    totalQuantity += item.QuantityGrams;
                    //ingredientYield += item.QuantityGrams * yieldFactor;
                    if (retentionFactor != null)
                        toadd = retentionFactor.Multiply(toadd);
                    result = result.Add(toadd);
                }
            }

            double cookedQuantity = totalQuantity - uncookedQuantity;
            if (YieldFactor.HasValue && totalQuantity > 0)
            {
                double recipeYieldQuantity = cookedQuantity * (YieldFactor ?? 1);
                recipeYieldQuantity += uncookedQuantity;
                result = result.Divide(recipeYieldQuantity / 100);
                TotalCookedGrams = recipeYieldQuantity;
            }
            else
                TotalCookedGrams = totalQuantity;
            //else if (ingredientYield > 0) //Use ingredient level yield factor, avoid divide by 0
            //{
            //    result = result.Divide(ingredientYield / 100.0);
            //    TotalCookedGrams = ingredientYield;
            //}
            //else
            //{
            //    result = result.Multiply(0);
            //    TotalCookedGrams = 0;
            //}

            FoodComposition.OverwriteValues(result);

            var usages = db.FoodItems.Where(x => x.FoodCompositionId == FoodComposition.Id);
            foreach (var usage in usages)
            {
                ImageRecordsController.RecalculateFoodItem(usage, db);
            }

        }
    }
}