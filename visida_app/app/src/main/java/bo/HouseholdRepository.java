package bo;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;

import bo.db.AppDatabase;
import bo.db.dao.HouseholdDao;
import bo.db.entity.GuestInformation;
import bo.db.entity.Household;
import bo.db.entity.HouseholdMember;
import bo.db.entity.Meal;
import bo.db.entity.Recipe;

/**
 * Created by jnc985 on 29-Nov-17.
 * Repository for holding Household member data. This class holds ALL of the household member together.
 */

public class HouseholdRepository {

    private Application app;
    private HouseholdDao mHouseholdDao;

    public HouseholdRepository(Application application) {
        this.app = application;
        this.mHouseholdDao = AppDatabase.getInstance(application).getHouseholdDao();
    }

    public Household getHousehold(){
        try{
            List<Household> households = new GetHouseholdAsync(mHouseholdDao).execute().get();
            if(households != null && households.size() > 0){
                return households.get(0);
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the entire houshold with all of the household members.
     * Calling this will result in at least 5 database calls. So only call when you need
     * ALL of the data in the databse.
     * @return
     */
    public Household getFullHousehold(){
        try{
            //Get the houshold
            RecipeRepository recipeRepo = new RecipeRepository(app);
            // TODO - this may break because we are not awaiting the whole amount of data to be returned
            LiveData<List<Recipe>> recipes = recipeRepo.getAllRecipes();
            MealRepository mealrepo = new MealRepository(app);
            List<Meal> meals = mealrepo.getAll();
            Household hh = new GetHouseholdAsync(mHouseholdDao).execute().get().get(0);
            //Get all of the repos
            FoodItemRepository fiRepo = new FoodItemRepository(app);
            List<GuestInformation> guestInfos = fiRepo.getAllGuestInformation();
            HouseholdMemberRepository hmRepo = new HouseholdMemberRepository(app);
            List<HouseholdMember> hms = hmRepo.getHouseholdMemberList();
            hh.setHouseholdMemberList(hms);
            hh.setHouseholdRecipes(recipes.getValue());
            hh.setHouseholdMeals(meals);
            hh.setHouseholdGuestInformation(guestInfos);
            return hh;
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return null;

    }

    public void addHousehold(Household hh) {
        try {
            new AddHouseholdAsync(mHouseholdDao).execute(hh).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }


    public void updateHousehold(Household household) {
        new UpdateHouseholdAsync(mHouseholdDao).execute(household);
    }

    private static class UpdateHouseholdAsync extends AsyncTask<Household, Void, Void>{

        private HouseholdDao mDao;
        UpdateHouseholdAsync(HouseholdDao dao){
            this.mDao = dao;
        }
        @Override
        protected Void doInBackground(Household... hms) {
            mDao.update(hms);
            return null;
        }
    }

    private static class AddHouseholdAsync extends AsyncTask<Household, Void, Void>{

        private HouseholdDao mDao;
        AddHouseholdAsync(HouseholdDao dao){
            this.mDao = dao;
        }

        @Override
        protected Void doInBackground(Household... hhs) {
            mDao.insert(hhs[0]);
            return null;
        }
    }

    private static class GetHouseholdAsync extends AsyncTask<Void, Void, List<Household>>{

        private HouseholdDao mDao;
        GetHouseholdAsync(HouseholdDao dao){
            this.mDao = dao;
        }

        @Override
        protected List<Household> doInBackground(Void... hhs) {
            return mDao.getAll();
        }
    }


}
