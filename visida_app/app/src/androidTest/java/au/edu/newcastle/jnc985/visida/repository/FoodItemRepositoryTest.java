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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import au.edu.newcastle.jnc985.visida.activity.MainActivity;
import bo.FoodItemRepository;
import bo.Utilities;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;

import static bo.AppConstants.FIID_PLACEHOLDER;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


/**
 * Created by Josh on 19-Dec-17.
 */
@RunWith(AndroidJUnit4.class)
public class FoodItemRepositoryTest {

    //Rule so we can access the application
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Mock
    private AppDatabase mDb;

    private Context mContext;

    private HouseholdMember mHm;

    //"%d_%d_%d_%d_%s_%s.mp3"
    private static final String IMAMENAME = "d_d_d_d_" + FIID_PLACEHOLDER + "_s.jpg";
    private static final String AUDIONAME = "d_d_d_d_" + FIID_PLACEHOLDER + "_s.mp3";

    @Before
    public void createDb() {
        mContext = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).build();
        AppDatabase.setInstance(mDb);
        //Add a household member
        mHm = new HouseholdMember();
        mHm.setUid(1);
        mDb.getHouseholdMemberDao().insert(mHm);
    }

    @After
    public void closeDb() {
        mDb.close();
    }


    @Test
    public void addNewFoodFoodItem() throws Exception {
        //Create Household member to have food record (to satisfy foreign key constraint)
        //Set up Database with:
        // 1 hosuehold member no food records
        Date today = new Date();

        //Create Food Record
        FoodRecord fr = new FoodRecord(mHm.getUid());
        fr.setFoodRecordId(mDb.getFoodRecordDao().insert(fr)[0]);

        //Create EatingOccasion
        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId((long) 1);
        eo.setFoodRecordId(fr.getFoodRecordId());
        mDb.getEatingOccasionDao().insert(eo);

        //Create FoodItem
        FoodItem fi = new FoodItem();
        fi.setEatingOccasionId(eo.getEatingOccasionId());
        fi.setImageUrl(IMAMENAME);
        fi.setAudioUrls(AUDIONAME);

        FoodItemRepository foodItemRepo = new FoodItemRepository(mActivityRule.getActivity().getApplication());
        foodItemRepo.addFoodItem(fi);

        List<FoodItem> insertedFis = foodItemRepo.getAllFoodItems();
        assertThat(insertedFis.size(), is(1));

        FoodItem insertedFi = insertedFis.get(0);
        //Check that the file names have been properly updated in the database
        assertThat(insertedFi.getAudioUrls(), not(containsString(FIID_PLACEHOLDER)));
        assertThat(insertedFi.getImageUrl(), not(containsString(FIID_PLACEHOLDER)));

        //Check that the food records are the same
        assertThat(insertedFi, is(fi));

    }

    @Test
    public void deletingFoodItemRenamesMediaFiles() throws IOException {
        String audioFileName = "audioFile.mp4";
        String imageFileName = "imageFile.mp4";

        //Add a food record to the database
        FoodRecord fr = new FoodRecord(mHm.getUid());
        fr.setFoodRecordId(mDb.getFoodRecordDao().insert(fr)[0]);

        //Create an Eating Occasion
        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId((long) 1);
        eo.setFoodRecordId(fr.getFoodRecordId());
        mDb.getEatingOccasionDao().insert(eo);

        //Create a food item for the eating occasion
        FoodItem fi = new FoodItem();
        fi.setAudioUrls(audioFileName);
        fi.setImageUrl(imageFileName);
        fi.setEatingOccasionId(eo.getEatingOccasionId());
        mDb.getFoodItemDao().insert(fi);

        //Create the files in the files system
        File audioFileB4 = new File(Utilities.getMediaDirectory(mContext), audioFileName);
        File imageFileB4 = new File(Utilities.getMediaDirectory(mContext), imageFileName);
        audioFileB4.createNewFile();
        imageFileB4.createNewFile();

        //Check the files exist
        assertThat(new File(Utilities.getMediaDirectory(mContext), audioFileName).exists(), is(true));
        assertThat(new File(Utilities.getMediaDirectory(mContext), imageFileName).exists(), is(true));

        //Use the repository to delete the food item
        FoodItemRepository fiRepo = new FoodItemRepository(mActivityRule.getActivity().getApplication());
        fiRepo.deleteFoodItem(mActivityRule.getActivity().getApplicationContext(), fi);


        //Sleep for a second tp let the file operations complete
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        //Check that the files no longer exist
        assertThat(new File(Utilities.getMediaDirectory(mContext), audioFileName).exists(), is(false));
        assertThat(new File(Utilities.getMediaDirectory(mContext), imageFileName).exists(), is(false));

        //Check the files have "DELETED_ prepended
        String newAudioName = "DELETED_" + audioFileName;
        String newImageName = "DELETED_" + imageFileName;
        assertThat(new File(Utilities.getMediaDirectory(mContext), newAudioName).exists(), is(true));
        assertThat(new File(Utilities.getMediaDirectory(mContext), newImageName).exists(), is(true));


    }
}
