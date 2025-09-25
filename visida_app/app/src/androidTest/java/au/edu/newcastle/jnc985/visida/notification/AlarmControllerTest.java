package au.edu.newcastle.jnc985.visida.notification;

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

import au.edu.newcastle.jnc985.visida.TestUtilities;
import au.edu.newcastle.jnc985.visida.activity.MainActivity;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodRecord;
import notification.AlarmController;
import notification.NotificationPublisher;

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.CoreMatchers.is;

/**
 * Created by Josh on 19-Dec-17.
 */
@RunWith(AndroidJUnit4.class)
public class AlarmControllerTest {

    //Rule so we can access the application
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    private Context mContext;
    private String ppid = "ppid";
    private long FR_ID = 1;
    private long EO_ID = 1;


    @Before
    public void setupContext() {
        mContext = InstrumentationRegistry.getTargetContext();
    }

    @After
    public void cancelAllAlarms() {
        AlarmController ac = new AlarmController(mActivityRule.getActivity().getApplication());
        ac.cancelUnfinalizedEatingOccasionNotification(EO_ID);
        //ac.cancelRecordReviewNotification(ppid);
    }

    @Test
    public void alarmIsSetForEatingOccasion() {
        EatingOccasion eo = scheduleEatingOccasion();

        //Build the expected intent
        Intent expectedIntent = new Intent(mActivityRule.getActivity().getApplication(), NotificationPublisher.class);
        assertThat(TestUtilities.isAlarmSet(mActivityRule.getActivity().getApplication(), eo.getEatingOccasionId().intValue(), expectedIntent), is(true));
    }

    @Test
    public void alarmIsSetForFoodRecord() {
        FoodRecord fr = scheduleFoodRecord();

        //Build the expected intent
        int hash = ppid.hashCode();
        Intent expectedIntent = new Intent(mActivityRule.getActivity().getApplication(), NotificationPublisher.class);
        assertThat(TestUtilities.isAlarmSet(mActivityRule.getActivity().getApplication(), hash, expectedIntent), is(true));
    }

    private EatingOccasion scheduleEatingOccasion() {
        EatingOccasion dummyEo = new EatingOccasion();
        dummyEo.setEatingOccasionId(EO_ID);
        dummyEo.setFoodRecordId(FR_ID);
        AlarmController alarmController = new AlarmController(mActivityRule.getActivity().getApplication());
        alarmController.scheduleUnfinalizedEatingOccasionNotification(ppid, dummyEo);

        return dummyEo;
    }

    private FoodRecord scheduleFoodRecord() {
        //Create dummy food reccord
        FoodRecord dummyFr = new FoodRecord(1);
        dummyFr.setFoodRecordId(FR_ID);

        AlarmController alarmController = new AlarmController(mActivityRule.getActivity().getApplication());
        alarmController.scheduleRecordReviewNotification(ppid, FR_ID);
        return dummyFr;
    }

    @Test
    public void alarmIsCancelledForFoodRecord() {
        //Create dummy food reccord
        FoodRecord fr = scheduleFoodRecord();

        //Build the expected intent
        int notificationId = ppid.hashCode();
        Intent expectedIntent = new Intent(mActivityRule.getActivity().getApplication(), NotificationPublisher.class);
        assertThat(TestUtilities.isAlarmSet(mActivityRule.getActivity().getApplication(), notificationId, expectedIntent), is(true));

        //Cancel The alarm
        AlarmController ac = new AlarmController(mActivityRule.getActivity().getApplication());
        ac.cancelRecordReviewNotification(notificationId);
        assertThat(TestUtilities.isAlarmSet(mActivityRule.getActivity().getApplication(), notificationId, expectedIntent), is(false));
    }

    @Test
    public void alarmIsCancelledForEatingOccasion() {
        //Create dummy food reccord
        EatingOccasion eo = scheduleEatingOccasion();

        //Build the expected intent
        Intent expectedIntent = new Intent(mActivityRule.getActivity().getApplication(), NotificationPublisher.class);
        assertThat(TestUtilities.isAlarmSet(mActivityRule.getActivity().getApplication(), (int) eo.getEatingOccasionId().intValue(), expectedIntent), is(true));

        //Cancel The alarm
        AlarmController ac = new AlarmController(mActivityRule.getActivity().getApplication());
        ac.cancelUnfinalizedEatingOccasionNotification(eo.getFoodRecordId().intValue());
        assertThat(TestUtilities.isAlarmSet(mActivityRule.getActivity().getApplication(), eo.getEatingOccasionId().intValue(), expectedIntent), is(false));
    }
}
