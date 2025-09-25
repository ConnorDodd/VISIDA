package au.edu.newcastle.jnc985.visida.notification;

import android.Manifest;
import android.app.NotificationManager;
import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.RecyclerViewMatcher;
import au.edu.newcastle.jnc985.visida.activity.MainActivity;
import au.edu.newcastle.jnc985.visida.activity.RecordReviewActivity;
import au.edu.newcastle.jnc985.visida.activity.SelectEatingOccasionActivity;
import au.edu.newcastle.jnc985.visida.activity.SetupHouseholdActivity;
import bo.NotificationRepository;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.EatingOccasionNotification;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;
import bo.db.entity.ReviewNotification;

import static android.content.Context.NOTIFICATION_SERVICE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.PickerActions.setTime;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static bo.AppConstants.HOUSEHOLDID;
import static bo.AppConstants.PPID;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.UNFINALIZEDEOIDS;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by Josh on 19-Dec-17.
 */
@RunWith(AndroidJUnit4.class)
public class EatingOccasionNotificationTests {

    //Rule so we can access the application
    @Rule
    public IntentsTestRule<MainActivity> mIntentsTestRule = new IntentsTestRule<>(MainActivity.class, true, false);
    @Rule
    public GrantPermissionRule mGrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO);

    protected AppDatabase mDb;
    private Context mContext;
    private String ppid = "ppid";
    private HouseholdMember mHm;
    private long FR_ID = 1;
    private long EO_ID = 1;


    @Before
    public void setupContext() {
        mContext = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).build();
        AppDatabase.setInstance(mDb);

        //Add a household member
        mHm = new HouseholdMember(1, "hhid", "hm1", "", 11, false, false);
        mHm.setParticipantHouseholdMemberId(ppid);
        mDb.getHouseholdMemberDao().insert(mHm);

        clearAllNotifications();
    }

    private void clearAllNotifications() {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.openNotification();
        UiObject2 clearAllButton = device.findObject(By.textContains("CLEAR ALL"));
        if(clearAllButton != null) {
            clearAllButton.click();
        }
        device.pressBack();
    }

    @After
    public void pressDeviceHome() {
        //AlarmController ac = new AlarmController(mActivityRule.getActivity().getApplication());
        //ac.cancelUnfinalizedEatingOccasionNotification(EO_ID);
        //ac.cancelRecordReviewNotification(ppid);
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.pressHome();
    }

    @Test
    public void bellClearedWhenEatingOccasionFinalisedViaReview() throws Exception{
        //Create an eating occasion and notificaiton for it
        FoodRecord fr = new FoodRecord(mHm.getUid());
        fr.setFoodRecordId(1L);
        mDb.getFoodRecordDao().insert(fr);

        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId(1L);
        eo.setFoodRecordId(fr.getFoodRecordId());
        eo.setFinalized(false);
        mDb.getEatingOccasionDao().insert(eo);

        FoodItem fi1 = new FoodItem();
        fi1.setFoodItemId(1L);
        fi1.setEatingOccasionId(eo.getEatingOccasionId());
        fi1.setAudioUrls("");
        fi1.setImageUrl("");
        mDb.getFoodItemDao().insert(fi1);

        //Create eating notification to be delivered now
        EatingOccasionNotification eon1 = new EatingOccasionNotification();
        eon1.setEatingOccasionId(1L);
        eon1.setSeen(false);
        eon1.setPpid(ppid);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -10);
        eon1.setDeliveryDate(cal.getTime());
        mDb.getNotificationDao().insertEatingOccasion(eon1);

        //Launch the activity
        mIntentsTestRule.launchActivity(new Intent());

        //Check the notification is there under the bell
        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("1")));

        //Access the verify use case via the settings menu
        onView(withId(R.id.action_manage)).perform(click());
        //Enter the PIN
        String PIN = mIntentsTestRule.getActivity().getResources().getString(R.string.pin_value);
        onView(withId(R.id.txtPin)).perform(typeText(PIN));
        onView(withText(android.R.string.yes)).perform(click());

        //Click the Verify button
        onView(withId(R.id.btnVerify)).perform(click());

        //Click the food record
        onView(new RecyclerViewMatcher(R.id.rvEatingOccasions).atPositionOnView(0, R.id.txtEoTime)).perform(click());

        //Click Day Complete
        onView(withId(R.id.btnNo)).perform(click());

        //Click home
        onView(withId(R.id.btnHome)).perform(click());

        //Check the bell is now clear
        onView(withId(R.id.notification_dot)).check(matches(not(isDisplayed())));
    }

    @Test
    public void eatingOccasionNotificationPresentInListIfUnseen() throws Exception {
        //Create 2 eating occasion notifications
        //Seen
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -10);
        EatingOccasionNotification eon1 = new EatingOccasionNotification();
        eon1.setEatingOccasionId(1L);
        eon1.setSeen(true);
        eon1.setPpid("ppid");
        eon1.setDeliveryDate(cal.getTime());
        //Unseen
        EatingOccasionNotification eon2 = new EatingOccasionNotification();
        eon2.setEatingOccasionId(2L);
        eon2.setSeen(false);
        eon2.setPpid("ppid");
        eon2.setDeliveryDate(cal.getTime());
        mDb.getNotificationDao().insertEatingOccasion(eon1, eon2);

        //Launch the activity
        mIntentsTestRule.launchActivity(new Intent());

        //Make sure the red icon and 1 is visible
        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("1")));

        //Check the notification is in the system tray
//        NotificationManager mNotificationManager = (NotificationManager) mIntentsTestRule.getActivity().getSystemService(NOTIFICATION_SERVICE);
//        StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
//        assertThat(notifications.length, is(1));

        //clickBellCheckTrayIsEmpty();
    }

    @Test
    public void clickBellThenFinalizeNotificationMovesToSelectEatingOccasionActivity(){
        //Create a food record for the eating occasion to be included into
        long frId = 1;
        FoodRecord fr = new FoodRecord(1);
        fr.setFoodRecordId(frId);
        mDb.getFoodRecordDao().insert(fr);
        //Create an eating occasion to finalize
        long eoId = 1;
        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId(eoId);
        eo.setFinalized(false);
        eo.setFoodRecordId(frId);
        mDb.getEatingOccasionDao().insert(eo);
        //Create an eating occasion notification
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -10);
        EatingOccasionNotification eon1 = new EatingOccasionNotification();
        eon1.setEatingOccasionId(eoId);
        eon1.setSeen(false);
        eon1.setPpid("ppid");
        eon1.setDeliveryDate(cal.getTime());
        mDb.getNotificationDao().insertEatingOccasion(eon1);

        mIntentsTestRule.launchActivity(new Intent());

        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("1")));

        onView(withId(R.id.notification_list)).perform(click());
        String msg = eon1.getMessage(mContext);
        onView(withText(msg)).perform(click());

        intended(allOf(
                hasComponent(SelectEatingOccasionActivity.class.getName()),
                hasExtra(UNFINALIZEDEOIDS, new Long[]{eoId})
        ));
    }

    @Test
    public void bellClearedWhenLastFoodItemDeletedFromEatingOccasion() throws Exception{
        //Create a food record for the eating occasion to be included into
        long frId = 1;
        FoodRecord fr = new FoodRecord(1);
        fr.setFoodRecordId(frId);
        mDb.getFoodRecordDao().insert(fr);
        //Create an eating occasion to finalize
        long eoId = 1;
        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId(eoId);
        eo.setFinalized(false);
        eo.setFoodRecordId(frId);
        mDb.getEatingOccasionDao().insert(eo);

        //Create Food Item
        FoodItem fi = new FoodItem();
        fi.setFoodItemId(1L);
        fi.setEatingOccasionId(eo.getEatingOccasionId());
        fi.setImageUrl("");
        fi.setAudioUrls("");
        mDb.getFoodItemDao().insert(fi);

        //Create an eating occasion notification
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -10);
        EatingOccasionNotification eon1 = new EatingOccasionNotification();
        eon1.setEatingOccasionId(eoId);
        eon1.setSeen(false);
        eon1.setPpid("ppid");
        eon1.setDeliveryDate(cal.getTime());
        mDb.getNotificationDao().insertEatingOccasion(eon1);

        mIntentsTestRule.launchActivity(new Intent());

        //Go into theEating Occasion
        onView(withId(R.id.btnEat)).perform(click());

        //LongClick the First FoodItem in the list
        onView(new RecyclerViewMatcher(R.id.rvFoodItemList).atPositionOnView(0, R.id.imgIngredientImage)).perform(longClick());

        //Confirm the delete
        onView(withText(R.string.yes)).perform(click());

        //Click submit to leave back to home
        onView(withId(R.id.btnFinish)).perform(click());

        //Check that the bell has been cleared
        onView(withId(R.id.notification_dot)).check(matches(not(isDisplayed())));
    }

    @Test
    public void finaliseViaNotificationCrashWhenTakingPhoto(){
        //Create a food record for the eating occasion to be included into
        long frId = 1;
        FoodRecord fr = new FoodRecord(1);
        fr.setFoodRecordId(frId);
        mDb.getFoodRecordDao().insert(fr);
        //Create an eating occasion to finalize
        long eoId = 1;
        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId(eoId);
        eo.setFinalized(false);
        eo.setFoodRecordId(frId);
        mDb.getEatingOccasionDao().insert(eo);

        //Create Food Item
        FoodItem fi = new FoodItem();
        fi.setFoodItemId(1L);
        fi.setEatingOccasionId(eo.getEatingOccasionId());
        fi.setImageUrl("");
        fi.setAudioUrls("");
        mDb.getFoodItemDao().insert(fi);

        //Create an eating occasion notification
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -10);
        EatingOccasionNotification eon1 = new EatingOccasionNotification();
        eon1.setEatingOccasionId(eoId);
        eon1.setSeen(false);
        eon1.setPpid("ppid");
        eon1.setDeliveryDate(cal.getTime());
        mDb.getNotificationDao().insertEatingOccasion(eon1);

        mIntentsTestRule.launchActivity(new Intent());

        onView(withId(R.id.notification_list)).perform(click());
        String msg = eon1.getMessage(mContext);
        onView(withText(msg)).perform(click());

        //Click the finalise button
        onView(new RecyclerViewMatcher(R.id.rvEatingOccasions).atPositionOnView(0,R.id.btnFinalizeItem)).perform(click());

        //Click the ate most button
        onView(withId(R.id.btnAteSome)).perform(click());

        //Take an image
        onView(withId(R.id.btnTakePicture)).perform(click());

        //Click the confirm button
        onView(withId(R.id.btnImageAccept)).perform(click());

        //Check we can see the audio activity
        onView(withId(R.id.btnRecordAudio)).check(matches(isDisplayed()));
    }

    private void clickBellCheckTrayIsEmpty() {
        NotificationManager mNotificationManager = (NotificationManager) mIntentsTestRule.getActivity().getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
        //Click the bell and notification
        //Manually get the review notification to test against
        ReviewNotification rn1 = mDb.getNotificationDao().getReviewNotificationByTodayAndPpId(ppid);
        onView(withId(R.id.notification_list)).perform(click());
        String reviewDay = mIntentsTestRule.getActivity().getResources().getString(R.string.review_your_day);
        System.out.println(reviewDay + " " + rn1.getPpid());
        onView(withText(reviewDay + " " + rn1.getPpid())).perform(click());

        intended(allOf(
                hasComponent(RecordReviewActivity.class.getName()),
                hasExtra(PPID, rn1.getPpid())));

        //Click Day COmplete
        onView(withId(R.id.btnNo)).perform(click());

        //Check the red dot and 1 is now gone
        onView(withId(R.id.notification_dot)).check(matches(not(isDisplayed())));

        //Check the system tray no longer has the notification
        notifications = mNotificationManager.getActiveNotifications();
        assertThat(notifications.length, is(0));
    }


    private void recordAudioAccept() throws InterruptedException {
        onView(withId(R.id.btnRecordAudio)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.btnAudioFile)).perform(click());
        //Accept the audio
        onView(withId(R.id.btnAudioAccept)).perform(click());
    }

    private void takePhotoAccept() throws InterruptedException {
        Thread.sleep(500);
        onView(withId(R.id.btnTakePicture)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.btnImageAccept)).perform(click());
    }
}
