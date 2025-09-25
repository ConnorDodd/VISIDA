package au.edu.newcastle.jnc985.visida.activity;

import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.intent.rule.IntentsTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import au.edu.newcastle.jnc985.visida.R;
import bo.FoodRecordViewModel;
import bo.HouseholdMembersViewModel;
import bo.State;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.STATE;

/**
 * Created by jnc985 on 30-Nov-17.
 */

@RunWith(MockitoJUnitRunner.class)
public class SelectHouseholdMemberActivityAusTest {
    private final static long MINUTE = 60 * 1000;

    private AppDatabase mDb;

    @Mock
    private HouseholdMembersViewModel hmViewModel;
    @Mock
    private FoodRecordViewModel frViewModel;

    private Context mContext;

    private HouseholdMember hm1;

    private Intent mIntent;
    //Set up Rule
    @Rule
    public IntentsTestRule<SelectHouseholdMemberActivity> intentsTestRule = new IntentsTestRule<SelectHouseholdMemberActivity>(SelectHouseholdMemberActivity.class, true, false) {
        @Override
        protected Intent getActivityIntent() {
            mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
            mIntent = new Intent(mContext, SelectHouseholdMemberActivity.class);

            //Put the EAT state in as default Each test will have to set the state accordingly
            mIntent.putExtra(STATE, State.EAT);
            return mIntent;
        }
    };


    @Before
    public void createDb() throws Throwable {
        mContext = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).build();
        AppDatabase.setInstance(mDb);

        //Create the list of dummy household members
        hm1 = new HouseholdMember(1, "1", "HM1", "", 1, false, true);

        //Add hms to database
        mDb.getHouseholdMemberDao().insert(hm1);

    }

    @After
    public void closeDb() {
        mDb.close();
    }

    @Test
    public void autoSelectHouseholdMemberIfOnlyOneExistsEAT(){
        //Set up state to not be invalid
        SharedPreferences sharedPref = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(STATE, State.EAT.ordinal());
        editor.commit();

        intentsTestRule.launchActivity(new Intent());
        //Straight up expect to see the Eating Occasion page
        onView(withId(R.id.btnAddFoodItem)).check(matches(isDisplayed()));
    }

    @Test
    public void autoSelectHouseholdMemberIfOnlyOneExistsEATClickBackReturnsToSelectPage(){
        //Set up state to not be invalid
        SharedPreferences sharedPref = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(STATE, State.EAT.ordinal());
        editor.commit();

        intentsTestRule.launchActivity(new Intent());
        //Straight up expect to see the Eating Occasion page
        onView(withId(R.id.btnAddFoodItem)).check(matches(isDisplayed()));

        onView(withId(R.id.btnBack)).perform(click());

        onView(withId(R.id.listViewHouseholdMembers)).check(matches(isDisplayed()));
    }

    @Test
    public void autoSelectHouseholdMemberIfOnlyOneExistsFINALIZE(){
        //Straight up expect to see the SelectEatingoCcasion Page
        //Set up state to not be invalid
        SharedPreferences sharedPref = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(STATE, State.FINALIZE.ordinal());
        editor.commit();

        //Add A food record to finalise
        FoodRecord fr = new FoodRecord(hm1.getUid());
        fr.setFoodRecordId(1);
        mDb.getFoodRecordDao().insert(fr);

        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId((long)1);
        eo.setFoodRecordId(fr.getFoodRecordId());
        mDb.getEatingOccasionDao().insert(eo);

        intentsTestRule.launchActivity(new Intent());
        //Straight up expect to see the Eating Occasion page
        onView(withId(R.id.rvEatingOccasions)).check(matches(isDisplayed()));

    }

    @Test
    public void autoSelectHouseholdMemberIfOnlyOneExistsBREASTFEED(){
        //Straight up expect to see the Breastfeeding Page
        //Set up state to not be invalid
        SharedPreferences sharedPref = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(STATE, State.BREASTFEED.ordinal());
        editor.commit();

        intentsTestRule.launchActivity(new Intent());
        //Straight up expect to see the Eating Occasion page
        onView(withId(R.id.btnSaveBreastfeed)).check(matches(isDisplayed()));
    }

}
