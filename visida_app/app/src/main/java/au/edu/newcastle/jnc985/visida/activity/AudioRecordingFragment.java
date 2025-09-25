package au.edu.newcastle.jnc985.visida.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import au.edu.newcastle.jnc985.visida.R;
import ui.HelpAudioButtonController;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.ALLOW_TEXT;
import static bo.AppConstants.AUDIOFILE_NAME;
import static bo.AppConstants.DIALOG_MODE;
import static bo.AppConstants.HELP_AUDIO;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link au.edu.newcastle.jnc985.visida.activity.AudioRecordingFragment.AudioRecorderHandler} interface
 * to handle interaction events.
 * Use the {@link AudioRecordingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AudioRecordingFragment extends DialogFragment implements  View.OnClickListener{
       private static final String MAX_AUDIO_LENGTH = "maxaudiolength";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final String FRAGMENT_DIALOG = "dialog";

    public interface AudioRecorderHandler{
        void onRecordComplete(File outputFile);
        void onTextComplete(String textDescription);
    }

    /**
     * Calling activity to handle the recording upon completion
     */
    private AudioRecorderHandler mAudioRecorderHandler;

    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "AudioRecordFragment";


    /**
     * Maximum length a recording can take. In miliseconds.
     */
    private long MAX_RECORDING_LENGTH;

    /**
     * Length of a second in miliseconds
     */
    private static final long SECOND = 1000;

    /**
     * Buttons in the fragment
     */
    private Button btnMicrophone;
    private Button btnText;
    private Button btnCancel;
    private Button btnMediaControl;

    /**
     * Progress bar to show duration of the recording
     */
    private ProgressBar mProgressBar;

    /**
     * Countdown Timer to countdown the progress of an audio recording
     */
    private CountDownTimer mCountDownTimer;

    /**
     * Current state of the recording fragment
     */
    private RECORDING_STATE mState = RECORDING_STATE.STOPPED;

    /**
     * States which the recording fragment can be in.
     */
    private enum RECORDING_STATE  {STOPPED, RECORDING, RECORDED}

    /**
     * Audio Recorder
     */
    private MediaRecorder mAudioRecorder;

    /**
     * Media player used for playing back the audio file
     */
    private MediaPlayer mMediaPlayer;

    /**
     * boolean to tell if the audio file is currently playing
     */
    private boolean mIsAudioPlaying;

    /**
     * File name (full path) for the current audio file
     */
    private String mAudioFileName;

    /**
     * String to hold the text description of the food item
     * Default to Empty since Database is NOT NULL.
     */
    private String mTextDescription = "";

    /**
     * Boolean to track if the fragment is used for an audio only record
     */
    private boolean mDialogMode;

    /**
     * Boolean to track if the dialog will alow the user to enter text.
     */
    private boolean mAllowText;

    /**
     * Controller for the help button
     */
    private HelpAudioButtonController helpAudioButtonController;

    public boolean isAudioOnly() {
        return mDialogMode;
    }

    public void setAudioOnly(boolean mAudioOnly) {
        this.mDialogMode = mAudioOnly;
    }

    public AudioRecordingFragment() {/*Required empty public constructor*/}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * When created using onCreateDialog is only called if NOT Tag is provided.
     * eg: AudioRecordingFragment frag = AudioRecordingFragment.newInstance(outputFile.getAbsolutePath(), true, AudioActivity.FOODITEM_DESCRIPTION_MAX_LENGTH);
     *
     *  If a tag is provided {@link #onCreateDialog(Bundle)} is not run.
     *
     * @param maxAudioLength the maximum amount of time (in seconds) a recording can last.
     * @return A new instance of fragment AudioRecordingFragment.
     */
    public static AudioRecordingFragment newInstance(String audioFilePath, boolean dialogMode, boolean allowText,  int maxAudioLength) {
        AudioRecordingFragment fragment = new AudioRecordingFragment();
        Bundle args = new Bundle();
        args.putBoolean(DIALOG_MODE, dialogMode);
        args.putLong(MAX_AUDIO_LENGTH, maxAudioLength * SECOND);
        args.putString(AUDIOFILE_NAME, audioFilePath);
        args.putBoolean(ALLOW_TEXT, allowText);
        fragment.setArguments(args);
        return fragment;
    }

    public static AudioRecordingFragment newInstance(String audioFilePath, int resId, boolean dialogMode, boolean allowText,  int maxAudioLength) {
        AudioRecordingFragment fragment = new AudioRecordingFragment();
        Bundle args = new Bundle();
        args.putBoolean(DIALOG_MODE, dialogMode);
        args.putLong(MAX_AUDIO_LENGTH, maxAudioLength * SECOND);
        args.putString(AUDIOFILE_NAME, audioFilePath);
        args.putBoolean(ALLOW_TEXT, allowText);
        args.putInt(HELP_AUDIO, resId);
        fragment.setArguments(args);
        return fragment;
    }

    private int helpAudioResId;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestAudioRecordPermission();
        }

        Bundle args = getArguments();
        if (args != null) {
            this.mDialogMode = args.getBoolean(DIALOG_MODE);
            this.MAX_RECORDING_LENGTH = args.getLong(MAX_AUDIO_LENGTH);
            this.mAudioFileName = args.getString(AUDIOFILE_NAME);
            this.mAllowText = args.getBoolean(ALLOW_TEXT);
            this.helpAudioResId = args.getInt(HELP_AUDIO, -1);
        }

        mIsAudioPlaying = false;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstance){
        btnMicrophone = this.getView().findViewById(R.id.btnRecordAudio);
        btnText = this.getView().findViewById(R.id.btnAddText);
        btnMediaControl = view.findViewById(R.id.btnAudioFile);
        btnMicrophone.setOnClickListener(this);
        btnText.setOnClickListener(this);
        btnMediaControl.setOnClickListener(this);

        view.findViewById(R.id.btnAudioDelete).setOnClickListener(this);
        view.findViewById(R.id.btnAudioAccept).setOnClickListener(this);

        if(mDialogMode){
            btnCancel = view.findViewById(R.id.btnCancel);
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setOnClickListener(this);

        }
        if(!mAllowText){
            Button btnText = view.findViewById(R.id.btnAddText);
            btnText.setVisibility(View.GONE);
        }
        if(this.helpAudioResId >0){
            setHelpAudioButtonController(new HelpAudioButtonController(getContext(), view.findViewById(R.id.imgHelp), this.helpAudioResId));
        }

        mProgressBar = view.findViewById(R.id.audioRecorProgressBar);
        mProgressBar.setMax((int)(this.MAX_RECORDING_LENGTH / SECOND));
        mCountDownTimer = new CountDownTimer(this.MAX_RECORDING_LENGTH, SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                mProgressBar.setProgress(mProgressBar.getProgress() + 1);
            }

            @Override
            public void onFinish() {
                Toast.makeText(getContext(), "TIMER FINISHED", Toast.LENGTH_LONG).show();

                if(mState == RECORDING_STATE.RECORDING) {
                    //Finish the audio.
                    mState = RECORDING_STATE.RECORDED;

                    //Stop recording
                    stopRecording();

                    //Set up the UI
                    toggleButtonsTo(false);
                    setButtonToPlay();
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_audio_recording, container, false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AudioRecorderHandler) {
           mAudioRecorderHandler = (AudioRecorderHandler) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AudioRecorderHandler");
        }
    }

    @Override
    public void onPause(){
        //Stop Playing audio
        if(mIsAudioPlaying){
            stopAudio();
        }
        //Stop recording
        if(mState == RECORDING_STATE.RECORDING){
            mState = RECORDING_STATE.STOPPED;
            stopRecording();
            deleteRecordedFile();
        }
        super.onPause();
    }

    private void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isActive()){
            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mAudioRecorderHandler = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    TakePhotoFragment.ErrorDialog
                            .newInstance(getString(R.string.request_permission_audio))
                            .show(getChildFragmentManager(), FRAGMENT_DIALOG);
                            //As part of the Error dialog if they clock ok the activity finishes

                } else {
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
        }

    }

    private void requestAudioRecordPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @Override
    public void onClick(View view) {
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked " + getResources().getResourceEntryName(view.getId()));
        switch (view.getId()) {
            case R.id.btnRecordAudio: {
                captureAudio();
                break;
            }
            case R.id.btnAudioAccept:{
                acceptAudio();
                break;
            }
            case R.id.btnAudioDelete:{
                //Reset the views and buttons
                //Hide the media control button
                btnMediaControl.setBackground(null);

                //Hide the Accept/Delete buttons
                toggleButtonsTo(true);

                //Reset progress
                mProgressBar.setProgress(0);

                //Delete File
                deleteRecordedFile();

                //Change state
                mState = RECORDING_STATE.STOPPED;

                //Show the controls again
                showControls();
                break;
            }
            case R.id.btnAudioFile:{
                //Play the audio file back
                handleMediaControl();
                break;
            }
            case R.id.btnCancel:
            {
                //Delete the file before canceling the dialog
                File f = getOuputFile();
                if(f.exists()){
                    f.delete();
                }
                Dialog dialog = getDialog();
                dialog.dismiss();
                break;
            }
            case R.id.btnAddText:
                addText();
                break;
        }
    }



    private void handleMediaControl() {
        switch(mState){
            case RECORDING: {
                Log.i(ACTIVITY_LOG_TAG, TAG + ": Stop recording");
                //Change to stopped
                this.mState = RECORDING_STATE.RECORDED;

                //Stop Recording
                stopRecording();

                //Set the media control button to the Play Sign
                setButtonToPlay();

                //Set the state of the buttons
                toggleButtonsTo(false);

                break;
            }
            case RECORDED:{
                playAudio();
            }
        }
    }

    private void setButtonToPlay() {
        Drawable icon = getContext().getDrawable(R.drawable.ic_btn_play);
        btnMediaControl.setBackground(icon);
        btnMediaControl.setText("");
    }

    private void addText() {
        //Create an alert Dialog with an Edit Text box to take the input.
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.fragment_text_input_dialog, null);
        Button btnAcceptText = dialogLayout.findViewById(R.id.btnTextAccept);
        Button btnDeclineText = dialogLayout.findViewById(R.id.btnTextDecline);
        EditText editText = dialogLayout.findViewById(R.id.dialog_edittext);
        //Hide the line at the bottom
        editText.setBackground(null);

        //Add the TextBox to the Dialog
        builder.setView(dialogLayout);
        builder.setTitle(R.string.add_text_description_dialog_title);
        //Create the dialog
        AlertDialog dialog = builder.create();

        btnAcceptText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextDescription = editText.getText().toString();
                if (mTextDescription.isEmpty()) {
                    Toast.makeText(getContext(), R.string.empty_text, Toast.LENGTH_SHORT).show();
                } else {
                    mAudioRecorderHandler.onTextComplete(mTextDescription);
                    closeKeyboard();
                    dialog.dismiss();
                }
            }
        });
        btnDeclineText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                closeKeyboard();
                dialog.dismiss();
            }
        });

        //Open the Keyboard. Force the keyboard to open.
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        dialog.show();
    }

    private void acceptAudio() {
        //Reset the ui
        toggleButtonsTo(true);
        mProgressBar.setProgress(0);
        mAudioRecorderHandler.onRecordComplete(getOuputFile());
    }

    /**
     * True: Can see the "Record" button
     * False: Can see "Accept" and "Delete"
     * @param recordMode
     */
    private void toggleButtonsTo(boolean recordMode){
        //Activate the Play and delete buttons
        Button accept = this.getView().findViewById(R.id.btnAudioAccept);
        Button delete = this.getView().findViewById(R.id.btnAudioDelete);
        Button text   = this.getView().findViewById(R.id.btnAddText);

        if(recordMode) {
            accept.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
            btnMicrophone.setVisibility(View.VISIBLE);
            if(mAllowText) {
                text.setVisibility(View.VISIBLE);
            }
        }
        else{
            accept.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
            btnMicrophone.setVisibility(View.GONE);
            if(mAllowText) {
                text.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Method for orchestrating the capture of audio.
     */
    private void captureAudio(){
        this.mState = RECORDING_STATE.RECORDING;

        //Hide the main controls
        hideControls();

        //Show the stop button
        btnMediaControl.setVisibility(View.VISIBLE);
        Drawable stopRecording = getContext().getDrawable(R.drawable.ic_btn_stop);
        btnMediaControl.setBackground(stopRecording);
        btnMediaControl.setText(R.string.stop);

        //Actually capture the audio from the microphone
        recordAudio();
    }

    private void hideControls() {
        btnMicrophone.setVisibility(View.INVISIBLE);
        if(mAllowText){
            btnText.setVisibility(View.INVISIBLE);
        }
        if(mDialogMode){
            btnCancel.setVisibility(View.INVISIBLE);
        }
    }

    private void showControls(){
        btnMicrophone.setVisibility(View.VISIBLE);
        if(mAllowText){
            btnText.setVisibility(View.VISIBLE);
        }
        if(mDialogMode){
            btnCancel.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Stops the audio recorder recording and releases its resources.
     */
    private void stopRecording() {
        try {
            //Stop timer
            mCountDownTimer.cancel();
            mAudioRecorder.stop();
        } catch (RuntimeException ex) {
            deleteRecordedFile();
        }
        mAudioRecorder.reset();
        mAudioRecorder.release();
    }

    private void deleteRecordedFile() {
        //If aduio is playing stop it first.
        if(mIsAudioPlaying){
            stopAudio();
        }
        //Delete the file
        File f = getOuputFile();
        f.delete();
        //Reset the Progress Bar
        mProgressBar.setProgress(0);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void recordAudio() {
        mAudioRecorder = new MediaRecorder();
        mAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mAudioRecorder.setAudioSamplingRate(48000);
        mAudioRecorder.setAudioEncodingBitRate(384000);
        File outputFile = getOuputFile();
        if(outputFile != null) {
            mAudioRecorder.setOutputFile(outputFile.getPath());
            try {
                mAudioRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Start the Progress bar
            mProgressBar.setProgress(0);
            mCountDownTimer.start();
            mAudioRecorder.start();
        }
        else{
            Log.e("TAG", "Error recording audio, outputfile was null");
        }
    }

    private File getOuputFile() {
        File f = new File(mAudioFileName);
        return f;
    }


    private void playAudio() {
        if(mState == RECORDING_STATE.RECORDED) {
            if(!mIsAudioPlaying) {
                Log.i(ACTIVITY_LOG_TAG, TAG + ": Start playing audio");
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        stopAudio();
                    }
                });
                try {
                    File dataSource = getOuputFile();
                    mMediaPlayer.setDataSource(dataSource.getAbsolutePath());
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                } catch (IOException ex) {
                    Log.e("TAG", ex.getMessage());
                }
                mIsAudioPlaying = true;
                //TODO Update image view to a stop button
                Drawable icon = getContext().getDrawable(R.drawable.ic_btn_replay);
                btnMediaControl.setBackground(icon);
            }
            else{
                stopAudio();
            }
        }
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if(helpAudioButtonController != null && helpAudioButtonController.isPlaying()){
            helpAudioButtonController.destroy();
        }
        super.onDismiss(dialog);

    }

    private void stopAudio(){
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Stop audio");
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;
        mIsAudioPlaying = false;
        Drawable icon = getContext().getDrawable(R.drawable.ic_btn_play);
        btnMediaControl.setBackground(icon);
    }

    public void setHelpAudioButtonController(HelpAudioButtonController controller){
        this.helpAudioButtonController = controller;
    }


    /**
     * Shows OK/Cancel confirmation dialog about Audio permission.
     */
    public static class ConfirmationDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.request_permission_audio)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            parent.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                                    REQUEST_RECORD_AUDIO_PERMISSION);
                        }
                    })
                    .setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Activity activity = parent.getActivity();
                                    if (activity != null) {
                                        activity.finish();
                                    }
                                }
                            })
                    .create();
        }
    }
}
