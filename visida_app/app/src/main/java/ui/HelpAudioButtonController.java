package ui;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;

import au.edu.newcastle.jnc985.visida.BuildConfig;
import au.edu.newcastle.jnc985.visida.R;

import static bo.AppConstants.ACTIVITY_LOG_TAG;

public class HelpAudioButtonController {
    private static final String TAG = "HelpAudioButtonController";
    private final MediaPlayer mMediaPlayer;

    public HelpAudioButtonController(Context context, View view, int audioFileResId){
        //If Khmer version add the help buttons
        if(BuildConfig.forceKhmer || BuildConfig.forceSwahili) {
            //TODO Add actual Audio Clip for instructions
            ImageView imgHelp;
            if(!(view instanceof ImageView)) {
                imgHelp = view.findViewById(R.id.imgHelp);
            }
            else{
                imgHelp = (ImageView) view;
            }
            imgHelp.setVisibility(View.VISIBLE);
            mMediaPlayer = MediaPlayer.create(context, audioFileResId);
            
            //Set the Help button to play the audio file
            imgHelp.setOnClickListener(v -> {
                if (mMediaPlayer.isPlaying()) {
                    //Stop playing. Inorder to continue playing on subsequent presses
                    //We need to prepare the player. See State Diagram (https://developer.android.com/reference/android/media/MediaPlayer)
                    try {
                        mMediaPlayer.stop();
                        mMediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.i(ACTIVITY_LOG_TAG, TAG + ": Played Audio");
                    mMediaPlayer.start();
                }
            });
        }
        else{
            mMediaPlayer = null;
        }
    }

    public boolean isPlaying(){
        return mMediaPlayer.isPlaying();
    }

    public void destroy(){
        if(mMediaPlayer != null) {
            this.mMediaPlayer.stop();
            this.mMediaPlayer.release();
        }
    }
}
