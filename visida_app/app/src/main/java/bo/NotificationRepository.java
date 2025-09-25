package bo;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import android.os.AsyncTask;
import androidx.annotation.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import bo.db.AppDatabase;
import bo.db.dao.EatingOccasionDao;
import bo.db.dao.NotificationDoa;
import bo.db.entity.Deliverable;
import bo.db.entity.EatingOccasion;
import bo.db.entity.EatingOccasionNotification;
import bo.db.entity.ReviewNotification;

/**
 * Created by jnc985 on 30-Apr-18.
 * Repository Class for {@link ReviewNotification} objects.
 */

public class NotificationRepository {
    private NotificationDoa mNotificationDao;
    private EatingOccasionDao mEatingOccasionDao;

    public NotificationRepository(Application application) {
        this.mNotificationDao = AppDatabase.getInstance(application).getNotificationDao();
        this.mEatingOccasionDao = AppDatabase.getInstance(application).getEatingOccasionDao();
    }

    public void add(ReviewNotification n) {
        new InsertReviewNotificationAsync(mNotificationDao).execute(n);
    }

    public void add(EatingOccasionNotification eoNotification) {
        new InsertEatingOccasionNotificationAsync(mNotificationDao).execute(eoNotification);
    }

    /**
     * Since we can have two forms of Review Notification we can identify them
     * uniquely by the
     * @param ppId
     * @return
     */
    public List<ReviewNotification> getNotificationsForTodayAndPpId(String ppId) {
        List<ReviewNotification> todaysRns = null;
        try {
            todaysRns = new GetAllTodaysReviewNotificationsForPpId(mNotificationDao).execute(ppId).get();
            return todaysRns;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public MediatorLiveData<List<? extends Deliverable>> getAllObservableRelevantNotifications() {
        try {
            //Get the review notifications
            LiveData<List<ReviewNotification>> reviewNotifications = new GetObservableReviewNotificationAsync(mNotificationDao).execute().get();

            //Get the Eating Occasion notifications
            LiveData<List<EatingOccasionNotification>> eatingOccasionNotifications = new GetObservableEatingOccasionNotificationAsync(mNotificationDao).execute().get();

            //Merge them with MediatorLiveData

            MediatorLiveData<List<? extends Deliverable>> liveDataMerger = new MediatorLiveData<>();
            liveDataMerger.addSource(reviewNotifications, value -> {
                liveDataMerger.setValue(combineLiveDatas(reviewNotifications, eatingOccasionNotifications));
            });
            liveDataMerger.addSource(eatingOccasionNotifications, value -> {
                liveDataMerger.setValue(combineLiveDatas(reviewNotifications, eatingOccasionNotifications));
            });
            return liveDataMerger;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;

    }

    private List<? extends Deliverable> combineLiveDatas(LiveData<List<ReviewNotification>> reviewNotifications, LiveData<List<EatingOccasionNotification>> eatingOccasionNotifications) {
        List<ReviewNotification> rns = reviewNotifications.getValue() == null ?  new ArrayList<>() : reviewNotifications.getValue();
        List<EatingOccasionNotification> eons = eatingOccasionNotifications.getValue() == null ?  new ArrayList<>() : eatingOccasionNotifications.getValue();

        List<Deliverable> merged = new ArrayList<>(rns);
        merged.addAll(eons);
        return merged;
    }

    public ReviewNotification getTodaysDefaultForPpid(String ppId) {
        ReviewNotification rn = null;
        try {
            rn = new GetReviewNotificationByTodayAndPpId(mNotificationDao).execute(ppId).get();
            if(rn != null) {
                return rn;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void update(ReviewNotification rn) {
        new UpdateReviewNotificationAsync(mNotificationDao).execute(rn);
    }
    private void update(EatingOccasionNotification eo) {
        new UpdateEatingOccasionNotificationAsync(mNotificationDao).execute(eo);
    }


    public List<ReviewNotification> getNotificationsForPpId(String ppId) {
        List<ReviewNotification> todaysRns = null;
        try {
            todaysRns = new GetAllUnseenReviewNotificationsForPpId(mNotificationDao).execute(ppId).get();
            return todaysRns;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void markSeen(EatingOccasion mEo) {
        //Get the notifications with the same eating occasion id
        try {
            List<EatingOccasionNotification> eons = new GetEatingOccasionNotificationsAsync(mNotificationDao).execute(mEo.getEatingOccasionId()).get();
            for(EatingOccasionNotification eo : eons){
                eo.setSeen(true);
                update(eo);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setFoodRecordsEatingOccasionRemindersToSeen(long frId) {
        //Get all the eating occasions for this FR
        List<EatingOccasion> eos = null;
        try {
            eos = new GetEatingOccasionForFoodRecordAsync(mEatingOccasionDao).execute(frId).get();
            //For each eating occasion, find the EatingOccasionNotification with same id
            for(EatingOccasion eo : eos){
                //Set to seen
                markSeen(eo);
            }
        } catch (InterruptedException | ExecutionException  e) {
            e.printStackTrace();
        }
    }

    public void removeEatingOccasionNotification(long eoId) {
        new DeleteEatingOccasionNotification(mNotificationDao).execute(eoId);
    }

    private static class DeleteEatingOccasionNotification extends AsyncTask<Long, Void, Void>{

        private NotificationDoa mDao;
        DeleteEatingOccasionNotification(NotificationDoa eoDao){
            this.mDao = eoDao;
        }
        @Override
        protected Void doInBackground(Long... longs) {
            mDao.deleteEatingOccasionNotification(longs[0]);
            return null;
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


    private static class GetEatingOccasionNotificationsAsync extends AsyncTask<Long, Void, List<EatingOccasionNotification>>{
        private NotificationDoa mDoa;

        GetEatingOccasionNotificationsAsync(NotificationDoa doa) {
            this.mDoa = doa;
        }

        @Override
        protected List<EatingOccasionNotification> doInBackground(Long... ids) {
            return mDoa.getEatingOccasionNotifications(ids[0]);
        }
    }

    private static class GetAllTodaysReviewNotificationsForPpId extends AsyncTask<String, Void, List<ReviewNotification>>{
        private NotificationDoa mDoa;

        GetAllTodaysReviewNotificationsForPpId(NotificationDoa doa) {
            this.mDoa = doa;
        }

        @Override
        protected List<ReviewNotification> doInBackground(String... ids) {
            return mDoa.getAllTodaysNotificationsForPpid(ids[0]);
        }
    }
    private static class GetAllUnseenReviewNotificationsForPpId extends AsyncTask<String, Void, List<ReviewNotification>>{
        private NotificationDoa mDoa;

        GetAllUnseenReviewNotificationsForPpId(NotificationDoa doa) {
            this.mDoa = doa;
        }

        @Override
        protected List<ReviewNotification> doInBackground(String... ids) {
            return mDoa.getAllUnseenNotificationsForPpid(ids[0]);
        }
    }
    private static class GetObservableReviewNotificationAsync extends AsyncTask<Void, Void, LiveData<List<ReviewNotification>>>{
        private NotificationDoa mDoa;

        GetObservableReviewNotificationAsync(NotificationDoa doa) {
            this.mDoa = doa;
        }

        @Override
        protected LiveData<List<ReviewNotification>> doInBackground(Void... voids) {
            LiveData<List<ReviewNotification>> reviewNotifications = mDoa.getObservableRelevantReviewNotifications();
            return reviewNotifications;
        }
    }
    private static class GetObservableEatingOccasionNotificationAsync extends AsyncTask<Void, Void, LiveData<List<EatingOccasionNotification>>>{
        private NotificationDoa mDoa;

        GetObservableEatingOccasionNotificationAsync(NotificationDoa doa) {
            this.mDoa = doa;
        }

        @Override
        protected LiveData<List<EatingOccasionNotification>> doInBackground(Void... voids) {
            LiveData<List<EatingOccasionNotification>> eatingNotifications = mDoa.getObservableRelevantEatingNotifications();
            return eatingNotifications;
        }
    }

    private static class GetReviewNotificationByTodayAndPpId extends AsyncTask<String, Void, ReviewNotification>{
        private NotificationDoa mDoa;

        GetReviewNotificationByTodayAndPpId(NotificationDoa doa) {
            this.mDoa = doa;
        }

        @Override
        protected ReviewNotification doInBackground(String... ids) {
            return mDoa.getReviewNotificationByTodayAndPpId(ids[0]);
        }
    }

    private static class UpdateReviewNotificationAsync extends AsyncTask<ReviewNotification, Void, Void>{
        private NotificationDoa mDoa;

        UpdateReviewNotificationAsync(NotificationDoa doa) {
            this.mDoa = doa;
        }

        @Override
        protected Void doInBackground(ReviewNotification... rns) {
            mDoa.updateReview(rns[0]);
            return null;
        }
    }
    private static class UpdateEatingOccasionNotificationAsync extends AsyncTask<EatingOccasionNotification, Void, Void>{
        private NotificationDoa mDoa;

        UpdateEatingOccasionNotificationAsync(NotificationDoa doa) {
            this.mDoa = doa;
        }

        @Override
        protected Void doInBackground(EatingOccasionNotification... eons) {
            mDoa.updateEatingOccasionNotification(eons[0]);
            return null;
        }
    }

    private static class InsertReviewNotificationAsync extends AsyncTask<ReviewNotification, Void, Long>{
        private NotificationDoa mDoa;
        InsertReviewNotificationAsync(NotificationDoa doa) {
            this.mDoa = doa;
        }

        @Override
        protected Long doInBackground(ReviewNotification... notifications) {
            return mDoa.insertReview(notifications)[0];
        }
    }

    private static class InsertEatingOccasionNotificationAsync extends AsyncTask<EatingOccasionNotification, Void, Long>{
        private NotificationDoa mDoa;
        InsertEatingOccasionNotificationAsync(NotificationDoa doa) {
            this.mDoa = doa;
        }

        @Override
        protected Long doInBackground(EatingOccasionNotification... notifications) {
            return mDoa.insertEatingOccasion(notifications)[0];
        }
    }

    @VisibleForTesting
    public List<ReviewNotification> getReviewNotifications() {
        try {
            return new GetReviewNotificationsAsync(mNotificationDao).execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static class GetReviewNotificationsAsync extends AsyncTask<Void, Void, List<ReviewNotification>>{
        private NotificationDoa mDoa;

        GetReviewNotificationsAsync(NotificationDoa doa) {
            this.mDoa = doa;
        }

        @Override
        protected List<ReviewNotification> doInBackground(Void... ids) {
            return mDoa.getReviewNotifications();
        }
    }

    @VisibleForTesting
    public List<EatingOccasionNotification> getAllEatingOccasionNotifications() {
        try {
            return new GetAllEatingOccasionNotificationsAsync(mNotificationDao).execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static class GetAllEatingOccasionNotificationsAsync extends AsyncTask<Void, Void, List<EatingOccasionNotification>>{
        private NotificationDoa mDoa;

        GetAllEatingOccasionNotificationsAsync(NotificationDoa doa) {
            this.mDoa = doa;
        }

        @Override
        protected List<EatingOccasionNotification> doInBackground(Void... ids) {
            return mDoa.getAllEatingOccasionNotifications();
        }
    }

    public void deleteAll() {
        new DeleteAllReviewNotifications(mNotificationDao).execute();
    }
    private static class DeleteAllReviewNotifications extends AsyncTask<Void, Void, Void>{
        private NotificationDoa mDoa;

        DeleteAllReviewNotifications(NotificationDoa doa) {
            this.mDoa = doa;
        }

        @Override
        protected Void doInBackground(Void... ids) {
            mDoa.deleteAll();
            return null;
        }
    }
}
