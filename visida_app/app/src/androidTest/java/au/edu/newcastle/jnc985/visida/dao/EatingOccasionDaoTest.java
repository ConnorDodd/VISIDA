package au.edu.newcastle.jnc985.visida.dao;

import androidx.room.Room;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import androidx.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by Josh on 19-Dec-17.
 */
public class EatingOccasionDaoTest {
    private AppDatabase mDb;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
    }

    @After
    public void closeDb() {
        mDb.close();
    }

    /*
      @Query("SELECT * FROM eatingoccasion")
    List<EatingOccasion> getAll();

    @Insert
    void insert(EatingOccasion eatingOccasion);

    @Query("SELECT * FROM eatingoccasion WHERE foodRecordId IS :frId")
    List<EatingOccasion> getAllEatingOccasionsForFoodRecord(long frId);

     */

    @Test(expected = SQLiteConstraintException.class)
    public void insertNoFoodRecord() throws Exception {
        EatingOccasion newEO = new EatingOccasion();

        //Try to add Eating Occastion
        mDb.getEatingOccasionDao().insert(newEO);

        //Should Throw an exception
    }

    @Test
    public void insertWithRecord() throws Exception {
        EatingOccasion newEO = new EatingOccasion();

        //Insert Dummy HouseholdMmember for the FoodRecord
        mDb.getHouseholdMemberDao().insert(new HouseholdMember(1, "1", "", "", 1, false, false));

        //Insert a FoodRecord to satisfy the foreign key constraint
        FoodRecord newFr = new FoodRecord(1);
        newFr.setFoodRecordId(mDb.getFoodRecordDao().insert(newFr)[0]);

        newEO.setFoodRecordId(newFr.getFoodRecordId());
        //Try to add Eating Occastion
        newEO.setEatingOccasionId(mDb.getEatingOccasionDao().insert(newEO));

        //Check that it is inserted and is the same
        List<EatingOccasion> insertedEOs = mDb.getEatingOccasionDao().getAllEatingOccasionsForFoodRecord(newFr.getFoodRecordId());
        assertEquals(1, insertedEOs.size());

        assertEquals(newEO, insertedEOs.get(0));
    }

    @Test
    public void getAllEatingOccasionsForFoodRecordWithNoEatingOccasions() {
        //Insert Dummy HouseholdMmember for the FoodRecord
        mDb.getHouseholdMemberDao().insert(new HouseholdMember(1, "1", "", "", 1, false, false));

        //Insert a FoodRecord to satisfy the foreign key constraint
        FoodRecord newFr = new FoodRecord(1);
        newFr.setFoodRecordId(mDb.getFoodRecordDao().insert(newFr)[0]);

        //Check that it is inserted and is the same
        List<EatingOccasion> insertedEOs = mDb.getEatingOccasionDao().getAllEatingOccasionsForFoodRecord(newFr.getFoodRecordId());

        assertThat(insertedEOs, is(empty()));
    }

    @Test
    public void getAllEatingOccasoinsForFoodRecordWithMulitpleEatingOccasions() {
        EatingOccasion eo1 = new EatingOccasion();
        EatingOccasion eo2 = new EatingOccasion();
        EatingOccasion eo3 = new EatingOccasion();

        eo1.setEatingOccasionId((long) 1);
        eo2.setEatingOccasionId((long) 2);
        eo3.setEatingOccasionId((long) 3);

        eo1.setFoodRecordId((long) 1);
        eo2.setFoodRecordId((long) 1);
        eo3.setFoodRecordId((long) 2);

        //Insert Dummy HouseholdMmember for the FoodRecord
        mDb.getHouseholdMemberDao().insert(new HouseholdMember(1, "1", "", "", 1, false, false));
        mDb.getHouseholdMemberDao().insert(new HouseholdMember(2, "1", "", "", 1, false, false));

        //Insert a FoodRecord to satisfy the foreign key constraint
        FoodRecord newFr = new FoodRecord(1);
        FoodRecord newFr2 = new FoodRecord(2);
        newFr.setFoodRecordId(mDb.getFoodRecordDao().insert(newFr)[0]);
        newFr2.setFoodRecordId(mDb.getFoodRecordDao().insert(newFr2)[0]);

        //Insert the EatingOccasions
        mDb.getEatingOccasionDao().insert(eo1);
        mDb.getEatingOccasionDao().insert(eo2);
        mDb.getEatingOccasionDao().insert(eo3);

        //Check that all EOs were added
        List<EatingOccasion> allEos = mDb.getEatingOccasionDao().getAll();
        assertEquals(3, allEos.size());

        //Check that it is inserted and is the same
        List<EatingOccasion> insertedEOs = mDb.getEatingOccasionDao().getAllEatingOccasionsForFoodRecord(newFr.getFoodRecordId());

        assertThat(insertedEOs, is(not(empty())));
        assertEquals(2, insertedEOs.size());
    }

    @Test
    public void getAllNonFinalizedEatingOccasions() {
        EatingOccasion eo1 = new EatingOccasion();
        EatingOccasion eo2 = new EatingOccasion();
        EatingOccasion eo3 = new EatingOccasion();

        eo1.setEatingOccasionId((long) 1);
        eo2.setEatingOccasionId((long) 2);
        eo3.setEatingOccasionId((long) 3);

        eo1.setFoodRecordId((long) 1);
        eo2.setFoodRecordId((long) 1);
        eo3.setFoodRecordId((long) 1);

        eo2.setFinalized(true);

        mDb.getHouseholdMemberDao().insert(new HouseholdMember(1, "1", "", "", 1, false, false));

        FoodRecord newFr = new FoodRecord(1);
        newFr.setFoodRecordId(1);
        mDb.getFoodRecordDao().insert(newFr);

        //Insert the EatingOccasions
        mDb.getEatingOccasionDao().insert(eo1);
        mDb.getEatingOccasionDao().insert(eo2);
        mDb.getEatingOccasionDao().insert(eo3);

        //Check that all EOs were added
        List<EatingOccasion> allEos = mDb.getEatingOccasionDao().getAll();
        assertThat(allEos.size(), is(3));

        //Get all the non finalized eos
        List<EatingOccasion> nonFinalizedEos = mDb.getEatingOccasionDao().getAllNonFinalizedEatingOccasionsForFoodRecord(newFr.getFoodRecordId());
        assertThat(nonFinalizedEos.size(), is(2));
        assertThat(nonFinalizedEos, contains(eo1, eo3));
    }

}
