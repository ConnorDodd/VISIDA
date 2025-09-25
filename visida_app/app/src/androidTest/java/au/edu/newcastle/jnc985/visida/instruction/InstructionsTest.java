//package au.edu.newcastle.jnc985.visida.instruction;
//
//import android.app.Activity;
//import android.app.Instrumentation;
//import android.arch.persistence.room.Room;
//import android.content.Context;
//import android.content.Intent;
//import android.provider.MediaStore;
//import android.support.test.InstrumentationRegistry;
//import android.support.test.espresso.intent.rule.IntentsTestRule;
//import android.support.test.rule.ActivityTestRule;
//import android.text.Html;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import au.edu.newcastle.jnc985.visida.R;
//import au.edu.newcastle.jnc985.visida.RecyclerViewMatcher;
//import au.edu.newcastle.jnc985.visida.activity.MainActivity;
//import au.edu.newcastle.jnc985.visida.activity.RecordReviewActivity;
//import bo.db.AppDatabase;
//
//import static android.support.test.espresso.Espresso.onView;
//import static android.support.test.espresso.action.ViewActions.click;
//import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
//import static android.support.test.espresso.action.ViewActions.scrollTo;
//import static android.support.test.espresso.action.ViewActions.swipeLeft;
//import static android.support.test.espresso.action.ViewActions.typeText;
//import static android.support.test.espresso.assertion.ViewAssertions.matches;
//import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
//import static android.support.test.espresso.intent.Intents.intending;
//import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
//import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
//import static android.support.test.espresso.matcher.ViewMatchers.withId;
//import static android.support.test.espresso.matcher.ViewMatchers.withText;
//import static bo.AppConstants.FRID;
//import static org.hamcrest.CoreMatchers.allOf;
//
///**
// * Created by Josh on 13-Dec-17.
// */
//
//@RunWith(MockitoJUnitRunner.class)
//public class InstructionsTest {
//
//    @Rule
//    public IntentsTestRule<MainActivity> mMainActivityRule = new IntentsTestRule<MainActivity>(MainActivity.class, true, false);
//    @Rule
//    public ActivityTestRule<RecordReviewActivity> mRecordReviewRule = new ActivityTestRule<RecordReviewActivity>(RecordReviewActivity.class, true, false);
//
//    private Context mContext;
//    private AppDatabase mDb;
//
//    @Before
//    public void setupDatabase() {
//        mContext = InstrumentationRegistry.getTargetContext();
//        mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).build();
//
//        AppDatabase.setInstance(mDb);
//    }
//
//    @After
//    public void closeDb() {
//        mDb.close();
//    }
//
//    public void checkInstructions(int id) {
//        onView(withId(R.id.btnInstruction)).perform(click());
//        CharSequence[] instructionSet = mMainActivityRule.getActivity().getResources().getTextArray(id);
//        for (CharSequence s : instructionSet) {
//            onView(allOf(
//                    isCompletelyDisplayed(),
//                    withId(R.id.txtInstruction)))
//                    .check(matches(withText((String) s)))
//                    .perform(swipeLeft());
//
//            //Wait for the Swip Animation to finish.
//            try {
//                Thread.sleep(200);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//        }
//        onView(withId(R.id.btnDismiss)).perform(click());
//    }
//
//
//    @Test
//    public void instructionsAppearForAllActivitiy() {
//        //Main Activity
//        mMainActivityRule.launchActivity(new Intent());
//        checkInstructions(R.array.instruction_mainactivity);
//
//        //Go to Settings
//        onView(withId(R.id.action_manage)).perform(click());
//        //Enter the pin
//        String PIN = mMainActivityRule.getActivity().getResources().getString(R.string.pin_value);
//        onView(withId(R.id.txtPin)).perform(typeText(PIN));
//        //Click ok
//        onView(withText(R.string.yes)).perform(click());
//        checkInstructions(R.array.instruction_settingsactivity);
//
//        //Set Up household member (breastfed for simpliccity)
//        onView(withId(R.id.btnSetupHousehold)).perform(click());
//        checkInstructions(R.array.instruction_setuphouseholdctivity);
//
//        onView(withId(R.id.fab)).perform(click());
//        checkInstructions(R.array.instruction_createhouseholdmember);
//        createHouseholdMember();
//
//        //Go Home
//        onView(withId(R.id.btnHome)).perform(click());
//        //Go to Breastfeed
//        onView(withId(R.id.btnBreastFeed)).perform(click());
//        checkInstructions(R.array.instruction_selecthouseholdmemberactivity);
//        onView(withId(R.id.listViewHouseholdMembers))
//                .perform(actionOnItemAtPosition(0, click()));
//        checkInstructions(R.array.instruction_breastfeedactivity);
//        onView(withId(R.id.btnSaveBreastfeed)).perform(click());
//
//
//        //Go to Eat
//        onView(withId(R.id.btnEat)).perform(click());
//        onView(withId(R.id.listViewHouseholdMembers))
//                .perform(actionOnItemAtPosition(0, click()));
//        checkInstructions(R.array.instruction_eatingoccasionactivity);
//        onView(withId(R.id.btnAddFoodItem)).perform(click());
//
//        //Take Photo,
//        checkInstructions(R.array.instruction_cameraactivity);
//        onView(withId(R.id.btnTakePicture)).perform(click());
//        //Wait for dialog
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        onView(withText(R.string.tick)).perform(click());
//
//        //Record Audio
//        checkInstructions(R.array.instruction_audioactivity);
//        onView(withId(R.id.btnRecordAudio)).perform(click());
//        onView(withId(R.id.btnRecordAudio)).perform(click());
//        onView(withId(R.id.btnAudioAccept)).perform(click());
//        onView(withId(R.id.btnFinish)).perform(click());
//        onView(withId(R.id.btnHome)).perform(click());
//
//        //Go to Cook
//        onView(withId(R.id.btnCook)).perform(click());
//        checkInstructions(R.array.instruction_listrecipeactivity);
//        onView(withId(R.id.fab)).perform(click());
//
//        //Create Recipe
//        checkInstructions(R.array.instruction_createrecipe);
//        onView(withId(R.id.btnHome)).perform(click());
//
//        /*
//        //Check Shared dish
//        onView(withId(R.id.btnMeal)).perform(click());
//        checkInstructions(R.array.instruction_mealactivity);
//        onView(withId(R.id.btnHome)).perform(click());
//        */
//
//        //Go to Finalize Eat
//        onView(withId(R.id.btnFinalizeEat)).perform(click());
//        onView(withId(R.id.listViewHouseholdMembers))
//                .perform(actionOnItemAtPosition(0, click()));
//
//        //Finalize Eating Occasion
//        checkInstructions(R.array.instruction_selecteatingoccasionactivity);
//        onView(new RecyclerViewMatcher(R.id.rvEatingOccasions).atPositionOnView(0, R.id.btnFinalizeItem)).perform(click());
//        checkInstructions(R.array.instruction_finalizefooditemactivity);
//        onView(withId(R.id.btnAteAll));
//        onView(withId(R.id.btnHome)).perform(click());
//
//        //Go to Eat and make another Eating Occasion
//        onView(withId(R.id.btnEat)).perform(click());
//        onView(withId(R.id.listViewHouseholdMembers))
//                .perform(actionOnItemAtPosition(0, click()));
//        onView(withId(R.id.btnAddFoodItem)).perform(click());
//
//        //Take Photo,
//        onView(withId(R.id.btnTakePicture)).perform(click());
//        //Wait for dialog
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        onView(withText(R.string.tick)).perform(click());
//
//        //Record Audio
//        onView(withId(R.id.btnRecordAudio)).perform(click());
//        onView(withId(R.id.btnRecordAudio)).perform(click());
//        onView(withId(R.id.btnAudioAccept)).perform(click());
//        onView(withId(R.id.btnFinish)).perform(click());
//        onView(withId(R.id.btnHome)).perform(click());
//
//        //Force Open Record Review
//        Intent reviewIntent = new Intent();
//        reviewIntent.putExtra(FRID, (long) 1);
//        mRecordReviewRule.launchActivity(reviewIntent);
//        checkInstructions(R.array.instruction_recordreviewactivity);
//        onView(withId(R.id.btnNo)).perform(click());
//    }
//
//    private void createHouseholdMember() {
//        Instrumentation.ActivityResult activityResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, null);
//        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(activityResult);
//        onView(withId(R.id.txtHouseholdId)).perform(scrollTo(), typeText("householdid"), closeSoftKeyboard());
//        onView(withId(R.id.txtParticipantId)).perform(scrollTo(), typeText("participantid"), closeSoftKeyboard());
//        onView(withId(R.id.txtName)).perform(scrollTo(), typeText("testhouseholdmember"), closeSoftKeyboard());
//        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("2"), closeSoftKeyboard());
//        onView(withId(R.id.imgViewAvatar)).perform(scrollTo(), click());
//        onView(withId(R.id.radioBtnFemale)).perform(scrollTo(), click());
//        onView(withId(R.id.chkIsBreastfed)).perform(scrollTo(), click());
//        onView(withId(R.id.btnSaveHouseholdMember)).perform(click());
//    }
//
//}
