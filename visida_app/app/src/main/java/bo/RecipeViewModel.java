package bo;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.Objects;

import bo.db.entity.IngredientCapture;
import bo.db.entity.Recipe;
import notification.AlarmController;

/**
 * Created by jnc985 on 29-Nov-17.
 */

public class RecipeViewModel extends AndroidViewModel {

    private RecipeRepository mRecipeRepository;
    private Recipe mRecipe;
    private LiveData<List<IngredientCapture>> mIngredients;
    private TaskRunner.Callback<LiveData<List<IngredientCapture>>> mIngredientCallback;

    //https://android.jlelse.eu/android-architecture-components-now-with-100-more-mvvm-11629a630125
    public RecipeViewModel(Application application) {
        super(application);
        //ViewModel instantiates with activity only instantiate once.
        if(mRecipeRepository == null){
            mRecipeRepository = new RecipeRepository(Objects.requireNonNull(application));
        }
    }

    public void setRecipeNameAudio(String recipeNameAudioFileName) {
        mRecipe.setRecipeNameAudioUrl(recipeNameAudioFileName);
        mRecipeRepository.update(mRecipe);
    }

    public void setRecipeNameText(String name) {
        mRecipe.setRecipeNameText(name);
        mRecipeRepository.update(mRecipe);
    }

    public void setRecipe(long recipeId){
        this.mRecipe = mRecipeRepository.getRecipe(recipeId);
        if(mRecipe == null){
            this.mRecipe = new Recipe();
            // Add the recipe to the database
            // Update generatedId in the ViewModel via a callback
            mRecipeRepository.addRecipe(mRecipe, (generatedId) -> {
                mRecipe.setRecipeId(generatedId);

                // Set the alarm on creating the recipe in case the user uses the
                // home button to leave the CreateRecipeActivity instead of the
                // submit button.
                AlarmController ac = new AlarmController(getApplication());
                ac.scheduleRecipeImageReminderNotification(generatedId);

                // Refresh the ingredients list
                if (mIngredientCallback != null) {
                    loadIngredients();
                }
            });
        }
    }

    // Loads the ingredients for the recipe (initially this should be an empty list).
    private void loadIngredients() {
        // Get the repository and get the items from it
        this.mIngredients = mRecipeRepository.getIngredients(mRecipe.getRecipeId());
        mIngredientCallback.onComplete(mIngredients);
    }

    public void addIngredient(IngredientCapture ic) {
        mRecipeRepository.addIngredient(ic);
    }

    /**
     * Returns the Observable list of ingredients.
     */
    public void getObservableIngredients(TaskRunner.Callback<LiveData<List<IngredientCapture>>> callback) {
        if(mRecipe.getId() == 0){
            mIngredientCallback = callback;
        }
        else {
            callback.onComplete(mRecipeRepository.getIngredients(mRecipe.getRecipeId()));
        }
    }

    public int size(){
        if(mIngredients == null || mIngredients.getValue() == null){
            return -1;
        }
        else{
            return mIngredients.getValue().size();
        }
    }

    public long getRecipeId() {
        return mRecipe.getRecipeId();
    }

    public String getRecipeNameAudioFileName() {
        return mRecipe.getRecipeNameAudioUrl();
    }

    public void updateIngredient(IngredientCapture ingredient) {
        mRecipeRepository.update(ingredient);
    }

    public void addRecipeFinalImage(String fileName) {
        mRecipe.setFinalImageUrl(fileName);
        mRecipeRepository.update(mRecipe);
    }

    public void deleteIngredient(IngredientCapture ingredient) {
        mRecipeRepository.deleteIngredient(getApplication().getApplicationContext(), ingredient);
    }

    public String getRecipeNameText() {
        return mRecipe.getRecipeNameText();
    }

    public String getRecipeImage() {
        return mRecipe.getFinalImageUrl();
    }
}
