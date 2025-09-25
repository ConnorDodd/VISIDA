package au.edu.newcastle.jnc985.visida.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.fragment.app.Fragment;

import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.util.Date;
import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.activity.AudioActivity;
import au.edu.newcastle.jnc985.visida.activity.CameraActivity;
import au.edu.newcastle.jnc985.visida.activity.TakePhotoFragment;
import bo.Utilities;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static bo.AppConstants.FR;
import static bo.AppConstants.IMAGE_NAME;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Josh on 05-Jan-18.
 */
public class TakePhotoFragmentTest {

    //Build Dummy HouseholdMember for entire class
    private HouseholdMember hm = new HouseholdMember(1, "1", "TESTHM", "", 22, false, false);
    private FoodRecord fr = new FoodRecord(hm.getUid());
    private File mImageOutputDirectory;
    private TakePhotoFragment mFragment;

    //Set up Rule
    //@Rule
    public IntentsTestRule<CameraActivity> activtyRule = new IntentsTestRule<CameraActivity>(CameraActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
            Intent result = new Intent(targetContext, CameraActivity.class);

            //Build dummy Food Record for testing
            fr.setHouseholdMember(hm);
            fr.setDate(new Date());

            //Build Dummy Eating Occasion
            EatingOccasion eo = new EatingOccasion();
            eo.setFoodRecordId(fr.getFoodRecordId());

            fr.addEatingOccasion(eo);
            result.putExtra(FR, fr);
            return result;
        }
    };

    @Before
    public void setUp() throws Exception {
        cleanOutputDirectory();
    }

    @After
    public void cleanUp() throws Exception {
        cleanOutputDirectory();
    }

    private void cleanOutputDirectory() {
        //Clean out the media folder
        List<Fragment> frags = activtyRule.getActivity().getSupportFragmentManager().getFragments();
        mFragment = (TakePhotoFragment) (frags.get(0));
        mImageOutputDirectory = Utilities.getMediaDirectory(activtyRule.getActivity().getApplicationContext());
        if (mImageOutputDirectory.exists()) {
            for (File file : mImageOutputDirectory.listFiles()) {
                if (!file.isDirectory()) {
                    file.delete();
                }
            }
        }
    }

    // @Test
    public void rotateToPortraitHasNoEffect() {
        activtyRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        activtyRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    // @Test
    public void rotateToLandscapeHasNoEffect() {
        activtyRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        activtyRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    //Test that taking photo saves an image in correct location
    //@Test
    public void imageCreatedOnButtonClick() {
        //Click the take photo button
        onView(ViewMatchers.withId(R.id.btnTakePicture)).perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Check that an image has been created
        File[] output = mImageOutputDirectory.listFiles();
        assertThat(output.length, is(1));
    }

    private boolean containsImageFiles(File directory) {
        String[] extensions = new String[]{
                "jpg",
                "png",
                "bmp"
        };
        for (File file : directory.listFiles()) {
            if (!file.isDirectory()) {
                for (String ext : extensions) {
                    if (file.getAbsolutePath().contains(ext)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //Test that Intent to next page (audio recording) is created
    //intent should contain
    //@Test
    public void capturingImageMovesToAudioActivity() {
        //Get the output folder
        File[] dirs = mImageOutputDirectory.listFiles();
        //Check folder is empty of images
        assertThat(dirs.length, is(0));
        assertThat(containsImageFiles(mImageOutputDirectory), is(false));

        //Click the take photo button
        onView(ViewMatchers.withId(R.id.btnTakePicture)).perform(click());
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Check the image was captured
        assertThat(containsImageFiles(mImageOutputDirectory), is(true));

        //Get the name of the image taken
        dirs = mImageOutputDirectory.listFiles();
        String imageName = dirs[0].getName();

        intended(allOf(
                hasComponent(AudioActivity.class.getName()),
                hasExtra(FR, fr),
                hasExtra(IMAGE_NAME, imageName)));
    }

    //Test that after taking an image on returning to activity old image is deleted
    //@Test
    public void returningToFragmentRemovesPreviousImage() {
        //Get the output file
        assertThat(containsImageFiles(mImageOutputDirectory), is(false));

        //Click the take photo button
        onView(ViewMatchers.withId(R.id.btnTakePicture)).perform(click());
        //Check the image was captured
        assertThat(containsImageFiles(mImageOutputDirectory), is(true));

        //Get the name of the image taken
        File[] output = mImageOutputDirectory.listFiles();
        String imageName = output[0].getName();

        intended(allOf(
                hasComponent(AudioActivity.class.getName()),
                hasExtra(FR, fr),
                hasExtra(IMAGE_NAME, imageName)));

        //Call onResume to simulate coming back to the Activity
        try {
            activtyRule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mFragment.onResume();
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        //Check the image has been deleted
        assertThat(containsImageFiles(mImageOutputDirectory), is(false));
    }
}
