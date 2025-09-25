package bo.db.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import android.content.Context;
import android.content.Intent;

import java.util.Date;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.activity.RecordReviewActivity;
import bo.typeconverter.TimestampConverter;

import static bo.AppConstants.NOTIFICATION_ID;
import static bo.AppConstants.PPID;

/**
 * Created by jnc985 on 30-Apr-18.
 */

@Entity(indices = @Index(value = "reviewNotificationId", name = "notificationId"))
public class ReviewNotification implements Deliverable{

    @PrimaryKey(autoGenerate = true)
    private long reviewNotificationId;
    private boolean seen;
    private String ppid;
    @TypeConverters(TimestampConverter.class)
    private Date issueDate;
    @TypeConverters(TimestampConverter.class)
    private Date deliveryDate;
    private long foodRecordId;
    private int notificationId;

    public ReviewNotification(){
        issueDate = new Date();
        seen = false;
    }

    public long getReviewNotificationId() {
        return reviewNotificationId;
    }

    public void setReviewNotificationId(long reviewNotificationId) {
        this.reviewNotificationId = reviewNotificationId;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @Override
    public Intent getIntent(Context packageContext) {
        Intent itemIntent = new Intent(packageContext, RecordReviewActivity.class);
        itemIntent.putExtra(PPID, getId());
        itemIntent.putExtra(NOTIFICATION_ID, getNotificationId());
        return itemIntent;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public String getPpid() {
        return ppid;
    }

    public void setPpid(String ppid) {
        this.ppid = ppid;
    }

    public long getFoodRecordId() {
        return foodRecordId;
    }

    public void setFoodRecordId(long foodRecordId) {
        this.foodRecordId = foodRecordId;
    }

    public int getNotificationId() {
        return notificationId;
    }

    @Override
    public String getId() {
        return getPpid();
    }

    @Override
    public String getMessage(Context context) {
        String msg = context.getResources().getString(R.string.review_your_day);
        msg += " " + getPpid();
        return msg;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
}
