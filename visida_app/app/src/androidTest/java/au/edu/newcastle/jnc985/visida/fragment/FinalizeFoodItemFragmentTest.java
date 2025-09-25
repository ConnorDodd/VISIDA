package au.edu.newcastle.jnc985.visida.fragment;

import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.intent.rule.IntentsTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.activity.FinalizeFoodItemActivity;
import au.edu.newcastle.jnc985.visida.activity.TakePhotoFragment;
import bo.EatingOccasionRepository;
import bo.FoodItemRepository;
import bo.Utilities;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static bo.AppConstants.EOID;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Josh on 05-Jan-18.
 */
public class FinalizeFoodItemFragmentTest {

    //Build Dummy HouseholdMember for entire class
    private HouseholdMember hm = new HouseholdMember(1, "1", "TESTHM", "", 22, false, false);
    private FoodRecord fr = new FoodRecord(hm.getUid());
    private File mImageOutputDirectory;
    private TakePhotoFragment mFragment;

    private AppDatabase mDb;
    private Context mContext;

    private File mMediaDir;
    private String testImageName = "testImage.jpg";
    private File testImageFile1;
    private File testImageFile2;

    private Intent intent;
    //Set up Rule
    @Rule
    public IntentsTestRule<FinalizeFoodItemActivity> activtyRule = new IntentsTestRule<FinalizeFoodItemActivity>(FinalizeFoodItemActivity.class, true, false) {
        @Override
        protected Intent getActivityIntent() {
            Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

            return intent;
        }
    };

    @Before
    public void setUpDatbase() {
        mContext = InstrumentationRegistry.getTargetContext();
        //Add an image to the test directory
        mMediaDir = new File(Utilities.getMediaDirectory(mContext), "TESTMEDIA");
        if (!mMediaDir.exists()) {
            mMediaDir.mkdirs();
        }
        testImageFile1 = new File(mMediaDir, testImageName + "1");
        testImageFile2 = new File(mMediaDir, testImageName + "2");
        Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_thinking);
        saveBitmapToFile(testImageFile1.getAbsolutePath(), icon);
        saveBitmapToFile(testImageFile2.getAbsolutePath(), icon);

        mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).build();
        AppDatabase.setInstance(mDb);
        //Create household membmer
        HouseholdMember hm1 = new HouseholdMember(1, "1", "HM1", "", 1, false, false);
        mDb.getHouseholdMemberDao().insert(hm1);

        //Create Food Record
        FoodRecord fr1 = new FoodRecord(1);
        fr1.setFoodRecordId(1);
        mDb.getFoodRecordDao().insert(fr1);

        //Create Eating Occasion
        EatingOccasion eo1 = new EatingOccasion();
        eo1.setFoodRecordId(fr1.getFoodRecordId());
        eo1.setEatingOccasionId((long) 1);
        mDb.getEatingOccasionDao().insert(eo1);

        //Create Food Items
        FoodItem fi1 = new FoodItem();
        FoodItem fi2 = new FoodItem();
        fi1.setEatingOccasionId(eo1.getEatingOccasionId());
        fi2.setEatingOccasionId(eo1.getEatingOccasionId());
        fi1.setImageUrl("TESTMEDIA/" + testImageFile1.getName());
        fi2.setImageUrl("TESTMEDIA/" + testImageFile2.getName());
        fi1.setAudioUrls("");
        fi2.setAudioUrls("");
        mDb.getFoodItemDao().insert(fi1, fi2);

        this.intent = new Intent(mContext, FinalizeFoodItemActivity.class);
        intent.putExtra(EOID, new Long(1));
        activtyRule.launchActivity(intent);
    }

    @After
    public void cleanUp() {
        mMediaDir = new File(Utilities.getMediaDirectory(mContext), "TESTMEDIA");
        for (File f : mMediaDir.listFiles()) {
            f.delete();
        }
        mMediaDir.delete();
        mDb.close();
    }

    private void saveBitmapToFile(String fullFileName, Bitmap icon) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fullFileName);
            icon.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void uiViewsArePresent() {
        onView(withId(R.id.btnAteAll)).check(matches(isDisplayed()));
        onView(withId(R.id.btnAteSome)).check(matches(isDisplayed()));
        onView(withId(R.id.btnAteNone)).check(matches(isDisplayed()));
        onView(withId(R.id.imgFoodItem)).check(matches(isDisplayed()));
    }

    //TODO Test that clicking ate none deletes the image and audio files
    @Test
    public void clickAteNoneDoesNotRemovesImageandAudio() throws ExecutionException, InterruptedException {
        //Check the media directory has the two test images
        assertThat(mMediaDir.listFiles().length, is(2));

        onView(withId(R.id.btnAteNone)).perform(click());

        assertThat(mMediaDir.listFiles().length, is(2));

        //Check the food Items didnt eat bit is set
        FoodItemRepository fiRepo = new FoodItemRepository(activtyRule.getActivity().getApplication());

        FoodItem fi = fiRepo.getFoodItem(1);
        assertThat(fi.isDidnteat(), is(true));
    }

    @Test
    public void clickAteAllFinalizesFoodItem() {
        onView(withId(R.id.btnAteAll)).perform(click());

        //Get the Eating occasion
        EatingOccasionRepository eoRepo = new EatingOccasionRepository(activtyRule.getActivity().getApplication());
        EatingOccasion eoToFinalize = eoRepo.getEatingOccasion(1);

        assertThat(eoToFinalize.getFoodItems().get(0).isFinalized(), is(true));

        onView(withId(R.id.btnAteAll)).perform(click());

        //Get the Eating occasion again
        eoToFinalize = eoRepo.getEatingOccasion(1);

        assertThat(eoToFinalize.getFoodItems().get(1).isFinalized(), is(true));
    }
}
