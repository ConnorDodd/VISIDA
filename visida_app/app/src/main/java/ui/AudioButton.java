package ui;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.IOException;

import au.edu.newcastle.jnc985.visida.R;

import static bo.AppConstants.ACTIVITY_LOG_TAG;

public class AudioButton extends androidx.appcompat.widget.AppCompatButton implements View.OnLongClickListener{
    private static final String TAG = "AudioButton";

    private static MediaPlayer mMediaPlayer;
    private static AudioButton previousAudioButton;
    private int mAudioFileResId;

    public AudioButton(Context context) {
        super(context);
        initialise(context, null);
    }

    public AudioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise(context, attrs);
        this.mMediaPlayer = new MediaPlayer();
    }

    public AudioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise(context, attrs);
    }

    private void initialise(Context context, AttributeSet attrs){
        //If first button, create the media player.
        if(AudioButton.mMediaPlayer == null){
            mMediaPlayer = new MediaPlayer();
        }

        if(attrs != null) {
            //Set the audio file
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AudioButton);
            this.mAudioFileResId = a.getResourceId(R.styleable.AudioButton_audioFile, -1);
            a.recycle();
        }

        //Doesn't have long click listener by default so add it here
        setTouchListener();
    }

    public void setAudioFileResId(int resId){
        this.mAudioFileResId = resId;
    }

    private void setTouchListener(){
        this.setOnLongClickListener(this);
    }

    @Override
    public boolean performClick(){
        super.performClick();
        resetPrevious();
        AudioButton.mMediaPlayer.reset();
        return true;
    }

    @Override
    public boolean onLongClick(View v) {
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Long Clicked " + getResources().getResourceEntryName(getId()));
        //Reset the previous audio button
        resetPrevious();

        //Reset the static media player
        AudioButton.mMediaPlayer.reset();

        //Set the on complete listenr for this button
        AudioButton.mMediaPlayer.setOnCompletionListener(mp -> {
            resetPrevious();
        });

        //Set the datasource of the media player to this buttons file
        setDataSource();

        //Start playing the audio
        playAudio();

        AudioButton.previousAudioButton = this;

         return true;
    }

    private void playAudio() {
        post(new Runnable() {
            @Override
            public void run() {
                //Return the alpha to original
                AudioButton.this.setAlpha(0.7f);
            }
        });
        AudioButton.mMediaPlayer.start();
    }

    /**
     * Sets the datasource for the media player to the classes resId
     * Also calls MediaPLayer.prepare().
     */
    private void setDataSource() {
        try {
            AssetFileDescriptor afd = getContext().getResources().openRawResourceFd(this.mAudioFileResId);
            if (afd == null) {
                System.out.println("SOMETHING WENT WRONG");
            }
            AudioButton.mMediaPlayer.setDataSource(afd);
            afd.close();
            AudioButton.mMediaPlayer.prepare();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private void resetPrevious() {
        //Return the alpha to original
        if(AudioButton.previousAudioButton != null) {
            AudioButton.previousAudioButton.setAlpha(1.0f);
        }
    }

    public void reset() {
        post(new Runnable() {
            @Override
            public void run() {
                //Return the alpha to original
                AudioButton.this.setAlpha(1.0f);
            }
        });
    }

    public int getAudioFile() {
        return mAudioFileResId;
    }




}
