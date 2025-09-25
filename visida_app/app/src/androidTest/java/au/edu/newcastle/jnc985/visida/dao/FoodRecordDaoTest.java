package au.edu.newcastle.jnc985.visida.dao;

import androidx.room.Room;
import android.content.Context;
import androidx.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bo.db.AppDatabase;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;
import bo.typeconverter.DateTypeConverter;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * Created by Josh on 19-Dec-17.
 */
public class FoodRecordDaoTest {
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


    @Test(expected = android.database.sqlite.SQLiteConstraintException.class)
    public void insertFoodRecordWithNoHouseholdMembers() {
        FoodRecord fr = new FoodRecord(1);
        mDb.getFoodRecordDao().insert(fr);
    }

    @Test
    public void insert() throws Exception {
        FoodRecord newFr = new FoodRecord(1);
        //Insert a hosuehold member to satisfy the foreign key constraint
        //int uid, int household, String name, String avatar, int age, boolean isMother, boolean isBreastfed
        mDb.getHouseholdMemberDao().insert(new HouseholdMember(1, "1", "", "", 1, false, false));
        mDb.getFoodRecordDao().insert(newFr);

        List<FoodRecord> fromDb = mDb.getFoodRecordDao().getAllFoodRecordsForHouseholdMember(1);
        assertFalse(fromDb.isEmpty());

    }

    @Test
    public void getAllFoodRecordsForHouseholdMember() throws Exception {
        //Fill sample household members
        HouseholdMember hm1 = new HouseholdMember();
        HouseholdMember hm2 = new HouseholdMember();
        HouseholdMember hm3 = new HouseholdMember();
        hm1.setUid(1);
        hm2.setUid(2);
        hm3.setUid(3);
        List<HouseholdMember> hms = new ArrayList<>();
        hms.add(hm1);
        hms.add(hm2);
        hms.add(hm3);

        mDb.getHouseholdMemberDao().insert(hms.toArray(new HouseholdMember[hms.size()]));

        //Add sample food records
        FoodRecord fr1 = new FoodRecord(1);
        FoodRecord fr2 = new FoodRecord(2);
        FoodRecord fr3 = new FoodRecord(3);
        FoodRecord fr4 = new FoodRecord(1);

        List<FoodRecord> foodRecords = new ArrayList<>();
        foodRecords.add(fr1);
        foodRecords.add(fr2);
        foodRecords.add(fr3);
        foodRecords.add(fr4);

        mDb.getFoodRecordDao().insert(foodRecords.toArray(new FoodRecord[foodRecords.size()]));

        //Get the food records for hm1
        List<FoodRecord> hm1sFoodRecords = mDb.getFoodRecordDao().getAllFoodRecordsForHouseholdMember(hm1.getUid());
        assertFalse(hm1sFoodRecords.isEmpty());
        assertThat(hm1sFoodRecords.size(), equalTo(2));
    }

    @Test
    public void getTodaysFoodRecordForHouseholdMember() throws Exception {
        Date today = new Date();
        Date yesterday = new Date(today.getTime() - (1000 * 60 * 60 * 24));

        HouseholdMember hm1 = new HouseholdMember();
        hm1.setUid(1);
        mDb.getHouseholdMemberDao().insert(hm1);

        //Add sample food records
        FoodRecord fr1 = new FoodRecord(1);
        FoodRecord fr2 = new FoodRecord(1);

        fr2.setFoodRecordId(2);
        fr2.setDate((today));
        fr1.setFoodRecordId(1);
        fr1.setDate(yesterday);

        List<FoodRecord> foodRecords = new ArrayList<>();
        foodRecords.add(fr1);
        foodRecords.add(fr2);

        mDb.getFoodRecordDao().insert(foodRecords.toArray(new FoodRecord[foodRecords.size()]));

        List<FoodRecord> hm1sFoodRecordToday = mDb.getFoodRecordDao().getTodaysFoodRecordForHouseholdMember(1, DateTypeConverter.dateToTimestamp(today));

        assertThat(hm1sFoodRecordToday.size(), equalTo(1));
        assertThat(hm1sFoodRecordToday.get(0), equalTo(fr2));
    }

}
