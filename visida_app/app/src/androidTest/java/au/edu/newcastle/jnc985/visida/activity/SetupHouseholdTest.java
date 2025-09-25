package au.edu.newcastle.jnc985.visida.activity;

import android.Manifest;
import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.TestUtilities;
import bo.HouseholdRepository;
import bo.Utilities;
import bo.db.AppDatabase;
import bo.db.entity.Household;
import bo.db.entity.HouseholdMember;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static bo.AppConstants.HOUSEHOLDID;
import static bo.AppConstants.HOUSEHOLDMEMBER;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.SETUP;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;

/**
 * Created by jnc985 on 30-Nov-17.
 */

@RunWith(MockitoJUnitRunner.class)
public class SetupHouseholdTest {

    //Intent to start activity
    Intent intent = new Intent();
    //Set up Rule
    @Rule
    //public IntentsTestRule<MainActivity> mIntentsTestRule = new IntentsTestRule<>(MainActivity.class, true, false);
    public ActivityTestRule<SetupHouseholdActivity> intentsTestRule = new ActivityTestRule<SetupHouseholdActivity>(SetupHouseholdActivity.class, true, false);

    @Rule
    public GrantPermissionRule mGrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);

    private AppDatabase mDb;

    private HouseholdMember hm1;
    private Context mContext;

    @Before
    public void setUp() throws Throwable {
        mContext = getInstrumentation().getTargetContext();
        setUpState();
        setupDatabase();

    }

    @After
    public void cleanUp() {
        Intents.release();
        mDb.close();
        File hm1Imag = new File(hm1.getAvatar());
        if (hm1Imag.exists()) {
            hm1Imag.delete();
        }
    }

    private void setUpState() {
        //Default to SETUP=false
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SETUP, false);
        editor.commit();
        Intents.init();
    }


    private void resetHouseholdUUID() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(HOUSEHOLDID);
        editor.commit();
    }

    private void setupDatabase() throws Throwable {
        Context context = getInstrumentation().getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        AppDatabase.setInstance(mDb);

        //Create the list of dummy household members
        List<HouseholdMember> hms = new ArrayList<>();
        hm1 = new HouseholdMember(1, "1", "HM1", "", 1, false, false);
        addBitmapToHouseholdMember(hm1);
        HouseholdMember hm2 = new HouseholdMember(2, "1", "HM2", "", 2, false, false);
        HouseholdMember hm3 = new HouseholdMember(3, "1", "HM3", "", 3, false, false);
        hms.add(hm1);
        hms.add(hm2);
        hms.add(hm3);

        //Add hms to database
        mDb.getHouseholdMemberDao().insert(hm1, hm2, hm3);
    }

    private void addBitmapToHouseholdMember(HouseholdMember hm1) throws Throwable {
        File householdDir = Utilities.getHouseholdDirectory(mContext);
        Drawable d = mContext.getResources().getDrawable(R.drawable.ic_btn_microphone, null);
        Bitmap img = Utilities.drawableToBitmap(d);
        if (!householdDir.exists())
            householdDir.mkdirs();
        File hmImgFile = new File(householdDir, hm1.getUid() + "avatar.png");
        FileOutputStream fOut = new FileOutputStream(hmImgFile);
        img.compress(Bitmap.CompressFormat.PNG, 85, fOut);
        fOut.flush();
        fOut.close();
        hm1.setAvatar(hmImgFile.getAbsolutePath());
    }

    private void setSetupToTrue() {
        Context context = getInstrumentation().getTargetContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SETUP, true);
        editor.commit();
    }

    @Test
    public void clickFabTakesToAddHouseholdMemberActivity() {
        //Set the set up state to true
        setSetupToTrue();

        intentsTestRule.launchActivity(intent);

        onView(ViewMatchers.withId(R.id.fab)).perform(click());
        //Check that the intent is create dand going to the next page
        intended(hasComponent(CreateHouseholdMemberActivity.class.getName()));
    }

    @Test
    public void clickHouseholdMemberTakesToCreateHouseholdMemberWithDetailsFilled() {
        setSetupToTrue();

        intentsTestRule.launchActivity(intent);

        //Click the first entry in the recycler view
        onView(withId(R.id.recyclelistHouseholdMembers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        intended(allOf(
                hasComponent(CreateHouseholdMemberActivity.class.getName()),
                hasExtraWithKey(HOUSEHOLDMEMBER)));
    }

    @Test
    public void longClickRemovesHouseholdMember() throws InterruptedException {
        //Set the set up state to true
        setSetupToTrue();

        intentsTestRule.launchActivity(intent);

        List<HouseholdMember> hms = TestUtilities.getValue(mDb.getHouseholdMemberDao().getAll());
        int hmCountBefore = hms.size();

        HouseholdMember hmToDelete = hms.get(0);
        File hmAvatar = new File(hmToDelete.getAvatar());
        assertThat(hmAvatar.exists(), is(true));

        //Click the first entry in the recycler view
        onView(withId(R.id.recyclelistHouseholdMembers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, longClick()));

        //Click accept
        onView(withText(R.string.yes)).perform(click());

        //Check the database has 1 less household member
        hms = TestUtilities.getValue(mDb.getHouseholdMemberDao().getAll());
        int hmCountAfter = hms.size();

        assertThat(hmCountAfter, is(hmCountBefore - 1));

        //Check that the avatar has been delete
        assertThat(hmAvatar.exists(), is(false));
    }

//    @Test
//    public void deleteHouseholdMemberWithNoPhoto(){
//        // Change preferences to not show pop up
//        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean(SETUP, true);
//        editor.commit();
//
//        //Start The activity
//        intentsTestRule.launchActivity(intent);
//
//        //Create Houseyhold membmer with no photo
//        onView(withId(R.id.fab)).perform(click());
//
//        //Enter all the details but don't
//        String householdId = "hhid";
//        String ppid = "ppid1";
//        String hmName = "hmToDelete";
//        try {
//            onView(withId(R.id.txtHouseholdId)).perform(scrollTo(), typeText(householdId), closeSoftKeyboard());
//        }
//        catch(PerformException ex){
//            //HHID Already existe so no need to enter it.
//            //Using try catch here cause its easier than working out a way to build the whole test suit to make it work
//        }
//        onView(withId(R.id.txtParticipantId)).perform(scrollTo(), typeText(ppid), closeSoftKeyboard());
//        onView(withId(R.id.txtName)).perform(scrollTo(), typeText(hmName), closeSoftKeyboard());
//        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("25"), closeSoftKeyboard());
//
//        onView(withId(R.id.btnSaveHouseholdMember)).perform(click());
//
//        //Delete household member
//        //Click the first entry in the recycler view
//        onView(withId(R.id.recyclelistHouseholdMembers))
//                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(hmName)), longClick()));
//
//        //Click yes to confirm the delete
//        onView(withText(R.string.yes)).perform(click());
//    }

    @Test
    public void clickSetUpTimesMovesToTimeSetupPage(){
        setSetupToTrue();
        intentsTestRule.launchActivity(intent);

        onView(withId(R.id.btnSetupTimes)).perform(click());

        intended(hasComponent(SetupTimesActivity.class.getName()));
    }

//    @Test
//    public void clickConsentSetsHouseholdVariable(){
//        //Reset HouseholdId
//        resetHouseholdUUID();
//        intentsTestRule.launchActivity(intent);
//
//        //Check the checkbox
//        onView(withId(R.id.chkAccept)).perform(click());
//
//        //Click ok
//        onView(withText(android.R.string.ok)).perform(click());
//
//        //Check the household
//        HouseholdRepository hhRepo = new HouseholdRepository(intentsTestRule.getActivity().getApplication());
//        Household hh = hhRepo.getHousehold();
//
//        assertThat(hh.isConsent(), is(true));
//    }

//    @Test
//    public void dontConsentSetsHouseholdVariable(){
//        //Reset HouseholdId
//        resetHouseholdUUID();
//        intentsTestRule.launchActivity(intent);
//
//        //Click ok
//        onView(withText(android.R.string.ok)).perform(click());
//
//        //Check the household
//        HouseholdRepository hhRepo = new HouseholdRepository(intentsTestRule.getActivity().getApplication());
//        Household hh = hhRepo.getHousehold();
//
//        assertThat(hh.isConsent(), is(false));
//    }
}
