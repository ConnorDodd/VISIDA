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
import java.util.List;

import au.edu.newcastle.jnc985.visida.activity.MainActivity;
import bo.db.AppDatabase;
import bo.db.entity.Reminder;
import bo.scheduler.ReminderRepository;

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.CoreMatchers.is;

/**
 * Created by Josh on 19-Dec-17.
 */
@RunWith(AndroidJUnit4.class)
public class ReminderRepositoryTest {

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
    public void insertWorks(){
        AppDatabase.setInstance(mDb);

        List<Reminder> rs = mDb.getReminderDao().getReminders();
        assertThat(rs.size(), is(0));

        Reminder r1 = new Reminder();
        r1.setReminderDay(1);
        r1.setDate(new Date());

        ReminderRepository repo = new ReminderRepository(mActivityRule.getActivity().getApplication());
        repo.addReminder(r1);

        //Sleep here to let the instert complete.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        rs = mDb.getReminderDao().getReminders();
        assertThat(rs.size(), is(1));
    }


    @Test
    public void testRemindersAreOrderedByDayInsertedInOrder() throws InterruptedException{
        //Set up Database with:
        // 1 hosuehold member no food records
        //Prepare the static class
        AppDatabase.setInstance(mDb);

        Reminder r1 = new Reminder();
        r1.setReminderDay(1);
        r1.setDate(new Date());

        Reminder r2 = new Reminder();
        r2.setReminderDay(2);
        r2.setDate(new Date());

        Reminder r3 = new Reminder();
        r3.setReminderDay(3);
        r3.setDate(new Date());

        Reminder r4 = new Reminder();
        r4.setReminderDay(4);
        r4.setDate(new Date());

        Reminder r5 = new Reminder();
        r5.setReminderDay(5);
        r5.setDate(new Date());

        Reminder r6 = new Reminder();
        r6.setReminderDay(6);
        r6.setDate(new Date());

        Reminder r7 = new Reminder();
        r7.setReminderDay(7);
        r7.setDate(new Date());

        mDb.getReminderDao().insert(r1,r2,r3,r4,r5,r6,r7);

        ReminderRepository repo = new ReminderRepository(mActivityRule.getActivity().getApplication());
        List<Reminder> rs = repo.getReminders();

        Reminder r = rs.get(0);
        assertThat(r.getReminderDay(), is(1));
        r = rs.get(1);
        assertThat(r.getReminderDay(), is(2));
        r = rs.get(2);
        assertThat(r.getReminderDay(), is(3));
        r = rs.get(3);
        assertThat(r.getReminderDay(), is(4));
        r = rs.get(4);
        assertThat(r.getReminderDay(), is(5));
        r = rs.get(5);
        assertThat(r.getReminderDay(), is(6));
        r = rs.get(6);
        assertThat(r.getReminderDay(), is(7));
    }

    @Test
    public void testRemindersAreOrderedByDayInsertedOutOfOrder() throws InterruptedException{
        //Set up Database with:
        // 1 hosuehold member no food records
        //Prepare the static class
        AppDatabase.setInstance(mDb);

        Reminder r1 = new Reminder();
        r1.setReminderDay(1);
        r1.setDate(new Date());

        Reminder r2 = new Reminder();
        r2.setReminderDay(2);
        r2.setDate(new Date());

        Reminder r3 = new Reminder();
        r3.setReminderDay(3);
        r3.setDate(new Date());

        Reminder r4 = new Reminder();
        r4.setReminderDay(4);
        r4.setDate(new Date());

        Reminder r5 = new Reminder();
        r5.setReminderDay(5);
        r5.setDate(new Date());

        Reminder r6 = new Reminder();
        r6.setReminderDay(6);
        r6.setDate(new Date());

        Reminder r7 = new Reminder();
        r7.setReminderDay(7);
        r7.setDate(new Date());

        mDb.getReminderDao().insert(r5,r1,r4,r6,r2,r7,r3);

        ReminderRepository repo = new ReminderRepository(mActivityRule.getActivity().getApplication());
        List<Reminder> rs = repo.getReminders();

        Reminder r = rs.get(0);
        assertThat(r.getReminderDay(), is(1));
        r = rs.get(1);
        assertThat(r.getReminderDay(), is(2));
        r = rs.get(2);
        assertThat(r.getReminderDay(), is(3));
        r = rs.get(3);
        assertThat(r.getReminderDay(), is(4));
        r = rs.get(4);
        assertThat(r.getReminderDay(), is(5));
        r = rs.get(5);
        assertThat(r.getReminderDay(), is(6));
        r = rs.get(6);
        assertThat(r.getReminderDay(), is(7));
    }

}