package au.edu.newcastle.jnc985.visida.activity;


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
import org.mockito.junit.MockitoJUnitRunner;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.RecyclerViewMatcher;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;
import recordverification.RecordVerificationActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static au.edu.newcastle.jnc985.visida.RecyclerViewItemCountAssertion.withItemCount;

/**
 * Created by jnc985 on 30-Nov-17.
 */


@RunWith(MockitoJUnitRunner.class)
public class RecordVerificationActivityTest {
    private AppDatabase mDb;

    private long mFrId = 1;
    private long mFrId2 = 2;
    private HouseholdMember mHm;
    private HouseholdMember mHm2;
    private FoodRecord mFr;

    private Intent intent;
    //Set up Rule
    @Rule
    public IntentsTestRule<RecordVerificationActivity> mActivityTestRule = new IntentsTestRule<RecordVerificationActivity>(RecordVerificationActivity.class, true, false) ;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        AppDatabase.setInstance(mDb);

        setUpData();
    }

    @After
    public void closeDb() {
        mDb.close();
    }

    private void setUpData() {
        mHm = new HouseholdMember(1, "", "HM1", "", 1, false, false);
        mHm.setParticipantHouseholdMemberId("ppid");
        mHm2 = new HouseholdMember(2, "", "HM2", "", 1, false, false);
        mHm2.setParticipantHouseholdMemberId("ppid2");
        mDb.getHouseholdMemberDao().insert(mHm, mHm2);

        mFr = new FoodRecord(mHm.getUid());
        mFr.setFoodRecordId(mFrId);
        mFr.setReviewed(false);
        FoodRecord fr2 = new FoodRecord(mHm2.getUid());
        fr2.setFoodRecordId(mFrId2);
        fr2.setReviewed(true);
        mDb.getFoodRecordDao().insert(mFr, fr2);

        //Create 2 eating occasions one finalized one not
        EatingOccasion eo1 = new EatingOccasion();
        eo1.setEatingOccasionId((long) 1);
        eo1.setFinalized(false);
        eo1.setFoodRecordId(mFr.getFoodRecordId());
        EatingOccasion eo2 = new EatingOccasion();
        eo2.setEatingOccasionId((long) 2);
        eo2.setFinalized(false);
        eo2.setFoodRecordId(mFr.getFoodRecordId());
        EatingOccasion eo3 = new EatingOccasion();
        eo3.setEatingOccasionId((long) 3);
        eo3.setFinalized(false);
        eo3.setFoodRecordId(fr2.getFoodRecordId());
        mDb.getEatingOccasionDao().insert(eo1, eo2, eo3);

        //Create food items
        FoodItem fi1 = new FoodItem();
        fi1.setEatingOccasionId(eo1.getEatingOccasionId());
        fi1.setImageUrl("");
        FoodItem fi2 = new FoodItem();
        fi2.setEatingOccasionId(eo1.getEatingOccasionId());
        fi2.setImageUrl("");
        FoodItem fi3 = new FoodItem();
        fi3.setEatingOccasionId(eo2.getEatingOccasionId());
        fi3.setImageUrl("");
        FoodItem fi4 = new FoodItem();
        fi4.setEatingOccasionId(eo3.getEatingOccasionId());
        fi4.setImageUrl("");
        mDb.getFoodItemDao().insert(fi1, fi2, fi3, fi4);
    }

    @Test
    public void listContainsAllHouseholdMembers() {
        mActivityTestRule.launchActivity(new Intent());
        onView(withId(R.id.rvHouseholdMemberFoodRecrods)).check(withItemCount(2));
    }

    @Test
    public void eachFoodRecordListContainsCorrectEatingOccasions(){
        mActivityTestRule.launchActivity(new Intent());
        onView(new RecyclerViewMatcher(R.id.rvEatingOccasions).atPositionOnView(0, R.id.rvFoodItems)).check(withItemCount(3));
    }

    @Test
    public void clickInvalidFoodRecordMovesToRecordReview(){
        mActivityTestRule.launchActivity(new Intent());

        //Nothing should happen so click the red now
        onView(new RecyclerViewMatcher(R.id.rvEatingOccasions).atPositionOnView(0, R.id.txtEoTime)).perform(click());

        intended(hasComponent(RecordReviewActivity.class.getName()));
    }

}
