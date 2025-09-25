package au.edu.newcastle.jnc985.visida.dao;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import android.content.Context;
import androidx.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import au.edu.newcastle.jnc985.visida.TestUtilities;
import bo.db.AppDatabase;
import bo.db.dao.FoodRecordDao;
import bo.db.dao.HouseholdMemberDao;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created by jnc985 on 30-Nov-17.
 */

public class HouseholdMemberDaoTest {

    //Rule which swaps the background executor used by Architecture Components with one that executes tasks synchronously
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private Context mContext;
    private HouseholdMemberDao hmDao;
    private FoodRecordDao frDao;
    private AppDatabase mDb;

    @Before
    public void createDb() {
        mContext = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).allowMainThreadQueries().build();
        hmDao = mDb.getHouseholdMemberDao();
        frDao = mDb.getFoodRecordDao();
    }

    @After
    public void closeDb() {
        mDb.close();
    }

    @Test
    public void insertSingleHouseholdMemberIntoEmptyDb() throws InterruptedException {
        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        hmDao.insert(hm);
        assertEquals(1, hm.getUid());

        List<HouseholdMember> hms = TestUtilities.getValue(hmDao.getAll());
        assertThat(hms.size(), is(1));
        assertThat(hms.get(0), equalTo(hm));
    }

    @Test
    public void insertFoodRecordWithCorrectHouseholdMembers() {
        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        hmDao.insert(hm);

        FoodRecord fr = new FoodRecord(1);
        frDao.insert(fr);
    }

    @Test
    public void getHouseholdMemberByListOfIds() {
        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        HouseholdMember hm2 = new HouseholdMember();
        hm2.setUid(2);
        HouseholdMember hm3 = new HouseholdMember();
        hm3.setUid(3);
        hmDao.insert(hm, hm2, hm3);

        List<Long> ids = new ArrayList<>();
        ids.add((long) 1);
        ids.add((long) 3);

        List<HouseholdMember> hms = hmDao.getHouseholdMembers(ids);
        assertThat(hms.size(), is(2));
    }

    @Test
    public void participantIdExistsBothUpperAndLowerCase(){
        //Add a household member with known participant id
        String ppid = "PPID";

        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        hm.setParticipantHouseholdMemberId(ppid);
        hmDao.insert(hm);


        Integer exists = hmDao.participantIdExists(ppid);
        assertThat(exists, is(1));

        Integer existsLower = hmDao.participantIdExists(ppid.toLowerCase());
        assertThat(existsLower, is(1));

        Integer doesntExist = hmDao.participantIdExists("NOTPPID");
        assertThat(doesntExist, is(0));
    }


}
