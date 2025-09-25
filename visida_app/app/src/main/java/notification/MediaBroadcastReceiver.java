package notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;

import static bo.AppConstants.ACTIVITY_LOG_TAG;

public class MediaBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "MediaBroadcastReciever";

    private static MediaPlayer mMediaPlayer;

    @Override
    public void onReceive(Context context, Intent intent){
        //Get the audio file out of the pending intent and play it with a media player
        if(mMediaPlayer != null){
            if(mMediaPlayer.isPlaying()){
                mMediaPlayer.stop();
            }
        }
        int audioFile = intent.getExtras().getInt("MEDIA_ID", -1);
        if(audioFile >= 0) {
            mMediaPlayer = MediaPlayer.create(context, audioFile);
            mMediaPlayer.start();
            Log.i(ACTIVITY_LOG_TAG, TAG + ": Media Broadcast Received");
        }
    }
}
