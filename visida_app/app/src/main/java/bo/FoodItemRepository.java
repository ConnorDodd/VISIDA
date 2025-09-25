package bo;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;

import bo.db.AppDatabase;
import bo.db.dao.FoodItemDao;
import bo.db.dao.GuestInfoDao;
import bo.db.entity.FoodItem;
import bo.db.entity.GuestInformation;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.FIID_PLACEHOLDER;

/**
 * Created by jnc985 on 29-Nov-17.
 * Repository for holding Household member data. This class holds ALL of the household member together.
 */

public class FoodItemRepository {

    private static final String TAG = "FoodItemRepository";

    private FoodItemDao mFiDao;
    private GuestInfoDao mguestInfoDao;
    public FoodItemRepository(Application application) {
        this.mFiDao = AppDatabase.getInstance(application).getFoodItemDao();
        this.mguestInfoDao = AppDatabase.getInstance(application).getGuestInfoDao();
    }

    /**
     * Adds a FoodItem to the Database and Updates the Containing EatingOccasion
     * and FoodRecord accordingly.
     * @param fi FoodItem to be added.
     */
    public void addFoodItem(FoodItem fi) {

        //Add th food Item and get its generated id
        Long generatedId = null;
        try {
            generatedId = new AddFoodItemAsync(mFiDao).execute(fi).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        fi.setFoodItemId(generatedId);

        //Update URL strings
        if(fi.getAudioUrls() != null) {
            fi.setAudioUrls(fi.getAudioUrls().replaceAll(FIID_PLACEHOLDER, generatedId.toString()));
        }
        if(fi.getImageUrl() != null) {
            fi.setImageUrl(fi.getImageUrl().replaceAll(FIID_PLACEHOLDER, generatedId.toString()));
        }
        else{
            fi.setImageUrl("");
        }

        //Check if we have to update the leftover names aswell
        if(fi.getLeftoverAudioUrls() != null && fi.getLeftoverImageUrl() != null){
            fi.setLeftoverAudioUrls(fi.getLeftoverAudioUrls().replaceAll(FIID_PLACEHOLDER, generatedId.toString()));
            fi.setLeftoverImageUrl(fi.getLeftoverImageUrl().replaceAll(FIID_PLACEHOLDER, generatedId.toString()));
        }

            //Update the database entry
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Food Item Updated Image " + fi.getImageName() + " Audio " + fi.getAudioUrls());
        new UpdateFoodItemAsync(mFiDao).execute(fi);
    }

    public Long addNewFoodItem(FoodItem fi){
        try {
            return new AddFoodItemAsync(mFiDao).execute(fi).get();
        }
        catch (InterruptedException | ExecutionException ex){
            ex.printStackTrace();
        }
        return null;

    }

    public List<FoodItem> getAllFoodItems() {
        try {
            return new GetFoodItemsAsync(mFiDao).execute().get();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public List<FoodItem> getAllFoodItemsForEatingOccasion(Long eoId) throws ExecutionException, InterruptedException{
        return new GetFoodItemsForEatingOccasionAsync(mFiDao).execute(eoId).get();
    }
    public LiveData<List<FoodItem>> getAllObservableFoodItemsForEatingOccasion(Long eoId) throws ExecutionException, InterruptedException{
        return new GetObservableFoodItemsForEatingOccasionAsync(mFiDao).execute(eoId).get();
    }

    public LiveData<List<FoodItem>> getObservableDishesForMeal(long mealId){
        try {
            return new GetObservableDishesForMealAsync(mFiDao).execute(mealId).get();
        }
        catch (InterruptedException | ExecutionException ex){
            ex.printStackTrace();
        }
        return null;
    }

    public void addMealLeftoverAudio(String imageName, String leftOverImageName, String leftoverAudioName) {
        new UpdateMealWithLeftoversAudio(mFiDao).execute(imageName, leftOverImageName, leftoverAudioName);
    }

    public void addMealLeftoverText(String imageName, String leftOverImageName, String descritpion) {
        new UpdateMealWithLeftoversText(mFiDao).execute(imageName, leftOverImageName, descritpion);
    }

    public void addGuestInfo(long baseFoodItemId, GuestInformation gi) {
        //add the geust info to all of the food items with the same base food item id
        try{
            new UpdateGuestInfoInFoodItems(mFiDao, baseFoodItemId).execute(gi.getGeustInfoId());
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public List<GuestInformation> getAllGuestInformation() {
        //add the geust info to all of the food items with the same base food item id
        try{
            return new GetAllGuestInformation(mguestInfoDao).execute().get();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public long addNewGuestInfo(GuestInformation gi) {
        try{
            return new InsertNewGuestInformation(mguestInfoDao).execute(gi).get();
        }
        catch(InterruptedException | ExecutionException ex){
            ex.printStackTrace();
        }
        return -1;
    }

    private static class GetAllGuestInformation extends AsyncTask<Void, Void, List<GuestInformation>> {

        private GuestInfoDao mDao;

        GetAllGuestInformation(GuestInfoDao dao){
            this.mDao = dao;
        }
        @Override
        protected List<GuestInformation> doInBackground(Void... voids) {
            return mDao.all();
        }
    }

    private static class UpdateGuestInfoInFoodItems extends AsyncTask<Long, Void, Long> {

        private FoodItemDao mDao;
        private long mBaseFoodItemId;

        UpdateGuestInfoInFoodItems(FoodItemDao dao, long baseFoodItemId){
            this.mDao = dao;
            this.mBaseFoodItemId = baseFoodItemId;
        }
        @Override
        protected Long doInBackground(Long... gis) {
            mDao.updateGuestInfo(mBaseFoodItemId, gis[0]);
            return null;
        }
    }

    private static class InsertNewGuestInformation extends AsyncTask<GuestInformation, Void, Long> {

        private GuestInfoDao mDao;
        InsertNewGuestInformation(GuestInfoDao dao){
            this.mDao = dao;
        }
        @Override
        protected Long doInBackground(GuestInformation... gis) {
            return mDao.insert(gis[0]);
        }
    }

    private static class UpdateMealWithLeftoversAudio extends AsyncTask<String, Void, Void> {

        private FoodItemDao mDao;
        UpdateMealWithLeftoversAudio(FoodItemDao dao){
            this.mDao = dao;
        }
        @Override
        protected Void doInBackground(String... images) {
            mDao.updateMealLeftoversAudio(images[0], images[1], images[2]);
            return null;
        }
    }

    private static class UpdateMealWithLeftoversText extends AsyncTask<String, Void, Void> {

        private FoodItemDao mDao;
        UpdateMealWithLeftoversText(FoodItemDao dao){
            this.mDao = dao;
        }
        @Override
        protected Void doInBackground(String... images) {
            mDao.updateMealLeftoversText(images[0], images[1], images[2]);
            return null;
        }
    }

    private static class GetObservableDishesForMealAsync extends AsyncTask<Long, Void, LiveData<List<FoodItem>>> {

        private FoodItemDao mDao;
        GetObservableDishesForMealAsync(FoodItemDao dao){
            this.mDao = dao;
        }
        @Override
        protected LiveData<List<FoodItem>> doInBackground(Long... mId) {
            return mDao.getObservableDishesForMeal(mId[0]);
        }
    }


    public void updateFoodItem(FoodItem fi) {
        new UpdateFoodItemAsync(mFiDao).execute(fi);
    }

    public void deleteFoodItem(Context c, FoodItem fi){
        //Rename the image file
        String newImageName = "DELETED_" + fi.getImageName();
        String newAudioName = "DELETED_" + fi.getAudioName();
        Utilities.renameMediaFile(c, fi.getImageName(), newImageName);
        Utilities.renameMediaFile(c, fi.getAudioName(), newAudioName);

        new DeleteFoodItemAsync(mFiDao).execute(fi);
    }

    public FoodItem getFoodItem(long foodItemId) {
        try {
            return new GetFoodSingleItemAsync(mFiDao).execute(foodItemId).get();
        }
        catch(InterruptedException | ExecutionException ex){
            ex.printStackTrace();
        }
        return null;
    }

    private static class AddFoodItemAsync extends AsyncTask<FoodItem, Void, Long> {

        private FoodItemDao mDao;
        AddFoodItemAsync(FoodItemDao dao){
            this.mDao = dao;
        }
        @Override
        protected Long doInBackground(FoodItem... fis) {
            return mDao.insert(fis[0]);
        }
    }

    private static class GetFoodSingleItemAsync extends AsyncTask<Long, Void, FoodItem> {

        private FoodItemDao mDao;
        GetFoodSingleItemAsync(FoodItemDao dao){
            this.mDao = dao;
        }

        @Override
        protected FoodItem doInBackground(Long... fiIds) {
            return mDao.getFoodItem(fiIds[0]).get(0);
        }
    }

    private static class GetFoodItemsAsync extends AsyncTask<Void, Void, List<FoodItem>> {

        private FoodItemDao mDao;
        GetFoodItemsAsync(FoodItemDao dao){
            this.mDao = dao;
        }

        @Override
        protected List<FoodItem> doInBackground(Void... voids) {
            return mDao.getAll();
        }
    }

    private static class GetObservableFoodItemsForEatingOccasionAsync extends AsyncTask<Long, Void, LiveData<List<FoodItem>>> {

        private FoodItemDao mDao;
        GetObservableFoodItemsForEatingOccasionAsync(FoodItemDao dao){
            this.mDao = dao;
        }

        @Override
        protected LiveData<List<FoodItem>> doInBackground(Long... eoIds) {
            return mDao.getAllObservableFoodItemsForEatingOccasion(eoIds[0]);
        }
    }

    private static class GetFoodItemsForEatingOccasionAsync extends AsyncTask<Long, Void, List<FoodItem>> {

        private FoodItemDao mDao;
        GetFoodItemsForEatingOccasionAsync(FoodItemDao dao){
            this.mDao = dao;
        }

        @Override
        protected List<FoodItem> doInBackground(Long... eoIds) {
            return mDao.getAllFoodItemsForEatingOccasion(eoIds[0]);
        }
    }

    private static class UpdateFoodItemAsync extends AsyncTask<FoodItem, Void, Integer>{

        private FoodItemDao mDao;
        UpdateFoodItemAsync(FoodItemDao dao){
            this.mDao = dao;
        }

        @Override
        protected Integer doInBackground(FoodItem... fis) {
            return mDao.update(fis[0]);
        }
    }

    private static class DeleteFoodItemAsync extends AsyncTask<FoodItem, Void, Void> {

        private FoodItemDao mDao;
        DeleteFoodItemAsync(FoodItemDao dao){
            this.mDao = dao;
        }

        @Override
        protected Void doInBackground(FoodItem... fis) {
            mDao.deleteFoodItem(fis[0]);
            return null;
        }
    }
}
