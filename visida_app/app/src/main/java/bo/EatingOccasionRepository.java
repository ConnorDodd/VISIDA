package bo;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutionException;

import bo.db.AppDatabase;
import bo.db.dao.EatingOccasionDao;
import bo.db.dao.FoodItemDao;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;

/**
 * Created by jnc985 on 29-Nov-17.
 * Repository for holding Household member data. This class holds ALL of the household member together.
 */

public class EatingOccasionRepository {
    private static String TAG = "eatingoccaisonrepository";

    private EatingOccasionDao mEoDao;
    private FoodItemDao mFiDao;
    private Application app;

    public EatingOccasionRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.app = application;
        this.mEoDao = db.getEatingOccasionDao();
        this.mFiDao = db.getFoodItemDao();
    }

    public EatingOccasion getEatingOccasion(long eoId){
        EatingOccasion eo = null;

        try {
            eo  = new GetEatingOccasionAsync(mEoDao).execute(eoId).get();
            if(eo == null){
                Log.e(TAG, "No Eating Occasion Id" + eoId + " exists in database");
            }
            //For each eating occasion loop through and get the food items
            FoodItemRepository fiRepo = new FoodItemRepository(app);
            List<FoodItem> foodItems = fiRepo.getAllFoodItemsForEatingOccasion(eo.getEatingOccasionId());
            //Add the food Items to the Eating occasion
            eo.setFoodItems(foodItems);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return eo;
    }

    //Get the eating occasions for a given food record
    public List<EatingOccasion> getEatingOccasions(long foodRecordId){
        //Get all of the eating occasions
        List<EatingOccasion> eos = null;
        try {
            eos = new GetEatingOccasionForFoodRecordAsync(mEoDao).execute(foodRecordId).get();

            //For each eating occasion loop through and get the food items
            for(EatingOccasion eo : eos){
                FoodItemRepository fiRepo = new FoodItemRepository(app);
                List<FoodItem> foodItems = fiRepo.getAllFoodItemsForEatingOccasion(eo.getEatingOccasionId());
                //Add the food Items to the Eating occasion
                eo.setFoodItems(foodItems);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return eos;
    }

    public List<EatingOccasion> getEatingOccasions(Long[] eoIds) {

        //Get all of the eating occasions
        List<EatingOccasion> eos = null;
        try {
            eos = new GetEatingOccasionsAsync(mEoDao).execute(eoIds).get();

            //For each eating occasion loop through and get the food items
            for(EatingOccasion eo : eos){
                FoodItemRepository fiRepo = new FoodItemRepository(app);
                List<FoodItem> foodItems = fiRepo.getAllFoodItemsForEatingOccasion(eo.getEatingOccasionId());
                //Add the food Items to the Eating occasion
                eo.setFoodItems(foodItems);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return eos;
    }


    public List<EatingOccasion> getNonFinalizedEatingOccasions(long frId) {
        List<EatingOccasion> eos = null;
        try{
            eos = new GetNonFinalizedEatingOccasionForFoodRecordAsync(mEoDao).execute(frId).get();
            //For each eating occasion loop through and get the food items
            for(EatingOccasion eo : eos){
                FoodItemRepository fiRepo = new FoodItemRepository(app);
                List<FoodItem> foodItems = fiRepo.getAllFoodItemsForEatingOccasion(eo.getEatingOccasionId());
                //Add the food Items to the Eating occasion
                eo.setFoodItems(foodItems);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return eos;
    }

    public List<EatingOccasion> getNonFinalizedEatingOccasionsForHouseholdMember(long hmId) {
        try {
            return new GetNonFinalizedEatingOccasionForHMAsync(mEoDao).execute(hmId).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adds the given Eating Ocassion to the database
     * @param eo
     * @return Long the uid of the newly added eating Occasion
     */
    public Long addEatingOccasion(EatingOccasion eo) {
        //TODO Should this method add any FoodItems as well?
        //Atm it is assumed the FoodItems are already in the DB
        try {
            return new AddEatingOccasionAsync(mEoDao).execute(eo).get();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public void updateEatingOccasion(EatingOccasion eo)  {
        try {
            new UpdateEatingOccasionAsync(mEoDao, mFiDao).execute(eo);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public boolean hasNonFinalizedEatingOccasion(long hmId){
        try {
            //Get the Food Record Ids
            FoodRecordRepository frRepo = new FoodRecordRepository(app);
            List<FoodRecord> frs = frRepo.getFoodRecordsForHouseholdMember(hmId);
            for (FoodRecord fr : frs) {
                //Get the Eating OCcasions
                List<EatingOccasion> eos = new GetEatingOccasionForFoodRecordAsync(mEoDao).execute(fr.getFoodRecordId()).get();
                for (EatingOccasion eo : eos) {
                    if (!eo.isFinalized()) {
                        return true;
                    }
                }
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    public List<EatingOccasion> getEmptyEatingOccasions() {
        try {
            return new GetEmptyEatingOccasionsAsync(mEoDao).execute().get();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public void delete(EatingOccasion... eatingOccasions) {
        try {
            new DeleteEatingOccasionAsync(mEoDao).execute(eatingOccasions);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private static class DeleteEatingOccasionAsync extends AsyncTask<EatingOccasion, Void, Void> {

        private EatingOccasionDao mEoDao;
        DeleteEatingOccasionAsync(EatingOccasionDao eoDao){
            this.mEoDao = eoDao;
        }

        @Override
        protected Void doInBackground(EatingOccasion... eos) {
            mEoDao.delete(eos);
            return null;
        }
    }

    private static class GetEmptyEatingOccasionsAsync extends AsyncTask<Void, Void, List<EatingOccasion>> {

        private EatingOccasionDao mEoDao;
        GetEmptyEatingOccasionsAsync(EatingOccasionDao eoDao){
            this.mEoDao = eoDao;
        }

        @Override
        protected List<EatingOccasion> doInBackground(Void... voids) {
            return mEoDao.getEmpty();
        }
    }

    private static class AddEatingOccasionAsync extends AsyncTask<EatingOccasion, Void, Long> {

        private EatingOccasionDao mEoDao;
        AddEatingOccasionAsync(EatingOccasionDao eoDao){
            this.mEoDao = eoDao;
        }

        @Override
        protected Long doInBackground(EatingOccasion... eatingOccasion) {
            return mEoDao.insert(eatingOccasion[0]);
        }
    }
    private static class GetEatingOccasionsAsync extends AsyncTask<Long[], Void, List<EatingOccasion>>{

        private EatingOccasionDao mEoDao;
        GetEatingOccasionsAsync(EatingOccasionDao eoDao){
            this.mEoDao = eoDao;
        }
        @Override
        protected List<EatingOccasion> doInBackground(Long[]... eoIds) {
            return mEoDao.get(eoIds[0]);
        }
    }

    private static class GetEatingOccasionForFoodRecordAsync extends AsyncTask<Long, Void, List<EatingOccasion>>{

        private EatingOccasionDao mEoDao;
        GetEatingOccasionForFoodRecordAsync(EatingOccasionDao eoDao){
            this.mEoDao = eoDao;
        }
        @Override
        protected List<EatingOccasion> doInBackground(Long... longs) {
             return mEoDao.getAllEatingOccasionsForFoodRecord(longs[0]);
        }
    }

    private static class GetNonFinalizedEatingOccasionForHMAsync extends AsyncTask<Long, Void, List<EatingOccasion>>{

        private EatingOccasionDao mEoDao;
        GetNonFinalizedEatingOccasionForHMAsync(EatingOccasionDao eoDao){
            this.mEoDao = eoDao;
        }
        @Override
        protected List<EatingOccasion> doInBackground(Long... hmId) {
             return mEoDao.getAllNonFinalizedEatingOccasionsForHouseholdMember(hmId[0]);
        }
    }

    private static class GetNonFinalizedEatingOccasionForFoodRecordAsync extends AsyncTask<Long, Void, List<EatingOccasion>>{

        private EatingOccasionDao mEoDao;
        GetNonFinalizedEatingOccasionForFoodRecordAsync(EatingOccasionDao eoDao){
            this.mEoDao = eoDao;
        }
        @Override
        protected List<EatingOccasion> doInBackground(Long... longs) {
            return mEoDao.getAllNonFinalizedEatingOccasionsForFoodRecord(longs[0]);
        }
    }

    private static class GetEatingOccasionAsync extends AsyncTask<Long, Void, EatingOccasion>{

        private EatingOccasionDao mEoDao;
        GetEatingOccasionAsync(EatingOccasionDao eoDao){
            this.mEoDao = eoDao;
        }
        @Override
        protected EatingOccasion doInBackground(Long... eoId) {
            List<EatingOccasion> eos = mEoDao.getEatingOccasion(eoId[0]);
            if(eos.size() > 0) {
                return eos.get(0);
            }
            return null;
        }
    }

    private static class UpdateEatingOccasionAsync extends AsyncTask<EatingOccasion, Void, Integer>{

        private EatingOccasionDao mEoDao;
        private FoodItemDao mFiDao;
        UpdateEatingOccasionAsync(EatingOccasionDao eoDao, FoodItemDao fiDao){
            this.mEoDao = eoDao;
            this.mFiDao = fiDao;
        }

        @Override
        protected Integer doInBackground(EatingOccasion... eos) {
            //Update each of the food items
            EatingOccasion eo = eos[0];
            for(FoodItem fi : eo.getFoodItems()){
                mFiDao.update(fi);
            }
            //Update the eating occassion
            return mEoDao.update(eo);
        }
    }
}
