package bo;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;

import java.util.List;

import bo.db.entity.FoodItem;
import bo.db.entity.Meal;

public class MealViewModel extends AndroidViewModel {

    private Meal mMeal;
    private MealRepository mMealRepo;
    private FoodItemRepository mFoodItemRepo;
    private LiveData<List<FoodItem>> mDishes;


    /**
     * Create a new instance of a MealViewModel.
     * @param application Current Application context (needed to create repository)
     * @param mealId Meal Id of the Meal this view model represents.
     *               If 0 gets the recent meal if none found then creates a new meal.
     *               If negative the meal will be null and will require the {@link #setMeal(long)}
     *               method to be called before using any methods on the meal object.
     */
    public MealViewModel(@NonNull Application application, long mealId) {
        super(application);

        if (mFoodItemRepo == null) {
            mFoodItemRepo = new FoodItemRepository(application);
        }
        if (mMealRepo == null) {
            mMealRepo = new MealRepository(application);
        }
        setMeal(mealId);
    }

    /**
     * Sets the current meal to the given ID
     * @param mealId If 0 tries to get the most recent meal (within {@link AppConstants#EATINGOCCASIONDURATION_HOURS} hours)
     *               if no meal is found it creates a new one and sets it to the view models current Meal.
     *               If > 0 selects the meal from the database.
     *               If negative remains null. Must call {@link #setMeal(long)} with id >= 0 to set the meal.
     */
    public void setMeal(long mealId) {

        if(mealId == 0) {
            this.mMeal = mMealRepo.getRecentMeal();
        }
        else if(mealId > 0){
            this.mMeal = mMealRepo.getMeal(mealId);
        }

        if (mMeal != null) {
            this.mDishes = mFoodItemRepo.getObservableDishesForMeal(mMeal.getMealId());
        }
    }


    public void removefoodItem(FoodItem fi) {
        mFoodItemRepo.deleteFoodItem(getApplication().getApplicationContext(), fi);
    }

    public LiveData<List<FoodItem>> getObservableDishes() {
        return mDishes;
    }

    public Meal getMeal() {
        return mMeal;
    }

    public void addDish(String imageName, String audioName) {
        //Create a food item
        FoodItem newDish = new FoodItem();
        newDish.setImageUrl(imageName);
        newDish.setAudioUrls(audioName);
        newDish.setMealId(mMeal.getMealId());
        mFoodItemRepo.addFoodItem(newDish);
    }

    public void addDishTextOnly(String imageName, String description){
        FoodItem newDish = new FoodItem();
        newDish.setImageUrl(imageName);
        newDish.setDescription(description);
        newDish.setMealId(mMeal.getMealId());
        mFoodItemRepo.addFoodItem(newDish);
    }

    public List<Meal> getUnfinalizedMeals() {
        //Get all the unfinalized Meals
         return mMealRepo.getUnfinalizedMeals();
    }

    public List<FoodItem> getDishes(long mealId) {
        return mMealRepo.getDishes(mealId);
    }

    public void update(Meal m) {
        mMealRepo.update(m);
    }

    /**
     * Assumes the order of the array is
     * guests[0] = Adult Male
     * guests[1] = Adult Female
     * guests[2] = Child
     */
//    public void setGeusts(int[] guests) {
//        mMeal.setAdultMaleGuests(guests[0]);
//        mMeal.setAdultFemaleGuests(guests[1]);
//        mMeal.setChildGuests(guests[2]);
//        mMeal.setGuestInfoCaptured(true);
//        //Update database
//        mMealRepo.update(mMeal);
//    }

    public boolean hasGuestInfo() {
        return mMeal.isGuestInfoCaptured();
    }

    public List<Long> getLinkedRecipes(){
        return mMeal.getRecipeIds();
    }

    public void linkRecipes(List<Long> recipeIds) {
        mMeal.setRecipeIds(recipeIds);
        this.mMealRepo.update(mMeal);
    }

    public int foodItemCount() {
        System.out.println("MEAL FOOD ITEM COUNT: " + mDishes.getValue().size());
        return mDishes.getValue().size();
    }

    public void deleteMeal() {
        mMealRepo.deleteMeal(mMeal.getMealId());
    }
}
