package au.edu.newcastle.jnc985.visida.activity;

import android.app.Activity;
import android.app.Instrumentation;
import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.intent.rule.IntentsTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.RecyclerViewMatcher;
import au.edu.newcastle.jnc985.visida.TestUtilities;
import bo.EatingOccasionViewModel;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;
import notification.AlarmController;
import notification.NotificationPublisher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static bo.AppConstants.AUDIOFILE_NAME;
import static bo.AppConstants.FR;
import static bo.AppConstants.IMAGE_NAME;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;

/**
 * Created by jnc985 on 30-Nov-17.
 */

@RunWith(MockitoJUnitRunner.class)
public class EatingOccasionActivityTest {
    protected final static long MINUTE = 60 * 1000;

    protected AppDatabase mDb;

    @Mock
    protected EatingOccasionViewModel mEoViewModel;

    protected Context mContext;

    protected HouseholdMember hm1;

    protected String mHmName = "householdMemberName";
    private String ppid = "ppid";

    protected Intent mIntent;

    private FoodRecord mFr;
    private EatingOccasion mEo;
    private long mFrId = 1;
    private long mEoId = 1;
    //Set up Rule
    @Rule
    public IntentsTestRule<EatingOccasionActivity> mIntentsRule = new IntentsTestRule<EatingOccasionActivity>(EatingOccasionActivity.class, true, false) {
        @Override
        protected Intent getActivityIntent() {
            //Put the EAT state in as default Each test will have to set the state accordingly
            Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
            mIntent = new Intent(targetContext, EatingOccasionActivity.class);
            mIntent.putExtra(FR, new FoodRecord(0));
            return mIntent;
        }
    };


    @Before
    public void createDb() throws Throwable {
        mContext = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).build();
        AppDatabase.setInstance(mDb);
        mIntent = new Intent(mContext, EatingOccasionActivity.class);
        hm1 = new HouseholdMember(1, "", mHmName, "", 25, false, false);
        hm1.setParticipantHouseholdMemberId(ppid);
        mDb.getHouseholdMemberDao().insert(hm1);
        mFr = new FoodRecord(hm1.getUid());
        mFr.setFoodRecordId(mFrId);
        mFr.setHouseholdMember(hm1);
        mDb.getFoodRecordDao().insert(mFr);
        List<EatingOccasion> eos = new ArrayList<EatingOccasion>();
        mEo = new EatingOccasion();
        mEo.setFoodRecordId(mFr.getFoodRecordId());
        mEo.setEatingOccasionId(mEoId);
        mEo.setStartTime(new Date());
        mDb.getEatingOccasionDao().insert(mEo);
        eos.add(mEo);
        mFr.setEatingOccasions(eos);
        mIntent.putExtra(FR, mFr);
    }

    @After
    public void closeDb() {
        mDb.close();
    }

    @Test
    public void householdMemberNameAppearsAtTop() {
        //Mock the View Model
        doNothing().when(mEoViewModel).setEatingOccasion(any(EatingOccasion.class));

        mIntentsRule.launchActivity(mIntent);

        onView(withId(R.id.txtEoHmName)).check(matches(withText(mHmName)));
    }

    //Test with household members no food records
    @Test
    public void clickAddFoodItemMovesToCameraActivity() {
        //Mock the View Model
        doNothing().when(mEoViewModel).setEatingOccasion(any(EatingOccasion.class));

        mIntentsRule.launchActivity(mIntent);

        //Mock the intent
        Intent intent = new Intent();
        intent.putExtra(IMAGE_NAME, "NO_IMAGE");
        intent.putExtra(AUDIOFILE_NAME, "NO_AUDIO");

        Instrumentation.ActivityResult intentResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, intent);
        intending(anyIntent()).respondWith(intentResult);

        //Click the add FoodItem Button
        onView(withId(R.id.btnAddFoodItem)).perform(click());

        //Check that the Intent that is created contains a new Food Record (for today)
        intended(hasComponent(CameraActivity.class.getName()));
    }

    @Test
    public void addingFoodItemCreatesNotification(){
        //Create intent to start activity with
        Intent intent = new Intent(mContext, EatingOccasionActivity.class);
        intent.putExtra(FR, mFr);

        //Start Activity with the intent
        mIntentsRule.launchActivity(intent);

        //Cancel any alarms first. Need to get applicaiton so must launch activity first
        AlarmController ac = new AlarmController(mIntentsRule.getActivity().getApplication());
        ac.cancelUnfinalizedEatingOccasionNotification(mEoId);

        int hash = ppid.hashCode();
        ac.cancelRecordReviewNotification(hash);

        //Make sure alarm is not set already
        Intent expectedIntent = new Intent(mContext, NotificationPublisher.class);
        assertThat(TestUtilities.isAlarmSet(mContext, hash, expectedIntent), is(false));

        //Create Camera result intent
        Intent cameraResult = new Intent();
        cameraResult.putExtra(IMAGE_NAME, "NO_IMAGE");
        cameraResult.putExtra(AUDIOFILE_NAME, "NO_AUDIO");
        Instrumentation.ActivityResult cameraIntentResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, cameraResult);
        intending(hasComponent(CameraActivity.class.getName())).respondWith(cameraIntentResult);

        //Mock the Audio Intent
        Instrumentation.ActivityResult audioIntentResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, cameraResult);
        intending(hasComponent(AudioActivity.class.getName())).respondWith(audioIntentResult);

        //Click the add fooditem button
        onView(withId(R.id.btnAddFoodItem)).perform(click());

        //Check that Alarm is set for the notification
        assertThat(TestUtilities.isAlarmSet(mContext, hash, expectedIntent), is(true));
    }

    @Test
    public void deletingLastFoodItemDeletesNotifications(){
        FoodItem fi1 = new FoodItem();
        fi1.setEatingOccasionId(mEoId);
        mDb.getFoodItemDao().insert(fi1);

        //Create intent to start activity with
        Intent intent = new Intent(mContext, EatingOccasionActivity.class);
        intent.putExtra(FR, mFr);

        //Start Activity with the intent
        mIntentsRule.launchActivity(intent);

        //Set the two alarms
        AlarmController ac = new AlarmController(mIntentsRule.getActivity().getApplication());
        ac.scheduleUnfinalizedEatingOccasionNotification(ppid, mEo);
        ac.scheduleRecordReviewNotification(ppid, mFr.getFoodRecordId());

        //Make sure alarms are both set already
        int notificationId = ppid.hashCode();
        Intent expectedIntent = new Intent(mContext, NotificationPublisher.class);
        assertThat(TestUtilities.isAlarmSet(mContext, notificationId, expectedIntent), is(true));
        assertThat(TestUtilities.isAlarmSet(mContext, (int) mEoId, expectedIntent), is(true));


        //LongClick the First FoodItem in the list
        onView(new RecyclerViewMatcher(R.id.rvFoodItemList).atPositionOnView(0, R.id.imgIngredientImage)).perform(longClick());

        //Confirm the delete
        onView(withText(R.string.yes)).perform(click());

        //Check that Alarm is set for the notification
        //Deleting last food item does NOT remove the record review notification.
        assertThat(TestUtilities.isAlarmSet(mContext, notificationId, expectedIntent), is(true));
        assertThat(TestUtilities.isAlarmSet(mContext, (int) mEoId, expectedIntent), is(false));
    }

    @Test
    public void deletingLastFoodItemWithOtherEosContainingFoodItesmDoesNotDeleteTheRecordReviewNotification(){
        long eo2Id = 2;

        //Create an eating Occasion from 4 hours ago and give it a food item
        EatingOccasion eo2 = new EatingOccasion();
        eo2.setEatingOccasionId(eo2Id);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -4);
        eo2.setStartTime(cal.getTime());
        eo2.setFoodRecordId(mFrId);
        mFr.addEatingOccasion(eo2);
        FoodItem fi2 = new FoodItem();
        fi2.setEatingOccasionId(eo2Id);

        FoodItem fi1 = new FoodItem();
        fi1.setEatingOccasionId(mEoId);
        mDb.getEatingOccasionDao().insert(eo2);
        mDb.getFoodItemDao().insert(fi1, fi2);


        //Create intent to start activity with
        Intent intent = new Intent(mContext, EatingOccasionActivity.class);
        intent.putExtra(FR, mFr);

        //Start Activity with the intent
        mIntentsRule.launchActivity(intent);

        //Set the two alarms
        AlarmController ac = new AlarmController(mIntentsRule.getActivity().getApplication());
        ac.scheduleUnfinalizedEatingOccasionNotification(ppid, mEo);
        ac.scheduleRecordReviewNotification(ppid, mFr.getFoodRecordId());

        //Make sure alarms for new eating occasion (eo1) and Record Review are both set already
        int notificationId = ppid.hashCode();
        Intent expectedIntent = new Intent(mContext, NotificationPublisher.class);
        assertThat(TestUtilities.isAlarmSet(mContext, notificationId, expectedIntent), is(true));
        assertThat(TestUtilities.isAlarmSet(mContext, (int) mEoId, expectedIntent), is(true));

        //LongClick the First FoodItem in the list
        onView(new RecyclerViewMatcher(R.id.rvFoodItemList).atPositionOnView(0, R.id.imgIngredientImage)).perform(longClick());

        //Confirm the delete
        onView(withText(R.string.yes)).perform(click());

        //Check that Alarm for Record Review remains but eating occasion is gone.
        assertThat(TestUtilities.isAlarmSet(mContext, notificationId, expectedIntent), is(true));
        assertThat(TestUtilities.isAlarmSet(mContext, (int) mEoId, expectedIntent), is(false));
    }


    @Test
    public void deleting2ndFoodItemDoesNotRemoveNotification(){
        FoodItem fi1 = new FoodItem();
        fi1.setEatingOccasionId(mEoId);

        FoodItem fi2 = new FoodItem();
        fi2.setEatingOccasionId(mEoId);

        mDb.getFoodItemDao().insert(fi1, fi2);

        //Create intent to start activity with
        Intent intent = new Intent(mContext, EatingOccasionActivity.class);
        intent.putExtra(FR, mFr);

        //Start Activity with the intent
        mIntentsRule.launchActivity(intent);

        //Set the two alarms
        AlarmController ac = new AlarmController(mIntentsRule.getActivity().getApplication());
        ac.scheduleUnfinalizedEatingOccasionNotification(ppid, mEo);
        ac.scheduleRecordReviewNotification(ppid, mFr.getFoodRecordId());

        //Make sure alarms are both set already
        int notificationId = ppid.hashCode();
        Intent expectedIntent = new Intent(mContext, NotificationPublisher.class);
        assertThat(TestUtilities.isAlarmSet(mContext, notificationId, expectedIntent), is(true));
        assertThat(TestUtilities.isAlarmSet(mContext, (int) mEoId, expectedIntent), is(true));

        //LongClick the First FoodItem in the list
        onView(new RecyclerViewMatcher(R.id.rvFoodItemList).atPositionOnView(0, R.id.imgIngredientImage)).perform(longClick());

        //Confirm the delete
        onView(withText(R.string.yes)).perform(click());

        //Check that Alarm is still set for the notification
        assertThat(TestUtilities.isAlarmSet(mContext, notificationId, expectedIntent), is(true));
        assertThat(TestUtilities.isAlarmSet(mContext, (int) mEoId, expectedIntent), is(true));
    }

    @Test
    public void householdMemberProfilePictureVisibleOnScreen(){
        Intent intent = new Intent(mContext, EatingOccasionActivity.class);
        intent.putExtra(FR, mFr);

        //Start Activity with the intent
        mIntentsRule.launchActivity(intent);

        //Check the image view is dispalyed
        onView(withId(R.id.imgHmAvatar)).check(matches(isDisplayed()));
    }
}
