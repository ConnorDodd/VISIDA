using System;
using System.Collections.Generic;
using System.Data;
using System.Data.Entity;
using System.Data.Entity.Infrastructure;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using System.Web.Http.Description;
using VISIDA_API.Models;
using VISIDA_API.Models.FCT;

namespace VISIDA_API.Controllers
{
    public class RecipesController : AuthController
    {
        // GET: api/Recipes
        public IEnumerable<FoodCompositionRecipe> GetFoodCompositionRecipes()
        {
            return db.FoodCompositionRecipes.OrderBy(x => x.Name).ToList();
        }

        // GET: api/Recipes/5
        [ResponseType(typeof(FoodCompositionRecipe))]
        public IHttpActionResult GetFoodCompositionRecipe(int id)
        {
            FoodCompositionRecipe foodCompositionRecipe = db.FoodCompositionRecipes.Find(id);
            if (foodCompositionRecipe == null)
            {
                return NotFound();
            }

            return Ok(foodCompositionRecipe);
        }

        // PUT: api/Recipes/5
        [ResponseType(typeof(void))]
        public IHttpActionResult PutFoodCompositionRecipe(int id, FoodCompositionRecipe foodCompositionRecipe)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);
            if (id != foodCompositionRecipe.Id)
                return BadRequest();

            db.Entry(foodCompositionRecipe).State = EntityState.Modified;
            foreach (var item in foodCompositionRecipe.Ingredients)
            {
                db.Entry(item).State = EntityState.Modified;
            }

            try
            {
                db.SaveChanges();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!FoodCompositionRecipeExists(id))
                {
                    return NotFound();
                }
                else
                {
                    throw;
                }
            }

            return StatusCode(HttpStatusCode.Accepted);
        }

        // POST: api/Recipes
        [ResponseType(typeof(FoodCompositionRecipe))]
        public IHttpActionResult PostFoodCompositionRecipe(FoodCompositionRecipe recipe)
        {
            if (!ModelState.IsValid)
                return BadRequest("Model is invalid.");

            recipe.CreatedDate = DateTime.Now;

            if (db.FoodCompositionRecipes.FirstOrDefault(x => x.Name.Equals(recipe.Name)) != null)
                return BadRequest("There is already a recipe with that name.");

            db.FoodCompositionRecipes.Add(recipe);
            db.SaveChanges();

            return Ok(new { id = recipe.Id });
        }

        // DELETE: api/Recipes/5
        [ResponseType(typeof(FoodCompositionRecipe))]
        public IHttpActionResult DeleteFoodCompositionRecipe(int id)
        {
            FoodCompositionRecipe foodCompositionRecipe = db.FoodCompositionRecipes.Find(id);
            if (foodCompositionRecipe == null)
            {
                return NotFound();
            }

            db.FoodCompositionRecipes.Remove(foodCompositionRecipe);
            db.SaveChanges();

            return Ok(foodCompositionRecipe);
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                db.Dispose();
            }
            base.Dispose(disposing);
        }

        private bool FoodCompositionRecipeExists(int id)
        {
            return db.FoodCompositionRecipes.Count(e => e.Id == id) > 0;
        }
    }
}