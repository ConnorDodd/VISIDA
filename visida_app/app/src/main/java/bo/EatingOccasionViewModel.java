package bo;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.ReviewNotification;
import notification.AlarmController;

/**
 * Created by jnc985 on 29-Nov-17.
 */

public class EatingOccasionViewModel extends AndroidViewModel {

    //File name format: recipeId_ingredientId_timestamp.<format>

    private FoodItemRepository mFoodItemRepo;
    private EatingOccasionRepository mEatingOccasionRepo;
    private AlarmController mAlarmController;
    private EatingOccasion mEo;
    private LiveData<List<FoodItem>> mFoodItems;

    //https://android.jlelse.eu/android-architecture-components-now-with-100-more-mvvm-11629a630125
    public EatingOccasionViewModel(Application application) {
        super(application);
        //ViewModel instantiates with activity only instantiate once.
        if(mFoodItemRepo == null){
            mFoodItemRepo = new FoodItemRepository(Objects.requireNonNull(application));
        }
        if(mEatingOccasionRepo == null){
            mEatingOccasionRepo = new EatingOccasionRepository(application);
        }
        if (mAlarmController == null) {
            mAlarmController = new AlarmController(application);
        }
    }

    //Loads the Food Items for the EatingOccasion(initially this should be an empty list).
    private void loadFoodItems(long eoId) throws ExecutionException, InterruptedException {
        //Get the repostiory and get the items from it
        this.mFoodItems = mFoodItemRepo.getAllObservableFoodItemsForEatingOccasion(eoId);
    }

    public void addFoodItem(String ppid, FoodItem fi) {
        mFoodItemRepo.addFoodItem(fi);
        postNotification(ppid, mEo);
    }

    public void removefoodItem(FoodItem fi) {
        mFoodItemRepo.deleteFoodItem(getApplication().getApplicationContext(), fi);
        //If we just deleted the last food Item we need to cancel the notification
        try {
            //Have to quesry the database again since the live data will not have been notified of
            //the change yet.
            if (mFoodItemRepo.getAllFoodItemsForEatingOccasion(mEo.getEatingOccasionId()).isEmpty()){
                cancelNotifications();
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the Observable list of ingredients.
     * @return
     */
    public LiveData<List<FoodItem>> getObservableFoodItems() {
        return mFoodItems;
    }

    public int size(){
        if(mFoodItems == null || mFoodItems.getValue() == null){
            return -1;
        }
        else{
            return mFoodItems.getValue().size();
        }
    }

    public void setEatingOccasion(EatingOccasion eo) {
        this.mEo = eo;
        try {
            loadFoodItems(mEo.getEatingOccasionId());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void postNotification(String ppid, EatingOccasion eo) {
        mAlarmController.scheduleUnfinalizedEatingOccasionNotification(ppid, eo);
        //Find todays default notification for this ppid
        NotificationRepository nRepo = new NotificationRepository(getApplication());
        ReviewNotification todaysDefault = nRepo.getTodaysDefaultForPpid(ppid);
        //Set the default to seen so it dissapears from the bell list
        if(todaysDefault != null) {
            todaysDefault.setSeen(true);
            //Being seen will remain in the menu for 24 hours. Change delivery date to epoch time
            todaysDefault.setDeliveryDate(new Date(0));
            nRepo.update(todaysDefault);
            mAlarmController.cancelRecordReviewNotification(todaysDefault.getNotificationId());
        }
        mAlarmController.scheduleRecordReviewNotification(ppid, eo.getFoodRecordId());
    }

    /**
     * If Eating occasion is now empty for food items we need to cancel the finalise
     * notifications.
     */
    private void cancelNotifications(){
        mAlarmController.cancelUnfinalizedEatingOccasionNotification(mEo.getEatingOccasionId());
        //Check each Eating Occasion in the FoodRecord. If no EO's contain FI's cancel the notification.
//        for(EatingOccasion eo : mEatingOccasionRepo.getEatingOccasions(mEo.getFoodRecordId())){
//            if(!eo.getFoodItems().isEmpty()){
//                //Not empty so keep the notification
//                return;
//            }
//        }
//        //Cancel something here??
    }


    public void addEatingOccasion(EatingOccasion currentEo) {
        currentEo.setEatingOccasionId(mEatingOccasionRepo.addEatingOccasion(currentEo));
    }

    public EatingOccasion getEatingOccasion() {
        return mEo;
    }

    public FoodItem addNewFoodItem() {
        //create food item
        FoodItem newFi = new FoodItem();
        //Set food items eating occasion id
        newFi.setEatingOccasionId(mEo.getEatingOccasionId());
        //add food item to database updating fiId
        newFi.setFoodItemId(mFoodItemRepo.addNewFoodItem(newFi));
        //return food item
        return newFi;
    }

    public void clearEmpty() {
        //Get list of Eating occasoin which contain no food items
        List<EatingOccasion> emptyEos = mEatingOccasionRepo.getEmptyEatingOccasions();
        if(emptyEos != null){
            mEatingOccasionRepo.delete(emptyEos.toArray(new EatingOccasion[emptyEos.size()]));
        }
        deleteImages();
    }

    private void deleteImages() {
        //On a separate thread, loop through all the images, if it contains the placeholder delete.
        new Thread(){
            public void run(){
                File mediaDirectory = Utilities.getMediaDirectory(getApplication().getApplicationContext());
                File[] files = mediaDirectory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if(file.getName().contains(AppConstants.FIID_PLACEHOLDER)){
                            file.delete();
                        }
                    }
                }
            }
        }.start();
    }

    public File getHouseholdMemberImage(){
        //Get the FoodRecord
        long frId = mEo.getFoodRecordId();
        FoodRecord fr = new FoodRecordRepository(getApplication()).getFoodRecord(frId);
        //Get the houshold members image
        return new HouseholdMemberRepository(getApplication()).getHouseholdMemberImage(fr.getHouseholdMemberId());
    }

    public List<EatingOccasion> getEatingOccasions(Long[] eoIds) {
        return mEatingOccasionRepo.getEatingOccasions(eoIds);
    }

    public void addRecipes(List<Long> recipeIds) {
        //Add recipes to the eating occasion
        this.mEo.setRecipeIds(recipeIds);
        //Update the database
        this.mEatingOccasionRepo.updateEatingOccasion(mEo);
    }
}
