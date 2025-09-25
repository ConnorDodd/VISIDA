package au.edu.newcastle.jnc985.visida.activity;

import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import bo.NotificationRepository;
import bo.State;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;
import bo.db.entity.ReviewNotification;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.times;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static bo.AppConstants.FRID;
import static bo.AppConstants.HASBREASTFED;
import static bo.AppConstants.PPID;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.SETUP;
import static bo.AppConstants.STATE;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by jnc985 on 30-Nov-17.
 */

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private AppDatabase mDb;

    //Intent to start activity
    Intent intent = new Intent();
    private Context mContext;
    private HouseholdMember hm1;
    //Set up Rule
    @Rule
    public ActivityTestRule<MainActivity> mIntentsTestRule = new ActivityTestRule<MainActivity>(MainActivity.class, true, false);
    private String ppid = "ppid";

    @Before
    public void setUpState() {
        //Default to SETUP=true
        mContext = getInstrumentation().getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).build();
        hm1 = new HouseholdMember(1, "1", "HM1", "", 1, false, false);
        hm1.setParticipantHouseholdMemberId(ppid);
        mDb.getHouseholdMemberDao().insert(hm1);
        AppDatabase.setInstance(mDb);
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SETUP, true);
        editor.commit();
        Intents.init();
    }

    @After
    public void cleanUp() {
        Intents.release();
    }

    @Test
    public void clickEatMovesToSelectMemberPage() {
        mIntentsTestRule.launchActivity(intent);
        onView(ViewMatchers.withId(R.id.btnEat)).perform(click());
        //Check that the intent is create dand going to the next page
        intended(hasComponent(SelectHouseholdMemberActivity.class.getName()));
        SharedPreferences sharedPref = mIntentsTestRule.getActivity().getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        State currentState = State.values()[sharedPref.getInt(STATE, State.INVALID.ordinal())];
        assertThat(currentState, is(State.EAT));
    }

    @Test
    public void clickFinalizeEatMovesToSelectHouseholdMemberPageInFINALIZEState() {
        mIntentsTestRule.launchActivity(intent);
        onView(ViewMatchers.withId(R.id.btnFinalizeEat)).perform(click());
        //Check that the intent is create dand going to the next page
        intended(hasComponent(SelectHouseholdMemberActivity.class.getName()));
        SharedPreferences sharedPref = mIntentsTestRule.getActivity().getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        State currentState = State.values()[sharedPref.getInt(STATE, State.INVALID.ordinal())];
        assertThat(currentState, is(State.FINALIZE));
    }

    @Test
    public void clickFinalizeEatDeletesEmptyEatingOccasions() {
        //Add an empty eating occasion to the database
        //Add Empty Food Record
        FoodRecord fr = new FoodRecord(hm1.getUid());
        fr.setFoodRecordId((long) 1);
        mDb.getFoodRecordDao().insert(fr);
        EatingOccasion eo = new EatingOccasion();
        eo.setFoodRecordId(fr.getFoodRecordId());
        mDb.getEatingOccasionDao().insert(eo);

        List<EatingOccasion> eosBefore = mDb.getEatingOccasionDao().getAll();
        assertThat(eosBefore.size(), is(1));

        mIntentsTestRule.launchActivity(intent);
        onView(ViewMatchers.withId(R.id.btnFinalizeEat)).perform(click());
        //Check that the intent is create dand going to the next page
        intended(hasComponent(SelectHouseholdMemberActivity.class.getName()));
        SharedPreferences sharedPref = mIntentsTestRule.getActivity().getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        State currentState = State.values()[sharedPref.getInt(STATE, State.INVALID.ordinal())];
        assertThat(currentState, is(State.FINALIZE));

        List<EatingOccasion> eos = mDb.getEatingOccasionDao().getAll();
        assertThat(eos.size(), is(0));
    }

    @Test
    public void clickBreastfeedMovesToSelectHouseholdMemberPageInBREASTFEEDState() {
        SharedPreferences sharedPref = InstrumentationRegistry.getTargetContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(HASBREASTFED, true);
        editor.commit();
        mIntentsTestRule.launchActivity(intent);
        onView(ViewMatchers.withId(R.id.btnBreastFeed)).perform(click());
        //Check that the intent is create dand going to the next page
        intended(hasComponent(SelectHouseholdMemberActivity.class.getName()));
        State currentState = State.values()[sharedPref.getInt(STATE, State.INVALID.ordinal())];
        assertThat(currentState, is(State.BREASTFEED));
    }

    @Test
    public void movesToHouseholdSetupWhenNotSetup() {
        //Set the shared preference to false
        Context context = getInstrumentation().getTargetContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SETUP, false);
        editor.commit();

        //Start the activity
        mIntentsTestRule.launchActivity(intent);

        //Check that the set up household member activity is open
        intended(allOf(hasComponent(SetupHouseholdActivity.class.getName())));
    }

    @Test
    public void clickSpannerTakesToSetupHouseholdPageAskingForPIN() {
        //Set the shared preference to false
        Context context = getInstrumentation().getTargetContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SETUP, true);
        editor.commit();

        //Start the activity
        mIntentsTestRule.launchActivity(intent);

        //Click the settings button
        onView(withId(R.id.action_manage)).perform(click());

        //Check we are at the settings page
        onView(withId(R.id.txtPin)).check(matches(isDisplayed()));
        String PIN = mIntentsTestRule.getActivity().getResources().getString(R.string.pin_value);
        onView(withId(R.id.txtPin)).perform(typeText(PIN));
        //Click ok
        onView(withText(android.R.string.yes)).perform(click());
        onView(withId(R.id.btnSetupHousehold)).check(matches(isDisplayed()));
    }

    @Test
    public void breastfeedButtonAppearsWhenBreastfedHouseholdMember() {
        //Set the shared preference to true
        Context context = getInstrumentation().getTargetContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(HASBREASTFED, true);
        editor.commit();

        //Start the activity
        mIntentsTestRule.launchActivity(intent);

        onView(withId(R.id.btnBreastFeed)).check(matches(isDisplayed()));
    }

    @Test
    public void breastfeedButtonNotPresentWhenNoBreastfedHouseholdMember() {
        //Set the shared preference to true
        Context context = getInstrumentation().getTargetContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(HASBREASTFED, false);
        editor.commit();

        //Start the activity
        mIntentsTestRule.launchActivity(intent);

        onView(withId(R.id.btnBreastFeed)).check(matches(not(isDisplayed())));

    }

    //@Test
    public void clickSettingsButtonAsksForPinAndMovesToSetupHouseholdOnCorrectPin() {
        //Set the shared preference to false
        Context context = getInstrumentation().getTargetContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SETUP, true);
        editor.commit();

        //Start the activity
        mIntentsTestRule.launchActivity(intent);

        //Click the settings button
        onView(withId(R.id.action_manage)).perform(click());

        //Check the alert is displayed
        onView(withText(R.string.enter_pin)).check(matches(isDisplayed()));

        //Enter the correct pin
        onView(withId(R.id.txtName)).perform(typeText("ACTUAL PIN NUMBER"), closeSoftKeyboard());
        //                 ^^^^^^^^
        //Verify we are on the setuphouseholdactivity
        onView(withId(R.id.recyclelistHouseholdMembers)).check(matches(isDisplayed()));
        onView(withId(R.id.fab)).check(matches(isDisplayed()));
    }

    //@Test
    public void clickSettingsButtonAsksForPinAndMovesToMainActivityOnIncorrectPin() {
        //Set the shared preference to false
        Context context = getInstrumentation().getTargetContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SETUP, true);
        editor.commit();

        //Start the activity
        mIntentsTestRule.launchActivity(intent);

        //Click the settings button
        onView(withId(R.id.action_manage)).perform(click());

        //Check the alert is displayed
        onView(withText(R.string.enter_pin)).check(matches(isDisplayed()));

        //Enter the correct pin
        onView(withId(R.id.txtName)).perform(typeText("INCORRECT PIN NUMBER"), closeSoftKeyboard());
        //                 ^^^^^^^^

        //Verify we are on the setuphouseholdactivity
        onView(withId(R.id.btnEat)).check(matches(isDisplayed()));
        onView(withId(R.id.btnFinalizeEat)).check(matches(isDisplayed()));
    }

    @Test
    public void clickCooksetsStateToCook() {
        mIntentsTestRule.launchActivity(intent);
        onView(withId(R.id.btnCook)).perform(click());

        //Check that the intent is create dand going to the next page
        intended(hasComponent(ListRecipesActivity.class.getName()));
        SharedPreferences sharedPref = mIntentsTestRule.getActivity().getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        State currentState = State.values()[sharedPref.getInt(STATE, State.INVALID.ordinal())];
        assertThat(currentState, is(State.COOK));
    }

    @Test
    public void notificationsBlankWhenNoNotificationsPresent() {
        Context context = InstrumentationRegistry.getTargetContext();
        AppDatabase db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        AppDatabase.setInstance(db);

        mIntentsTestRule.launchActivity(intent);

        onView(withId(R.id.notification_dot)).check(matches(not(isDisplayed())));
    }

    @Test
    public void notificationsOneWhenSingleUnseenNotificationsPresent() {
        ReviewNotification rn1 = new ReviewNotification();
        rn1.setSeen(false);
        rn1.setReviewNotificationId(1);
        rn1.setPpid(ppid);
        mDb.getNotificationDao().insertReview(rn1);
        mIntentsTestRule.launchActivity(intent);

        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("1")));
    }

    @Test
    public void clickNotificationButNotDayCompleteLeavesNotificationUnseen() {
        ReviewNotification rn1 = new ReviewNotification();
        rn1.setSeen(false);
        rn1.setReviewNotificationId(1);
        rn1.setFoodRecordId(1l);
        rn1.setPpid(ppid);
        mDb.getNotificationDao().insertReview(rn1);
        mIntentsTestRule.launchActivity(intent);

        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("1")));

        onView(withId(R.id.notification_list)).perform(click());
        String reviewDay = mIntentsTestRule.getActivity().getResources().getString(R.string.review_your_day);
        System.out.println(reviewDay + " " + rn1.getPpid());
        onView(withText(reviewDay + " " + rn1.getPpid())).perform(click());

        intended(allOf(
                hasComponent(RecordReviewActivity.class.getName()),
                hasExtra(PPID, rn1.getPpid())));

        //Click the back button without doing anything to the review
        onView(withId(R.id.btnBack)).perform(click());
        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("1")));
    }

    @Test
    public void clickNotificationThenDayCompleteChangesToSeenAndRemovesNumber() {
        Date today = new Date();
        ReviewNotification rn1 = new ReviewNotification();
        rn1.setSeen(false);
        rn1.setReviewNotificationId(1);
        rn1.setFoodRecordId(1l);
        rn1.setPpid(ppid);
        rn1.setDeliveryDate(today);
        mDb.getNotificationDao().insertReview(rn1);
        mIntentsTestRule.launchActivity(intent);

        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("1")));

        onView(withId(R.id.notification_list)).perform(click());
        String reviewDay = mIntentsTestRule.getActivity().getResources().getString(R.string.review_your_day);
        System.out.println(reviewDay + " " + rn1.getPpid());
        onView(withText(reviewDay + " " + rn1.getPpid())).perform(click());

        intended(allOf(
                hasComponent(RecordReviewActivity.class.getName()),
                hasExtra(PPID, rn1.getPpid())));

        //Click "Day Complete" to complete notification
        onView(withId(R.id.btnNo)).perform(click());

        //Click the back button
        onView(withId(R.id.btnBack)).perform(click());
        onView(withId(R.id.notification_dot)).check(matches(not(isDisplayed())));
    }

    @Test
    public void clickFirstNotificationDayCompleteChangesToSeenAndDecrementsNumber() {
        Date today = new Date();
        HouseholdMember hm2 = new HouseholdMember(2, "1", "HM1", "", 1, false, false);
        hm2.setParticipantHouseholdMemberId("ppid2");
        mDb.getHouseholdMemberDao().insert(hm2);
        ReviewNotification rn1 = new ReviewNotification();
        rn1.setSeen(false);
        rn1.setFoodRecordId(1L);
        rn1.setReviewNotificationId(1);
        rn1.setPpid(hm2.getParticipantHouseholdMemberId());
        rn1.setDeliveryDate(today);
        ReviewNotification rn2 = new ReviewNotification();
        rn2.setSeen(false);
        rn2.setReviewNotificationId(2);
        rn2.setFoodRecordId(2L);
        rn2.setPpid(ppid);
        rn2.setDeliveryDate(today);
        mDb.getNotificationDao().insertReview(rn1, rn2);
        mIntentsTestRule.launchActivity(intent);

        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("2")));

        onView(withId(R.id.notification_list)).perform(click());
        String text = mIntentsTestRule.getActivity().getResources().getString(R.string.review_your_day);
        onView(withText(text + " " + rn1.getPpid())).perform(click());

        intended(allOf(
                hasComponent(RecordReviewActivity.class.getName()),
                hasExtra(PPID, rn1.getPpid())));

        //Click Day Complete
        onView(withId(R.id.btnNo)).perform(click());

        //Click the back button
        onView(withId(R.id.btnBack)).perform(click());
        onView(withId(R.id.notification_dot)).check(matches(withText("1")));
    }

    @Test
    public void clickFirstNotificationNotDayCompleteLeavesUnseenAndNumber() {
        HouseholdMember hm2 = new HouseholdMember(2, "1", "HM1", "", 1, false, false);
        hm2.setParticipantHouseholdMemberId("ppid2");
        mDb.getHouseholdMemberDao().insert(hm2);
        ReviewNotification rn1 = new ReviewNotification();
        rn1.setSeen(false);
        rn1.setReviewNotificationId(1);
        rn1.setPpid(hm2.getParticipantHouseholdMemberId());
        rn1.setFoodRecordId(1L);
        ReviewNotification rn2 = new ReviewNotification();
        rn2.setSeen(false);
        rn2.setReviewNotificationId(2);
        rn2.setPpid(ppid);
        rn2.setFoodRecordId(2L);
        mDb.getNotificationDao().insertReview(rn1, rn2);
        mIntentsTestRule.launchActivity(intent);

        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("2")));

        onView(withId(R.id.notification_list)).perform(click());
        String text = mIntentsTestRule.getActivity().getResources().getString(R.string.review_your_day);
        onView(withText(text + " " + rn1.getPpid())).perform(click());

        intended(allOf(
                hasComponent(RecordReviewActivity.class.getName()),
                hasExtra(PPID, rn1.getPpid())));


        //Click the back button
        onView(withId(R.id.btnBack)).perform(click());
        onView(withId(R.id.notification_dot)).check(matches(withText("2")));
    }


    @Test
    public void notificationCounterOnlyCountsUnseenAndNonNullOrEmptyPpidNotifications() {
        ReviewNotification rn1 = new ReviewNotification();
        rn1.setSeen(false);
        rn1.setReviewNotificationId(1);
        rn1.setPpid(ppid);
        rn1.setFoodRecordId(1L);
        ReviewNotification rn2 = new ReviewNotification();
        rn2.setSeen(true);
        rn2.setReviewNotificationId(2);
        rn2.setFoodRecordId(2L);
        ReviewNotification rn3 = new ReviewNotification();
        rn3.setSeen(false);
        rn3.setReviewNotificationId(3);
        rn3.setPpid(null);
        rn3.setFoodRecordId(3L);
        ReviewNotification rn4 = new ReviewNotification();
        rn4.setSeen(false);
        rn4.setReviewNotificationId(4);
        rn4.setPpid("");
        rn4.setFoodRecordId(4L);
        mDb.getNotificationDao().insertReview(rn1, rn2, rn3, rn4);
        mIntentsTestRule.launchActivity(intent);

        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("1")));
    }

    @Test
    public void notificationsOnlyPresentWithin24HoursAllUnseen() {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        //Make a date >24hours ago
        cal.add(Calendar.HOUR_OF_DAY, -25);
        Date yesterday = cal.getTime();

        ReviewNotification rn1 = new ReviewNotification();
        rn1.setSeen(false);
        rn1.setReviewNotificationId(1);
        rn1.setIssueDate(yesterday);
        rn1.setPpid(ppid);
        rn1.setFoodRecordId(1L);
        ReviewNotification rn2 = new ReviewNotification();
        rn2.setSeen(false);
        rn2.setReviewNotificationId(2);
        rn2.setIssueDate(now);
        rn2.setPpid("ppid2");
        rn2.setFoodRecordId(2L);
        mDb.getNotificationDao().insertReview(rn1, rn2);

        mIntentsTestRule.launchActivity(intent);

        //Should see both
        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("2")));

        //Click the bell
        onView(withId(R.id.notification_list)).perform(click());
        //check that both notifications are there
        String text = mIntentsTestRule.getActivity().getResources().getString(R.string.review_your_day);
        onView(withText(text + " " + rn1.getPpid())).check(matches(isDisplayed()));
        onView(withText(text + " " + rn2.getPpid())).check(matches(isDisplayed()));
    }

    @Test
    //We want notificaitons to appear if:
    // - they are unseen
    // - they are issued the same day as current day.
    // If it is issued on a prior day and unseen it should still be displayed.
    public void unseenNotificationPresentIfLaterDayThanIssue() {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        //Make a date 2 days ago
        cal.add(Calendar.DATE, -2);
        Date yesterday = cal.getTime();

        //Create two notifications for 2 days ago, one seen one not.
        ReviewNotification rn1 = new ReviewNotification();
        rn1.setSeen(false);
        rn1.setReviewNotificationId(1);
        rn1.setIssueDate(yesterday);
        rn1.setPpid(ppid);
        ReviewNotification rn2 = new ReviewNotification();
        rn2.setSeen(true);
        rn2.setReviewNotificationId(2);
        rn2.setIssueDate(yesterday);
        rn2.setPpid("ppid2");
        mDb.getNotificationDao().insertReview(rn1, rn2);

        mIntentsTestRule.launchActivity(intent);

        //Should only see 1, the NOT seen notification.
        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        //Only one is unseen
        onView(withId(R.id.notification_dot)).check(matches(withText("1")));

        //Click the bell
        onView(withId(R.id.notification_list)).perform(click());
        //check that both notifications are there
        String text = mIntentsTestRule.getActivity().getResources().getString(R.string.review_your_day);
        onView(withText(text + " " + rn1.getPpid())).check(matches(isDisplayed()));
        onView(withText(text + " " + rn2.getPpid())).check(doesNotExist());
    }

    @Test
    public void notificationsOnlyPresentOnSameDayAsIssueWhenAllSeen() {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        //Make a date >24hours ago
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();

        //Create 2 notifications, BOTH SEEN. One from yesterday, the other today
        ReviewNotification rn1 = new ReviewNotification();
        rn1.setSeen(true);
        rn1.setReviewNotificationId(1);
        rn1.setIssueDate(yesterday);
        rn1.setDeliveryDate(yesterday);
        rn1.setPpid(ppid);
        ReviewNotification rn2 = new ReviewNotification();
        rn2.setSeen(true);
        rn2.setReviewNotificationId(2);
        rn2.setIssueDate(now);
        rn2.setDeliveryDate(now);
        rn2.setPpid("ppid2");
        mDb.getNotificationDao().insertReview(rn1, rn2);

        mIntentsTestRule.launchActivity(intent);

        //Since it has been seen not dot is present
        onView(withId(R.id.notification_dot)).check(matches(not(isDisplayed())));

        //Click the bell
        onView(withId(R.id.notification_list)).perform(click());
        //check that only the new notification is there.
        String text = mIntentsTestRule.getActivity().getResources().getString(R.string.review_your_day);
        onView(withText(text + " " + rn1.getPpid())).check(doesNotExist());
        onView(withText(text + " " + rn2.getPpid())).check(matches(isDisplayed()));
    }

    @Test
    public void notificationsONlyVisibleIfUnseenOrSameDayAsDelivery() {
        /*
        4 cases:
            Seen Issued Today = Visible
            Seen Issued Yesterday = Invisible
            Unseend Issued Today = Visible, Red Dot shows +1
            Unseen Issued Yesterday = Visible, Red Dot shows +1
         */
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        //Make a date yesterday
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();

        //Create the 4 ReviewNotifications
        ReviewNotification seenToday = new ReviewNotification();
        seenToday.setReviewNotificationId(1L);
        seenToday.setSeen(true);
        seenToday.setDeliveryDate(today);
        seenToday.setPpid(ppid);
        seenToday.setFoodRecordId(1L);
        ReviewNotification seenYesterday = new ReviewNotification();
        seenYesterday.setReviewNotificationId(2L);
        seenYesterday.setSeen(true);
        seenYesterday.setDeliveryDate(yesterday);
        seenYesterday.setPpid("ppid2");
        seenYesterday.setFoodRecordId(2L);
        ReviewNotification unseenToday = new ReviewNotification();
        unseenToday.setReviewNotificationId(3L);
        unseenToday.setSeen(false);
        unseenToday.setDeliveryDate(today);
        unseenToday.setPpid("ppid3");
        unseenToday.setFoodRecordId(3L);
        ReviewNotification unseenYesterday = new ReviewNotification();
        unseenYesterday.setReviewNotificationId(4L);
        unseenYesterday.setSeen(false);
        unseenYesterday.setDeliveryDate(yesterday);
        unseenYesterday.setPpid("ppid4");
        unseenYesterday.setFoodRecordId(4L);
        mDb.getNotificationDao().insertReview(seenToday, seenYesterday, unseenToday, unseenYesterday);

        //Start the activity
        mIntentsTestRule.launchActivity(intent);

        //There are two unseen notifications so red dot should be displayed with "2" in it
        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("2")));

        //Click the bell to bring up the menu
        onView(withId(R.id.notification_list)).perform(click());

        //Should see the two unseen notification.
        String text = mIntentsTestRule.getActivity().getResources().getString(R.string.review_your_day);
        onView(withText(text + " " + unseenToday.getPpid())).check(matches(isDisplayed()));
        onView(withText(text + " " + unseenYesterday.getPpid())).check(matches(isDisplayed()));

        //Should also see the seen notification for TODAY
        onView(withText(text + " " + seenToday.getPpid())).check(matches(isDisplayed()));

        //Should NOT see seen from yesterday
        onView(withText(text + " " + seenYesterday.getPpid())).check(doesNotExist());
    }

    @Test
    public void notificationWithNullPpidDoesNotAppear() {
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        //Make a date yesterday
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();

        //Create the ReviewNotification
        ReviewNotification unseenYesterday = new ReviewNotification();
        unseenYesterday.setReviewNotificationId(-ppid.hashCode());
        unseenYesterday.setSeen(false);
        unseenYesterday.setDeliveryDate(yesterday);
        unseenYesterday.setPpid(null);
        mDb.getNotificationDao().insertReview(unseenYesterday);

        //Start the activity
        mIntentsTestRule.launchActivity(intent);

        //There are two unseen notifications so red dot should be displayed with "2" in it
        onView(withId(R.id.notification_dot)).check(matches(not(isDisplayed())));

        //Click the bell to bring up the menu
        onView(withId(R.id.notification_list)).perform(click());

        //Should see the two unseen notification.
        String text = mIntentsTestRule.getActivity().getResources().getString(R.string.review_your_day);
        onView(withText(text)).check(doesNotExist());
    }


    @Test
    public void notificationOfForgottenRecordingDayBecomesSeenWhenClickedAndDayCompleted(){
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        //Make a date yesterday
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();

        //Create the ReviewNotification
        ReviewNotification unseenYesterday = new ReviewNotification();
        unseenYesterday.setReviewNotificationId(-ppid.hashCode());
        unseenYesterday.setFoodRecordId(1L);
        unseenYesterday.setSeen(false);
        unseenYesterday.setIssueDate(yesterday);
        unseenYesterday.setDeliveryDate(yesterday);
        unseenYesterday.setPpid(ppid);
        mDb.getNotificationDao().insertReview(unseenYesterday);

        //Start the activity
        mIntentsTestRule.launchActivity(intent);

        //There are two unseen notifications so red dot should be displayed with "2" in it
        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("1")));

        //Click the bell to bring up the menu
        onView(withId(R.id.notification_list)).perform(click());

        //Should see the two unseen notification.
        String text = mIntentsTestRule.getActivity().getResources().getString(R.string.review_your_day);
        onView(withText(text + " " + unseenYesterday.getPpid())).check(matches(isDisplayed()));

        //Click the notification
        onView(withText(text + " " + unseenYesterday.getPpid())).perform(click());

        //Click Day Complete
        onView(withId(R.id.btnNo)).perform(click());

        //Check the 1 is gone
        onView(withId(R.id.notification_dot)).check(matches(not(isDisplayed())));
    }

    @Test
    public void notificationOfForgottenRecordingDayBecomesSeenWhenClickedAndDayCompletedMultipleOldNotifications(){
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        //Make a date yesterday
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();
        cal.add(Calendar.DATE, -1);
        Date twoDaysAgo = cal.getTime();

        //Create the ReviewNotification
        ReviewNotification unseenYesterday = new ReviewNotification();
        unseenYesterday.setReviewNotificationId(ppid.hashCode());
        unseenYesterday.setFoodRecordId(1L);
        unseenYesterday.setSeen(false);
        unseenYesterday.setIssueDate(twoDaysAgo);
        unseenYesterday.setDeliveryDate(yesterday);
        unseenYesterday.setPpid(ppid);
        mDb.getNotificationDao().insertReview(unseenYesterday);
        ReviewNotification unseenTwoDaysAgo = new ReviewNotification();
        unseenTwoDaysAgo.setReviewNotificationId(ppid.hashCode());
        unseenTwoDaysAgo.setFoodRecordId(2L);
        unseenTwoDaysAgo.setSeen(false);
        unseenTwoDaysAgo.setIssueDate(twoDaysAgo);
        unseenTwoDaysAgo.setDeliveryDate(twoDaysAgo);
        unseenTwoDaysAgo.setPpid(ppid);
        mDb.getNotificationDao().insertReview(unseenYesterday, unseenTwoDaysAgo);

        //Start the activity
        mIntentsTestRule.launchActivity(intent);

        //There are two unseen notifications
        //But since we only allow notificaitons to be clicked
        //for 24 hours it should still be displayed with "1" in it
        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("1")));

        //Click the bell to bring up the menu
        onView(withId(R.id.notification_list)).perform(click());

        //Should see the two unseen notification.
        String text = mIntentsTestRule.getActivity().getResources().getString(R.string.review_your_day);
        onView(withText(text + " " + unseenYesterday.getPpid())).check(matches(isDisplayed()));

        //Click the notification
        onView(withText(text + " " + unseenYesterday.getPpid())).perform(click());

        //Click Day Complete
        onView(withId(R.id.btnNo)).perform(click());

        //Check the 2 is now a 1
        onView(withId(R.id.notification_dot)).check(matches(not(isDisplayed())));
    }

    @Test
    public void notificationOfForgottenRecordingDayRemainsIfDayNotComplete(){
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        //Make a date yesterday
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();

        //Create the ReviewNotification
        ReviewNotification unseenYesterday = new ReviewNotification();
        unseenYesterday.setReviewNotificationId(-ppid.hashCode());
        unseenYesterday.setSeen(false);
        unseenYesterday.setIssueDate(yesterday);
        unseenYesterday.setDeliveryDate(yesterday);
        unseenYesterday.setPpid(ppid);
        mDb.getNotificationDao().insertReview(unseenYesterday);

        //Start the activity
        mIntentsTestRule.launchActivity(intent);

        //There are two unseen notifications so red dot should be displayed with "2" in it
        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("1")));

        //Click the bell to bring up the menu
        onView(withId(R.id.notification_list)).perform(click());

        //Should see the two unseen notification.
        String text = mIntentsTestRule.getActivity().getResources().getString(R.string.review_your_day);
        onView(withText(text + " " + unseenYesterday.getPpid())).check(matches(isDisplayed()));

        //Click the notification
        onView(withText(text + " " + unseenYesterday.getPpid())).perform(click());

        //Return home
        onView(withId(R.id.btnHome)).perform(click());

        //Check the 1 is still there
        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("1")));
    }

    @Test
    public void notificationOfForgottenRecordingDayBecomesSeenWhenClickedAndDayCompleteAndWorksWhenClickedAgain(){
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        Date yesterday = cal.getTime();

        //Create the ReviewNotification
        ReviewNotification unseenToday = new ReviewNotification();
        unseenToday.setReviewNotificationId(-ppid.hashCode());
        unseenToday.setFoodRecordId(1L);
        unseenToday.setSeen(false);
        unseenToday.setIssueDate(yesterday);
        unseenToday.setPpid(ppid);
        unseenToday.setDeliveryDate(yesterday);
        mDb.getNotificationDao().insertReview(unseenToday);

        //Start the activity
        mIntentsTestRule.launchActivity(intent);

        //There are two unseen notifications so red dot should be displayed with "2" in it
        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("1")));

        //Click the bell to bring up the menu
        onView(withId(R.id.notification_list)).perform(click());

        NotificationRepository nRepo = new NotificationRepository(mIntentsTestRule.getActivity().getApplication());
        List<ReviewNotification> rns = nRepo.getReviewNotifications();
        assertThat(rns.size(), is(1));
        ReviewNotification rn = rns.get(0);
        //assertThat(rn.getRecordId(), is(lessThan(0L)));

        //Should see the two unseen notification.
        String text = mIntentsTestRule.getActivity().getResources().getString(R.string.review_your_day);
        onView(withText(text + " " + unseenToday.getPpid())).check(matches(isDisplayed()));

        //Click the notification
        onView(withText(text + " " + unseenToday.getPpid())).perform(click());

        //Click Day Complete
        onView(withId(R.id.btnNo)).perform(click());

        //Check the ReviewNotifications id is now positive
        rns = nRepo.getReviewNotifications();
        assertThat(rns.size(), is(1));
        rn = rns.get(0);
        //assertThat(rn.getRecordId(), is(1L));

        //Check the 1 is gone
        onView(withId(R.id.notification_dot)).check(matches(not(isDisplayed())));

        //Click the bell to bring up the menu
        onView(withId(R.id.notification_list)).perform(click());
        //Click the notification
        onView(withText(text + " " + unseenToday.getPpid())).perform(click());
        //Check we are on the
        intended(hasComponent(RecordReviewActivity.class.getName()), times(2));
    }

    @Test
    public void notificationOfForgottenRecordingDayBRemainsUnseenWhenClickedNotDayCompleteAndWorksWhenClickedAgain(){
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        Date yesterday = cal.getTime();

        //Create the ReviewNotification
        ReviewNotification unseenToday = new ReviewNotification();
        unseenToday.setReviewNotificationId(-ppid.hashCode());
        unseenToday.setSeen(false);
        unseenToday.setIssueDate(yesterday);
        unseenToday.setDeliveryDate(yesterday);
        unseenToday.setPpid(ppid);
        mDb.getNotificationDao().insertReview(unseenToday);

        //Start the activity
        mIntentsTestRule.launchActivity(intent);

        //There are two unseen notifications so red dot should be displayed with "2" in it
        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("1")));

        //Click the bell to bring up the menu
        onView(withId(R.id.notification_list)).perform(click());

        NotificationRepository nRepo = new NotificationRepository(mIntentsTestRule.getActivity().getApplication());
        List<ReviewNotification> rns = nRepo.getReviewNotifications();
        assertThat(rns.size(), is(1));
        ReviewNotification rn = rns.get(0);
        //assertThat(rn.getRecordId(), is(lessThan(0L)));

        //Should see the two unseen notification.
        String text = mIntentsTestRule.getActivity().getResources().getString(R.string.review_your_day);
        onView(withText(text + " " + unseenToday.getPpid())).check(matches(isDisplayed()));

        //Click the notification
        onView(withText(text + " " + unseenToday.getPpid())).perform(click());

        //Return home
        onView(withId(R.id.btnHome)).perform(click());

        //Check the ReviewNotifications id is now positive
        rns = nRepo.getReviewNotifications();
        assertThat(rns.size(), is(1));
        rn = rns.get(0);
        //assertThat(rn.getRecordId(), is(1L));

        //Check the 1 is still there
        onView(withId(R.id.notification_dot)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_dot)).check(matches(withText("1")));

        //Click the bell to bring up the menu
        onView(withId(R.id.notification_list)).perform(click());
        //Click the notification
        onView(withText(text + " " + unseenToday.getPpid())).perform(click());
        //Check we are on the
        intended(hasComponent(RecordReviewActivity.class.getName()), times(2));
    }
}
