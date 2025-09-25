package au.edu.newcastle.jnc985.visida.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;

import au.edu.newcastle.jnc985.visida.R;
import bo.AppConstants;
import bo.State;
import bo.Utilities;
import bo.db.entity.FoodItem;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.ALLOW_TEXT;
import static bo.AppConstants.AUDIOFILE_NAME;
import static bo.AppConstants.FI;
import static bo.AppConstants.FOODITEM_DESCRIPTION_MAX_LENGTH;
import static bo.AppConstants.IMAGE_NAME;
import static bo.AppConstants.SHARED_DISH;
import static bo.AppConstants.TEXT_DESCRIPTION;


public class AudioActivity extends AppCompatActivity implements AudioRecordingFragment.AudioRecorderHandler {


    private static final String AUDIO_FRAGMENT = "audiofragment";
    private static final String TAG = "AudioActivity";

    private AudioRecordingFragment mAudioRecordingFragment;
    private NavigationBarFragment mNavBar;
    private FoodItem mFi;
    private String mImageName;
    private String mAudioFileName;
    private boolean mSharedDish;
    private boolean mAllowText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Audio Activity Created");
        // Run this at the start of every activity
        Utilities.updateLanguage(getBaseContext());
        setTitle(R.string.title_activity_record_audio);
        setContentView(R.layout.activity_audio);

        FragmentManager fm = getSupportFragmentManager();
        mNavBar = (NavigationBarFragment) fm.findFragmentByTag(AppConstants.NAVBAR);

        if(mNavBar == null) {
            //Load the fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, NavigationBarFragment.newInstance(), AppConstants.NAVBAR)
                    .commit();
        }

        mAudioRecordingFragment = (AudioRecordingFragment)fm.findFragmentByTag(AUDIO_FRAGMENT);
        if (savedInstanceState == null) {
            //First time this activity has loaded. Get data from intent
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();

            if(extras != null){
                this.mImageName =  extras.getString(IMAGE_NAME, "NO_IMAGE");
                this.mSharedDish = extras.getBoolean(SHARED_DISH, false);
                this.mAudioFileName = extras.getString(AUDIOFILE_NAME);
                //Get the extra that says if they user is allowed to enter text. Default to true.
                this.mAllowText = extras.getBoolean(ALLOW_TEXT, true);
                if(State.FINALIZE == Utilities.getState(this)){
                    //Get the foodItem form Intent
                    this.mFi = extras.getParcelable(FI);
                }
            }
        }
        else{
            //Unpack the savedInstance bundle
            this.mImageName = savedInstanceState.getString(IMAGE_NAME);
            this.mSharedDish = savedInstanceState.getBoolean(SHARED_DISH, false);
            this.mAudioFileName = savedInstanceState.getString(AUDIOFILE_NAME);
            this.mAllowText = savedInstanceState.getBoolean(ALLOW_TEXT);
            if(State.FINALIZE == Utilities.getState(this)){
                //Get the foodItem form Intent
                this.mFi = savedInstanceState.getParcelable(FI);
            }
        }

        if(mAudioRecordingFragment == null) {
            //Get a file name
            //String fileName = getOutputFileName();
            File outputFile = new File(Utilities.getMediaDirectory(this), mAudioFileName);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.audiocontainer, AudioRecordingFragment.newInstance(outputFile.getAbsolutePath(), false, mAllowText, FOODITEM_DESCRIPTION_MAX_LENGTH), AUDIO_FRAGMENT)
                    .commit();
            fm.executePendingTransactions();
            mAudioRecordingFragment = (AudioRecordingFragment)fm.findFragmentByTag(AUDIO_FRAGMENT);
        }

        setUpUI();
    }

    /**
     * Set up the UI state.
     * The household member and imagepath must not be null.
     */
    private void setUpUI(){
        if(!this.mImageName.isEmpty()) {
            //TODO Temporary getting of image
            File hmDirectory = Utilities.getMediaDirectory(this);
            File newImageFile = new File(hmDirectory, mImageName);
            ImageView imgView = findViewById(R.id.imgFood);
            Glide.with(this)
                    .load(newImageFile)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_food_placeholder_100))
                    .into(imgView);
        }
        else{
            //If there is no image set the audio fragment to audio only.
            mAudioRecordingFragment.setAudioOnly(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        //Save the image path
        outState.putString(IMAGE_NAME, this.mImageName);
        outState.putBoolean(SHARED_DISH, this.mSharedDish);
        outState.putString(AUDIOFILE_NAME, this.mAudioFileName);
        outState.putBoolean(ALLOW_TEXT, this.mAllowText);
        if(State.FINALIZE == Utilities.getState(this)){
            //Save the food Item
            outState.putParcelable(FI, mFi);
        }
    }

    @Override
    public void onRecordComplete(File outputFile) {
        //Return the audio file
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Audio Recording Complete");
        Intent result = new Intent();
        result.putExtra(AUDIOFILE_NAME, this.mAudioFileName);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    @Override
    public void onTextComplete(String textDescription) {
        //TODO Delete the Audio File (If it exists)
        //Return the text description
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Text Recording Complete");
        Intent result = new Intent();
        result.putExtra(TEXT_DESCRIPTION, textDescription);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    /**
     * TESTING ONLY
     * @return
     */
    public FoodItem getFoodItem() {
        return mFi;
    }
}
