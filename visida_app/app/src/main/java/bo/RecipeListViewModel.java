package bo;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.Objects;

import bo.db.entity.Recipe;

/**
 * Created by jnc985 on 29-Nov-17.
 */

public class RecipeListViewModel extends AndroidViewModel {
    //File name format: recipeId_ingredientId_timestamp.<format>
    private RecipeRepository mRecipeRepository;
    private LiveData<List<Recipe>> mRecipes;

    //https://android.jlelse.eu/android-architecture-components-now-with-100-more-mvvm-11629a630125
    public RecipeListViewModel(Application application) {
        super(application);
        //ViewModel instantiates with activity only instantiate once.
        if(mRecipeRepository == null){
            mRecipeRepository = new RecipeRepository(Objects.requireNonNull(application));
        }
        loadRecipes();
    }

    //Loads the ingredients for the recipe (initially this should be an empty list).
    private void loadRecipes() {
        //Delete any ingredient captures which don't have an image
        this.mRecipeRepository.deleteEmptyIngredients();
        //Get the repostiory and get the items from it
        this.mRecipes = mRecipeRepository.getObservableUnsavedRecipes();
    }

    public void addRecipe(Recipe r) {
        mRecipeRepository.addRecipe(r, null);
    }

    /**
     * Returns the Observable list of ingredients.
     * @return
     */
    public LiveData<List<Recipe>> getObservableRecipes() {
        return mRecipes;
    }

    public int size(){
        if(mRecipes == null || mRecipes.getValue() == null){
            return -1;
        }
        else{
            return mRecipes.getValue().size();
        }
    }

    public void deleteRecipe(Recipe r) {
        mRecipeRepository.deleteRecipe(r);
    }

    public LiveData<List<Recipe>> getRecipes() {
        return mRecipeRepository.getAllRecipes();
    }
}
