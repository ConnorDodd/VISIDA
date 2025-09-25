package au.edu.newcastle.jnc985.visida.dao;

import androidx.room.Room;
import android.content.Context;
import androidx.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Josh on 19-Dec-17.
 */
public class FoodItemDaoTest {
    private AppDatabase mDb;

    private HouseholdMember mHm = new HouseholdMember(1, "1", "", "", 1, false, false);

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
    }

    @After
    public void closeDb() {
        mDb.close();
    }

    @Test
    public void insertWithEatingOccasion() throws Exception {
        FoodItem fi = new FoodItem();

        EatingOccasion newEO = new EatingOccasion();

        //Insert Dummy HouseholdMmember for the FoodRecord
        mDb.getHouseholdMemberDao().insert(mHm);

        //Insert a FoodRecord to satisfy the foreign key constraint
        FoodRecord newFr = new FoodRecord(1);
        newFr.setFoodRecordId(mDb.getFoodRecordDao().insert(newFr)[0]);

        newEO.setFoodRecordId(newFr.getFoodRecordId());
        //Add Eating Occastion
        newEO.setEatingOccasionId(mDb.getEatingOccasionDao().insert(newEO));

        //Set up the FoodItem
        fi.setEatingOccasionId(newEO.getEatingOccasionId());

        //Add Fi to the database
        fi.setFoodItemId(mDb.getFoodItemDao().insert(fi));

        //Check that it is inserted and is the same
        List<FoodItem> insertedFis = mDb.getFoodItemDao().getAll();
        assertThat(insertedFis.size(), is(1));
        assertThat(insertedFis.get(0), is(fi));
    }

    @Test
    public void getAllFoodItemsForEatingOccasion() {
        //Insert Dummy HouseholdMmember for the FoodRecord
        mDb.getHouseholdMemberDao().insert(mHm);

        //Create dummy EatingOccasion
        EatingOccasion newEO = new EatingOccasion();

        //Insert a FoodRecord to satisfy the foreign key constraint
        FoodRecord newFr = new FoodRecord(1);
        newFr.setFoodRecordId(mDb.getFoodRecordDao().insert(newFr)[0]);

        newEO.setFoodRecordId(newFr.getFoodRecordId());
        //Add Eating Occastion
        newEO.setEatingOccasionId(mDb.getEatingOccasionDao().insert(newEO));

        //Create Two food Item for the Eating Occasion
        FoodItem fi1 = new FoodItem();
        FoodItem fi2 = new FoodItem();

        //Set the fiId's
        fi1.setFoodItemId(1);
        fi2.setFoodItemId(2);

        //Set the eoId's
        fi1.setEatingOccasionId(newEO.getEatingOccasionId());
        fi2.setEatingOccasionId(newEO.getEatingOccasionId());

        //Insert foodItems into database
        mDb.getFoodItemDao().insert(fi1);
        mDb.getFoodItemDao().insert(fi2);

        //Check that both were inserted and are the same and both are returned when queried for eo Id
        List<FoodItem> foodItemsForEO = mDb.getFoodItemDao().getAllFoodItemsForEatingOccasion(newEO.getEatingOccasionId());
        assertThat(foodItemsForEO.size(), is(2));
        assertThat(foodItemsForEO, containsInAnyOrder(fi1, fi2));

    }
}
