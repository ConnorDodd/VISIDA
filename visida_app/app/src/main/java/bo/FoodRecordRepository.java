package bo;

import android.app.Application;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import android.os.AsyncTask;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import bo.db.AppDatabase;
import bo.db.dao.FoodRecordDao;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;
import notification.AlarmController;

/**
 * Created by jnc985 on 29-Nov-17.
 * Repository for holding Household member data. This class holds ALL of the household member together.
 */

public class FoodRecordRepository {


    private FoodRecordDao mFrDao;
    private Application app;
    //TODO Cache the data queried. At this stage the only time the Food records are queried is
    //when they are initially created or added to. This will require some testing on performance
    //but currently due to the expected number of times this stuff will be queried we wont bother
    //caching the data.
    private Map<Integer, List<FoodRecord>> foodRecordCache;

    public FoodRecordRepository(Application application) {
        this.app = application;
        this.mFrDao = AppDatabase.getInstance(application).getFoodRecordDao();
        this.foodRecordCache = new HashMap<>();
    }

    /**
     * Returns a songle food record for a household member
     * @param hmId
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public FoodRecord getTodaysFoodRecordFor(long hmId, Date today) throws ExecutionException, InterruptedException {
        //Get all the food records for the household member
        List<FoodRecord> allFoodRecords = new GetFoodRecordForHmAsync(mFrDao)
                .execute(hmId)
                .get();

        //Find todays food record
        for(FoodRecord fr : allFoodRecords){
            //If there is a current food record return it.
            if(Utilities.sameDay(fr.getDate(), today)){
                //Get the eating occasions for this food record
                EatingOccasionRepository eoRepo = new EatingOccasionRepository(app);
                List<EatingOccasion> eatingOccasions = eoRepo.getEatingOccasions(fr.getFoodRecordId());
                fr.setEatingOccasions(eatingOccasions);
                return fr;
            }
        }
         return null;
    }

    public FoodRecord getFoodRecord(long frId){
        try {
            //Get the FoodRecord
            FoodRecord fr = new GetFoodRecordAsync(mFrDao).execute(frId).get();
            if(fr != null) {
                //Get the household member
                HouseholdMemberRepository hmRepo = new HouseholdMemberRepository(app);
                HouseholdMember hm = hmRepo.getHouseholdMember(fr.getHouseholdMemberId());
                fr.setHouseholdMember(hm);
                //Get the eating occasions
                EatingOccasionRepository eoRepo = new EatingOccasionRepository(app);
                List<EatingOccasion> eos = eoRepo.getEatingOccasions(frId);
                fr.setEatingOccasions(eos);
            }
            return fr;
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Inserts a food record into the database and returns the inserted id (Primary Key)
     * @param fr
     * @return
     */
    public Long addNewFoodRecord(FoodRecord fr) throws ExecutionException, InterruptedException {
        return new AddFoodRecordAsync(mFrDao).execute(fr).get()[0];
    }

    public List<FoodRecord> getFoodRecordsForHouseholdMember(long householdMemberId) {
        try {
            List<FoodRecord> frs = new GetFoodRecordForHmAsync(mFrDao).execute(householdMemberId).get();
            for(FoodRecord fr : frs){
                EatingOccasionRepository eoRepo = new EatingOccasionRepository(app);
                List<EatingOccasion> eos = eoRepo.getEatingOccasions(fr.getFoodRecordId());
                fr.setEatingOccasions(eos);
            }
            return frs;
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public void finalizeReviewed(long frId) {
        //Get the food record.
        FoodRecord fr = getFoodRecord(frId);
        //TODO IF FR IS NULL??
        //Loop through each eating occasion and finalise it and save in database
        for(EatingOccasion eo : fr.getEatingOccasions()){
            if(!eo.isFinalized()) {
                //eo.finalise();
                //EatingOccasionRepository eoRepo = new EatingOccasionRepository(app);
                //eoRepo.updateEatingOccasion(eo);
                //Cancel any notifications for this eating occasion
                AlarmController ac = new AlarmController(app);
                ac.cancelUnfinalizedEatingOccasionNotification(eo.getEatingOccasionId());
            }
        }
        setReviewed(fr);
    }

    public void setReviewed(FoodRecord fr) {
        //Set the FoodRecord that it has been reviewd
        System.out.println(fr.getFoodRecordId());
        fr.setReviewed(true);
        new UpdateFoodRecord(mFrDao).execute(fr);
    }

    public LiveData<List<FoodRecord>> getAllObservableFoodRecords() {
        //Get all the food records
        List<FoodRecord> frs = null;
        try {
            frs = new GetAllFoodRecords(mFrDao).execute().get();
            //Get all the eating occasions for those food records
            EatingOccasionRepository eoRepo = new EatingOccasionRepository(app);
            for(FoodRecord fr : frs){
                fr.setEatingOccasions(eoRepo.getEatingOccasions(fr.getFoodRecordId()));
            }
        } catch (InterruptedException | ExecutionException  e) {
            e.printStackTrace();
        }

        MutableLiveData observableFoodRecords = new MutableLiveData<>();
        observableFoodRecords.setValue(frs);

        return observableFoodRecords;
    }

    public LiveData<List<FoodRecord>> getObservableFoodRecordsForHouseholdMember(long uid) {
        //Get all the food records
        LiveData<List<FoodRecord>> frs = null;
        try{
            frs = new GetObservableAllFoodRecordsForHouseholdMember(mFrDao).execute(uid).get();
            frs = Transformations.map(frs, new Function<List<FoodRecord>, List<FoodRecord>>() {
                @Override
                public List<FoodRecord> apply(List<FoodRecord> input) {
                    //Using a transformation join the eating occasions to the foodrecords
                    EatingOccasionRepository eoRepo = new EatingOccasionRepository(app);
                    for(FoodRecord fr : input){
                        fr.setEatingOccasions(eoRepo.getEatingOccasions(fr.getFoodRecordId()));
                    }
                    return input;
                }
            });
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return frs;
    }

    public FoodRecord getFoodRecordFromPpId(String ppid) {
        try {
            HouseholdMemberRepository hmRepo = new HouseholdMemberRepository(app);
            FoodRecord fr = new GetFoodRecordForParticpantHouseholdMemberId(mFrDao).execute(ppid).get();
            if(fr == null){
                //No food record for that ppid for today. Create a new one.
                //Get the household Member
                HouseholdMember hm = hmRepo.getHouseholdMember(ppid);
                FoodRecord newFr = new FoodRecord(hm.getUid());
                newFr.setHouseholdMember(hm);
                newFr.setFoodRecordId(addNewFoodRecord(newFr));
                return newFr;
            }
            else {
                HouseholdMember hm = hmRepo.getHouseholdMember(fr.getHouseholdMemberId());
                fr.setHouseholdMember(hm);
                //Get the eating occasions
                EatingOccasionRepository eoRepo = new EatingOccasionRepository(app);
                List<EatingOccasion> eos = eoRepo.getEatingOccasions(fr.getFoodRecordId());
                fr.setEatingOccasions(eos);
                return fr;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class GetFoodRecordForParticpantHouseholdMemberId extends AsyncTask<String, Void, FoodRecord>{
        private FoodRecordDao mDao;
        GetFoodRecordForParticpantHouseholdMemberId(FoodRecordDao dao){
            this.mDao = dao;
        }

        @Override
        protected FoodRecord doInBackground(String... ppids) {
            return mDao.getTodaysFoodRecordForPpId(ppids[0]);
        }
    }
    private static class GetObservableAllFoodRecordsForHouseholdMember extends AsyncTask<Long, Void, LiveData<List<FoodRecord>>>{
        private FoodRecordDao mDao;
        GetObservableAllFoodRecordsForHouseholdMember(FoodRecordDao dao){
            this.mDao = dao;
        }

        @Override
        protected LiveData<List<FoodRecord>> doInBackground(Long... uids) {
            return mDao.getObservableAllFoodRecordsForHouseholdMember(uids[0]);
        }
    }

    private static class GetAllFoodRecords extends AsyncTask<Void, Void, List<FoodRecord>> {

        private FoodRecordDao mDao;
        GetAllFoodRecords(FoodRecordDao dao){
            this.mDao = dao;
        }

        @Override
        protected List<FoodRecord> doInBackground(Void... voids) {
            return mDao.getAllFoodRecords();
        }
    }

    private static class GetFoodRecordAsync extends AsyncTask<Long, Void, FoodRecord> {

        private FoodRecordDao mDao;
        GetFoodRecordAsync(FoodRecordDao dao){
            this.mDao = dao;
        }

        @Override
        protected FoodRecord doInBackground(Long... frIds) {
            List<FoodRecord> foodRecords = mDao.getFoodRecord(frIds[0]);

            if(foodRecords != null && foodRecords.size() > 0){
                return foodRecords.get(0);
            }
            return null;
        }
    }

    private static class GetFoodRecordForHmAsync extends AsyncTask<Long, Void, List<FoodRecord>> {

        private FoodRecordDao mDao;
        GetFoodRecordForHmAsync(FoodRecordDao dao){
            this.mDao = dao;
        }

        @Override
        protected List<FoodRecord> doInBackground(Long... hmIds) {
            return mDao.getAllFoodRecordsForHouseholdMember(hmIds[0]);
        }
    }
    private static class AddFoodRecordAsync extends AsyncTask<FoodRecord, Void, Long[]> {

        private FoodRecordDao mDao;
        AddFoodRecordAsync(FoodRecordDao dao){
            this.mDao = dao;
        }

        @Override
        protected Long[] doInBackground(FoodRecord... foodRecords) {
            return mDao.insert(foodRecords);
        }
    }

    private static class UpdateFoodRecord extends AsyncTask<FoodRecord, Void, Void> {

        private FoodRecordDao mDao;
        UpdateFoodRecord(FoodRecordDao dao){
            this.mDao = dao;
        }

        @Override
        protected Void doInBackground(FoodRecord... foodRecords) {
            mDao.update(foodRecords);
            return null;
        }
    }

}
