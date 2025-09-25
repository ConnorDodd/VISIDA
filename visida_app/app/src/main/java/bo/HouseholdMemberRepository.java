package bo;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import android.os.AsyncTask;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import bo.db.AppDatabase;
import bo.db.dao.HouseholdMemberDao;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;

/**
 * Created by jnc985 on 29-Nov-17.
 * Repository for holding Household member data. This class holds ALL of the household member together.
 */

public class HouseholdMemberRepository {

    private HouseholdMemberDao mHmDao;
    private Application app;

    private static MediatorLiveData<List<HouseholdMember>> mObservableHouseholdMembers;

    public HouseholdMemberRepository(Application application) {
        this.app = application;
        this.mHmDao = AppDatabase.getInstance(application).getHouseholdMemberDao();
        mObservableHouseholdMembers = new MediatorLiveData<>();
        LiveData<List<HouseholdMember>> hms = mHmDao.getAll();

        mObservableHouseholdMembers.addSource(hms,
                new Observer<List<HouseholdMember>>() {
                    @Override
                    public void onChanged(@Nullable List<HouseholdMember> householdMembers) {
                        mObservableHouseholdMembers.postValue(householdMembers);
                    }
                });
    }

    public File getHouseholdMemberImage(long hmId){
        try{
            String path = new GetHouseholdMemberImagePathAsync(this.mHmDao).execute(hmId).get();
            return new File(path);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public LiveData<List<HouseholdMember>> getHouseholdMembers(){
        return mObservableHouseholdMembers;
    }

    /**
     * Gets the Underlying list of Household members. This is the unobservable
     * version of the list of household members.
     * @return
     */
    public List<HouseholdMember> getHouseholdMemberList(){
        try {
            List<HouseholdMember> hms = new GetHouseholdMembersAsync(mHmDao).execute().get();
            //Populate the Food Records
            for(HouseholdMember hm : hms){
                FoodRecordRepository frRepo = new FoodRecordRepository(app);
                List<FoodRecord> frs = frRepo.getFoodRecordsForHouseholdMember(hm.getUid());
                hm.setFoodRecords(frs);
            }
            return hms;
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Adds a single household member to the database.
     * @param hm Household member object to add.
     */
    public void addHouseholdMember(HouseholdMember hm) {
        Long[] insertedId = new Long[0];
        try {
            insertedId = new AddHouseholdMemberAsync(mHmDao).execute(hm).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        hm.setUid(insertedId[0]);
    }

    /**
     * Gets a single Household Member with the given household member Id
     * @param householdMemberId Id of the requested household member.
     * @return
     */
    public HouseholdMember getHouseholdMember(long householdMemberId) {
        try {
            HouseholdMember hm = new GetHouseholdMemberAsync(mHmDao).execute(householdMemberId).get();
            return hm;
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Boolean participantIdExists(String ppId){
        //Return if the given participant id exists in the database
        try{
            return new CheckParticpantIdAsync(mHmDao).execute(ppId).get();
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public HouseholdMember getHouseholdMember(String ppid) {
        try {
            return new GetHouseholdMemberFromPpIdAsync(mHmDao).execute(ppid).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean hasChild() {
        try {
            return new GetIfChildExists(mHmDao).execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasBrestfedMember() {
        try {
            return new GetIfHouseholdHasBreastfedMember(mHmDao).execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return true;
    }
    private static class GetIfHouseholdHasBreastfedMember extends AsyncTask<Void, Void, Boolean>{
        private HouseholdMemberDao mDao;
        GetIfHouseholdHasBreastfedMember(HouseholdMemberDao dao){
            this.mDao = dao;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            //return true;
            return mDao.countBreastfed() > 0;
        }
    }

    private static class GetIfChildExists extends AsyncTask<Void, Void, Boolean>{
        private HouseholdMemberDao mDao;
        GetIfChildExists(HouseholdMemberDao dao){
            this.mDao = dao;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            HouseholdMember hm = mDao.childExists();
            return hm != null;
        }
    }

    private static class GetHouseholdMemberFromPpIdAsync extends AsyncTask<String, Void, HouseholdMember>{
        private HouseholdMemberDao mDao;
        GetHouseholdMemberFromPpIdAsync(HouseholdMemberDao dao){
            this.mDao = dao;
        }

        @Override
        protected HouseholdMember doInBackground(String... ppid) {
            return mDao.getHouseholdMember(ppid[0]);
        }
    }

    private static class GetHouseholdMemberImagePathAsync extends AsyncTask<Long, Void, String>{
        private HouseholdMemberDao mDao;
        GetHouseholdMemberImagePathAsync(HouseholdMemberDao dao){
            this.mDao = dao;
        }

        @Override
        protected String doInBackground(Long... longs) {
            return mDao.getHouseholdMemberImagePath(longs[0]);
        }
    }
    private static class CheckParticpantIdAsync extends AsyncTask<String, Void, Boolean>{

        private HouseholdMemberDao hmDao;
        CheckParticpantIdAsync(HouseholdMemberDao dao){
            this.hmDao = dao;
        }

        @Override
        protected Boolean doInBackground(String... ppid) {
            Integer result = hmDao.participantIdExists(ppid[0]);
            return result == 1;
        }
    }

    public void deleteHouseholdMember(HouseholdMember hm) {
        try{
            new DeleteHouseholdMembersAsync(mHmDao).execute(hm);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public List<HouseholdMember> getHouseholdMembers(List<Long> householdMembers) {
        try{
            return new GetMultipleHouseholdMembersAsync(mHmDao).execute(householdMembers).get();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return new ArrayList<>();
    }

    private static class GetMultipleHouseholdMembersAsync extends AsyncTask<List<Long>, Void, List<HouseholdMember>>{

        private HouseholdMemberDao hmDao;
        GetMultipleHouseholdMembersAsync(HouseholdMemberDao dao){
            this.hmDao = dao;
        }

        @Override
        protected List<HouseholdMember>doInBackground(List<Long>... hms) {
            return hmDao.getHouseholdMembers(hms[0]);
        }
    }

    private static class AddHouseholdMemberAsync extends AsyncTask<HouseholdMember, Void, Long[]>{

        private HouseholdMemberDao hmDao;
        AddHouseholdMemberAsync(HouseholdMemberDao dao){
            this.hmDao = dao;
        }

        @Override
        protected Long[] doInBackground(HouseholdMember... hms) {
            return hmDao.insert(hms);
        }
    }

    private static class GetHouseholdMemberAsync extends AsyncTask<Long, Void, HouseholdMember>{

        private HouseholdMemberDao hmDao;
        GetHouseholdMemberAsync(HouseholdMemberDao dao){
            this.hmDao = dao;
        }

        @Override
        protected HouseholdMember doInBackground(Long... longs) {
            return hmDao.getHouseholdMember(longs[0]);
        }
    }

    private static class GetHouseholdMembersAsync extends AsyncTask<Void, Void, List<HouseholdMember>>{

        private HouseholdMemberDao hmDao;
        GetHouseholdMembersAsync(HouseholdMemberDao dao){
            this.hmDao = dao;
        }

        @Override
        protected List<HouseholdMember> doInBackground(Void... longs) {
            return hmDao.getHouseholdMembers();
        }
    }

    private static class DeleteHouseholdMembersAsync extends AsyncTask<HouseholdMember, Void, Void>{

        private HouseholdMemberDao hmDao;
        DeleteHouseholdMembersAsync(HouseholdMemberDao dao){
            this.hmDao = dao;
        }

        @Override
        protected Void doInBackground(HouseholdMember... hms) {
            //Delete the image aswell
            for(HouseholdMember hm : hms) {
                //If avatar image exists, delete the image.
                if (hm.getAvatar() != null) {
                    File imageFile = new File(hm.getAvatar());
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                }
            }
            hmDao.delete(hms);
            return null;
        }
    }
}
