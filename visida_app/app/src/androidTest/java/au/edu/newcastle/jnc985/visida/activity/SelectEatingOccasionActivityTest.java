package au.edu.newcastle.jnc985.visida.activity;

import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import androidx.test.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import bo.EatingOccasionRepository;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static au.edu.newcastle.jnc985.visida.RecyclerViewItemCountAssertion.withItemCount;
import static bo.AppConstants.UNFINALIZEDEOIDS;
import static org.hamcrest.core.Is.is;

/**
 * Created by jnc985 on 09-Apr-18.
 */

@RunWith(AndroidJUnit4.class)
public class SelectEatingOccasionActivityTest {

    @Rule
    public ActivityTestRule<SelectEatingOccasionActivity> mActivityRule = new ActivityTestRule<SelectEatingOccasionActivity>(SelectEatingOccasionActivity.class, true, false);

    private AppDatabase mDb;
    private Context mContext;
    private int mHmId = 1;
    private HouseholdMember mHm;
    private long mFrId = (long) 1;
    private FoodRecord mFr;

    @Before
    public void setupDb() {
        mContext = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).build();
        AppDatabase.setInstance(mDb);

        //Create Household Membmer
        mHm = new HouseholdMember(mHmId, "", "HM1", "", 25, false, false);

        //Create Food Record
        mFr = new FoodRecord(mHmId);
        mFr.setFoodRecordId(mFrId);

        //Add to database
        mDb.getHouseholdMemberDao().insert(mHm);
        mDb.getFoodRecordDao().insert(mFr);
    }

    @After
    public void closeDb() {
        mDb.close();
    }

    @Test
    public void listEmptyWhenNoEatingOccasions() {
        Long[] eoIds = new Long[]{};
        Intent i = new Intent();
        i.putExtra(UNFINALIZEDEOIDS, eoIds);
        mActivityRule.launchActivity(i);
        onView(withId(R.id.rvEatingOccasions)).check(withItemCount(0));
    }

    @Test
    public void listContainsTodaysNonFinalizedEatingOccasion() {
        EatingOccasion eo = new EatingOccasion();
        eo.setFoodRecordId(mFrId);
        eo.setFinalized(false);
        eo.setEatingOccasionId((long) 1);
        mDb.getEatingOccasionDao().insert(eo);

        FoodItem fi = new FoodItem();
        fi.setEatingOccasionId(eo.getEatingOccasionId());
        fi.setImageUrl("");
        fi.setAudioUrls("");
        mDb.getFoodItemDao().insert(fi);


        Long[] eoIds = new Long[]{eo.getEatingOccasionId()};
        Intent i = new Intent();
        i.putExtra(UNFINALIZEDEOIDS, eoIds);
        mActivityRule.launchActivity(i);

        EatingOccasionRepository eoRepo = new EatingOccasionRepository(mActivityRule.getActivity().getApplication());
        List<EatingOccasion> eos = eoRepo.getEatingOccasions(eoIds);

        System.out.println(eos.size());

        onView(withId(R.id.rvEatingOccasions)).check(withItemCount(1));
    }

    @Test
    public void clickFinalizeRemovesEmptyEatingOccasions(){
        //Create an eating occasion which contsina a single food item
        EatingOccasion withFoodItem = new EatingOccasion();
        withFoodItem.setEatingOccasionId((long) 1);
        withFoodItem.setFoodRecordId(mFrId);
        withFoodItem.setFinalized(false);

        //Create an empty eating occasion
        EatingOccasion emptyEo = new EatingOccasion();
        emptyEo.setEatingOccasionId((long) 2);
        emptyEo.setFoodRecordId(mFrId);
        emptyEo.setFinalized(false);
        mDb.getEatingOccasionDao().insert(withFoodItem, emptyEo);

        //Create food item for "withFoodItem"
        FoodItem fi = new FoodItem();
        fi.setEatingOccasionId(withFoodItem.getEatingOccasionId());
        fi.setImageUrl("");
        fi.setAudioUrls("");
        mDb.getFoodItemDao().insert(fi);

        //Get the Eating Occasions
        List<EatingOccasion> eos = mDb.getEatingOccasionDao().getAll();
        assertThat(eos.size(), is(2));

        Long[] eoIds = new Long[]{withFoodItem.getEatingOccasionId(), emptyEo.getEatingOccasionId() };
        Intent i = new Intent();
        i.putExtra(UNFINALIZEDEOIDS, eoIds);
        mActivityRule.launchActivity(i);

        //Check there is only the one eo in the list
        List<EatingOccasion> eosAfter = mDb.getEatingOccasionDao().getAll();
        assertThat(eosAfter.size(), is(1));
        onView(withId(R.id.rvEatingOccasions)).check(withItemCount(1));

    }
}
