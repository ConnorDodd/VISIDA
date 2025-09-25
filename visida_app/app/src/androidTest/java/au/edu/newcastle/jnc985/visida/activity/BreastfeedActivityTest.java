package au.edu.newcastle.jnc985.visida.activity;

import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import au.edu.newcastle.jnc985.visida.R;
import bo.FoodRecordRepository;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasFlag;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static bo.AppConstants.FR;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.Is.is;

/**
 * Created by jnc985 on 30-Nov-17.
 */

@RunWith(AndroidJUnit4.class)
public class BreastfeedActivityTest {
    private final static long MINUTE = 60 * 1000;

    private AppDatabase mDb;

    private HouseholdMember hm1;
    private FoodRecord mFr;

    private final int HMID = 1;
    private final long FRID = 1;
    //Set up Rule
    @Rule
    public IntentsTestRule<BreastfeedActivity> mIntentsRule = new IntentsTestRule<BreastfeedActivity>(BreastfeedActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
            Intent result = new Intent();

            mFr = new FoodRecord(HMID);
            mFr.setFoodRecordId(FRID);
            result.putExtra(FR, mFr);
            return result;
        }
    };

    @Before
    public void createDb() throws Throwable {
        Context context = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();

        //Create the list of dummy household members
        hm1 = new HouseholdMember(HMID, "1", "HM1", "", 1, false, false);
        mFr.setHouseholdMemberId(hm1.getUid());

        //Add hms to database
        mDb.getHouseholdMemberDao().insert(hm1);
        mDb.getFoodRecordDao().insert(mFr);
    }

    @After
    public void closeDb() {
        mDb.close();
    }

    //Test with household members no food records
    @Test
    public void activityCreatesEatingOccasoinInGivenFoodRecordandDatabase() throws ExecutionException, InterruptedException {
        //Set up Database with:
        // 1 hosuehold member no food records
        //Prepare the static class
        AppDatabase.setInstance(mDb);

        List<FoodRecord> originalFoodRecords = mDb.getFoodRecordDao().getAllFoodRecordsForHouseholdMember(hm1.getUid());
        assertThat(originalFoodRecords.size(), is(1));

        //Click the button
        onView(withId(R.id.btnSaveBreastfeed)).perform(click());

        FoodRecordRepository frRepo = new FoodRecordRepository(mIntentsRule.getActivity().getApplication());
        FoodRecord todaysFr = frRepo.getTodaysFoodRecordFor(hm1.getUid(), new Date());
        List<EatingOccasion> eos = todaysFr.getEatingOccasions();

        assertThat(eos.size(), is(1));

        intended(allOf(
                hasComponent(MainActivity.class.getName()),
                hasFlag(Intent.FLAG_ACTIVITY_NEW_TASK),
                hasFlag(Intent.FLAG_ACTIVITY_CLEAR_TOP)));


    }
}
