package au.edu.newcastle.jnc985.visida.activity;

import android.app.Instrumentation;
import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.TestUtilities;
import bo.State;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;
import notification.AlarmController;
import notification.NotificationPublisher;

import static android.app.Activity.RESULT_OK;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static bo.AppConstants.AUDIOFILE_NAME;
import static bo.AppConstants.EOID;
import static bo.AppConstants.FR;
import static bo.AppConstants.IMAGE_NAME;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.STATE;
import static bo.AppConstants.UNFINALIZEDEOIDS;
import static org.hamcrest.core.Is.is;

/**
 * Created by jnc985 on 30-Nov-17.
 */


@RunWith(MockitoJUnitRunner.class)
public class FinalizeFoodItemActivityTest {
    private Context mContext;

    private AppDatabase mDb;

    private static FoodRecord mFr;
    private static EatingOccasion mEo;
    private Intent intent;

    private final long mHmId = 1;
    private final long mFrId = 1;
    private final long mEOidUnFinalized = 1;

    //Set up Rule
    @Rule
    public IntentsTestRule<FinalizeFoodItemActivity> mIntentsRule = new IntentsTestRule<FinalizeFoodItemActivity>(FinalizeFoodItemActivity.class, true, false) {
        @Override
        protected Intent getActivityIntent() {
            Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
            Intent result = new Intent(targetContext, FinalizeFoodItemActivity.class);

            //Return the food record when queried
            result.putExtra(FR, mFr);
            return result;
        }
    };

    @Rule
    public ActivityTestRule<SelectEatingOccasionActivity> mActivtyRule = new ActivityTestRule<>(SelectEatingOccasionActivity.class, true, false);
    private long mEOidFinalized = (long) 2;


    @Before
    public void createDb() {
        mContext = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).build();
        //Add the mock database to the app
        AppDatabase.setInstance(mDb);
    }

    @After
    public void closeDb() {
        mDb.close();
    }

    private void setUpData() {
        HouseholdMember hm = new HouseholdMember();
        hm.setUid(mHmId);
        mDb.getHouseholdMemberDao().insert(hm);

        mFr = new FoodRecord(mHmId);
        mFr.setFoodRecordId(mFrId);
        mDb.getFoodRecordDao().insert(mFr);

        //Create 2 eating occasions one finalized one not
        EatingOccasion eo1 = new EatingOccasion();
        eo1.setEatingOccasionId(mEOidUnFinalized);
        eo1.setFinalized(false);
        eo1.setFoodRecordId(mFrId);
        EatingOccasion eo2 = new EatingOccasion();
        eo2.setEatingOccasionId(mEOidFinalized);
        eo2.setFinalized(true);
        eo2.setFoodRecordId(mFrId);

        mDb.getEatingOccasionDao().insert(eo1);
        mDb.getEatingOccasionDao().insert(eo2);

        //Create food items
        FoodItem fi1 = new FoodItem();
        fi1.setEatingOccasionId(mEOidUnFinalized);
        fi1.setImageUrl("");
        FoodItem fi2 = new FoodItem();
        fi2.setEatingOccasionId(mEOidUnFinalized);
        fi2.setImageUrl("");
        FoodItem fi3 = new FoodItem();
        fi3.setEatingOccasionId(2);
        FoodItem fi4 = new FoodItem();
        fi4.setEatingOccasionId(2);

        mDb.getFoodItemDao().insert(fi1, fi2, fi3, fi4);
        mEo = eo1;
    }

    private void launchActivity() {
        intent = new Intent(mContext, FinalizeFoodItemActivity.class);
        intent.putExtra(EOID, mEOidUnFinalized);
        mIntentsRule.launchActivity(intent);
    }

    @Test
    public void finalizingFinalFoodItemOfEatingOccasionRemovesAlarm() {
        setUpData();
        launchActivity();

        //Schedule alarm for this FoodRecord manually.
        AlarmController ac = new AlarmController(mIntentsRule.getActivity().getApplication());
        ac.scheduleUnfinalizedEatingOccasionNotification("ppid", mEo);

        //Check that Alarm is set for the notification
        Intent expectedIntent = new Intent(mIntentsRule.getActivity().getApplication(), NotificationPublisher.class);
        assertThat(TestUtilities.isAlarmSet(mIntentsRule.getActivity().getApplication(), mEo.getEatingOccasionId().intValue(), expectedIntent), is(true));

        //Click ate all button
        onView(withId(R.id.btnAteAll)).perform(click());
        onView(withId(R.id.btnAteAll)).perform(click());
        //Check we are at the
        assertThat(TestUtilities.isAlarmSet(mIntentsRule.getActivity().getApplication(), mEo.getEatingOccasionId().intValue(), expectedIntent), is(false));
    }

    @Test
    public void clickingBackOnFoodItemReturnsToSelectEatingOccasion() {
        /*
        Bug existed that when finalizing multiple items, if on the second (or later) item to finalizeItem
        And the user clicks back, they are returned to the previous item (which is already finalized.
        The back button on Finalize Item Activity should return the user the select eating occasion activity.
         */
        setUpData();
        Intent selectEOIntent = new Intent(InstrumentationRegistry.getTargetContext(), SelectEatingOccasionActivity.class);
        Long[] nonFinalizedEoIds = new Long[]{mEOidUnFinalized};
        selectEOIntent.putExtra(UNFINALIZEDEOIDS, nonFinalizedEoIds);
        //Have to launch the SelectEatingOccasion Activity before launching the Finalize Food Item Activity to
        //have correct back stack
        mActivtyRule.launchActivity(selectEOIntent);

        launchActivity();


        //Launching activity will load the first food item to finalizeItem
        //Click ate all
        onView(withId(R.id.btnAteAll)).perform(click());

        //Will be on the second item click back
        onView(withId(R.id.btnBack)).perform(click());

        //Check we are back at the selecteatingoccasion page
        onView(withId(R.id.rvEatingOccasions)).check(matches(isDisplayed()));
    }

    @Test
    public void takingLeftoverImageMovesToAudioActivity() {
        setUpData();

        intent = new Intent(mContext, FinalizeFoodItemActivity.class);
        intent.putExtra(EOID, (mEOidUnFinalized));
        mIntentsRule.launchActivity(intent);

        Intent i = new Intent();
        i.putExtra(IMAGE_NAME, "SAMPLE_IMAGE");
        i.putExtra(AUDIOFILE_NAME, "SAMPLE_AUDIO");
        Instrumentation.ActivityResult resultIntent = new Instrumentation.ActivityResult(RESULT_OK, i);
        intending(anyIntent()).respondWith(resultIntent);

        //Click ate most button
        onView(withId(R.id.btnAteSome)).perform(click());

        //Check it goes to the camera
        intended(hasComponent(CameraActivity.class.getName()));
        //Check it then goes to the audio
        intended(hasComponent(AudioActivity.class.getName()));

    }

    @Test
    public void savingAudioFileReturnsToFinalizeFragmentWithFoodItemFinalized() {
        setUpData();

        SharedPreferences sharedPref = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(STATE, State.FINALIZE.ordinal());
        editor.commit();

        EatingOccasion eo = new EatingOccasion();
        eo.setFoodRecordId(mFrId);
        eo.setEatingOccasionId(mDb.getEatingOccasionDao().insert(eo));

        FoodItem fi = new FoodItem();
        fi.setEatingOccasionId(eo.getEatingOccasionId());
        fi.setFinalized(false);
        fi.setImageUrl("");
        fi.setAudioUrls("");
        fi.setFoodItemId(mDb.getFoodItemDao().insert(fi));

        intent = new Intent(mContext, FinalizeFoodItemActivity.class);
        intent.putExtra(EOID, (eo.getEatingOccasionId()));
        mIntentsRule.launchActivity(intent);

        Intent i = new Intent();
        i.putExtra(IMAGE_NAME, "SAMPLE_IMAGE");
        i.putExtra(AUDIOFILE_NAME, "SAMPLE_AUDIO");
        Instrumentation.ActivityResult resultIntent = new Instrumentation.ActivityResult(RESULT_OK, i);
        intending(hasComponent(CameraActivity.class.getName())).respondWith(resultIntent);
        //intending(hasComponent(AudioActivity.class.getName())).respondWith(resultIntent);

        //Click ate most button
        onView(withId(R.id.btnAteSome)).perform(click());

        //Check it goes to the camera
        intended(hasComponent(CameraActivity.class.getName()));
        //Check it then goes to the audio
        intended(hasComponent(AudioActivity.class.getName()));

        //Click the record audio button
        onView(withId(R.id.btnRecordAudio)).perform(click());
        //Stop Recording
        onView(withId(R.id.btnAudioFile)).perform(click());
        //Accept the audio
        onView(withId(R.id.btnAudioAccept)).perform(click());

        intended(hasComponent(FinalizeFoodItemActivity.class.getName()));

        //Check the food item has been finalized
        FoodItem fiAfter = mDb.getFoodItemDao().getFoodItem(fi.getFoodItemId()).get(0);
        assertThat(fiAfter.isFinalized(), is(true));

        //Since only a single fooditem is present check that the eating occasion is also finalized
        EatingOccasion eoAfter = mDb.getEatingOccasionDao().getEatingOccasion(eo.getEatingOccasionId()).get(0);
        assertThat(eoAfter.isFinalized(), is(true));
    }
}
