package au.edu.newcastle.jnc985.visida.e2e;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.MediaStore;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;
import android.widget.DatePicker;
import android.widget.TimePicker;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.Date;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.RecyclerViewMatcher;
import au.edu.newcastle.jnc985.visida.activity.MainActivity;
import au.edu.newcastle.jnc985.visida.activity.RecordReviewActivity;
import bo.db.AppDatabase;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static bo.AppConstants.FRID;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.SETUP;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by jnc985 on 09-Jan-18.
 */

public class TestGenerateLogFile {

    @Rule
    public IntentsTestRule<MainActivity> mTestRule = new IntentsTestRule<>(MainActivity.class);
    @Rule
    public IntentsTestRule<RecordReviewActivity> mRecordReviewRule = new IntentsTestRule<>(RecordReviewActivity.class, false, false);
    @Rule
    public GrantPermissionRule mPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);


    private AppDatabase mDb;
    private Context context;

    //Create a date object for today
    private Date today = new Date();

    private File mMediaDirectory;
    private String MEDIA_DIR = "MEDIA";
    private String HOUSEHOLDID = "hhid";
    private String PARTICIPANTID1 = "ppid1";
    private String PARTICIPANTID2 = "ppid2";
    private String PARTICIPANTID3 = "ppid3";

    @Before
    public void createDb() {
        SharedPreferences sharedPreferences = mTestRule.getActivity().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putBoolean(SETUP, false);
        editor.commit();
        context = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();

        AppDatabase.setInstance(mDb);
    }

    @Before
    public void setUpMediaDirectory() {
//        mMediaDirectory = Utilities.getMediaDirectory(context);
//        mMediaDirectory.mkdirs();
//        //Make sure directory is empty
//        for (File f : mMediaDirectory.listFiles()) {
//            if (f.exists()) {
//                f.delete();
//            }
//        }
    }

    @After
    public void closeDb() {
        mDb.close();
    }

    @After
    public void deleteMediaDirectory() {
//        for (File f : mMediaDirectory.listFiles()) {
//            if (f.exists()) {
//                f.delete();
//            }
//        }
    }

    @Test
    @LargeTest
    public void testRunThroughEachProcessToGenerateLogFile() throws InterruptedException {
        //Create stub intent result for Profile picture
        Intent resultData = new Intent();
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(IntentMatchers.hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result);


        Thread.sleep(3000);
        //Create three household members
        onView(withId(R.id.fab)).perform(click());

        //Click to take image
        onView(withId(R.id.imgViewAvatar)).perform(click());
        //Enter hhid, ppid, hm name, age gender
        onView(withId(R.id.txtHouseholdId)).perform(scrollTo(), typeText(HOUSEHOLDID), closeSoftKeyboard());
        onView(withId(R.id.txtParticipantId)).perform(scrollTo(), typeText(PARTICIPANTID1), closeSoftKeyboard());
        onView(withId(R.id.txtName)).perform(scrollTo(), typeText("hm1"), closeSoftKeyboard());
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("25"), closeSoftKeyboard());
        onView(withId(R.id.btnSaveHouseholdMember)).perform(click());

        //Click FAB Again for hm2
        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.imgViewAvatar)).perform(click());
        onView(withId(R.id.txtParticipantId)).perform(scrollTo(), typeText(PARTICIPANTID2), closeSoftKeyboard());
        onView(withId(R.id.txtName)).perform(scrollTo(), typeText("hm2"), closeSoftKeyboard());
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("2"), closeSoftKeyboard());
        //Is Breastfed to bring up the button
        onView(withId(R.id.chkIsBreastfed)).perform(scrollTo(), click());
        onView(withId(R.id.btnSaveHouseholdMember)).perform(click());

        //Click FAB Again for hm3
        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.imgViewAvatar)).perform(click());
        onView(withId(R.id.txtParticipantId)).perform(scrollTo(), typeText(PARTICIPANTID3), closeSoftKeyboard());
        onView(withId(R.id.txtName)).perform(scrollTo(), typeText("hm3"), closeSoftKeyboard());
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("23"), closeSoftKeyboard());
        onView(withId(R.id.btnSaveHouseholdMember)).perform(click());

        //Click on one of the household members to edit them
        onView(withId(R.id.recyclelistHouseholdMembers)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.txtName)).perform(scrollTo(), typeText("householdMember1"), closeSoftKeyboard());
        onView(withId(R.id.btnSaveHouseholdMember)).perform(click());

        //Delete one of the household members
        onView(withId(R.id.recyclelistHouseholdMembers)).perform(RecyclerViewActions.actionOnItemAtPosition(0, longClick()));
        //Don't at first but then d0
        onView(withText(R.string.cancel)).perform(click());
        onView(withId(R.id.recyclelistHouseholdMembers)).perform(RecyclerViewActions.actionOnItemAtPosition(0, longClick()));
        onView(withText(R.string.yes)).perform(click());

        //Change the finalise reminder time
        onView(withId(R.id.txtWhatTime)).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(PickerActions.setTime(6, 30));
        onView(withText(android.R.string.ok)).perform(click());

        //Schedule Reminders
        onView(withId(R.id.btnSetupTimes)).perform(click());
        onView(withId(R.id.btnDateEO1)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(2019, 6, 25));
        onView(withText(android.R.string.ok)).perform(click());
        onView(withId(R.id.btnDateEO2)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(2019, 6, 26));
        onView(withText(android.R.string.ok)).perform(click());
        onView(withId(R.id.btnDateEO3)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(2019, 6, 27));
        onView(withText(android.R.string.ok)).perform(click());
        onView(withId(R.id.time1)).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(PickerActions.setTime(6, 30));
        onView(withText(android.R.string.ok)).perform(click());
        onView(withId(R.id.time2)).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(PickerActions.setTime(12, 30));
        onView(withText(android.R.string.ok)).perform(click());
        onView(withId(R.id.time3)).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(PickerActions.setTime(18, 30));
        onView(withText(android.R.string.ok)).perform(click());
        onView(withId(R.id.btnSensor)).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(PickerActions.setTime(6, 00));
        onView(withText(android.R.string.ok)).perform(click());
        onView(withId(R.id.btnSet)).perform(click());

        //Go Home
        onView(withId(R.id.btnHome)).perform(click());



        //Play the audio buttons
        onView(withId(R.id.btnEat)).perform(longClick());
        Thread.sleep(2000);
        //onView(withId(R.id.btnMeal)).perform(longClick());
        //Thread.sleep(4000);

        //Create an eating occasion
        onView(withId(R.id.btnEat)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.listViewHouseholdMembers)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.btnAddFoodItem)).perform(longClick());
        Thread.sleep(2000);
        onView(withId(R.id.btnAddFoodItem)).perform(click());
        takePhotoDecline();
        //Take another one
        takePhotoAccept();
        //Record an audio file
        onView(withId(R.id.btnRecordAudio)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.btnAudioFile)).perform(click());
        //Play the audio file
        onView(withId(R.id.btnAudioFile)).perform(click());
        Thread.sleep(800);
        onView(withId(R.id.btnAudioFile)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.btnAudioFile)).perform(click());
        //Decline the audio
        onView(withId(R.id.btnAudioDelete)).perform(click());
        //Record an audio file
        recordAudioAccept();
        //REcord another image and audio
        onView(withId(R.id.btnAddFoodItem)).perform(click());
        takePhotoAccept();
        recordAudioAccept();
        //Listen to the audio file of one of the food items
        onView(new RecyclerViewMatcher(R.id.rvFoodItemList).atPositionOnView(0, R.id.imgAudioPlaceholder)).perform(click());
        //Delete the food item
        onView(new RecyclerViewMatcher(R.id.rvFoodItemList).atPositionOnView(0, R.id.imgIngredientImage)).perform(longClick());
        onView(withText(R.string.yes)).perform(click());
        //Submit
        onView(withId(R.id.btnFinish)).perform(click());

        //COOK
        onView(withId(R.id.btnCook)).perform(click());
        //Create new recipe
        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.imgRecordAudioBtn)).perform(click());
        onView(withId(R.id.btnRecordAudio)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.btnAudioFile)).perform(click());
        //Decline the audio
        onView(withId(R.id.btnAudioDelete)).perform(click());
        //Cancel
        onView(withId(R.id.btnCancel)).perform(click());
        //Try again
        onView(withId(R.id.imgRecordAudioBtn)).perform(click());
        recordAudioAccept();
        //Play the recipe name back
        onView(withId(R.id.imgAudioFile)).perform(click());
        Thread.sleep(300);
        //Play audio button
        onView(withId(R.id.btnAddIngredient)).perform(longClick());
        Thread.sleep(800);
        onView(withId(R.id.btnAddIngredient)).perform(click());
        takePhotoAccept();
        //Record an audio file
        recordAudioAccept();
        //Record another ingredient
        onView(withId(R.id.btnAddIngredient)).perform(click());
        takePhotoAccept();
        //Record an audio file
        recordAudioAccept();
        //Delete an ingredient
        onView(new RecyclerViewMatcher(R.id.rvIngredientList).atPositionOnView(0, R.id.imgIngredientImage)).perform(longClick());
        onView(withText(R.string.yes)).perform(click());
        //Submit
        onView(withId(R.id.btnFinish)).perform(click());
        //Decline the final image photo
        onView(withId(R.id.btnCancel)).perform(click());

        //Create another new recipe forget to record recipe name
        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.btnAddIngredient)).perform(click());
        takePhotoAccept();
        //Record an audio file
        recordAudioAccept();
        //Submit
        onView(withId(R.id.btnFinish)).perform(click());
        //Forgot audio click yes. Takes to audio dialog
        onView(withText(R.string.yes)).perform(click());
        recordAudioAccept();
        //Try submit again
        onView(withId(R.id.btnFinish)).perform(click());

        //Take the final image photo
        onView(withId(R.id.btnConfirm)).perform(click());
        takePhotoAccept();

        //Edit the first recipe
        onView(new RecyclerViewMatcher(R.id.rvRecipes).atPositionOnView(0, R.id.btnFinalizeItem)).perform(click());
        //Take the final image
        //Submit
        onView(withId(R.id.btnFinish)).perform(click());
        //Take the final image photo
        onView(withId(R.id.btnConfirm)).perform(click());
        takePhotoAccept();
        //Delete a recipe
        onView(new RecyclerViewMatcher(R.id.rvRecipes).atPositionOnView(0, R.id.btnFinalizeItem)).perform(longClick());
        onView(withText(R.string.yes)).perform(click());
        //Go HOME
        onView(withId(R.id.btnHome)).perform(click());

        //SHARED PLATE
//        onView(withId(R.id.btnMeal)).perform(click());
//        //Link a recipe
//        onView(withId(R.id.btnLinkRecipe)).perform(click());
//        onView(new RecyclerViewMatcher(R.id.rvRecipeList).atPositionOnView(0, R.id.chkIsSelected)).perform(click());
//        //Click Accept
//        onView(withText(R.string.audio_accept)).perform(click());
//        //Add a dish
//        onView(withId(R.id.btnAddDish)).perform(click());
//        takePhotoAccept();
//        recordAudioAccept();
//        //Add a 2nd dish
//        onView(withId(R.id.btnAddDish)).perform(click());
//        takePhotoAccept();
//        recordAudioAccept();
//        //Play dish auidio
//        onView(new RecyclerViewMatcher(R.id.rvDishList).atPositionOnView(0, R.id.imgAudioPlaceholder)).perform(click());
//        //Delete the dish
//        onView(new RecyclerViewMatcher(R.id.rvDishList).atPositionOnView(0, R.id.imgIngredientImage)).perform(longClick());
//        onView(withText(R.string.yes)).perform(click());
//        //Submit
//        onView(withId(R.id.btnFinish)).perform(click());



        //Breastfeed
        onView(withId(R.id.btnBreastFeed)).perform(click());
        onView(withId(R.id.listViewHouseholdMembers)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.btnSaveBreastfeed)).perform(click());

        //FINALIZE
        onView(withId(R.id.btnFinalizeEat)).perform(longClick());
        Thread.sleep(3000);
        onView(withId(R.id.btnFinalizeEat)).perform(click());
        //Select a household member
        onView(withId(R.id.listViewHouseholdMembers)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        //Click an eating occasion to finalize
        onView(new RecyclerViewMatcher(R.id.rvEatingOccasions).atPositionOnView(0, R.id.btnFinalizeItem)).perform(click());
        //Ate most
        onView(withId(R.id.btnAteSome)).perform(click());
        takePhotoAccept();
        recordAudioAccept();
        //Repeat for second eating occasion
        //onView(new RecyclerViewMatcher(R.id.rvEatingOccasions).atPositionOnView(0, R.id.btnFinalizeItem)).perform(click());
        //Yes leftovers
//        onView(withId(R.id.btnConfirm)).perform(click());
//        takePhotoAccept();
//        recordAudioAccept();
//        //Guests JUST ACCEPT
//        onView(withText(R.string.tick)).perform(click());
//        onView(withId(R.id.btnAteAll)).perform(click());

        //GO HOME
        onView(withId(R.id.btnHome)).perform(click());

        //Click settings
        onView(withId(R.id.action_manage)).perform(click());
        onView(withId(R.id.txtPin)).perform(typeText("0000"));
        onView(withText(android.R.string.ok)).perform(click());
        onView(withId(R.id.btnVerify)).perform(click());
        onView(new RecyclerViewMatcher(R.id.rvHouseholdMemberFoodRecrods).atPositionOnView(0, R.id.txtEoTime)).perform(click());
        //onView(new RecyclerViewMatcher(R.id.rvHouseholdMemberFoodRecrods).atPositionOnView(0, R.id.txtEoTime)).perform(click());

        //Yes record an audio only
        onView(withId(R.id.btnYes)).perform(click());
        recordAudioAccept();
        //Click No
        onView(withId(R.id.btnNo)).perform(click());
        //Click back
        onView(withId(R.id.btnBack)).perform(click());
        //Export the data
        onView(withId(R.id.btnExportData)).perform(click());

        Thread.sleep(6000);
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

    private void takePhotoDecline() throws InterruptedException {
        Thread.sleep(500);
        onView(withId(R.id.btnTakePicture)).perform(click());
        Thread.sleep(500);
        //Decline the image
        onView(withId(R.id.btnImageDecline)).perform(click());
    }

    private Matcher<File> nameContains(String str) {
        return new FeatureMatcher<File, String>(containsString(str), "nameContains", "name") {
            @Override
            protected String featureValueOf(File actual) {
                return actual.getName();
            }
        };
    }
}
