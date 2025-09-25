package au.edu.newcastle.jnc985.visida.activity;

import android.app.Activity;
import android.app.Instrumentation;
import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.RecyclerViewMatcher;
import bo.State;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.GuestInformation;
import bo.db.entity.HouseholdMember;
import bo.db.entity.Meal;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static bo.AppConstants.AUDIOFILE_NAME;
import static bo.AppConstants.EOID;
import static bo.AppConstants.FR;
import static bo.AppConstants.IMAGE_NAME;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.SETUP;
import static bo.AppConstants.STATE;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by jnc985 on 30-Nov-17.
 */


@RunWith(MockitoJUnitRunner.class)
public class FinalizeFoodItemSharedActivityTest {
    private Context mContext;

    private AppDatabase mDb;

    private HouseholdMember hm;
    private HouseholdMember hm2;
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
        hm = new HouseholdMember();
        hm.setUid(mHmId);
        hm.setParticipantHouseholdMemberId("ppid");
        hm2 = new HouseholdMember();
        hm2.setUid(mHmId + 1);
        hm2.setParticipantHouseholdMemberId("ppid2");
        mDb.getHouseholdMemberDao().insert(hm, hm2);

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

        SharedPreferences sharedPref = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(STATE, State.FINALIZE.ordinal());
        editor.putBoolean(SETUP, true);
        editor.commit();
    }

    private void launchActivity() {
        intent = new Intent(mContext, FinalizeFoodItemActivity.class);
        intent.putExtra(EOID, mEOidUnFinalized);
        mIntentsRule.launchActivity(intent);
    }


    @Test
    public void finalizingSharedDishForFirstTimeAsksForLeftovers() {
        setUpData();

        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId((long) 3);
        eo.setFinalized(false);
        eo.setFoodRecordId(mFrId);
        mDb.getEatingOccasionDao().insert(eo);

        //Add a fooditem for a shared dish
        FoodItem sharedFi = new FoodItem();
        sharedFi.setEatingOccasionId(3);
        sharedFi.setMealId(1);
        sharedFi.setImageUrl("");

        mDb.getFoodItemDao().insert(sharedFi);

        intent = new Intent(mContext, FinalizeFoodItemActivity.class);
        intent.putExtra(EOID, (long) 3);
        mIntentsRule.launchActivity(intent);

        onView(withText(R.string.leftover_prompt)).check(matches(isDisplayed()));
    }

    @Test
    public void finalizingSharedFor2ndTimeDoesNotAskForLeftovers() {
        setUpData();

        Meal m = new Meal();
        m.setMealId((long) 1);
        mDb.getMealDao().insert(m);

        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId((long) 3);
        eo.setFinalized(false);
        eo.setFoodRecordId(mFrId);
        mDb.getEatingOccasionDao().insert(eo);

        //Add a fooditem for a shared dish
        FoodItem sharedFi = new FoodItem();
        sharedFi.setEatingOccasionId(3);
        sharedFi.setMealId(m.getMealId());
        sharedFi.setImageUrl("");
        sharedFi.setLeftoverImageUrl("LEFTOVERIMAGEPLACEHOLDER");

        mDb.getFoodItemDao().insert(sharedFi);

        intent = new Intent(mContext, FinalizeFoodItemActivity.class);
        intent.putExtra(EOID, (long) 3);
        mIntentsRule.launchActivity(intent);

        onView(withText(R.string.tick)).perform(click());

        onView(withText(R.string.leftover_prompt)).check(doesNotExist());
        onView(withId(R.id.imgFoodItem)).check(matches(isDisplayed()));
    }

    @Test
    public void clickYesForSharedLeftoversTakesToCameraAndAudioActivity() {
        setUpData();

        long mealId = 1;
        Meal meal = new Meal();
        meal.setMealId(mealId);
        meal.setGuestInfoCaptured(false);
        mDb.getMealDao().insert(meal);

        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId((long) 3);
        eo.setFinalized(false);
        eo.setFoodRecordId(mFrId);
        mDb.getEatingOccasionDao().insert(eo);

        //Add a fooditem for a shared dish
        FoodItem sharedFi = new FoodItem();
        sharedFi.setEatingOccasionId(3);
        sharedFi.setMealId(mealId);
        sharedFi.setImageUrl("");

        mDb.getFoodItemDao().insert(sharedFi);

        intent = new Intent(mContext, FinalizeFoodItemActivity.class);
        intent.putExtra(EOID, (long) 3);
        mIntentsRule.launchActivity(intent);

        Intent intent = new Intent();
        intent.putExtra(IMAGE_NAME, "NO_IMAGE");
        intent.putExtra(AUDIOFILE_NAME, "NO_AUDIO_FILE");
        Instrumentation.ActivityResult intentResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, intent);

        intending(hasComponent(CameraActivity.class.getName())).respondWith(intentResult);
        intending(hasComponent(AudioActivity.class.getName())).respondWith(intentResult);

        onView(withText(R.string.leftover_prompt)).check(matches(isDisplayed()));
        onView(withId(R.id.btnConfirm)).perform(click());

        intended(hasComponent(CameraActivity.class.getName()));
        intended(hasComponent(AudioActivity.class.getName()));
    }

    @Test
    public void finalisingAsksForGuestsForAllFoodItemsOnlyForFirstHm(){
        setUpData();

        long mealId = 1;
        Meal meal = new Meal();
        meal.setMealId(mealId);
        meal.setGuestInfoCaptured(false);
        mDb.getMealDao().insert(meal);

        //ADd food record for hm2
        FoodRecord fr2 = new FoodRecord(hm2.getUid());
        fr2.setFoodRecordId(2L);
        mDb.getFoodRecordDao().insert(fr2);

        //Add an eating occasion for both hm1 and hm2 for the shared dishes
        EatingOccasion eoHm1 = new EatingOccasion();
        eoHm1.setEatingOccasionId((long) 3);
        eoHm1.setFinalized(false);
        eoHm1.setFoodRecordId(mFrId);
        mDb.getEatingOccasionDao().insert(eoHm1);

        EatingOccasion eoHm2 = new EatingOccasion();
        eoHm2.setEatingOccasionId((long) 4);
        eoHm2.setFinalized(false);
        eoHm2.setFoodRecordId(fr2.getFoodRecordId());
        mDb.getEatingOccasionDao().insert(eoHm2);

        //Add a fooditem for a shared dish
        FoodItem sharedFi = new FoodItem();
        sharedFi.setFoodItemId(10L);
        sharedFi.setBaseFoodItemId(1L);
        sharedFi.setEatingOccasionId(eoHm1.getEatingOccasionId());
        sharedFi.setMealId(mealId);
        sharedFi.setImageUrl("");

        FoodItem sharedFi2 = new FoodItem();
        sharedFi2.setFoodItemId(11L);
        sharedFi2.setBaseFoodItemId(1L);
        sharedFi2.setEatingOccasionId(eoHm2.getEatingOccasionId());
        sharedFi2.setMealId(mealId);
        sharedFi2.setImageUrl("");

        FoodItem sharedFi3 = new FoodItem();
        sharedFi3.setFoodItemId(12L);
        sharedFi3.setBaseFoodItemId(2L);
        sharedFi3.setEatingOccasionId(eoHm1.getEatingOccasionId());
        sharedFi3.setMealId(mealId);
        sharedFi3.setImageUrl("");

        FoodItem sharedFi4 = new FoodItem();
        sharedFi4.setFoodItemId(13L);
        sharedFi4.setBaseFoodItemId(2L);
        sharedFi4.setEatingOccasionId(eoHm2.getEatingOccasionId());
        sharedFi4.setMealId(mealId);
        sharedFi4.setImageUrl("");

        mDb.getFoodItemDao().insert(sharedFi, sharedFi2, sharedFi3, sharedFi4);

        //Launch the intent with Eating occasion for hm1 with shared dishes
        intent = new Intent(mContext, FinalizeFoodItemActivity.class);
        intent.putExtra(EOID, (long) eoHm1.getEatingOccasionId());
        mIntentsRule.launchActivity(intent);


        onView(withText(R.string.leftover_prompt)).check(matches(isDisplayed()));
        onView(withText(R.string.no_leftovers)).perform(click());

        //Check the guest dialog is visible
        onView(withId(R.id.rvCheckBoxList)).check(matches(isDisplayed()));

        //Add one of each guest
        onView(new RecyclerViewMatcher(R.id.rvCheckBoxList).atPositionOnView(0,R.id.btnPlus)).perform(click());
        onView(new RecyclerViewMatcher(R.id.rvCheckBoxList).atPositionOnView(1,R.id.btnPlus)).perform(click());
        onView(new RecyclerViewMatcher(R.id.rvCheckBoxList).atPositionOnView(2,R.id.btnPlus)).perform(click());

        //Submit guests
        onView(withText(R.string.tick)).perform(click());

        //Check the guest information has been added to the database
        List<GuestInformation> gis = mDb.getGuestInfoDao().all();
        assertThat(gis.size(), is(1));
        FoodItem fi1AfterGuest = mDb.getFoodItemDao().getFoodItem(sharedFi.getFoodItemId()).get(0);
        assertThat(fi1AfterGuest.getGuestInfoId(), is(gis.get(0).getGeustInfoId()));

        //Check that all food items with same base id have the guest info added to them
        FoodItem fi1FromOtherHm = mDb.getFoodItemDao().getFoodItem(sharedFi2.getFoodItemId()).get(0);
        assertThat(fi1FromOtherHm.getGuestInfoId(), is(fi1AfterGuest.getGuestInfoId()));

        //Click ate all to move to the next food item
        onView(withId(R.id.btnAteAll)).perform(click());

        //Check the Guest is asking for the next food item
        //Check the guest dialog is visible
        onView(withId(R.id.rvCheckBoxList)).check(matches(isDisplayed()));

        //Add one of each guest
        onView(new RecyclerViewMatcher(R.id.rvCheckBoxList).atPositionOnView(0,R.id.btnPlus)).perform(click());
        onView(new RecyclerViewMatcher(R.id.rvCheckBoxList).atPositionOnView(1,R.id.btnPlus)).perform(click());
        onView(new RecyclerViewMatcher(R.id.rvCheckBoxList).atPositionOnView(2,R.id.btnPlus)).perform(click());
        //Submit guests
        onView(withText(R.string.tick)).perform(click());

        //Ate all
        onView(withId(R.id.btnAteAll)).perform(click());

        //Click the second household member
        onView(withId(R.id.listViewHouseholdMembers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

        //Click the eating occasion to finalise
        onView(new RecyclerViewMatcher(R.id.rvEatingOccasions).atPositionOnView(0,R.id.btnFinalizeItem)).perform(click());

        //Check guest pop up does not appear
        onView(withId(R.id.rvCheckBoxList)).check(doesNotExist());
    }

    @Test
    public void finalizingSharedDishAsksforGuestInfoIconIsPresent() {
        setUpData();

        long mealId = 1;
        Meal meal = new Meal();
        meal.setMealId(mealId);
        meal.setGuestInfoCaptured(false);
        mDb.getMealDao().insert(meal);

        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId((long) 3);
        eo.setFinalized(false);
        eo.setFoodRecordId(mFrId);
        mDb.getEatingOccasionDao().insert(eo);

        //Add a fooditem for a shared dish
        FoodItem sharedFi = new FoodItem();
        sharedFi.setEatingOccasionId(3);
        sharedFi.setMealId(mealId);
        sharedFi.setImageUrl("");

        mDb.getFoodItemDao().insert(sharedFi);

        intent = new Intent(mContext, FinalizeFoodItemActivity.class);
        intent.putExtra(EOID, (long) 3);
        mIntentsRule.launchActivity(intent);

        Intent intent = new Intent();
        intent.putExtra(IMAGE_NAME, "NO_IMAGE");
        intent.putExtra(AUDIOFILE_NAME, "NO_AUDIO_FILE");
        Instrumentation.ActivityResult intentResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, intent);

        intending(hasComponent(CameraActivity.class.getName())).respondWith(intentResult);
        intending(hasComponent(AudioActivity.class.getName())).respondWith(intentResult);

        onView(withText(R.string.leftover_prompt)).check(matches(isDisplayed()));
        onView(withId(R.id.btnConfirm)).perform(click());

        intended(hasComponent(CameraActivity.class.getName()));
        intended(hasComponent(AudioActivity.class.getName()));

        //Check the guest dialog is visible
        onView(withId(R.id.rvCheckBoxList)).check(matches(isDisplayed()));

        //Check the icon is visible
        onView(withId(R.id.imgSharedIcon)).check(matches(isDisplayed()));
    }


    @Test
    public void clickingBackOnAudioACtivityWhenFinalizingSharedDishStillAsksForLeftovers(){
        setUpData();

        long mealId = 1;
        Meal meal = new Meal();
        meal.setMealId(mealId);
        meal.setGuestInfoCaptured(false);
        mDb.getMealDao().insert(meal);

        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId((long) 3);
        eo.setFinalized(false);
        eo.setFoodRecordId(mFrId);
        mDb.getEatingOccasionDao().insert(eo);

        //Add a fooditem for a shared dish
        FoodItem sharedFi = new FoodItem();
        sharedFi.setEatingOccasionId(3);
        sharedFi.setMealId(mealId);
        sharedFi.setImageUrl("");

        mDb.getFoodItemDao().insert(sharedFi);

        intent = new Intent(mContext, FinalizeFoodItemActivity.class);
        intent.putExtra(EOID, (long) 3);
        mIntentsRule.launchActivity(intent);

        onView(withText(R.string.leftover_prompt)).check(matches(isDisplayed()));
        onView(withId(R.id.btnConfirm)).perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        intended(hasComponent(CameraActivity.class.getName()));
        onView(withId(R.id.btnTakePicture)).perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.btnImageAccept)).perform(click());

        intended(hasComponent(AudioActivity.class.getName()));
        //Start recording
        onView(withId(R.id.btnRecordAudio)).perform(click());
        //Stop recording
        onView(withId(R.id.btnAudioFile)).perform(click());

        //Click the back button
        onView(withId(R.id.btnBack)).perform(click());

        //Check the leftover prompt is visible
        onView(withId(R.id.dialog_imageview)).check(matches(isDisplayed()));
    }

    @Test
    public void clickingBackOnCameraActivityWhenFinalizingSharedDishStillAsksForLeftovers(){
        setUpData();

        long mealId = 1;
        Meal meal = new Meal();
        meal.setMealId(mealId);
        meal.setGuestInfoCaptured(false);
        mDb.getMealDao().insert(meal);

        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId((long) 3);
        eo.setFinalized(false);
        eo.setFoodRecordId(mFrId);
        mDb.getEatingOccasionDao().insert(eo);

        //Add a fooditem for a shared dish
        FoodItem sharedFi = new FoodItem();
        sharedFi.setEatingOccasionId(3);
        sharedFi.setMealId(mealId);
        sharedFi.setImageUrl("");

        mDb.getFoodItemDao().insert(sharedFi);

        intent = new Intent(mContext, FinalizeFoodItemActivity.class);
        intent.putExtra(EOID, (long) 3);
        mIntentsRule.launchActivity(intent);

        onView(withText(R.string.leftover_prompt)).check(matches(isDisplayed()));
        onView(withId(R.id.btnConfirm)).perform(click());

        intended(hasComponent(CameraActivity.class.getName()));

        //Click the back button
        onView(withId(R.id.btnBack)).perform(click());

        //Check the leftover prompt is visible
        onView(withId(R.id.dialog_imageview)).check(matches(isDisplayed()));
    }

    @Test
    public void noImageButtonNotAvailableWhenFinalizeingSharedDishesSavesPlaceholderImageName(){
        setUpData();

        long mealId = 1;
        Meal meal = new Meal();
        meal.setMealId(mealId);
        meal.setGuestInfoCaptured(false);
        mDb.getMealDao().insert(meal);

        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId((long) 3);
        eo.setFinalized(false);
        eo.setFoodRecordId(mFrId);
        mDb.getEatingOccasionDao().insert(eo);

        //Add a fooditem for a shared dish
        FoodItem sharedFi = new FoodItem();
        sharedFi.setEatingOccasionId(3);
        sharedFi.setMealId(mealId);
        sharedFi.setImageUrl("");

        sharedFi.setFoodItemId(mDb.getFoodItemDao().insert(sharedFi));

        intent = new Intent(mContext, FinalizeFoodItemActivity.class);
        intent.putExtra(EOID, (long) 3);
        mIntentsRule.launchActivity(intent);

        onView(withText(R.string.leftover_prompt)).check(matches(isDisplayed()));
        onView(withId(R.id.btnConfirm)).perform(click());

        intended(hasComponent(CameraActivity.class.getName()));
        //Check the no image button is present
        onView(withId(R.id.btnNoPhoto)).check(matches(not(isDisplayed())));
    }

    @Test
    public void clickNoLeftoversDismissesPopup(){
        setUpData();

        long mealId = 1;
        Meal meal = new Meal();
        meal.setMealId(mealId);
        meal.setGuestInfoCaptured(false);
        mDb.getMealDao().insert(meal);

        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId((long) 3);
        eo.setFinalized(false);
        eo.setFoodRecordId(mFrId);
        mDb.getEatingOccasionDao().insert(eo);

        //Add a fooditem for a shared dish
        FoodItem sharedFi = new FoodItem();
        sharedFi.setEatingOccasionId(3);
        sharedFi.setMealId(mealId);
        sharedFi.setImageUrl("");

        mDb.getFoodItemDao().insert(sharedFi);

        intent = new Intent(mContext, FinalizeFoodItemActivity.class);
        intent.putExtra(EOID, (long) 3);
        mIntentsRule.launchActivity(intent);

        Intent intent = new Intent();
        intent.putExtra(IMAGE_NAME, "NO_IMAGE");
        intent.putExtra(AUDIOFILE_NAME, "NO_AUDIO_FILE");
        Instrumentation.ActivityResult intentResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, intent);


        onView(withText(R.string.leftover_prompt)).check(matches(isDisplayed()));
        onView(withId(R.id.btnCancel)).perform(click());

       onView(withText(R.string.leftover_prompt)).check(doesNotExist());
    }

    //        //Close the app
//        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
//        try {
//            device.pressRecentApps();
//            Thread.sleep(2000);
//        } catch (RemoteException | InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        String appName = mIntentsRule.getActivity().getResources().getString(R.string.app_name);
//        //String finalise = mIntentsRule.getActivity().getResources().getString(R.string.finalize);
//        //UiObject2 appCard = device.findObject(By.textContains(finalise));
//        //UiObject2 appCard = new UiObject2(new UiSelector().resourceId(R.id.))
//        //appCard.swipe(Direction.LEFT, 1.0f);
//        int height = device.getDisplayHeight();
//        int width = device.getDisplayWidth();
//        device.swipe(width/2,height/2, width/2, height, 50);
//        UiObject2 clearAllButton = device.findObject(By.textContains("CLEAR ALL"));
//        clearAllButton.click();
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        //Reopen the app
//        device.pressHome();
//        UiObject2 appIcon = device.findObject(By.textContains(appName));
//         appIcon.click();
//
//        //Allow the system time to open the app.
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        //Go to finalise the meal
//        onView(withId(R.id.btnFinalizeEat)).perform(click());
//        //Clcik first household member
//        try {
//            onView(withId(R.id.listViewHouseholdMembers))
//                    .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
//        }
//        catch(NoMatchingViewException ex){}
//        onView(new RecyclerViewMatcher(R.id.rvEatingOccasions).atPositionOnView(1, R.id.btnFinalizeItem)).perform(click());
//
//        //Check dialog is still displayed
//        onView(withId(R.id.rvCheckBoxList)).check(matches(isDisplayed()));
}

