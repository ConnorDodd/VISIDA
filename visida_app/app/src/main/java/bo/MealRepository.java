package bo;

import android.app.Application;
import android.os.AsyncTask;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import bo.db.AppDatabase;
import bo.db.dao.MealDao;
import bo.db.entity.FoodItem;
import bo.db.entity.GuestInformation;
import bo.db.entity.Meal;

import static bo.AppConstants.EATINGOCCASIONDURATION_HOURS;

public class MealRepository {

    private MealDao mMDao;

    public MealRepository(Application app) {
        AppDatabase db = AppDatabase.getInstance(app);
        this.mMDao = db.getMealDao();
    }

    public long addMeal(Meal meal) throws InvalidParameterException{
        try {
            return new AddMealAsync(mMDao).execute(meal).get();
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        throw new InvalidParameterException("Invalid Meal being added to the Database");
    }

    public Meal getMeal(long mealId) {
        try {
            return new GetMealAsync(mMDao).execute(mealId).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Meal> getUnfinalizedMeals() {
        try{
            return new GetUnfinalizedMealsAsync(mMDao).execute().get();
        } catch (InterruptedException | ExecutionException ex){
            ex.printStackTrace();
        }
        return null;
    }

    public List<FoodItem> getDishes(long mealId) {
        try{
            return new GetDishesForMealAsync(mMDao).execute(mealId).get();
        } catch (InterruptedException | ExecutionException ex){
            ex.printStackTrace();
        }
        return null;
    }

    public void update(Meal m) {
        new UpdateMeanAsync(mMDao).execute(m);
    }

    /**
     * Check if a meal has been created in the last {@link AppConstants#EATINGOCCASIONDURATION_HOURS}
     * hours
     * @return
     */
    public Meal getRecentMeal() {
        Meal meal = null;
        try {
            meal = new GetRecentMealAsync(mMDao).execute(EATINGOCCASIONDURATION_HOURS).get();
        }
        catch (InterruptedException | ExecutionException ex){
            ex.printStackTrace();
        }
        if (meal == null) {
            meal = new Meal();
            meal.setMealId(addMeal(meal));
        }
        return meal;
    }

    public List<Meal> getAll() {
        try{
            return new GetAllMealsAsync(mMDao).execute().get();
        }
        catch (InterruptedException | ExecutionException ex){
            ex.printStackTrace();
        }
        return null;
    }

    public void deleteMeal(long mealId) {
        new DeleteMealAsync(mMDao).execute(mealId);
    }

    private static class DeleteMealAsync extends AsyncTask<Long, Void, Void>{
        private MealDao mDao;

        DeleteMealAsync(MealDao dao) {
            this.mDao = dao;
        }

        @Override
        protected Void doInBackground(Long... ids) {
            mDao.delete(ids[0]);
            return null;
        }
    }
    private static class GetAllMealsAsync extends AsyncTask<Void, Void, List<Meal>>{
        private MealDao mDao;

        GetAllMealsAsync(MealDao dao) {
            this.mDao = dao;
        }

        @Override
        protected List<Meal> doInBackground(Void... voids) {
            return mDao.getAll();
        }
    }
    private static class GetRecentMealAsync extends AsyncTask<Integer, Void, Meal>{
        private MealDao mDao;

        GetRecentMealAsync(MealDao dao) {
            this.mDao = dao;
        }

        @Override
        protected Meal doInBackground(Integer... delay) {
            return mDao.getRecentMeal();
        }
    }
    private static class GetMealAsync extends AsyncTask<Long, Void, Meal> {

        private MealDao mDao;

        GetMealAsync(MealDao dao) {
            this.mDao = dao;
        }
        @Override
        protected Meal doInBackground(Long... ids) {
            return mDao.getMeal(ids[0]);
        }
    }
    private static class UpdateMeanAsync extends AsyncTask<Meal, Void, Void> {

        private MealDao mDao;
        UpdateMeanAsync(MealDao dao) {
            this.mDao = dao;
        }
        @Override
        protected Void doInBackground(Meal... meals) {
             mDao.update(meals[0]);
             return null;
        }
    }
    private static class AddMealAsync extends AsyncTask<Meal, Void, Long> {

        private MealDao mDao;
        AddMealAsync(MealDao dao) {
            this.mDao = dao;
        }
        @Override
        protected Long doInBackground(Meal... meals) {
            return mDao.insert(meals)[0];
        }
    }
    private static class GetUnfinalizedMealsAsync extends AsyncTask<Void, Void, List<Meal>> {

        private MealDao mDao;

        GetUnfinalizedMealsAsync(MealDao dao) {
            this.mDao = dao;
        }
        @Override
        protected List<Meal> doInBackground(Void... ids) {
            return mDao.getAllUnfinalized();
        }
    }
    private static class GetDishesForMealAsync extends AsyncTask<Long, Void, List<FoodItem>> {

        private MealDao mDao;

        GetDishesForMealAsync(MealDao dao) {
            this.mDao = dao;
        }
        @Override
        protected List<FoodItem> doInBackground(Long... ids) {
            return mDao.getDishes(ids[0]);
        }
    }
}
