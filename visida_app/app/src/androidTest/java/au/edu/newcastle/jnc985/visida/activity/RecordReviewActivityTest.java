package au.edu.newcastle.jnc985.visida.activity;

import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.intent.rule.IntentsTestRule;

import org.hamcrest.core.IsNot;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import au.edu.newcastle.jnc985.visida.BuildConfig;
import au.edu.newcastle.jnc985.visida.R;
import bo.EatingOccasionRepository;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static bo.AppConstants.FRID;
import static bo.AppConstants.PPID;
import static org.hamcrest.core.Is.is;

/**
 * Created by jnc985 on 30-Nov-17.
 */


@RunWith(MockitoJUnitRunner.class)
public class RecordReviewActivityTest {
    private AppDatabase mDb;

    private long mFrId = 1;
    private HouseholdMember mHm;
    private String ppid = "hmPpid";
    private FoodRecord mFr = new FoodRecord(1);

    private Intent intent;
    //Set up Rule
    @Rule
    public IntentsTestRule<RecordReviewActivity> mActivityTestRule = new IntentsTestRule<RecordReviewActivity>(RecordReviewActivity.class, true, false) {
        @Override
        protected Intent getActivityIntent() {
            Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
            Intent result = new Intent(targetContext, RecordReviewActivity.class);

            //Return the food record when queried
            result.putExtra(FRID, mFrId);
            return result;
        }
    };

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        AppDatabase.setInstance(mDb);

        setUpData();
    }

    private void startAcvity(String ppid) {
        Context context = InstrumentationRegistry.getTargetContext();
        intent = new Intent(context, RecordReviewActivity.class);
        intent.putExtra(PPID, ppid);
        mActivityTestRule.launchActivity(intent);
    }

    @After
    public void closeDb() {
        mDb.close();
    }

    private void setUpData() {
        mHm = new HouseholdMember(1, "", "HM1", "", 1, false, false);
        mHm.setParticipantHouseholdMemberId(ppid);
        mDb.getHouseholdMemberDao().insert(mHm);

        mFr.setFoodRecordId(mFrId);
        mDb.getFoodRecordDao().insert(mFr);

        //Create 2 eating occasions one finalized one not
        EatingOccasion eo1 = new EatingOccasion();
        eo1.setEatingOccasionId((long) 1);
        eo1.setFinalized(false);
        eo1.setFoodRecordId((long) 1);
        EatingOccasion eo2 = new EatingOccasion();
        eo2.setEatingOccasionId((long) 2);
        eo2.setFinalized(false);
        eo2.setFoodRecordId((long) 1);

        mDb.getEatingOccasionDao().insert(eo1);
        mDb.getEatingOccasionDao().insert(eo2);

        //Create food items
        FoodItem fi1 = new FoodItem();
        fi1.setEatingOccasionId(1);
        fi1.setImageUrl("");
        FoodItem fi2 = new FoodItem();
        fi2.setEatingOccasionId(1);
        fi2.setImageUrl("");
        FoodItem fi3 = new FoodItem();
        fi3.setEatingOccasionId(2);
        fi3.setImageUrl("");
        FoodItem fi4 = new FoodItem();
        fi4.setEatingOccasionId(2);
        fi4.setImageUrl("");
        mDb.getFoodItemDao().insert(fi1, fi2, fi3, fi4);
    }

//    @Test
//    public void clickNoFinalizesAllEatingOccasions() {
//        startAcvity(ppid);
//        //Click the button
//        onView(withId(R.id.btnNo)).perform(click());
//
//        //Check all of the eating occasions are finalized
//        EatingOccasionRepository eoRepo = new EatingOccasionRepository(mActivityTestRule.getActivity().getApplication());
//        List<EatingOccasion> eos = eoRepo.getEatingOccasions(mFr.getFoodRecordId());
//
//        for (EatingOccasion eo : eos) {
//            assertThat(eo.isFinalized(), is(true));
//            //Check that all food items are finalized
//            for (FoodItem fi : eo.getFoodItems()) {
//                assertThat(fi.isFinalized(), is(true));
//            }
//        }
//    }

    @Test
    public void clickYesOpensAudioFragment() {
        startAcvity(ppid);
        onView(withId(R.id.btnYes)).perform(click());
        onView(withId(R.id.btnRecordAudio)).check(matches(isDisplayed()));
    }

    @Test
    public void openingActivityRemovesEmptyEatingOccasion(){
        EatingOccasion eo3 = new EatingOccasion();
        eo3.setEatingOccasionId((long) 3);
        eo3.setFinalized(false);
        eo3.setFoodRecordId((long) 1);

        //Dont create any food items
        mDb.getEatingOccasionDao().insert(eo3);

        //Check that there are 3 eating occasions
        List<EatingOccasion> beforeEos = mDb.getEatingOccasionDao().getAll();
        assertThat(beforeEos.size(), is(3));

        startAcvity(ppid);

        //Make sure eo3 has been removed
        List<EatingOccasion> afterEos = mDb.getEatingOccasionDao().getAll();
        assertThat(afterEos.size(), is(2));

    }

    @Test
    public void clickNoMoreToAddSetsFoodRecordToReviewed(){
        //Check the food record's reviewed is set to false
        FoodRecord frB4 = mDb.getFoodRecordDao().getFoodRecord(mFrId).get(0);
        assertThat(frB4.isReviewed(), is(false));

        startAcvity(ppid);

        //Click NO
        onView(withId(R.id.btnNo)).perform(click());

        FoodRecord frAfter = mDb.getFoodRecordDao().getFoodRecord(mFrId).get(0);
        assertThat(frAfter.isReviewed(), is(true));
    }

    @Test
    public void clickYesOpensAudioWithTextInEnglishNoTextOtherwise(){
        //Check the food record's reviewed is set to false
        FoodRecord frB4 = mDb.getFoodRecordDao().getFoodRecord(mFrId).get(0);
        assertThat(frB4.isReviewed(), is(false));

        startAcvity(ppid);

        //Click Yes
        onView(withId(R.id.btnYes)).perform(click());

        if(BuildConfig.forceKhmer || BuildConfig.forceSwahili){
            //Check Text is not present
            onView(withId(R.id.btnAddText)).check(matches(IsNot.not(isDisplayed())));
        }
        else{
            onView(withId(R.id.btnAddText)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void clickYesRecordingAudioDoesNotSetFoodRecordToReviewed(){
        //Check the food record's reviewed is set to false
        FoodRecord frB4 = mDb.getFoodRecordDao().getFoodRecord(mFrId).get(0);
        assertThat(frB4.isReviewed(), is(false));

        startAcvity(ppid);

        //Click Yes
        onView(withId(R.id.btnYes)).perform(click());

        //Record an audio file
        onView(withId(R.id.btnRecordAudio)).perform(click());
        onView(withId(R.id.btnAudioFile)).perform(click());
        onView(withId(R.id.btnAudioAccept)).perform(click());

        //Check the record is now "reviewed"
        FoodRecord frAfter = mDb.getFoodRecordDao().getFoodRecord(mFrId).get(0);
        assertThat(frAfter.isReviewed(), is(false));
    }

    @Test
    public void clickYesAndCancelAudioLeavesRecordNotReviewed(){
        //Check the food record's reviewed is set to false
        FoodRecord frB4 = mDb.getFoodRecordDao().getFoodRecord(mFrId).get(0);
        assertThat(frB4.isReviewed(), is(false));

        startAcvity(ppid);

        //Click Yes
        onView(withId(R.id.btnYes)).perform(click());

        //Record an audio file
        onView(withId(R.id.btnCancel)).perform(click());

        //Check the record is now "reviewed"
        FoodRecord frAfter = mDb.getFoodRecordDao().getFoodRecord(mFrId).get(0);
        assertThat(frAfter.isReviewed(), is(false));
    }

    @Test
    public void clickHomeLeavesRecordNotReviewed(){
        //Check the food record's reviewed is set to false
        FoodRecord frB4 = mDb.getFoodRecordDao().getFoodRecord(mFrId).get(0);
        assertThat(frB4.isReviewed(), is(false));

        startAcvity(ppid);

        //Click Yes
        onView(withId(R.id.btnHome)).perform(click());

        //Check the record is now "reviewed"
        FoodRecord frAfter = mDb.getFoodRecordDao().getFoodRecord(mFrId).get(0);
        assertThat(frAfter.isReviewed(), is(false));
    }

}
