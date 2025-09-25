package au.edu.newcastle.jnc985.visida.notification;

import android.Manifest;
import android.app.Application;
import android.app.NotificationManager;
import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.service.notification.StatusBarNotification;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;
import android.widget.DatePicker;
import android.widget.TimePicker;

import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.TestUtilities;
import au.edu.newcastle.jnc985.visida.activity.MainActivity;
import au.edu.newcastle.jnc985.visida.activity.RecordReviewActivity;
import au.edu.newcastle.jnc985.visida.activity.SetupHouseholdActivity;
import bo.NotificationRepository;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodRecord;
import bo.db.entity.Household;
import bo.db.entity.HouseholdMember;
import bo.db.entity.ReviewNotification;
import notification.AlarmController;
import notification.NotificationPublisher;

import static android.content.Context.NOTIFICATION_SERVICE;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.PickerActions.setTime;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static au.edu.newcastle.jnc985.visida.EspressoTestMatchers.withDrawable;
import static bo.AppConstants.HOUSEHOLDID;
import static bo.AppConstants.PPID;
import static bo.AppConstants.PREFERENCES;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by Josh on 19-Dec-17.
 */
@RunWith(AndroidJUnit4.class)
public class ReviewNotificationTests {

    //Rule so we can access the application
    @Rule
    public IntentsTestRule<SetupHouseholdActivity> mIntentsTestRule = new IntentsTestRule<>(SetupHouseholdActivity.class, true, false);
    @Rule
    public GrantPermissionRule mGrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO);

    protected AppDatabase mDb;
    private Context mContext;
    private String ppid = "ppid";
    private long FR_ID = 1;
    private long EO_ID = 1;


    @Before
    public void setupContext() {
        mContext = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).build();
        AppDatabase.setInstance(mDb);

        setHouseholdId();

        //Add a household member
        HouseholdMember hm = new HouseholdMember(1, "hhid", "hm1", "", 11, false, false);
        hm.setParticipantHouseholdMemberId(ppid);
        mDb.getHouseholdMemberDao().insert(hm);

        clearAllNotifications();

        mIntentsTestRule.launchActivity(new Intent());
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

    private void setHouseholdId(){

        SharedPreferences sharedPref = InstrumentationRegistry.getTargetContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(HOUSEHOLDID, null);
        editor.commit();
    }

    @After
    public void cancelAllAlarms() {
        //AlarmController ac = new AlarmController(mActivityRule.getActivity().getApplication());
        //ac.cancelUnfinalizedEatingOccasionNotification(EO_ID);
        //ac.cancelRecordReviewNotification(ppid);
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.pressHome();
    }

    @Test
    public void defaultReviewDayNotificationClearsBellAndSystemTrayWhenDayCompleteAccessedViaBell() throws Exception {
        NotificationRepository notificationRepository = new NotificationRepository((Application)InstrumentationRegistry.getTargetContext().getApplicationContext());
        notificationRepository.deleteAll();
        setupTimes();

        //Wait a few seconds
        Thread.sleep(2000);

        //Click home
        onView(withId(R.id.btnHome)).perform(click());

        //Wait a few more seconds
        Thread.sleep(15000);

        //Make sure the red icon and 1 is visible
        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("1")));

        //Check the notification is in the system tray
        NotificationManager mNotificationManager = (NotificationManager) mIntentsTestRule.getActivity().getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
        assertThat(notifications.length, is(1));

        clickBellCheckTrayIsEmpty();

        //Check the red dot and 1 is now gone
        onView(withId(R.id.notification_dot)).check(matches(not(isDisplayed())));

        //Check the system tray no longer has the notification
        notifications = mNotificationManager.getActiveNotifications();
        assertThat(notifications.length, is(0));

    }

    @Test
    public void reviewDayNotificationForFoodRecordClearsSystemTray() throws Exception{
        //Create a default review notification which will appear instantly
        setupTimes();

        //Create a food record for HM (which will spawn a new review notification
        onView(withId(R.id.btnHome)).perform(click());

        onView(withId(R.id.btnEat)).perform(click());
        onView(withId(R.id.btnAddFoodItem)).perform(click());
        Thread.sleep(2000);
        takePhotoAccept();
        recordAudioAccept();
        //Submit
        onView(withId(R.id.btnFinish)).perform(click());

        Thread.sleep(15000);

        //Make sure only one notificaiton is in the system tray
        NotificationManager mNotificationManager = (NotificationManager) mIntentsTestRule.getActivity().getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
        assertThat(notifications.length, is(1));

    }

    @Test
    public void reviewDayNotificationForFoodRecordOverwritesDefaultClearedWhenAccessedViaBell() throws Exception{
        //Create a default review notification which will appear instantly
        setupTimes();

        //Create a food record for HM (which will spawn a new review notification
        onView(withId(R.id.btnHome)).perform(click());

        onView(withId(R.id.btnEat)).perform(click());
        onView(withId(R.id.btnAddFoodItem)).perform(click());
        Thread.sleep(2000);
        takePhotoAccept();
        recordAudioAccept();
        Thread.sleep(500);
        //Submit
        onView(withId(R.id.btnFinish)).perform(click());

        Thread.sleep(8000);

        //Make sure only one notificaiton is in the system tray
        NotificationManager mNotificationManager = (NotificationManager) mIntentsTestRule.getActivity().getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
        assertThat(notifications.length, is(1));

        Thread.sleep(6000);
        clickBellCheckTrayIsEmpty();


        //Check the system tray no longer has the notification
        notifications = mNotificationManager.getActiveNotifications();
        assertThat(notifications.length, is(0));
    }


    @Test
    public void defaultReviewDayNotificationClearsBellAndSystemTrayWhenDayCompleteAccessedViaNotification() throws Exception{
        setupTimes();

        //Wait a few seconds
        Thread.sleep(2000);

        //Click home
        onView(withId(R.id.btnHome)).perform(click());

        //Wait a few more seconds
        Thread.sleep(10000);

        //Make sure the red icon and 1 is visible
        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("1")));

        //Check the notification is in the system tray
        NotificationManager mNotificationManager = (NotificationManager) mIntentsTestRule.getActivity().getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
        assertThat(notifications.length, is(1));

        //Minimize the app
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.pressHome();

        //Open the system tray and click the notification
        device.openNotification();
        String title = mIntentsTestRule.getActivity().getString(R.string.title_notification_review);
        device.wait(Until.hasObject(By.textStartsWith("VISIDA")), 3000);
        Thread.sleep(3000);
        UiObject2 notificationTitle = device.findObject(By.textContains(title));
        device.click(device.getDisplayWidth() / 2, 550);

        //Click Day COmplete
        onView(withId(R.id.btnNo)).perform(click());

        //Check the red dot and 1 is now gone
        onView(withId(R.id.notification_dot)).check(matches(not(isDisplayed())));

        //Check the system tray no longer has the notification
        notifications = mNotificationManager.getActiveNotifications();
        assertThat(notifications.length, is(0));
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

    private void setupTimes() throws Exception{
        Calendar cal = Calendar.getInstance();
        int hrsNow = cal.get(Calendar.HOUR_OF_DAY);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month++;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        //Set the record review time
        onView(withId(R.id.txtWhatTime)).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(PickerActions.setTime(hrsNow-2, 0));
        Thread.sleep(1000);
        onView(withText(android.R.string.ok)).perform(click());

        //Click the button to set the times
        onView(withId(R.id.btnSetupTimes)).perform(click());

        //Set the dates, first to today, the rest to tomorrow
        onView(withId(R.id.btnDateEO1)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(year, month, day));
        onView(withText(android.R.string.ok)).perform(click());
        onView(withId(R.id.btnDateEO2)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(year, month, day+1));
        onView(withText(android.R.string.ok)).perform(click());
        onView(withId(R.id.btnDateEO3)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(year, month, day+1));
        onView(withText(android.R.string.ok)).perform(click());

        //Set the times
        //Click the First Time and set it to future so they don't appear
        onView(withTagValue(Is.is("1"))).perform(click());
        //Set the time
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hrsNow +1, 0));
        onView(withText(android.R.string.ok)).perform(click());
        //Set the others times to the future so they don't appear
        onView(withTagValue(Is.is("2"))).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hrsNow+1, 0));
        onView(withText(android.R.string.ok)).perform(click());
        onView(withTagValue(Is.is("3"))).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hrsNow+2, 0));
        onView(withText(android.R.string.ok)).perform(click());
        //Set the sensor
        onView(withId(R.id.btnSensor)).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hrsNow +1, 0));
        onView(withText(android.R.string.ok)).perform(click());
        //Set the times
        onView(withId(R.id.btnSet)).perform(click());
    }
}
