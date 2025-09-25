package notification;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import java.util.Date;
import java.util.Locale;

import au.edu.newcastle.jnc985.visida.BuildConfig;
import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.activity.MainActivity;
import bo.HouseholdMemberRepository;
import bo.NotificationRepository;
import bo.db.entity.ReviewNotification;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.CHANNELIDKEY;
import static bo.AppConstants.DELIVERYDATE;
import static bo.AppConstants.FINALIZEEOCHANNELID;
import static bo.AppConstants.FRID;
import static bo.AppConstants.NOTIFICATION;
import static bo.AppConstants.NOTIFICATION_ID;
import static bo.AppConstants.PPID;
import static bo.AppConstants.RECIPEIMAGEREMINDERCHANNEL;
import static bo.AppConstants.RECORDREVIEWCHANNEL;
import static bo.AppConstants.REMINDERCHANNELID;
import static bo.AppConstants.SENSORREMINDERCHANNEL;

/**
 * Created by jnc985 on 20-Feb-18.
 */

public class NotificationPublisher extends BroadcastReceiver {

    private static final String TAG = "NotificationPublisher";

    /**
     * When the broadcast reciever recieves a broadcast, the intent will
     * contain a (Parcelable) Notification with key {@link bo.AppConstants#NOTIFICATION}
     * and ID with key {@link bo.AppConstants#NOTIFICATION_ID}
     * @param context
     * @param intent
     */
    public void onReceive(Context context, Intent intent) {
        setLanguage(context);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = null;
        int notificationId = 0;
        String channelId = "";
        Bundle extras = intent.getExtras();
        if (extras != null) {
            notification = extras.getParcelable(NOTIFICATION);
            notificationId = extras.getInt(NOTIFICATION_ID);
            channelId = intent.getStringExtra(CHANNELIDKEY);
        }

        //Create Notification Channel for android >= 26 (Oreo)
        CharSequence name = "";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String description = "";
            if(channelId.equals(FINALIZEEOCHANNELID)) {
                name = context.getString(R.string.finalize_eo_notification_channel_name);
                description = context.getString(R.string.finalize_eo_notification_channel_description);

            }
            else if (channelId.equals(RECORDREVIEWCHANNEL)) {
                name = context.getString(R.string.record_review_notification_channel_name);
                description = context.getString(R.string.record_review_notification_channel_description);
            }
            else if(channelId.equals(REMINDERCHANNELID)){
                name = context.getString(R.string.notification_reminder_channel_name);
                description = context.getString(R.string.notification_reminder_channel_description);
            }
            else if(channelId.equals(RECIPEIMAGEREMINDERCHANNEL)){
                name = context.getString(R.string.notification_recipe_image_reminder_channel_name);
                description = context.getString(R.string.notification_recipe_image_reminder_channel_description);
            }
            else if(channelId.equals(SENSORREMINDERCHANNEL)) {
                name = context.getString(R.string.notification_sensor_channel_name);
                description = context.getString(R.string.notification_sensor_description);
            }
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);
            mChannel.setDescription(description);
            mChannel.enableVibration(true);
            notificationManager.createNotificationChannel(mChannel);
        }

        //Create the notifications
        String description = "";
        if(channelId.equals(FINALIZEEOCHANNELID)) {
            //Notification built in Alarm Controller
        }
        else if (channelId.equals(RECORDREVIEWCHANNEL)) {
            //Notification built in Alarm Controller
        }
        else if(channelId.equals(RECIPEIMAGEREMINDERCHANNEL)){
            //Notification built in Alarm Controller
        }
        else if(channelId.equals(REMINDERCHANNELID)){
            //Build Notification
            String title = context.getResources().getString(R.string.title_notification_reminder);
            //Check if there is a child  in the system
            HouseholdMemberRepository hmRepo = new HouseholdMemberRepository((Application) context.getApplicationContext());
            String text;
            if(hmRepo.hasChild()){
                text = context.getResources().getString(R.string.notification_reminder_child_text);
            }
            else {
                text = context.getResources().getString(R.string.notification_reminder_text);
            }

            //Intent to launch when they click notification.
            Intent notficationIntent = new Intent(context, MainActivity.class);
            notficationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent notificationPIntent = PendingIntent.getActivity(context,
                    notificationId,
                    notficationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
            bigPictureStyle.bigPicture(BitmapFactory.decodeResource(context.getResources(), R.drawable.reminder_record_intake)).build();
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, REMINDERCHANNELID)
                    .setSmallIcon(R.drawable.ic_visida_simple)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setAutoCancel(true)
                    .setContentIntent(notificationPIntent)
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setStyle(bigPictureStyle);
            notification = mBuilder.build();
        }
        else if(channelId.equals(SENSORREMINDERCHANNEL)){
            String title = context.getString(R.string.notification_sensor_title);
            String text = context.getString(R.string.notification_sensor_text);
            RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_big_image_audio_expanded);
            RemoteViews notificationLayoutCollapsed = new RemoteViews(context.getPackageName(), R.layout.notification_big_image_audio);

            Intent playIntent = new Intent(context, MediaBroadcastReceiver.class);
            playIntent.putExtra("MEDIA_ID", R.raw.aa_rec_116);
            PendingIntent playAudioPendingIntent = PendingIntent.getBroadcast(context,
                    notificationId, playIntent, PendingIntent.FLAG_CANCEL_CURRENT);


            //Intent to launch when they click notification.
            Intent notficationIntent = new Intent(context, MainActivity.class);
            notficationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent notificationPIntent = PendingIntent.getActivity(context,
                    notificationId,
                    notficationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            //Set the data in the layout
            notificationLayout.setTextViewText(R.id.notification_txt_title, title);
            notificationLayoutCollapsed.setTextViewText(R.id.notification_txt_title, title);
            notificationLayout.setTextViewText(R.id.notification_txt_content, text);
            notificationLayoutCollapsed.setTextViewText(R.id.notification_txt_content, text);
            notificationLayout.setImageViewResource(R.id.notification_img_image, R.drawable.wrist_sensor);
            notificationLayout.setOnClickPendingIntent(R.id.imgHelp, playAudioPendingIntent);

            //Build the notification
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, SENSORREMINDERCHANNEL)
                    .setSmallIcon(R.drawable.ic_visida_simple)
                    .setStyle(new androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle())
                    .setAutoCancel(true)
                    .setPriority(NotificationManager.IMPORTANCE_MAX)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentIntent(notificationPIntent)
                    .setCustomContentView(notificationLayoutCollapsed)
                    .setCustomBigContentView(notificationLayout);
            notification = mBuilder.build();
        }

        //Send the notification
        if(notification != null){
            int id = intent.getIntExtra(NOTIFICATION_ID, 0);
            notificationManager.notify(id, notification);
            Log.i(ACTIVITY_LOG_TAG, TAG + ": Notification ID " + notificationId + " Published on Channel " + name);
        }

        //If it is a review notification. Add it to the database for the notification menu
        //to display.
        if(channelId.equals(RECORDREVIEWCHANNEL)) {
            ReviewNotification n = new ReviewNotification();
            String ppid = extras.getString(PPID);
            long frId = extras.getLong(FRID, -1);
            long deliveryDate = extras.getLong(DELIVERYDATE);
            n.setDeliveryDate(new Date(deliveryDate));
            n.setPpid(ppid);
            n.setFoodRecordId(frId);
            n.setNotificationId(notificationId);
            //Add the notification to the database.
            NotificationRepository notificationRepo = new NotificationRepository((Application) context.getApplicationContext());
            notificationRepo.add(n);
        }
    }

    private void setLanguage(Context c) {
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
            Configuration config = c.getResources().getConfiguration();
            config.setLocale(locale);
            c.getResources().updateConfiguration(config,
                    c.getResources().getDisplayMetrics());
        }
    }

    public static void cancel(Context context, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }
}
