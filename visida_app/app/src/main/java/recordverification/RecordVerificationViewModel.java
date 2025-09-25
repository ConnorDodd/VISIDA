package recordverification;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import bo.HouseholdMemberRepository;
import bo.db.entity.HouseholdMember;

public class RecordVerificationViewModel extends AndroidViewModel {

    public RecordVerificationViewModel(Application application){
        super(application);
    }

    public List<HouseholdMember> getHouseholdMembers(){
        //Get all the household members
        List<HouseholdMember> hms = new HouseholdMemberRepository(getApplication()).getHouseholdMemberList();
        return hms;
    }

}
