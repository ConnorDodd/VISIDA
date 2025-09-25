package ui;

import android.media.MediaPlayer;

/**
 * Created by jnc985 on 09-Apr-18.
 */

public class MediaPlayerManager {

    private static MediaPlayer instance;

    private MediaPlayerManager(){
        instance = new MediaPlayer();
    }

    public static MediaPlayer getInstance(){
        if (instance == null) {
            instance = new MediaPlayer();
            //Once the player has completed reset all of its parameters.
            instance.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    instance.reset();
                }
            });
        }
        return instance;
    }

    public static void resetPlayer(){
        if (instance != null) {
            instance.reset();
        }
    }
    public static void releasePlayer(){
        if (instance != null) {
            instance.release();
        }
    }
    public static void stopPlayer(){
        if (instance != null) {
            instance.stop();
        }
    }
}
