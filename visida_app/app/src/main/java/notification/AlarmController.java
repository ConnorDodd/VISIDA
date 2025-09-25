package notification;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.icu.util.Calendar;

import androidx.core.app.NotificationCompat;
import android.util.Log;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import au.edu.newcastle.jnc985.visida.BuildConfig;
import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.activity.CreateRecipeActivity;
import au.edu.newcastle.jnc985.visida.activity.RecordReviewActivity;
import au.edu.newcastle.jnc985.visida.activity.SelectEatingOccasionActivity;
import bo.AppConstants;
import bo.NotificationRepository;
import bo.Utilities;
import bo.db.entity.EatingOccasion;
import bo.db.entity.EatingOccasionNotification;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.DEFAULT_REVIEW_HOUR;
import static bo.AppConstants.DEFAULT_REVIEW_MINUTE;
import static bo.AppConstants.DELIVERYDATE;
import static bo.AppConstants.FINALIZEEOCHANNELID;
import static bo.AppConstants.FRID;
import static bo.AppConstants.PPID;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.RECIPEIMAGEREMINDERCHANNEL;
import static bo.AppConstants.RECIPEID;
import static bo.AppConstants.RECORDREVIEWCHANNEL;
import static bo.AppConstants.REMINDERCHANNELID;
import static bo.AppConstants.REVIEWTIME_HOUR;
import static bo.AppConstants.REVIEWTIME_MIN;
import static bo.AppConstants.SENSORREMINDERCHANNEL;
import static bo.AppConstants.UNFINALIZEDEOIDS;

/**
 * Created by Josh on 23-Feb-18.
 * Controller Class for setting alarms for notifications.
 */

public class AlarmController {
    private static final int EATING_OCCASION_NOTIFICATION_DELAY_HOURS = 1;
    private static final int FOOD_RECORD_REQUEST_CODE = 356;
    private static final int RECORD_REVIEW_REQUEST_CODE = 357;
    private static final int RECIPE_IMAGE_REQUEST_CODE = 358;
    private static final String TAG = "AlarmController";
    private static final int REMINDER_REQUEST_CODE_START = 5000;

    private AlarmManager mAlarmManager;
    private Context mContext;

    public AlarmController(Application context){
        this.mContext = context;
        this.mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        setLanguage();
    }

    private void setLanguage() {
        String languageToLoad  = null;
        if(BuildConfig.forceKhmer) {
            languageToLoad = "km";
        }
        else if(BuildConfig.forceSwahili){
            languageToLoad = "sw";
        }

        if (languageToLoad != null) {
            Locale locale = new Locale(languageToLoad);
            Locale.setDefault(locale);
            Configuration config = mContext.getResources().getConfiguration();
            config.setLocale(locale);
            mContext.getResources().updateConfiguration(config,
                    mContext.getResources().getDisplayMetrics());
        }
    }

    /**
     * Schedules an alarm to trigger the notification that A food record is to be finalized.
     * The Alarm is set for 2 hours after the creation of the eating occasion.
     */
    public void scheduleUnfinalizedEatingOccasionNotification(String ppid, EatingOccasion eo){
        //Intent to launch when they click notification.
        int eoId = eo.getEatingOccasionId().intValue();

        Intent notficationIntent = new Intent(mContext, SelectEatingOccasionActivity.class);
        notficationIntent.putExtra(UNFINALIZEDEOIDS, new Long[]{eo.getEatingOccasionId()});
        notficationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent notificationPIntent = PendingIntent.getActivity(mContext,
                eoId,
                notficationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String title = mContext.getResources().getString(R.string.title_notification_finalize_eating_occasion);
        String text = mContext.getResources().getString(R.string.notification_unfinalized_eating_occasion_text);

        //Build the notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, FINALIZEEOCHANNELID)
                .setSmallIcon(R.drawable.ic_visida_simple)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(notificationPIntent)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setDefaults(Notification.DEFAULT_ALL);

        //Result intent. This intent is the one that gets passed to the Notification Publisher
        Intent resultIntent = new Intent(mContext, NotificationPublisher.class);
        int notificationId = eo.getEatingOccasionId().intValue();
        resultIntent.putExtra(AppConstants.CHANNELIDKEY, FINALIZEEOCHANNELID);
        resultIntent.putExtra(AppConstants.NOTIFICATION_ID, notificationId);
        resultIntent.putExtra(AppConstants.NOTIFICATION, mBuilder.build());


        //Create a pending intent with the result intent as contents
        PendingIntent pIntent = PendingIntent.getBroadcast(mContext,
                eoId,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(calendar.getTimeInMillis() + TimeUnit.HOURS.toMillis(EATING_OCCASION_NOTIFICATION_DELAY_HOURS));

        /* Test Value */
        //calendar.setTimeInMillis(calendar.getTimeInMillis() + TimeUnit.SECONDS.toMillis(2));

        mAlarmManager.set(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pIntent);

        //Create and save the eating occasion notification in the database
        EatingOccasionNotification eoNotification = new EatingOccasionNotification();
        eoNotification.setEatingOccasionId(eo.getEatingOccasionId());
        eoNotification.setDeliveryDate(new Date(calendar.getTimeInMillis()));
        eoNotification.setNotificationId(eo.getEatingOccasionId().intValue());
        eoNotification.setPpid(ppid);
        NotificationRepository repo = new NotificationRepository((Application) mContext.getApplicationContext());
        repo.add(eoNotification);

        Log.i(ACTIVITY_LOG_TAG, TAG + ": Unfinalised Eating Occasion Alarm set for " + Utilities.DATE_FORMAT.format(calendar.getTime()));

    }

    //Filter Equals compares (action, data, type, class, and categories)

    public void cancelUnfinalizedEatingOccasionNotification(long eoId) {
        //Cancel the Alarm for the FoodRecord Id
        Intent i = new Intent(mContext, NotificationPublisher.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(mContext,
                (int) eoId,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //Cancel the alarm
        mAlarmManager.cancel(pIntent);
        //Cancel the Intent
        pIntent.cancel();
        //Cancel Notification in Notification Managr
        NotificationPublisher.cancel(mContext, (int) eoId);
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Unfinalised Eating Occasion Alarm cancelled id " + eoId);

        //Cancel the EatingOccasionNotification
        NotificationRepository notificationRepository = new NotificationRepository((Application) mContext);
        notificationRepository.removeEatingOccasionNotification(eoId);
    }

    public void scheduleDefaultRecordReviewNotification(String ppid, Calendar cal) {
        //Intent to launch when they click notification.
        Intent notficationIntent = new Intent(mContext, RecordReviewActivity.class);

        //Use the id instead of the actual food record to save space.
        notficationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notficationIntent.putExtra(PPID, ppid.substring(0, ppid.length()-1));
        PendingIntent notificationPIntent = PendingIntent.getActivity(mContext,
                RECORD_REVIEW_REQUEST_CODE,
                notficationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String title = mContext.getResources().getString(R.string.title_notification_review);
        String text = mContext.getResources().getString(R.string.notification_review_text);

        //Build the notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, RECORDREVIEWCHANNEL)
                .setSmallIcon(R.drawable.ic_visida_simple)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(notificationPIntent)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setDefaults(Notification.DEFAULT_ALL);

        //"ppid1 || ppid2 || ppid3
        int notificationId = (int) ppid.hashCode();
        //Result intent. This intent is the one that gets passed to the Notification Publisher
        Intent resultIntent = new Intent(mContext, NotificationPublisher.class);
        resultIntent.putExtra(AppConstants.CHANNELIDKEY, RECORDREVIEWCHANNEL);
        resultIntent.putExtra(AppConstants.NOTIFICATION_ID, notificationId);
        resultIntent.putExtra(AppConstants.NOTIFICATION, mBuilder.build());
        //Have to add the ppid to the bundle
        resultIntent.putExtra(PPID, ppid.substring(0, ppid.length()-1));
        resultIntent.putExtra(DELIVERYDATE, cal.getTimeInMillis());

        //Create a pending intent with the result intent as contents
        PendingIntent pIntent = PendingIntent.getBroadcast(mContext,
                notificationId,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mAlarmManager.set(AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(),
                pIntent);

        Log.i(ACTIVITY_LOG_TAG, TAG + ": Default Record Review Alarm ID " + notificationId + " set for time " + Utilities.DATE_FORMAT.format(cal.getTime()));
    }

    public void scheduleRecordReviewNotification(String ppid, long frId){
        //Intent to launch when they click notification.
        Intent notficationIntent = new Intent(mContext, RecordReviewActivity.class);

        //Use the id instead of the actual food record to save space.
        notficationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notficationIntent.putExtra(FRID, frId);
        notficationIntent.putExtra(PPID, ppid);
        PendingIntent notificationPIntent = PendingIntent.getActivity(mContext,
                RECORD_REVIEW_REQUEST_CODE,
                notficationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String title = mContext.getResources().getString(R.string.title_notification_review);
        String text = mContext.getResources().getString(R.string.notification_review_text);

        //Build the notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, RECORDREVIEWCHANNEL)
                .setSmallIcon(R.drawable.ic_visida_simple)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(notificationPIntent)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setDefaults(Notification.DEFAULT_ALL);


        SharedPreferences sharedPref = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        int hour = sharedPref.getInt(REVIEWTIME_HOUR, DEFAULT_REVIEW_HOUR);
        int minute = sharedPref.getInt(REVIEWTIME_MIN, DEFAULT_REVIEW_MINUTE);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);        //24 hour time
        cal.set(Calendar.MINUTE, minute);

        /* Test Value */
        //cal.setTimeInMillis(cal.getTimeInMillis() + TimeUnit.SECONDS.toMillis(10));

        //ppid no extras
        int notificationId = (int) ppid.hashCode();
        //Result intent. This intent is the one that gets passed to the Notification Publisher
        Intent resultIntent = new Intent(mContext, NotificationPublisher.class);
        resultIntent.putExtra(AppConstants.CHANNELIDKEY, RECORDREVIEWCHANNEL);
        resultIntent.putExtra(AppConstants.NOTIFICATION_ID, notificationId);
        resultIntent.putExtra(AppConstants.NOTIFICATION, mBuilder.build());
        //Have to add the ppid to the bundle
        resultIntent.putExtra(PPID, ppid);
        resultIntent.putExtra(FRID, frId);
        resultIntent.putExtra(DELIVERYDATE, cal.getTimeInMillis());

        //Create a pending intent with the result intent as contents
        PendingIntent pIntent = PendingIntent.getBroadcast(mContext,
                notificationId,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mAlarmManager.set(AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(),
                pIntent);

        Log.i(ACTIVITY_LOG_TAG, TAG + ": Record Review Alarm ID " + notificationId + " set for time " + Utilities.DATE_FORMAT.format(cal.getTime()));

    }

    public void cancelRecordReviewNotification(int notificationId){
        //Cancel the Alarm for the FoodRecord Id
        //int hash = ppid.hashCode();
        Intent i = new Intent(mContext, NotificationPublisher.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(mContext,
                notificationId,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //Cancel the alarm
        mAlarmManager.cancel(pIntent);
        //Cancel the Intent
        pIntent.cancel();


        //Cancel Notification in Notification Managr
        NotificationPublisher.cancel(mContext, notificationId);
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Record Review Alarm cancelled id " + notificationId);
    }

    public void scheduleReminder(int day, Calendar cal){
        //Result intent. This intent is the one that gets passed to the Notification Publisher
        Intent resultIntent = new Intent(mContext, NotificationPublisher.class);
        resultIntent.putExtra(AppConstants.CHANNELIDKEY, REMINDERCHANNELID);
        resultIntent.putExtra(AppConstants.NOTIFICATION_ID, REMINDER_REQUEST_CODE_START + day);

        //Create a pending intent with the result intent as contents
        PendingIntent pIntent = PendingIntent.getBroadcast(mContext,
                REMINDER_REQUEST_CODE_START + day,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mAlarmManager.set(AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(),
                pIntent);
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Schedule Reminder Alarm Day "  + day + "  set for " + Utilities.DATE_FORMAT.format(cal.getTime()));
    }

    public void scheduleSensorReminder(Calendar time){
        //We need to create the notification in the Notification Publisher
        //since having an Image will cause a
        //TransactionTooLargeException to be thrown when saving the alarm.
        int day = time.get(Calendar.DAY_OF_YEAR);
        int hour = time.get(Calendar.HOUR_OF_DAY);
        int minute = time.get(Calendar.MINUTE);
        String dateCode = ""+ day + hour + minute;
        int alarmRequestCode = Integer.valueOf(dateCode);

        Intent alarmIntent = new Intent(mContext, NotificationPublisher.class);
        alarmIntent.putExtra(AppConstants.CHANNELIDKEY, SENSORREMINDERCHANNEL);
        alarmIntent.putExtra(AppConstants.NOTIFICATION_ID, alarmRequestCode);

        PendingIntent pIntent = PendingIntent.getBroadcast(mContext, alarmRequestCode, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pIntent);

        Log.i(ACTIVITY_LOG_TAG, TAG + ": Sensor Reminder Alarm set for " + Utilities.DATE_FORMAT.format(time.getTime()));
    }

    public void scheduleRecipeImageReminderNotification(long recipeId){
        //Intent to launch when they click notification.
        Intent notficationIntent = new Intent(mContext, CreateRecipeActivity.class);
        int hash = ("recipe" + recipeId).hashCode();

        //Put the recipe id in the intent.
        notficationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //Have to add the ppid to the bundle
        notficationIntent.putExtra(RECIPEID, recipeId);
        PendingIntent notificationPIntent = PendingIntent.getActivity(mContext,
                RECIPE_IMAGE_REQUEST_CODE,
                notficationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String title = mContext.getResources().getString(R.string.notification_title_recipe_image_reminder);
        String text = mContext.getResources().getString(R.string.notification_text_recipe_image_reminder);

        //Build the notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, RECIPEIMAGEREMINDERCHANNEL)
                .setSmallIcon(R.drawable.ic_visida_simple)
                .setContentTitle(title)
                .setContentText(text)
                //.setAutoCancel(true)
                .setContentIntent(notificationPIntent)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setDefaults(Notification.DEFAULT_ALL);

        //Result intent. This intent is the one that gets passed to the Notification Publisher
        Intent resultIntent = new Intent(mContext, NotificationPublisher.class);
        resultIntent.putExtra(AppConstants.CHANNELIDKEY, RECIPEIMAGEREMINDERCHANNEL);
        resultIntent.putExtra(AppConstants.NOTIFICATION_ID, hash);
        resultIntent.putExtra(AppConstants.NOTIFICATION, mBuilder.build());

        //Create a pending intent with the result intent as contents
        PendingIntent pIntent = PendingIntent.getBroadcast(mContext,
                hash, //notificationId
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 2);        //24 hour time

        /* Test Value */
        //cal.setTimeInMillis(cal.getTimeInMillis() + TimeUnit.SECONDS.toMillis(20));

        mAlarmManager.set(AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(),
                pIntent);

        Log.i(ACTIVITY_LOG_TAG, TAG + ": Recipe final image reminder for " + recipeId + " set for time " + Utilities.DATE_FORMAT.format(cal.getTime()));
    }

    public void cancelRecipeFinalImageReminder(long recipeId) {
        //Cancel the Alarm for the FoodRecord Id
        int hash = ("recipe" + recipeId).hashCode();
        Intent i = new Intent(mContext, NotificationPublisher.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(mContext,
                hash,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //Cancel the alarm
        mAlarmManager.cancel(pIntent);
        //Cancel the Intent
        pIntent.cancel();
        //Cancel Notification in Notification Managr
        NotificationPublisher.cancel(mContext, hash);
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Recipe final image reminder cancelled recipeId" + recipeId);
    }
}
