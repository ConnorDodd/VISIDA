package bo;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import android.os.AsyncTask;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import bo.db.AppDatabase;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;
import bo.typeconverter.DateTypeConverter;

/**
 * Created by Josh on 18-Dec-17.
 */

public class FoodRecordViewModel extends AndroidViewModel {
    private FoodRecordRepository foodRecordRepository;

    public FoodRecordViewModel(Application application){
        super(application);
        foodRecordRepository = new FoodRecordRepository(Objects.requireNonNull(application));
    }


    /**
     * Returns a single food record for the given household member for TODAY
     * @param hm
     * @return Food record for Today for the given household member. If there is no food record
     * stored for TODAY a new one is created with TODAY's date.
     */
    public FoodRecord getTodaysFoodRecordFor(HouseholdMember hm) {

        //Get todays date
        Date today = new Date();
        FoodRecord todaysFr = null;
        try {
            todaysFr = foodRecordRepository.getTodaysFoodRecordFor(hm.getUid(), today);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        //If null we need to make a new one
        if(todaysFr == null){
            //If no food record for today was found create a new one and  return it
            //Create a new Food Record
            FoodRecord fr = new FoodRecord(hm.getUid());
            fr.setHouseholdMember(hm);
            //Add it to the database
            Long frId = null;
            try {
                frId = foodRecordRepository.addNewFoodRecord(fr);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            //Update the food record with its new id
            fr.setFoodRecordId(frId);
            //Return the new food record
            return fr;
        }
        else{
            //Add the household member to the food record and return
            todaysFr.setHouseholdMember(hm);
            return todaysFr;
        }
    }


    private class GetTodaysFoodRecordsAsync extends AsyncTask<HouseholdMember, Void, List<FoodRecord>> {

        @Override
        protected List<FoodRecord> doInBackground(HouseholdMember... hms) {
            return AppDatabase.getInstance(FoodRecordViewModel.this.getApplication())
                    .getFoodRecordDao()
                    .getTodaysFoodRecordForHouseholdMember(hms[0].getUid(), DateTypeConverter.dateToTimestamp(new Date()));
        }
    }
}
