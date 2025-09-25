package au.edu.newcastle.jnc985.visida.viewmodel;

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
import bo.FoodRecordViewModel;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;
import bo.typeconverter.DateTypeConverter;

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

/**
 * Created by Josh on 19-Dec-17.
 */
@RunWith(AndroidJUnit4.class)
public class FoodRecordViewModelTest {

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

    //Check that getTodaysFoodRecordFor returns a single food record when no food records exist
    @Test
    public void getTodaysFoodRecordForEmptyDatabase() throws Exception {
        //Set up Database with:
        // 1 hosuehold member no food records
        //Prepare the static class
        AppDatabase.setInstance(mDb);

        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        mDb.getHouseholdMemberDao().insert(hm);

        //Set up the ViewModel class
        FoodRecordViewModel frViewModel = new FoodRecordViewModel(mActivityRule.getActivity().getApplication());

        //Get the food records for the household member
        FoodRecord todaysRecord = frViewModel.getTodaysFoodRecordFor(hm);
        //Check the fields of the Food Record that it is a new FR for today
        assertThat(todaysRecord.getEatingOccasions(), is(empty()));

        Date today = new Date();
        //Check the dates are the same day
        assertEquals(DateTypeConverter.dateToTimestamp(today), DateTypeConverter.dateToTimestamp(todaysRecord.getDate()));
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

        //Set up the ViewModel class
        FoodRecordViewModel frViewModel = new FoodRecordViewModel(mActivityRule.getActivity().getApplication());

        FoodRecord todaysFoodRecord = frViewModel.getTodaysFoodRecordFor(hm);

        Date today = new Date();
        assertEquals(DateTypeConverter.dateToTimestamp(today), DateTypeConverter.dateToTimestamp(todaysFoodRecord.getDate()));
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


        //Set up the ViewModel class
        FoodRecordViewModel frViewModel = new FoodRecordViewModel(mActivityRule.getActivity().getApplication());
        FoodRecord todaysFoodRecord = frViewModel.getTodaysFoodRecordFor(hm);

        //Check the food records are the same
        assertEquals(fr, todaysFoodRecord);
        //Check that the food record contains the correct eating occasions
        assertEquals(todaysFoodRecord.getEatingOccasions(), eatingOcassions);
    }

    @Test
    public void newFoodRecordContainsTheHouseholdMember() throws Exception {
        //Set up Database with:
        // 1 hosuehold member no food records
        //Prepare the static class
        AppDatabase.setInstance(mDb);

        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        mDb.getHouseholdMemberDao().insert(hm);

        //Set up the ViewModel class
        FoodRecordViewModel frViewModel = new FoodRecordViewModel(mActivityRule.getActivity().getApplication());

        //Get the food records for the household member
        FoodRecord todaysRecord = frViewModel.getTodaysFoodRecordFor(hm);
        //Check the fields of the Food Record that it is a new FR for today
        assertThat(todaysRecord.getEatingOccasions(), is(empty()));
        assertThat(todaysRecord.getHouseholdMember(), is(hm));

        Date today = new Date();
        //Check the dates are the same day
        assertEquals(DateTypeConverter.dateToTimestamp(today), DateTypeConverter.dateToTimestamp(todaysRecord.getDate()));
    }

    @Test
    public void oldFoodRecordContainsTheHouseholdMember() throws Exception {
        //Set up Database with:
        // 1 hosuehold member no food records
        AppDatabase.setInstance(mDb);

        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        mDb.getHouseholdMemberDao().insert(hm);

        FoodRecord fr = new FoodRecord(hm.getUid());
        fr.setFoodRecordId(mDb.getFoodRecordDao().insert(fr)[0]);

        //Set up the ViewModel class
        FoodRecordViewModel frViewModel = new FoodRecordViewModel(mActivityRule.getActivity().getApplication());

        FoodRecord todaysFoodRecord = frViewModel.getTodaysFoodRecordFor(hm);
        assertThat(todaysFoodRecord.getHouseholdMember(), is(hm));

        Date today = new Date();
        assertEquals(DateTypeConverter.dateToTimestamp(today), DateTypeConverter.dateToTimestamp(todaysFoodRecord.getDate()));
        assertEquals(fr, todaysFoodRecord);
    }
}
