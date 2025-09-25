package bo;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;

import java.io.File;

import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;

public class FinalizeFoodItemViewModel extends AndroidViewModel {


    public FinalizeFoodItemViewModel(Application application){
        super(application);

        //Set up Repo
    }

    public File getHouseholdMemberImage(FoodItem fi){
        //Get the eating occasiom th efood item
        long eoId = fi.getEatingOccasionId();
        EatingOccasion eo = new EatingOccasionRepository(getApplication()).getEatingOccasion(eoId);
        //Get the FoodRecord
        long frId = eo.getFoodRecordId();
        FoodRecord fr = new FoodRecordRepository(getApplication()).getFoodRecord(frId);
        //Get the houshold members image
        return new HouseholdMemberRepository(getApplication()).getHouseholdMemberImage(fr.getHouseholdMemberId());

    }
}
