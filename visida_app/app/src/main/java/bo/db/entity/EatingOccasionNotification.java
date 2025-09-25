package bo.db.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import android.content.Context;
import android.content.Intent;

import java.util.Date;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.activity.SelectEatingOccasionActivity;
import bo.typeconverter.TimestampConverter;

import static bo.AppConstants.UNFINALIZEDEOIDS;

/**
 * Created by jnc985 on 30-Apr-18.
 */

@Entity(indices = @Index(value = "eatingOccasionId", name = "eonotificationId"))
public class EatingOccasionNotification implements Deliverable{

    @PrimaryKey
    private long eatingOccasionId;
    private boolean seen;
    private int notificationId;
    private String ppid;
    @TypeConverters(TimestampConverter.class)
    private Date issueDate;
    @TypeConverters(TimestampConverter.class)
    private Date deliveryDate;

    public EatingOccasionNotification(){
        this.issueDate = new Date();
        seen = false;
    }

    public long getEatingOccasionId() {
        return eatingOccasionId;
    }

    public void setEatingOccasionId(long eatingOccasionId) {
        this.eatingOccasionId = eatingOccasionId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    @Override
    public int getNotificationId() {
        return this.notificationId;
    }

    @Override
    public String getId() {
        return ppid;
    }

    @Override
    public String getMessage(Context context) {
        return context.getString(R.string.title_notification_finalize_eating_occasion) + ": " + getId();
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @Override
    public Intent getIntent(Context packageContext) {
        Intent notficationIntent = new Intent(packageContext, SelectEatingOccasionActivity.class);
        notficationIntent.putExtra(UNFINALIZEDEOIDS, new Long[]{getEatingOccasionId()});
        return notficationIntent;
    }

    public String getPpid() {
        return ppid;
    }

    public void setPpid(String ppid) {
        this.ppid = ppid;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
}
