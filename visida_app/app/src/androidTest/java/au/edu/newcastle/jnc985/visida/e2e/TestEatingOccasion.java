package au.edu.newcastle.jnc985.visida.e2e;

import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.filters.LargeTest;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.activity.MainActivity;
import bo.Utilities;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;
import bo.typeconverter.DateTypeConverter;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.SETUP;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

/**
 * Created by jnc985 on 09-Jan-18.
 */

public class TestEatingOccasion {

    @Rule
    public IntentsTestRule<MainActivity> eatingOccasiontTestRule = new IntentsTestRule<>(MainActivity.class);

    @Mock
    private AppDatabase mDb;
    private Context context;

    private HouseholdMember hm1;
    private HouseholdMember hm2;

    //Create a date object for today
    private Date today = new Date();

    private File mMediaDirectory;
    private String MEDIA_DIR = "MEDIA";

    @Before
    public void createDb() {
        context = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();

        //Create 2 Household Members and add to database
        hm1 = new HouseholdMember(1, "1", "HM1", "", 1, false, false);
        hm1.setParticipantHouseholdMemberId("ppid");
        hm2 = new HouseholdMember(2, "1", "HM2", "", 2, false, false);
        hm2.setParticipantHouseholdMemberId("ppid2");
        mDb.getHouseholdMemberDao().insert(hm1, hm2);

        AppDatabase.setInstance(mDb);
    }

    @Before
    public void setUpMediaDirectory() {
        mMediaDirectory = Utilities.getMediaDirectory(context);
        mMediaDirectory.mkdirs();
        //Make sure directory is empty
        for (File f : mMediaDirectory.listFiles()) {
            if (f.exists()) {
                f.delete();
            }
        }
    }

    @After
    public void closeDb() {
        mDb.close();
    }

    @After
    public void deleteMediaDirectory() {
        for (File f : mMediaDirectory.listFiles()) {
            if (f.exists()) {
                f.delete();
            }
        }
    }

    @Test
    @LargeTest
    public void testCreateFirstEatingOccasionWithNoFoodRecord() {
        //Set the state to set up:
        //Check shared preferences if the household member have been set up
        SharedPreferences sharedPreferences = eatingOccasiontTestRule.getActivity().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SETUP, true);
        editor.commit();

        eatingOccasiontTestRule.finishActivity();
        eatingOccasiontTestRule.launchActivity(new Intent());

        //On Main Activity. Click the eat button
        onView(withId(R.id.btnEat)).perform(click());
        //Make sure we are on the select household member page
        onView(withId(R.id.listViewHouseholdMembers)).check(matches(isDisplayed()));

        //Click HM1
        onView(withId(R.id.listViewHouseholdMembers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //Check the database now contains a single new food record
        List<FoodRecord> todaysFodRecords = mDb.getFoodRecordDao().getTodaysFoodRecordForHouseholdMember(hm1.getUid(), DateTypeConverter.dateToTimestamp(today));
        assertThat(1, is(todaysFodRecords.size()));

        FoodRecord fr = todaysFodRecords.get(0);

        //Make sure the eating occasion is created new with current time (+- 1min)
        List<EatingOccasion> foodRecordsEatingOccasions = mDb.getEatingOccasionDao().getAllEatingOccasionsForFoodRecord(fr.getFoodRecordId());
        assertThat(foodRecordsEatingOccasions.size(), is(1));


        //Click add food Item
        onView(withId(R.id.btnAddFoodItem)).perform(click());

        //Check that we are now on the Capture Image Activity
        onView(withId(R.id.btnTakePicture)).check(matches(isDisplayed()));

        //Capture the image
        onView(withId(R.id.btnTakePicture)).perform(click());

        //Sleep for a second to let the image save
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.btnImageAccept)).perform(click());

        //Check that the photo has been captured
        File[] output = mMediaDirectory.listFiles();
        assertThat(output.length, is(1));
        File imageFile = output[0];
        assertThat(imageFile.getName(), containsString("jpg"));

        //Check that we are in the audio activity
        onView(withId(R.id.btnRecordAudio)).check(matches(isDisplayed()));

        //Click the record audio button
        onView(withId(R.id.btnRecordAudio)).perform(click());

        //Sleep for a second to record something
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Click stop recording
        onView(withId(R.id.btnAudioFile)).perform(click());

        //Click accept
        onView(withId(R.id.btnAudioAccept)).perform(click());

        //Check that audio file has been created.
        output = mMediaDirectory.listFiles();
        assertThat(output.length, is(2));
        assertThat(Arrays.asList(output), hasItems(nameContains("mp3")));

        //Check that foodItem has been created
        List<FoodItem> foodItems = mDb.getFoodItemDao().getAll();
        assertThat(foodItems.size(), is(1));

        //Check that we are back on the select household member activity
        onView(withId(R.id.rvFoodItemList)).check(matches(isDisplayed()));
    }

    private Matcher<File> nameContains(String str) {
        return new FeatureMatcher<File, String>(containsString(str), "nameContains", "name") {
            @Override
            protected String featureValueOf(File actual) {
                return actual.getName();
            }
        };
    }
}
