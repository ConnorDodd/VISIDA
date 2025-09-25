package bo.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import bo.db.entity.EatingOccasionNotification;
import bo.db.entity.ReviewNotification;

import static androidx.room.OnConflictStrategy.IGNORE;

/**
 * Created by jnc985 on 30-Apr-18.
 */

@Dao
public interface NotificationDoa {
    @Query("SELECT * from ReviewNotification where seen is 0")
    List<ReviewNotification> getAllUnseenReview();

    @Query("SELECT * FROM ReviewNotification")
    List<ReviewNotification> getReviewNotifications();

    //Only allow review notifications if unseen or were issued on the same day.
    @Query("SELECT * FROM ReviewNotification WHERE seen IS 0 " +
            "OR datetime(deliveryDate, 'start of day') IS datetime('now', 'localtime', 'start of day') " +
            "GROUP BY foodRecordId")
    LiveData<List<ReviewNotification>> getObservableRelevantReviewNotifications(); //+ "GROUP BY ppid"

    @Query("SELECT * FROM EatingOccasionNotification WHERE seen IS 0 " +
            "AND datetime(deliveryDate) < datetime('now', 'localtime')")
    LiveData<List<EatingOccasionNotification>> getObservableRelevantEatingNotifications();

    @Insert(onConflict = IGNORE)
    Long[] insertReview(ReviewNotification... notifications);

    @Insert(onConflict = IGNORE)
    Long[] insertEatingOccasion(EatingOccasionNotification... notifications);

    @Delete
    void deleteNotification(ReviewNotification... notifications);

    @Query("DELETE FROM EatingOccasionNotification where eatingOccasionId IS :eoId")
    void deleteEatingOccasionNotification(Long eoId);

    @Update
    void updateReview(ReviewNotification notification);
    @Update
    void updateEatingOccasionNotification(EatingOccasionNotification eon);

    @Query("SELECT * FROM ReviewNotification where reviewNotificationId IS :id")
    ReviewNotification getReviewNotification(Long id);

    @Query("SELECT * FROM ReviewNotification where " +
            "datetime(deliveryDate, 'start of day') IS datetime('now', 'localtime', 'start of day') " +
            "AND ppid IS :ppId")
    ReviewNotification getReviewNotificationByTodayAndPpId(String ppId);

    @Query("SELECT * FROM ReviewNotification where " +
            "datetime(deliveryDate, 'start of day') IS datetime('now', 'localtime', 'start of day') " +
            "AND ppid IS :id")
    List<ReviewNotification> getAllTodaysNotificationsForPpid(String id);

    @Query("SELECT * FROM ReviewNotification where " +
            "ppid IS :id AND seen IS 0")
    List<ReviewNotification> getAllUnseenNotificationsForPpid(String id);

    @Query("SELECT * FROM EatingOccasionNotification WHERE eatingOccasionId IS :id")
    List<EatingOccasionNotification> getEatingOccasionNotifications(Long id);

    @Query("SELECT * FROM EatingOccasionNotification")
    List<EatingOccasionNotification> getAllEatingOccasionNotifications();

    @Query("DELETE FROM ReviewNotification")
    int deleteAll();
}
