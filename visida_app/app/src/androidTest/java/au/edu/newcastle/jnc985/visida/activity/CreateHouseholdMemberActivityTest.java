package au.edu.newcastle.jnc985.visida.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.MediaStore;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.TestUtilities;
import bo.db.AppDatabase;
import bo.db.entity.HouseholdMember;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static au.edu.newcastle.jnc985.visida.EspressoTestMatchers.hasTextInputLayoutErrorText;
import static au.edu.newcastle.jnc985.visida.EspressoTestMatchers.withDrawable;
import static bo.AppConstants.HASBREASTFED;
import static bo.AppConstants.PARTICIPANTHOUSEHOLDID;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.SETUP;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.Is.is;

/**
 * Created by jnc985 on 30-Nov-17.
 */


@RunWith(MockitoJUnitRunner.class)
public class CreateHouseholdMemberActivityTest {
    private final static long MINUTE = 60 * 1000;
    private static final String HOUSEHOLDID = "householdid";
    private static final String PARTICIPANTID = "participantid";

    private AppDatabase mDb;

    private Intent intent;
    @Rule
    public GrantPermissionRule mGrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);
    //Set up Rule
    @Rule
    public IntentsTestRule<CreateHouseholdMemberActivity> mCreateHouseholdMemberRule = new IntentsTestRule<CreateHouseholdMemberActivity>(CreateHouseholdMemberActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            SharedPreferences prefs =
                    InstrumentationRegistry.getTargetContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.commit();
        }
    };

    @AfterClass
    public static void clearPreferences() {
        SharedPreferences prefs =
                InstrumentationRegistry.getTargetContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        //Add the mock database to the app
        AppDatabase.setInstance(mDb);
    }

    @After
    public void closeDb() {
        mDb.close();
    }

    @Test
    public void clickImageViewStartsCameraActivity() {
        Intent intent = new Intent();
        Instrumentation.ActivityResult intentResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, intent);

        intending(anyIntent()).respondWith(intentResult);

        onView(withId(R.id.imgViewAvatar)).perform(click());
        intended(allOf(
                hasExtraWithKey(MediaStore.EXTRA_OUTPUT),
                hasAction(Matchers.equalTo((MediaStore.ACTION_IMAGE_CAPTURE)))));
    }

    @Test
    public void enteringDetailsSavesNewHouseholdMember() throws InterruptedException {
        Instrumentation.ActivityResult activityResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, null);

        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(activityResult);
        onView(withId(R.id.txtHouseholdId)).perform(scrollTo(), typeText(HOUSEHOLDID), closeSoftKeyboard());
        onView(withId(R.id.txtParticipantId)).perform(scrollTo(), typeText(PARTICIPANTID), closeSoftKeyboard());
        onView(withId(R.id.txtName)).perform(scrollTo(), typeText("testhouseholdmember"), closeSoftKeyboard());
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("25"), closeSoftKeyboard());
        onView(withId(R.id.imgViewAvatar)).perform(scrollTo(), click());

        onView(withId(R.id.btnSaveHouseholdMember)).perform(click());

        //Get the household memebmers
        List<HouseholdMember> hms = TestUtilities.getValue(mDb.getHouseholdMemberDao().getAll());
        assertThat(hms.size(), is(1));
    }

    @Test
    public void takingAvatarThenRetakingandClickingDeclineDoesNotResetImageView() throws InterruptedException {
        //Take the first image
        Instrumentation.ActivityResult activityResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, null);
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(activityResult);
        onView(withId(R.id.imgViewAvatar)).perform(click());

        //Go to take the 2nd but cancel the photo
        activityResult = new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null);
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(activityResult);
        onView(withId(R.id.imgViewAvatar)).perform(click());

        onView(withId(R.id.imgViewAvatar)).perform(scrollTo()).perform(scrollTo()).check(matches(withDrawable(R.drawable.ic_thinking)));

        //Click the submit button
        onView(withId(R.id.btnSaveHouseholdMember)).perform(click());

        //Check that the errors are visible on the form
        String nameError = mCreateHouseholdMemberRule.getActivity().getResources().getString(R.string.error_name);
        onView(withId(R.id.txtNameLayout)).perform(scrollTo()).perform(scrollTo()).check(matches(hasTextInputLayoutErrorText(nameError)));
        String ageError = mCreateHouseholdMemberRule.getActivity().getResources().getString(R.string.error_age);
        onView(withId(R.id.txtAgeLayout)).perform(scrollTo()).perform(scrollTo()).check(matches(hasTextInputLayoutErrorText(ageError)));
    }

    @Test
    public void spinnerOnlyVisibleWhenFemaleIsCheckedAndOver18() throws InterruptedException {
        //Male < 18
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("15"), closeSoftKeyboard());
        onView(withId(R.id.radioBtnMale)).perform(scrollTo(), click());
        onView(withId(R.id.spinnerLifeStage)).check(matches(not(isDisplayed())));
        onView(withId(R.id.txtAge)).perform(clearText());
        //Male = 18
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("18"), closeSoftKeyboard());
        onView(withId(R.id.radioBtnMale)).perform(click());
        onView(withId(R.id.spinnerLifeStage)).check(matches(not(isDisplayed())));
        onView(withId(R.id.txtAge)).perform(clearText());
        //Male > 18
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("20"), closeSoftKeyboard());
        onView(withId(R.id.radioBtnMale)).perform(click());
        onView(withId(R.id.spinnerLifeStage)).check(matches(not(isDisplayed())));
        onView(withId(R.id.txtAge)).perform(clearText());
        //Female < 18
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("15"), closeSoftKeyboard());
        onView(withId(R.id.radioBtnFemale)).perform(click());
        onView(withId(R.id.spinnerLifeStage)).check(matches(not(isDisplayed())));
        onView(withId(R.id.txtAge)).perform(clearText());
        //Female = 18
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("18"), closeSoftKeyboard());
        onView(withId(R.id.radioBtnFemale)).perform(click());
        onView(withId(R.id.spinnerLifeStage)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.txtAge)).perform(clearText());
        //Female > 18
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("20"), closeSoftKeyboard());
        onView(withId(R.id.radioBtnFemale)).perform(click());
        onView(withId(R.id.spinnerLifeStage)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.txtAge)).perform(clearText());
    }

    @Test
    public void createMaleHasNoneAsLifeStage() throws InterruptedException {

        Intent intent = new Intent();
        Instrumentation.ActivityResult intentResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, intent);
        intending(anyIntent()).respondWith(intentResult);

        Instrumentation.ActivityResult activityResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, null);

        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(activityResult);
        onView(withId(R.id.txtHouseholdId)).perform(scrollTo(), typeText(HOUSEHOLDID), closeSoftKeyboard());
        onView(withId(R.id.txtParticipantId)).perform(scrollTo(), typeText(PARTICIPANTID), closeSoftKeyboard());
        onView(withId(R.id.txtName)).perform(scrollTo(), typeText("testhouseholdmember"), closeSoftKeyboard());
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("25"), closeSoftKeyboard());
        onView(withId(R.id.imgViewAvatar)).perform(scrollTo(), click());
        onView(withId(R.id.radioBtnMale)).perform(scrollTo(), click());

        onView(withId(R.id.btnSaveHouseholdMember)).perform(click());
        intended(allOf(hasComponent(SetupHouseholdActivity.class.getName())));

        //Get the household memebmers
        List<HouseholdMember> hms = TestUtilities.getValue(mDb.getHouseholdMemberDao().getAll());
        assertThat(hms.size(), is(1));

        HouseholdMember hm = hms.get(0);
        String[] lifestages = mCreateHouseholdMemberRule.getActivity().getResources().getStringArray(R.array.life_stages);
        String none = lifestages[0];
        assertThat(hm.getLifeStage(), is("None"));
        assertThat(hm.isFemale(), is(false));
    }

    @Test
    public void createFemaleOver18PregnantHasPregnantAsLifeStage() throws InterruptedException {
        Instrumentation.ActivityResult activityResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, null);

        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(activityResult);
        onView(withId(R.id.txtHouseholdId)).perform(scrollTo(), typeText(HOUSEHOLDID), closeSoftKeyboard());
        onView(withId(R.id.txtParticipantId)).perform(scrollTo(), typeText(PARTICIPANTID), closeSoftKeyboard());
        onView(withId(R.id.txtName)).perform(scrollTo(), typeText("testhouseholdmember"), closeSoftKeyboard());
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("25"), closeSoftKeyboard());
        onView(withId(R.id.imgViewAvatar)).perform(scrollTo(), click());
        onView(withId(R.id.radioBtnFemale)).perform(scrollTo(), click());

        onView(withId(R.id.spinnerLifeStage)).perform(scrollTo(), click());
        String[] lifestages = mCreateHouseholdMemberRule.getActivity().getResources().getStringArray(R.array.life_stages);
        String pregnant = lifestages[1];
        onData(allOf(is(instanceOf(String.class)), is(pregnant))).perform(click());
        onView(withId(R.id.spinnerLifeStage)).perform(scrollTo()).check(matches(withSpinnerText(containsString(pregnant))));

        onView(withId(R.id.btnSaveHouseholdMember)).perform(click());

        //Get the household memebmers
        List<HouseholdMember> hms = TestUtilities.getValue(mDb.getHouseholdMemberDao().getAll());
        assertThat(hms.size(), is(1));

        HouseholdMember hm = hms.get(0);
        //Make sure it says pregnant in ENGLISH
        assertThat(hm.getLifeStage(), is("Pregnant"));
    }

    @Test
    public void breastfedCheckboxIsfalseIfNotVisible() {
        //Check its false to start
        onView(withId(R.id.chkIsBreastfed)).check(matches(not(isChecked())));
        //Enter an age less than 5
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("2"), closeSoftKeyboard());
        onView(withId(R.id.chkIsBreastfed)).perform(scrollTo()).check(matches(isDisplayed()));
        //Check the checkbox
        onView(withId(R.id.chkIsBreastfed)).perform(click());
        onView(withId(R.id.chkIsBreastfed)).perform(scrollTo()).check(matches(isChecked()));
        //Now change the age
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("6"), closeSoftKeyboard());
        onView(withId(R.id.chkIsBreastfed)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        //Make sure check box is now false;
        onView(withId(R.id.chkIsBreastfed)).check(matches(not(isChecked())));
    }

    @Test
    public void enterAgeBelow5MakesBreastfedOptionAppear() {
        //Enter an age less than 5
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("2"), closeSoftKeyboard());
        onView(withId(R.id.chkIsBreastfed)).perform(scrollTo()).perform(scrollTo()).check(matches(isDisplayed()));
    }

    @Test
    public void enterAgeOf5MakesBreatfeedGone() {
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("5"), closeSoftKeyboard());
        onView(withId(R.id.chkIsBreastfed)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void enterAgeAbove5MakesBreatfeedGone() {
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("6"), closeSoftKeyboard());
        onView(withId(R.id.chkIsBreastfed)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void breastfedHouseholdMemberSetsSharedPreferenceToTrue() throws InterruptedException {
        Instrumentation.ActivityResult activityResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, null);

        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(activityResult);
        onView(withId(R.id.txtHouseholdId)).perform(scrollTo(), typeText(HOUSEHOLDID), closeSoftKeyboard());
        onView(withId(R.id.txtParticipantId)).perform(scrollTo(), typeText(PARTICIPANTID), closeSoftKeyboard());
        onView(withId(R.id.txtName)).perform(scrollTo(), typeText("breastfedhouseholdemember"), closeSoftKeyboard());
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("3"), closeSoftKeyboard());
        onView(withId(R.id.imgViewAvatar)).perform(scrollTo(), click());
        onView(withId(R.id.radioBtnFemale)).perform(scrollTo(), click());

        onView(withId(R.id.chkIsBreastfed)).perform(click());

        onView(withId(R.id.btnSaveHouseholdMember)).perform(click());

        //Get the household memebmers
        List<HouseholdMember> hms = TestUtilities.getValue(mDb.getHouseholdMemberDao().getAll());
        assertThat(hms.size(), is(1));

        SharedPreferences sharedPref = getInstrumentation().getTargetContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        boolean hasBreastfed = sharedPref.getBoolean(HASBREASTFED, false);

        assertThat(hasBreastfed, is(true));
    }

    @Test
    public void firstParticipantIdSetsSharedPreference() {
        Instrumentation.ActivityResult activityResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, null);
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(activityResult);

        //check the shared prefence has not been set
        SharedPreferences sharedPref = getInstrumentation().getTargetContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        String particpantHhId = sharedPref.getString(PARTICIPANTHOUSEHOLDID, null);

        assertThat(particpantHhId, is(nullValue()));

        onView(withId(R.id.txtHouseholdId)).perform(scrollTo(), typeText(HOUSEHOLDID), closeSoftKeyboard());
        onView(withId(R.id.txtParticipantId)).perform(scrollTo(), typeText(PARTICIPANTID), closeSoftKeyboard());
        onView(withId(R.id.txtName)).perform(scrollTo(), typeText("breastfedhouseholdemember"), closeSoftKeyboard());
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("3"), closeSoftKeyboard());
        onView(withId(R.id.imgViewAvatar)).perform(scrollTo(), click());
        onView(withId(R.id.radioBtnFemale)).perform(scrollTo(), click());

        //Save the HM
        onView(withId(R.id.btnSaveHouseholdMember)).perform(click());

        //Check the shared preference HHID has been set.
        particpantHhId = sharedPref.getString(PARTICIPANTHOUSEHOLDID, null);
        assertThat(particpantHhId, is(HOUSEHOLDID));

    }

    @Test
    public void newHouseholdMemberWithHouseholdIdPrepopulated() {
        Instrumentation.ActivityResult activityResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, null);
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(activityResult);

        //check the shared prefence has not been set
        SharedPreferences sharedPref = getInstrumentation().getTargetContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        //Set the setup to true so we can click the fab
        editor.putBoolean(SETUP, true);
        editor.commit();
        String particpantHhId = sharedPref.getString(PARTICIPANTHOUSEHOLDID, null);

        assertThat(particpantHhId, is(nullValue()));

        onView(withId(R.id.txtHouseholdId)).perform(scrollTo(), typeText(HOUSEHOLDID), closeSoftKeyboard());
        onView(withId(R.id.txtParticipantId)).perform(scrollTo(), typeText(PARTICIPANTID), closeSoftKeyboard());
        onView(withId(R.id.txtName)).perform(scrollTo(), typeText("breastfedhouseholdemember"), closeSoftKeyboard());
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("3"), closeSoftKeyboard());
        onView(withId(R.id.imgViewAvatar)).perform(scrollTo(), click());
        onView(withId(R.id.radioBtnFemale)).perform(scrollTo(), click());

        //Save the HM
        onView(withId(R.id.btnSaveHouseholdMember)).perform(click());

        //Check the shared preference HHID has been set.
        particpantHhId = sharedPref.getString(PARTICIPANTHOUSEHOLDID, null);
        assertThat(particpantHhId, is(HOUSEHOLDID));

        //Click the FAB to create a new household member
        onView(withId(R.id.fab)).perform(click());
        //check the household id is pre populated
        onView(withId(R.id.txtHouseholdId)).perform(scrollTo()).check(matches(withText(HOUSEHOLDID)));

    }

    @Test
    public void participantIdErrorMessageOnDuplicateEntry(){
        //Add A household member with the particpant id
        HouseholdMember hm = new HouseholdMember();
        hm.setUid(1);
        hm.setParticipantHouseholdMemberId(PARTICIPANTID);
        mDb.getHouseholdMemberDao().insert(hm);

        Instrumentation.ActivityResult activityResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, null);
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(activityResult);

        //check the shared prefence has not been set
        SharedPreferences sharedPref = getInstrumentation().getTargetContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        //Set the setup to true so we can click the fab
        editor.putBoolean(SETUP, true);
        editor.commit();
        String particpantHhId = sharedPref.getString(PARTICIPANTHOUSEHOLDID, null);

        assertThat(particpantHhId, is(nullValue()));

        onView(withId(R.id.txtHouseholdId)).perform(scrollTo(), typeText(HOUSEHOLDID), closeSoftKeyboard());
        onView(withId(R.id.txtParticipantId)).perform(scrollTo(), typeText(PARTICIPANTID), closeSoftKeyboard());
        onView(withId(R.id.txtName)).perform(scrollTo(), typeText("hmwithsameppid"), closeSoftKeyboard());
        onView(withId(R.id.txtAge)).perform(scrollTo(), typeText("3"), closeSoftKeyboard());
        onView(withId(R.id.imgViewAvatar)).perform(scrollTo(), click());
        onView(withId(R.id.radioBtnFemale)).perform(scrollTo(), click());

        //Save the HM
        onView(withId(R.id.btnSaveHouseholdMember)).perform(click());

        //Check the error message appears for PPID Already Exists
        onView(withId(R.id.txtParticipantIdLayout)).check(matches(hasTextInputLayoutErrorText(mCreateHouseholdMemberRule.getActivity().getString(R.string.error_participant_id_already_exists))));

    }
}
