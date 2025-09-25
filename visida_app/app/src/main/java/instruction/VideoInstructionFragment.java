package instruction;


import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import au.edu.newcastle.jnc985.visida.R;
import bo.Utilities;

import static bo.AppConstants.ACTIVITY_LOG_TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoInstructionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoInstructionFragment extends InstructionFragment implements FragmentLifecycle{
    private static final String TAG = "VideoInstructionFragment";
    private VideoView mVideoView;
    private MediaController mMediaController;

    public VideoInstructionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment VideoInstructionFragment.
     */
    public static VideoInstructionFragment newInstance(String text, int mediaResId, int audioResId) {
        VideoInstructionFragment fragment = new VideoInstructionFragment();
        Bundle args = new Bundle();
        args.putString(TEXT, text);
        args.putInt(MEDIA_RES_ID, mediaResId);
        args.putInt(AUDIO_RES_ID, audioResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mText = getArguments().getString(TEXT);
            mMediaResId = getArguments().getInt(MEDIA_RES_ID);
            mAudioResId = getArguments().getInt(AUDIO_RES_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_video_instruction, container, false);

        mVideoView = v.findViewById(R.id.vidInstruction);
        Uri videoFile = Utilities.getUri(getContext(), mMediaResId);
        mVideoView.setVideoURI(videoFile);
        mVideoView.setOnTouchListener((v1, event) -> {
            if(mVideoView.isPlaying()){
                Log.i(ACTIVITY_LOG_TAG, TAG + ": Pausing video");
                mVideoView.pause();
                mMediaController.hide();
            }
            else{
                Log.i(ACTIVITY_LOG_TAG, TAG + ": Playing video");
                mVideoView.start();
            }
            return false;
        });
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.seekTo(1);
                mMediaController = new MediaController(getActivity());
                mMediaController.setAnchorView(mVideoView);

                mVideoView.setMediaController(mMediaController);
            }
        });

        return v;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser){
        super.setUserVisibleHint(isVisibleToUser);
        if(mVideoView != null){
            if(!isVisibleToUser) {
                if (mVideoView.isPlaying()) {
                    mVideoView.stopPlayback();
                    mMediaController.hide();
                }
            }
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if(mMediaController != null) {
            mMediaController.hide();
            System.out.println("PAUSE");
        }
    }

    @Override
    public void onFragmentPause() {
        if(mMediaController != null) {
            mMediaController.hide();
            System.out.println("HIDDEN");
        }
    }

    @Override
    public void onFragmentResume() {
        System.out.println("Resume VIDEO");
    }
}
