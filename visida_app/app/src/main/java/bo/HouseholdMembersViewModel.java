package bo;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.Objects;

import bo.db.entity.HouseholdMember;

/**
 * Created by jnc985 on 29-Nov-17.
 */

public class HouseholdMembersViewModel extends AndroidViewModel {
    private HouseholdMemberRepository householdMemberRepo;

    private LiveData<List<HouseholdMember>> householdMembers;

    //https://android.jlelse.eu/android-architecture-components-now-with-100-more-mvvm-11629a630125
    public HouseholdMembersViewModel(Application application) {
        super(application);
        //ViewModel instantiates with activity only instantiate once.
        if(householdMemberRepo == null){
            householdMemberRepo = new HouseholdMemberRepository(Objects.requireNonNull(application));
        }
        loadHouseholdMembers();
    }

    //Methods to access data (add HM, Delete HM etc)
    private void loadHouseholdMembers() {
        //Get the repostiory and get the items from it
        this.householdMembers = householdMemberRepo.getHouseholdMembers();
    }


    public LiveData<List<HouseholdMember>> getObservableHouseholdMembers() {
        return householdMembers;
    }

    public int size(){
        if(householdMembers == null || householdMembers.getValue() == null){
            return -1;
        }
        else{
            return householdMembers.getValue().size();
        }
    }

    public void addHouseholdMember(HouseholdMember hm) {
        householdMemberRepo.addHouseholdMember(hm);
    }

    public void addHouseholdMember(List<HouseholdMember> hms) {
        for(HouseholdMember hm : hms) {
            householdMemberRepo.addHouseholdMember(hm);
        }
    }

    public void deleteHouseholdMember(HouseholdMember hm) {
        householdMemberRepo.deleteHouseholdMember(hm);
    }

    /**
     * Need an unobservable version of the household member list so we can check the list
     * Observable lists don't populate until something observes which doesn't help if we
     * need to pre check the list ie if there is only a single household member.
     * @return
     */
    public List<HouseholdMember> getHouseholdMembers() {
        return householdMemberRepo.getHouseholdMemberList();
    }
}
