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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import au.edu.newcastle.jnc985.visida.activity.MainActivity;
import bo.EatingOccasionRepository;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by Josh on 19-Dec-17.
 */
@RunWith(AndroidJUnit4.class)
public class EatingOccasionRepositoryTest {

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


    @Test
    public void testGetEatingOccasionForFoodRecordWithSingleEatingOccasionNoFoodItems() {
        //Set up Database with:
        // 1 hosuehold member no food records
        //Prepare the static class
        AppDatabase.setInstance(mDb);

        Date today = new Date();


        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        mDb.getHouseholdMemberDao().insert(hm);

        FoodRecord fr = new FoodRecord(hm.getUid());

        //Create Eating occasion
        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId((long) 1);
        eo.setFoodRecordId((long) 1);
        eo.setStartTime(today);
        List<EatingOccasion> eatingOcassions = new ArrayList<EatingOccasion>();
        eatingOcassions.add(eo);

        //Add eating occasion to the food record.
        fr.setEatingOccasions(eatingOcassions);
        fr.setFoodRecordId(mDb.getFoodRecordDao().insert(fr)[0]);

        //Add Eo to the database
        mDb.getEatingOccasionDao().insert(eo);

        EatingOccasionRepository eoRepo = new EatingOccasionRepository(mActivityRule.getActivity().getApplication());
        List<EatingOccasion> eatingOcacsions = eoRepo.getEatingOccasions(fr.getFoodRecordId());

        //Check the food records are the same
        assertThat(eatingOcacsions.size(), is(1));
        assertThat(eatingOcacsions.get(0).getFoodItems(), is(notNullValue()));

    }

    @Test
    public void testGetEatingOccasionForFoodRecordWithSingleEatingOccasionMultipleFoodItems() {
        //Set up Database with:
        // 1 hosuehold member no food records
        //Prepare the static class
        AppDatabase.setInstance(mDb);

        Date today = new Date();


        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        mDb.getHouseholdMemberDao().insert(hm);

        FoodRecord fr = new FoodRecord(hm.getUid());

        //Create Eating occasion
        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId((long) 1);
        eo.setFoodRecordId((long) 1);
        eo.setStartTime(today);
        List<EatingOccasion> eatingOcassions = new ArrayList<EatingOccasion>();
        eatingOcassions.add(eo);

        //Add eating occasion to the food record.
        fr.setEatingOccasions(eatingOcassions);
        fr.setFoodRecordId(mDb.getFoodRecordDao().insert(fr)[0]);

        //Add the eating occasion to the databse
        mDb.getEatingOccasionDao().insert(eo);

        FoodItem fi1 = new FoodItem();
        fi1.setFoodItemId(1);
        fi1.setEatingOccasionId(1);
        FoodItem fi2 = new FoodItem();
        fi2.setFoodItemId(2);
        fi2.setEatingOccasionId(1);

        //Add food items to the database
        mDb.getFoodItemDao().insert(fi1);
        mDb.getFoodItemDao().insert(fi2);

        EatingOccasionRepository eoRepo = new EatingOccasionRepository(mActivityRule.getActivity().getApplication());
        List<EatingOccasion> eatingOcacsions = eoRepo.getEatingOccasions(fr.getFoodRecordId());

        //Check the food records are the same
        assertThat(eatingOcacsions.size(), is(1));
        assertThat(eatingOcacsions.get(0).getFoodItems(), is(notNullValue()));
        assertThat(eatingOcacsions.get(0).getFoodItems(), is(not(empty())));
        assertThat(eatingOcacsions.get(0).getFoodItems().size(), is(2));
    }

    @Test
    public void testGetEatingOccasionForFoodRecordWithMultipleEatingOccasionMultipleFoodItems() {
        //Set up Database with:
        // 1 hosuehold member no food records
        //Prepare the static class
        AppDatabase.setInstance(mDb);

        Date today = new Date();


        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        mDb.getHouseholdMemberDao().insert(hm);

        FoodRecord fr = new FoodRecord(hm.getUid());

        //Create Eating occasion
        EatingOccasion eo1 = new EatingOccasion();
        eo1.setEatingOccasionId((long) 1);
        eo1.setFoodRecordId((long) 1);
        eo1.setStartTime(today);
        EatingOccasion eo2 = new EatingOccasion();
        eo2.setEatingOccasionId((long) 2);
        eo2.setFoodRecordId((long) 1);
        eo2.setStartTime(today);
        List<EatingOccasion> eatingOcassions = new ArrayList<EatingOccasion>();
        eatingOcassions.add(eo1);
        eatingOcassions.add(eo2);

        //Add eating occasion to the food record.
        fr.setEatingOccasions(eatingOcassions);
        fr.setFoodRecordId(mDb.getFoodRecordDao().insert(fr)[0]);

        //Add the eating occasions to the databse
        mDb.getEatingOccasionDao().insert(eo1);
        mDb.getEatingOccasionDao().insert(eo2);

        FoodItem fi1 = new FoodItem();
        fi1.setFoodItemId(1);
        fi1.setEatingOccasionId(1);
        FoodItem fi2 = new FoodItem();
        fi2.setFoodItemId(2);
        fi2.setEatingOccasionId(1);

        //Add food items to the database
        mDb.getFoodItemDao().insert(fi1);
        mDb.getFoodItemDao().insert(fi2);

        EatingOccasionRepository eoRepo = new EatingOccasionRepository(mActivityRule.getActivity().getApplication());
        List<EatingOccasion> eatingOcacsions = eoRepo.getEatingOccasions(fr.getFoodRecordId());

        //Check the food records are the same
        assertThat(eatingOcacsions.size(), is(2));
        assertThat(eatingOcacsions.get(0).getFoodItems(), is(notNullValue()));
        assertThat(eatingOcacsions.get(0).getFoodItems(), is(not(empty())));
        assertThat(eatingOcacsions.get(0).getFoodItems().size(), is(2));
    }


    @Test
    public void getNonFinalizedEatingOccasionForHouseholdMemberNoFinalizedForSingleDay() {
        //Set up Database with:
        // 1 household member
        // 1 Food Record
        // 2 Non Finalized Eating Occasions
        //Prepare the static class
        AppDatabase.setInstance(mDb);

        Date today = new Date();

        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        mDb.getHouseholdMemberDao().insert(hm);

        FoodRecord fr = new FoodRecord(hm.getUid());
        fr.setFoodRecordId((long) 1);
        mDb.getFoodRecordDao().insert(fr);

        //Create Eating occasion
        EatingOccasion eo1 = new EatingOccasion();
        eo1.setEatingOccasionId((long) 1);
        eo1.setFinalized(false);
        eo1.setFoodRecordId(fr.getFoodRecordId());
        eo1.setStartTime(today);
        EatingOccasion eo2 = new EatingOccasion();
        eo2.setEatingOccasionId((long) 2);
        eo1.setFinalized(false);
        eo2.setFoodRecordId(fr.getFoodRecordId());
        eo2.setStartTime(today);

        //Add the eating occasions to the databse
        mDb.getEatingOccasionDao().insert(eo1, eo2);

        EatingOccasionRepository eoRepo = new EatingOccasionRepository(mActivityRule.getActivity().getApplication());
        List<EatingOccasion> eos = eoRepo.getNonFinalizedEatingOccasionsForHouseholdMember(hm.getUid());

        assertThat(eos.size(), is(2));
    }

    @Test
    public void getNonFinalizedEatingOccasionForHouseholdMemberFinalizedAndNonFinalizedForSingleDay() {
        //Set up Database with:
        // 1 household member
        // 1 Food Record
        // 1 Non Finalized Eating Occasions
        // 1 Finalized EO
        //Prepare the static class
        AppDatabase.setInstance(mDb);

        Date today = new Date();

        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        mDb.getHouseholdMemberDao().insert(hm);

        FoodRecord fr = new FoodRecord(hm.getUid());
        fr.setFoodRecordId((long) 1);
        mDb.getFoodRecordDao().insert(fr);

        //Create Eating occasion
        EatingOccasion eo1 = new EatingOccasion();
        eo1.setEatingOccasionId((long) 1);
        eo1.setFinalized(false);
        eo1.setFoodRecordId(fr.getFoodRecordId());
        eo1.setStartTime(today);
        EatingOccasion eo2 = new EatingOccasion();
        eo2.setEatingOccasionId((long) 2);
        eo1.setFinalized(true);
        eo2.setFoodRecordId(fr.getFoodRecordId());
        eo2.setStartTime(today);

        //Add the eating occasions to the databse
        mDb.getEatingOccasionDao().insert(eo1, eo2);

        EatingOccasionRepository eoRepo = new EatingOccasionRepository(mActivityRule.getActivity().getApplication());
        List<EatingOccasion> eos = eoRepo.getNonFinalizedEatingOccasionsForHouseholdMember(hm.getUid());

        assertThat(eos.size(), is(1));
    }

    @Test
    public void getNonFinalizedEatingOccasionForHouseholdMemberFinalizedAndNonFinalizedForMultiDay() {
        //Set up Database with:
        // 1 household member
        // 1 Food Record
        // 1 Non Finalized Eating Occasion Today
        // 1 Non Finalized Eating Occasion 2 days ago
        //Prepare the static class
        AppDatabase.setInstance(mDb);

        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DATE, -2);
        Date before = cal.getTime();

        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        mDb.getHouseholdMemberDao().insert(hm);

        FoodRecord fr = new FoodRecord(hm.getUid());
        fr.setFoodRecordId((long) 1);
        mDb.getFoodRecordDao().insert(fr);

        //Create Eating occasion
        EatingOccasion eo1 = new EatingOccasion();
        eo1.setEatingOccasionId((long) 1);
        eo1.setFinalized(false);
        eo1.setFoodRecordId(fr.getFoodRecordId());
        eo1.setStartTime(today);
        EatingOccasion eo2 = new EatingOccasion();
        eo2.setEatingOccasionId((long) 2);
        eo1.setFinalized(false);
        eo2.setFoodRecordId(fr.getFoodRecordId());
        eo2.setStartTime(before);

        //Add the eating occasions to the databse
        mDb.getEatingOccasionDao().insert(eo1, eo2);

        EatingOccasionRepository eoRepo = new EatingOccasionRepository(mActivityRule.getActivity().getApplication());
        List<EatingOccasion> eos = eoRepo.getNonFinalizedEatingOccasionsForHouseholdMember(hm.getUid());

        assertThat(eos.size(), is(2));
    }
}
