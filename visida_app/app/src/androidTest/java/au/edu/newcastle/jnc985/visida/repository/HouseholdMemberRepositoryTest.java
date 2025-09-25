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

import java.util.Date;

import au.edu.newcastle.jnc985.visida.activity.MainActivity;
import bo.HouseholdMemberRepository;
import bo.db.AppDatabase;
import bo.db.entity.HouseholdMember;

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.CoreMatchers.is;

/**
 * Created by Josh on 19-Dec-17.
 */
@RunWith(AndroidJUnit4.class)
public class HouseholdMemberRepositoryTest {

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
    public void participantHouseholdMemberIdExists() {
        //Set up Database with:
        // 1 hosuehold member no food records
        //Prepare the static class
        AppDatabase.setInstance(mDb);

        Date today = new Date();


        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        String ppid = "ppid";
        hm.setParticipantHouseholdMemberId(ppid);
        mDb.getHouseholdMemberDao().insert(hm);


        HouseholdMemberRepository hmRepo = new HouseholdMemberRepository(mActivityRule.getActivity().getApplication());
        Boolean ppidExists = hmRepo.participantIdExists(ppid);

        assertThat(ppidExists, is(true));

        Boolean ppidDoesntExist = hmRepo.participantIdExists("NotPPID");
        assertThat(ppidDoesntExist, is(false));

    }

}
