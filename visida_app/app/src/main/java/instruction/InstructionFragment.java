package instruction;

import android.media.MediaPlayer;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import au.edu.newcastle.jnc985.visida.BuildConfig;

import static bo.AppConstants.ACTIVITY_LOG_TAG;

public class InstructionFragment extends Fragment {
    private static final String TAG = "InstructionFragment";
    protected static final String TEXT = "test";
    protected static final String MEDIA_RES_ID = "videoresid";
    protected static final String AUDIO_RES_ID = "audioresid";

    protected String mText;
    protected int mMediaResId;
    protected int mAudioResId;

    protected MediaPlayer mMediaPlayer;

    public static Fragment getInstructionFragment(String resType, String mInstructionText, int mInstructionMediaId, int mInstructionAudioId) {
        switch (resType){
            case "drawable":
                return ImageInstructionFragment.newInstance(mInstructionText, mInstructionMediaId, mInstructionAudioId);
            case "raw":
                return VideoInstructionFragment.newInstance(mInstructionText, mInstructionMediaId, mInstructionAudioId);

        }
        return null;
    }

    protected void setupAudio(ImageView audioImg) {
        if(!BuildConfig.forceKhmer && !BuildConfig.forceSwahili) {
            audioImg.setVisibility(View.INVISIBLE);
        }
        else{
            audioImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mMediaPlayer != null){
                        //Stop the current media player
                        mMediaPlayer.reset();
                    }
                    mMediaPlayer = MediaPlayer.create(InstructionFragment.this.getContext(), mAudioResId);
                    Log.i(ACTIVITY_LOG_TAG, TAG + ": Playing audio");
                    mMediaPlayer.start();
                }
            });
        }
    }

    protected void stopMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopMediaPlayer();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser){
        super.setUserVisibleHint(isVisibleToUser);
        if(mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
        }
    }
}
