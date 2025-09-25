package au.edu.newcastle.jnc985.visida.activity;

import android.Manifest;
import android.app.Activity;
import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;
import androidx.fragment.app.Fragment;
import android.widget.ProgressBar;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import au.edu.newcastle.jnc985.visida.R;
import bo.FoodItemRepository;
import bo.State;
import bo.Utilities;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.ActivityResultMatchers.hasResultCode;
import static androidx.test.espresso.contrib.ActivityResultMatchers.hasResultData;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static au.edu.newcastle.jnc985.visida.EspressoTestMatchers.withBackGroundDrawable;
import static bo.AppConstants.AUDIOFILE_NAME;
import static bo.AppConstants.IMAGE_NAME;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.STATE;
import static bo.AppConstants.TEXT_DESCRIPTION;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Josh on 13-Dec-17.
 */

@RunWith(MockitoJUnitRunner.class)
public class AudioActivityTest {

    protected AppDatabase mDb;
    protected Context mContext;
    protected File mMediaDir;
    protected String testImageName = "testImage.jpg";
    protected String testAudioName = "testAudio.mp3";
    protected File testImageFile1;
    protected File testImageFile2;

    //Build Dummy HouseholdMember for entire class
    protected HouseholdMember hm = new HouseholdMember(1, "1", "TESTHM", "", 22, false, false);
    protected FoodRecord fr = new FoodRecord(hm.getUid());
    protected EatingOccasion eo = new EatingOccasion();
    protected FoodItem fi = new FoodItem();

    //protected File mOutputDirectory;
    protected Intent mIntent;

    @Mock
    protected FoodItemRepository mMockFoodItemRepo;

    @Rule
    public GrantPermissionRule mGrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO);

    @Rule
    public IntentsTestRule<AudioActivity> mAudioActivityActivityTestRule = new IntentsTestRule<AudioActivity>(AudioActivity.class, true, false) {
        @Override
        protected Intent getActivityIntent() {
            fr.setHouseholdMember(hm);
            fr.setFoodRecordId((long) 1);
            eo.setEatingOccasionId((long) 1);
            eo.setFoodRecordId(fr.getFoodRecordId());
            fr.addEatingOccasion(eo);

            fi.setFoodItemId(1);
            fi.setImageUrl("");
            fi.setAudioUrls("");
            fi.setEatingOccasionId(eo.getEatingOccasionId());


            //Set up Intent
            mIntent = new Intent(mContext, AudioActivity.class);
            mIntent.putExtra(IMAGE_NAME, testImageName);
            mIntent.putExtra(AUDIOFILE_NAME, testAudioName);
            return mIntent;
        }
    };

    private UiDevice mDevice;

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getTargetContext();
        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        startFragment();
    }

    public void startFragment() {
        //Set state to EAT
        SharedPreferences sharedPref = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(STATE, State.EAT.ordinal());
        editor.commit();

        mAudioActivityActivityTestRule.launchActivity(mIntent);

        //SetUp Media Directory
        setupMediaDir();
        //Set up Database
        setupDatabase();
    }

    protected void setupMediaDir() {
        //Set up Test media Directory
        mMediaDir = Utilities.getMediaDirectory(mContext);
        //Clean out Media Directory before starting tests
        if (mMediaDir.exists()) {
            for (File file : mMediaDir.listFiles()) {
                if (!file.isDirectory()) {
                    file.delete();
                }
            }
        }

        //Check that the media directory exists
        testImageFile1 = new File(mMediaDir, testImageName);

        boolean doSave = true;
        if (!testImageFile1.exists()) {
            try {
                //TODO This should be handled by the install process/CMS
                mMediaDir.mkdirs();
                doSave = testImageFile1.createNewFile();

                if (doSave) {
                    Bitmap bm = BitmapFactory.decodeResource(mAudioActivityActivityTestRule.getActivity().getResources(), R.drawable.ic_thinking);
                    saveBitmapToFile(testImageFile1.getAbsolutePath(), bm);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setupDatabase() {
        //Create Mock Database
        mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).build();
        //Add the mock database to the app
        AppDatabase.setInstance(mDb);

        //Add dummy data to database
        mDb.getHouseholdMemberDao().insert(hm);
        mDb.getFoodRecordDao().insert(fr);
        mDb.getEatingOccasionDao().insert(eo);
        mDb.getFoodItemDao().insert(fi);

    }

    @After
    public void cleanUpOutputDirectory() {
        mMediaDir = Utilities.getMediaDirectory(mContext);
        if (mMediaDir.exists()) {
            for (File file : mMediaDir.listFiles()) {
                if (!file.isDirectory()) {
                    file.delete();
                }
            }
        }
        mDb.close();
    }

    protected void saveBitmapToFile(String fullFileName, Bitmap icon) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fullFileName);
            icon.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected boolean containsAudioFiles(File directory) {
        for (File file : directory.listFiles()) {
            if (!file.isDirectory()) {
                if (file.getAbsolutePath().contains(".mp3")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    public void testInitialUIState() {
        //Check button says start recording
        onView(withId(R.id.btnRecordAudio)).check(matches(withBackGroundDrawable(R.drawable.ic_btn_microphone)));
        onView(withId(R.id.audioRecorProgressBar)).check(matches(isDisplayed()));
    }

    @Test
    public void testButtonUpdatesTextOnPress() {
        onView(withId(R.id.btnRecordAudio)).check(matches(withBackGroundDrawable(R.drawable.ic_btn_microphone)));

        //Start recording. Check the text updates.
        onView(withId(R.id.btnRecordAudio)).perform(click());
        //onView(withId(R.id.btnRecordAudio)).check(matches(withText(stopString)));
        onView(withId(R.id.btnAudioFile)).check(matches(withBackGroundDrawable(R.drawable.ic_btn_stop)));

        //Stop recording. Check the button disappears and new buttons appear
        onView(withId(R.id.btnAudioFile)).perform(click());
        onView(withId(R.id.btnRecordAudio)).check(matches(not(isDisplayed())));
        onView(withId(R.id.btnAudioAccept)).check(matches(isDisplayed()));
        onView(withId(R.id.btnAudioDelete)).check(matches(isDisplayed()));
    }

    //TODO Test that if app is closed image is deleted
    //https://stackoverflow.com/questions/7881558/android-how-to-cleanup-application-when-it-is-killed

    @Test
    public void pressRecordStartsRecording() {
        //Check that directory is empty
        assertThat(containsAudioFiles(mMediaDir), is(false));

        //Click the record butotn
        onView(withId(R.id.btnRecordAudio)).perform(click());

        //Check that an image has been created
        assertThat(containsAudioFiles(mMediaDir), is(true));

    }

    @Test
    public void pressStopStopsRecording() {
        //Check that directory is empty
        assertThat(containsAudioFiles(mMediaDir), is(false));

        //Click the record butotn
        onView(withId(R.id.btnRecordAudio)).perform(click());

        //WAit for 1 second to record some audio
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Press Stop
        onView(withId(R.id.btnAudioFile)).perform(click());

        //Check that an audio file has been created
        assertThat(containsAudioFiles(mMediaDir), is(true));

        //Check that Record button is invisible
        onView(withId(R.id.btnRecordAudio)).check(matches(not(isDisplayed())));
        //Check that the Accept and Delete Buttons are visible
        onView(withId(R.id.btnAudioAccept)).check(matches(isDisplayed()));
        onView(withId(R.id.btnAudioDelete)).check(matches(isDisplayed()));

        //Check the img view icon updates
        onView(withId(R.id.btnAudioFile)).check(matches(withBackGroundDrawable(R.drawable.ic_btn_play)));
    }

    @Test
    public void pressPlayButtonUpdatesIconAndBack() {
        //Check that directory is empty
        assertThat(containsAudioFiles(mMediaDir), is(false));

        //Click the record butotn
        onView(withId(R.id.btnRecordAudio)).perform(click());

        //WAit for 1 second to record some audio
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Press Stop
        onView(withId(R.id.btnAudioFile)).perform(click());

        //Check that an audio file has been created
        assertThat(containsAudioFiles(mMediaDir), is(true));

        //Check that Record button is invisible
        onView(withId(R.id.btnRecordAudio)).check(matches(not(isDisplayed())));
        //Check that the Accept and Delete Buttons are visible
        onView(withId(R.id.btnAudioAccept)).check(matches(isDisplayed()));
        onView(withId(R.id.btnAudioDelete)).check(matches(isDisplayed()));

        //Check the img view icon updates
        onView(withId(R.id.btnAudioFile)).check(matches(withBackGroundDrawable(R.drawable.ic_btn_play)));
        //Click the play button
        onView(withId(R.id.btnAudioFile)).perform(click());
        onView(withId(R.id.btnAudioFile)).check(matches(withBackGroundDrawable(R.drawable.ic_btn_replay)));

        //Click the pause button
        onView(withId(R.id.btnAudioFile)).perform(click());
        //Check it has gone back to Play
        onView(withId(R.id.btnAudioFile)).check(matches(withBackGroundDrawable(R.drawable.ic_btn_play)));

        //Click delete
        onView(withId(R.id.btnAudioDelete)).perform(click());
    }

    @Test
    public void playButtonReturnsToPlayAfterAudioPlaybackComplete() {
        //Check that directory is empty
        assertThat(containsAudioFiles(mMediaDir), is(false));

        //Click the record butotn
        onView(withId(R.id.btnRecordAudio)).perform(click());

        //WAit for 1 second to record some audio
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Press Stop
        onView(withId(R.id.btnAudioFile)).perform(click());

        //Check that an audio file has been created
        assertThat(containsAudioFiles(mMediaDir), is(true));

        //Check that Record button is invisible
        onView(withId(R.id.btnRecordAudio)).check(matches(not(isDisplayed())));
        //Check that the Accept and Delete Buttons are visible
        onView(withId(R.id.btnAudioAccept)).check(matches(isDisplayed()));
        onView(withId(R.id.btnAudioDelete)).check(matches(isDisplayed()));

        //Check the img view icon updates
        onView(withId(R.id.btnAudioFile)).check(matches(withBackGroundDrawable(R.drawable.ic_btn_play)));
        //Click the play button
        onView(withId(R.id.btnAudioFile)).perform(click());
        onView(withId(R.id.btnAudioFile)).check(matches(withBackGroundDrawable(R.drawable.ic_btn_replay)));

        //Wait for the audio to finsih
        try {
            Thread.sleep(1500);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Check the butotn is play again
        //Check the img view icon updates
        onView(withId(R.id.btnAudioFile)).check(matches(withBackGroundDrawable(R.drawable.ic_btn_play)));

        //Click delete
        onView(withId(R.id.btnAudioDelete)).perform(click());
    }

    @Test
    public void pressAcceptSavesAudioReturnsWithResultOKAndAudioFile() throws ExecutionException, InterruptedException {
        //Inject the Repo into the fragment
        List<Fragment> fragments = mAudioActivityActivityTestRule.getActivity().getSupportFragmentManager().getFragments();
        AudioRecordingFragment fragment = null;
        for (Fragment f : fragments) {
            if (f instanceof AudioRecordingFragment) {
                fragment = (AudioRecordingFragment) f;
            }
        }
        assertThat(fragment, is(notNullValue()));

        //Check that directory is empty
        assertThat(containsAudioFiles(mMediaDir), is(false));

        //Click the record butotn
        onView(withId(R.id.btnRecordAudio)).perform(click());

        //WAit for 1 second to record some audio
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Press Stop
        onView(withId(R.id.btnAudioFile)).perform(click());

        //Check that an audio file has been created
        assertThat(containsAudioFiles(mMediaDir), is(true));

        //Check that Record button is invisible
        onView(withId(R.id.btnRecordAudio)).check(matches(not(isDisplayed())));
        //Check that the Accept and Delete Buttons are visible
        onView(withId(R.id.btnAudioAccept)).check(matches(isDisplayed()));
        onView(withId(R.id.btnAudioDelete)).check(matches(isDisplayed()));

        //Click the accept button
        onView(withId(R.id.btnAudioAccept)).perform(click());

        assertThat(mAudioActivityActivityTestRule.getActivityResult(), hasResultCode(Activity.RESULT_OK));
        assertThat(mAudioActivityActivityTestRule.getActivityResult(), hasResultData(hasExtraWithKey(AUDIOFILE_NAME)));
    }

    @Test
    public void pressDeleteRemovesAudioReadyToRecordAgain() {
        //Check that directory is empty
        assertThat(containsAudioFiles(mMediaDir), is(false));

        //Click the record butotn
        onView(withId(R.id.btnRecordAudio)).perform(click());

        //WAit for 1 second to record some audio
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Press Stop
        onView(withId(R.id.btnAudioFile)).perform(click());

        //Check that an audio file has been created
        assertThat(containsAudioFiles(mMediaDir), is(true));

        //Check that Record button is invisible
        onView(withId(R.id.btnRecordAudio)).check(matches(not(isDisplayed())));
        //Check that the Accept and Delete Buttons are visible
        onView(withId(R.id.btnAudioAccept)).check(matches(isDisplayed()));
        onView(withId(R.id.btnAudioDelete)).check(matches(isDisplayed()));

        //Click the delete button
        onView(withId(R.id.btnAudioDelete)).perform(click());

        //Make sre file has been removed
        assertThat(containsAudioFiles(mMediaDir), is(false));

        //Check the state of the views
        onView(withId(R.id.btnRecordAudio)).check(matches(withBackGroundDrawable(R.drawable.ic_btn_microphone)));
        onView(withId(R.id.audioRecorProgressBar)).check(matches(isDisplayed()));
        onView(withId(R.id.btnAudioAccept)).check(matches(not(isDisplayed())));
        onView(withId(R.id.btnAudioDelete)).check(matches(not(isDisplayed())));

        ProgressBar progresBar = mAudioActivityActivityTestRule.getActivity().findViewById(R.id.audioRecorProgressBar);
        assertThat(progresBar.getProgress(), is(0));
    }


    @Test
    public void testWhenAppIsPausedWhenNotRecordingNothingChanges() {
        //Check button says start recording
        //onView(withId(R.id.btnRecordAudio)).check(matches(withText("Start Recording")));
        onView(withId(R.id.btnRecordAudio)).check(matches(withBackGroundDrawable(R.drawable.ic_btn_microphone)));
        onView(withId(R.id.audioRecorProgressBar)).check(matches(isDisplayed()));

        //Minimize and reopen the activity
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                mAudioActivityActivityTestRule.getActivity().recreate();
            }
        });

        //Check button says start recording
        onView(withId(R.id.btnRecordAudio)).check(matches(withBackGroundDrawable(R.drawable.ic_btn_microphone)));
        onView(withId(R.id.audioRecorProgressBar)).check(matches(isDisplayed()));
    }

    @Test
    public void testWhenAppIsPausedWhenRecordingFileDeletedUIReset() {
        //Check that an audio file has been created
        assertThat(containsAudioFiles(mMediaDir), is(false));

        //Check button says start recording
        onView(withId(R.id.btnRecordAudio)).check(matches(withBackGroundDrawable(R.drawable.ic_btn_microphone)));
        onView(withId(R.id.audioRecorProgressBar)).check(matches(isDisplayed()));

        //Start recording
        //Click the record butotn
        onView(withId(R.id.btnRecordAudio)).perform(click());

        assertThat(containsAudioFiles(mMediaDir), is(true));

        //Minimize and reopen the activity
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                mAudioActivityActivityTestRule.getActivity().recreate();
            }
        });

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(containsAudioFiles(mMediaDir), is(false));

        //Check button says start recording
        //onView(withId(R.id.btnRecordAudio)).check(matches(withText("Start Recording")));
        onView(withId(R.id.btnRecordAudio)).check(matches(withBackGroundDrawable(R.drawable.ic_btn_microphone)));
        onView(withId(R.id.audioRecorProgressBar)).check(matches(isDisplayed()));
    }

    @Test
    public void testWhenAppIsPausedWhenFinishedRecordingFilePersistsUIReadyForPlayback() throws RemoteException, InterruptedException {
        //setState(State.EAT);
        //Check that an audio file has been created
        assertThat(containsAudioFiles(mMediaDir), is(false));

        //Check button is the microphone
        onView(withId(R.id.btnRecordAudio)).check(matches(withBackGroundDrawable(R.drawable.ic_btn_microphone)));
        onView(withId(R.id.audioRecorProgressBar)).check(matches(isDisplayed()));

        //Start recording
        //Click the record butotn
        onView(withId(R.id.btnRecordAudio)).perform(click());

        //Wait
        Thread.sleep(1000);

        //Stop Recording
        onView(withId(R.id.btnAudioFile)).perform(click());

        mDevice.pressHome();

        //Reopen the app from recents
        mDevice.pressRecentApps();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Reopen the app
        mDevice.click(mDevice.getDisplayWidth() / 2, mDevice.getDisplayHeight() / 2);

        //Reopen the app by clicking the center of the screen
        mDevice.click(mDevice.getDisplayWidth()/2, mDevice.getDisplayHeight() / 2);
        assertThat(containsAudioFiles(mMediaDir), is(true));

        Thread.sleep(2000);

        assertThat(containsAudioFiles(mMediaDir), is(true));

        //Check that Record button is invisible
        onView(withId(R.id.btnRecordAudio)).check(matches(not(isDisplayed())));
        //Check that the Accept and Delete Buttons are visible
        onView(withId(R.id.btnAudioAccept)).check(matches(isDisplayed()));
        onView(withId(R.id.btnAudioDelete)).check(matches(isDisplayed()));

        //Check the img view icon updates
        onView(withId(R.id.btnRecordAudio)).check(matches(withBackGroundDrawable(R.drawable.ic_btn_microphone)));
    }


    @Test
    public void recordingAudioThenClickingDeleteThenRecordingTextReturnsTextNoAudioFileCreated(){
        //Check no audio file is there yet
        assertThat(containsAudioFiles(mMediaDir), is(false));

        //Start recording
        //Click the record butotn
        onView(withId(R.id.btnRecordAudio)).perform(click());

        //Wait
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Stop Recording
        onView(withId(R.id.btnAudioFile)).perform(click());

        assertThat(containsAudioFiles(mMediaDir), is(true));

        //Click delete
        onView(withId(R.id.btnAudioDelete)).perform(click());

        //Check file is gone
        assertThat(containsAudioFiles(mMediaDir), is(false));

        //Enter text
        onView(withId(R.id.btnAddText)).perform(click());

        String sampleText = "sample text blah blahb blah";
        onView(withClassName(endsWith("EditText"))).perform(typeText(sampleText), closeSoftKeyboard());

        onView(withId(R.id.btnTextAccept)).perform(click());

        assertThat(mAudioActivityActivityTestRule.getActivityResult(), hasResultCode(Activity.RESULT_OK));
        assertThat(mAudioActivityActivityTestRule.getActivityResult(), hasResultData(hasExtraWithKey(TEXT_DESCRIPTION)));
    }
    
}
