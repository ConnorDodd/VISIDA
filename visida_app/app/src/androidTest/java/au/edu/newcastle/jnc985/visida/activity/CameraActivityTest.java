package au.edu.newcastle.jnc985.visida.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.Date;

import au.edu.newcastle.jnc985.visida.R;
import bo.State;
import bo.Utilities;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.ActivityResultMatchers.hasResultCode;
import static androidx.test.espresso.contrib.ActivityResultMatchers.hasResultData;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static au.edu.newcastle.jnc985.visida.activity.CameraActivity.ALLOW_NO_IMAGE;
import static bo.AppConstants.FR;
import static bo.AppConstants.IMAGE_NAME;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.STATE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Josh on 05-Jan-18.
 */
public class CameraActivityTest {

    //Build Dummy HouseholdMember for entire class
    private HouseholdMember hm = new HouseholdMember(1, "1", "TESTHM", "", 22, false, false);
    private FoodRecord fr = new FoodRecord(hm.getUid());

    private Context mContext;
    private Intent mIntent;

    @Rule
    public GrantPermissionRule mGrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO);
    //Set up Rule
    @Rule
    public IntentsTestRule<CameraActivity> mActivtyRule = new IntentsTestRule<CameraActivity>(CameraActivity.class, true, false) {
        @Override
        protected Intent getActivityIntent() {
            return mIntent;
        }
    };

    @Before
    public void setUp() throws Exception {
        this.mContext = InstrumentationRegistry.getTargetContext();

        SharedPreferences sharedPref = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(STATE, State.EAT.ordinal());
        editor.commit();

        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mIntent = new Intent(targetContext, CameraActivity.class);

        //Build dummy Food Record for testing
        fr.setHouseholdMember(hm);
        fr.setDate(new Date());

        //Build Dummy Eating Occasion
        EatingOccasion eo = new EatingOccasion();
        eo.setFoodRecordId(fr.getFoodRecordId());

        fr.addEatingOccasion(eo);
        mIntent.putExtra(ALLOW_NO_IMAGE, true);
        mIntent.putExtra(FR, fr);

        mActivtyRule.launchActivity(mIntent);
        //Ensure the orientation is portrait on start
        mActivtyRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Test
    public void acceptImageReturnsResultOKWithImagesSaved() {
        //Count the number of items in the media directory
        File mediaDir = Utilities.getMediaDirectory(mActivtyRule.getActivity().getApplicationContext());
        int fileCountBefore = mediaDir.list().length;

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Click the take picture button
        onView(withId(R.id.btnTakePicture)).perform(click());

        //Wait for the dialog to open
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Click accept
        onView(withId(R.id.btnImageAccept)).perform(click());

        assertThat(mActivtyRule.getActivityResult(), hasResultCode(Activity.RESULT_OK));
        assertThat(mActivtyRule.getActivityResult(), hasResultData(hasExtraWithKey(IMAGE_NAME)));

        int fileCountAfter = mediaDir.list().length;
        assertThat(fileCountAfter, is(fileCountBefore + 1));
    }

    @Test
    public void clickNoPhotoReturnsWithResultOKAndPlaceholderImageName() {
        //Click the no photo button
        onView(withId(R.id.btnNoPhoto)).perform(click());

        //check the result code and IMAGE_NAME extra
        assertThat(mActivtyRule.getActivityResult(), hasResultCode(Activity.RESULT_OK));
        String expectedImageName = mActivtyRule.getActivity().getResources().getString(R.string.NO_PHOTO);
        assertThat(mActivtyRule.getActivityResult(), hasResultData(hasExtra(IMAGE_NAME, expectedImageName)));
    }
}
