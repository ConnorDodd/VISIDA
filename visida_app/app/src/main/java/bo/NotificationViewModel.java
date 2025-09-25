package bo;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.Objects;

import bo.db.entity.Deliverable;
import bo.db.entity.ReviewNotification;

/**
 * Created by jnc985 on 29-Nov-17.
 * View Model class for Notifications. Provides an observable list of review notifications
 */

public class NotificationViewModel extends AndroidViewModel {
    private NotificationRepository mNotificationRepo;
    private LiveData<List<? extends Deliverable>> mNotifications;

    //https://android.jlelse.eu/android-architecture-components-now-with-100-more-mvvm-11629a630125
    public NotificationViewModel(Application application) {
        super(application);
        //ViewModel instantiates with activity only instantiate once.
        if(mNotificationRepo == null){
            mNotificationRepo = new NotificationRepository(Objects.requireNonNull(application));
        }
        loadNotifications();
    }

    private void loadNotifications() {
        //Get the repostiory and get the items from it
        this.mNotifications = mNotificationRepo.getAllObservableRelevantNotifications();
    }

     /**
     * Returns the Observable list of ingredients.
     * @return LiveData of {@link ReviewNotification}
     */
    public LiveData<List<? extends Deliverable>> getObservableNotifications() {
        return mNotifications;
    }

    public int size(){
        if(mNotifications == null || mNotifications.getValue() == null){
            return -1;
        }
        else{
            return mNotifications.getValue().size();
        }
    }

    //TODO is it better to query the database here?
    public int countUnseen() {
        int i =0;
        List<? extends Deliverable> notifications = mNotifications.getValue();
        if(notifications == null){
            return 0;
        }
        for(Deliverable rn : notifications){
            if(!rn.isSeen() && rn.getId() != null && !rn.getId().isEmpty()){
                i++;
            }
        }
        return i;
    }
}
