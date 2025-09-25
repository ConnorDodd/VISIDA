package au.edu.newcastle.jnc985.visida.repository;

import androidx.room.Room;
import android.content.Context;
import androidx.test.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import au.edu.newcastle.jnc985.visida.activity.MainActivity;
import bo.FoodRecordRepository;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by Josh on 19-Dec-17.
 */
@RunWith(AndroidJUnit4.class)
public class FoodRecordRepositoryTest {

    //Rule so we can access the application
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Mock
    private AppDatabase mDb;

    private Context context;

    @Before
    public void createDb() {
        context = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
    }

    @After
    public void closeDb() {
        mDb.close();
    }


    //Check that if no food records for today exist a new one is created and added to the database
    @Test
    public void getTodaysFoodRecordForEmptyDatabase() throws Exception {
        //Set up Database with:
        // 1 hosuehold member no food records
        //Prepare the static class
        AppDatabase.setInstance(mDb);

        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        mDb.getHouseholdMemberDao().insert(hm);

        FoodRecordRepository foodRepo = new FoodRecordRepository(mActivityRule.getActivity().getApplication());
        Date today = new Date();
        FoodRecord todaysRecord = foodRepo.getTodaysFoodRecordFor(hm.getUid(), today);

        assertNull(todaysRecord);
    }

    //Check that if a food record exists for today it is returned with no eating occasions
    @Test
    public void getTodaysFoodRecordWithValidFoodRecordInDatabaseNoEatingOccasions() throws Exception {
        //Set up Database with:
        // 1 hosuehold member no food records
        AppDatabase.setInstance(mDb);

        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        mDb.getHouseholdMemberDao().insert(hm);

        FoodRecord fr = new FoodRecord(hm.getUid());
        fr.setFoodRecordId(mDb.getFoodRecordDao().insert(fr)[0]);

        FoodRecordRepository foodRepo = new FoodRecordRepository(mActivityRule.getActivity().getApplication());
        Date today = new Date();
        FoodRecord todaysFoodRecord = foodRepo.getTodaysFoodRecordFor(hm.getUid(), today);

        assertEquals(fr, todaysFoodRecord);

    }

    //Check that if a food record exists for today it is returned with all eating occasions
    @Test
    public void getTodaysFoodRecordWithValidFoodRecordInDatabaseWithEatingOccasions() throws Exception {
        //Set up Database with:
        // 1 hosuehold member no food records
        AppDatabase.setInstance(mDb);
        Date today = new Date();
        Date yesterday = new Date(today.getTime() - (1000 * 60 * 60 * 24));

        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        mDb.getHouseholdMemberDao().insert(hm);

        FoodRecord fr = new FoodRecord(hm.getUid());

        //Create Eating occasion
        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId((long) 1);
        eo.setFoodRecordId((long) 1);
        eo.setStartTime(yesterday);
        eo.setEndTime(new Date(yesterday.getTime() + 1000 * 60 * 30));
        List<EatingOccasion> eatingOcassions = new ArrayList<EatingOccasion>();
        eatingOcassions.add(eo);

        //Add eating occasion to the food record.
        fr.setEatingOccasions(eatingOcassions);
        fr.setFoodRecordId(mDb.getFoodRecordDao().insert(fr)[0]);

        //Add Eo to the database
        mDb.getEatingOccasionDao().insert(eo);


        FoodRecordRepository foodRepo = new FoodRecordRepository(mActivityRule.getActivity().getApplication());
        FoodRecord todaysFoodRecord = foodRepo.getTodaysFoodRecordFor(hm.getUid(), today);

        //Check the food records are the same
        assertEquals(fr, todaysFoodRecord);
        //Check that the food record contains the correct eating occasions
        assertEquals(todaysFoodRecord.getEatingOccasions(), eatingOcassions);


    }

    //Check that if a food record exists for today it is returned with all eating occasions
    @Test
    public void getTodaysFoodRecordWithValidFoodRecordInDatabaseWithEatingOccasionWithFoodItem() throws Exception {
        //Set up Database with:
        // 1 hosuehold member no food records
        AppDatabase.setInstance(mDb);
        Date today = new Date();
        Date yesterday = new Date(today.getTime() - (1000 * 60 * 60 * 24));

        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        mDb.getHouseholdMemberDao().insert(hm);

        FoodRecord fr = new FoodRecord(hm.getUid());

        //Create Eating occasion
        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId((long) 1);
        eo.setFoodRecordId((long) 1);
        eo.setStartTime(yesterday);
        eo.setEndTime(new Date(yesterday.getTime() + 1000 * 60 * 30));
        List<EatingOccasion> eatingOcassions = new ArrayList<EatingOccasion>();
        eatingOcassions.add(eo);

        //Add eating occasion to the food record.
        fr.setEatingOccasions(eatingOcassions);
        fr.setFoodRecordId(mDb.getFoodRecordDao().insert(fr)[0]);

        //Add Eo to the database
        mDb.getEatingOccasionDao().insert(eo);

        FoodItem fi = new FoodItem();
        fi.setFoodItemId(1);
        fi.setEatingOccasionId(1);
        mDb.getFoodItemDao().insert(fi);


        FoodRecordRepository foodRepo = new FoodRecordRepository(mActivityRule.getActivity().getApplication());
        FoodRecord todaysFoodRecord = foodRepo.getTodaysFoodRecordFor(hm.getUid(), today);

        //Check the food records are the same
        assertEquals(fr, todaysFoodRecord);
        //Check that the food record contains the correct eating occasions
        assertEquals(todaysFoodRecord.getEatingOccasions(), eatingOcassions);

        assertThat(todaysFoodRecord.getEatingOccasions().get(0).getFoodItems(), is(notNullValue()));
        assertThat(todaysFoodRecord.getEatingOccasions().get(0).getFoodItems().size(), is(1));


    }

    @Test
    public void addNewFoodRecordNoEatingOccasions() throws Exception {
        //Create Household member to have food record (to satisfy foreign key constraint)
        //Set up Database with:
        // 1 hosuehold member no food records
        AppDatabase.setInstance(mDb);
        Date today = new Date();

        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        mDb.getHouseholdMemberDao().insert(hm);

        //Create Food Record
        FoodRecord fr = new FoodRecord(hm.getUid());

        fr.setFoodRecordId(mDb.getFoodRecordDao().insert(fr)[0]);

        FoodRecordRepository foodRepo = new FoodRecordRepository(mActivityRule.getActivity().getApplication());
        FoodRecord todaysFoodRecord = foodRepo.getTodaysFoodRecordFor(hm.getUid(), today);
        //Check the food records are the same
        assertEquals(fr, todaysFoodRecord);

    }

    @Test
    public void addNewFoodRecordMultipleEatingOccasions() throws Exception {
        //Create Household member to have food record (to satisfy foreign key constraint)
        //Set up Database with:
        // 1 hosuehold member no food records
        AppDatabase.setInstance(mDb);
        Date today = new Date();
        Date yesterday = new Date(today.getTime() - (1000 * 60 * 60 * 24));

        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        mDb.getHouseholdMemberDao().insert(hm);

        //Create Food Record
        FoodRecord fr = new FoodRecord(hm.getUid()); //Create Eating occasion
        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId((long) 1);
        eo.setFoodRecordId((long) 1);
        eo.setStartTime(yesterday);
        eo.setEndTime(new Date(yesterday.getTime() + 1000 * 60 * 30));
        List<EatingOccasion> eatingOcassions = new ArrayList<EatingOccasion>();
        eatingOcassions.add(eo);

        //Add eating occasion to the food record.
        fr.setEatingOccasions(eatingOcassions);
        fr.setFoodRecordId(mDb.getFoodRecordDao().insert(fr)[0]);

        //Add Eo to the database
        mDb.getEatingOccasionDao().insert(eo);

        FoodRecordRepository foodRepo = new FoodRecordRepository(mActivityRule.getActivity().getApplication());
        FoodRecord todaysFoodRecord = foodRepo.getTodaysFoodRecordFor(hm.getUid(), today);
        //Check the food records are the same
        assertEquals(fr, todaysFoodRecord);

    }


}
