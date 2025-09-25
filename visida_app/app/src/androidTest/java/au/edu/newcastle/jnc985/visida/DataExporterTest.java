package au.edu.newcastle.jnc985.visida;

import android.Manifest;
import androidx.room.Room;
import android.content.Context;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import au.edu.newcastle.jnc985.visida.activity.SettingsActivity;
import bo.AppConstants;
import bo.DataExporter;
import bo.HouseholdRepository;
import bo.Utilities;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.Household;
import bo.db.entity.HouseholdMember;

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static bo.AppConstants.EXPORT_MEDIA_DIR;
import static bo.AppConstants.ZIP_FILE;
import static org.hamcrest.CoreMatchers.is;

/**
 * Created by Josh on 19-Dec-17.
 */
@RunWith(AndroidJUnit4.class)
public class DataExporterTest {

    //Rule so we can access the application
    @Rule
    public ActivityTestRule<SettingsActivity> mActivityRule = new ActivityTestRule<>(SettingsActivity.class, false, false);
    //Give permission to read and write to the downloads directory
    @Rule
    public GrantPermissionRule mPermissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Mock
    private AppDatabase mDb;

    private Context context;

    private final String HHID = "1";
    private Household mHh;

    @Before
    public void createDb() {
        context = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        AppDatabase.setInstance(mDb);

        //Build test data
        mHh = new Household();
        mHh.setHouseholdId(HHID);
        mDb.getHouseholdDao().insert(mHh);
        List<HouseholdMember> hms = new ArrayList<>();
        HouseholdMember hm1 = new HouseholdMember(1, mHh.getHouseholdId(), "HM1", "", 1, false, false);
        HouseholdMember hm2 = new HouseholdMember(2, mHh.getHouseholdId(), "HM2", "", 1, false, false);
        hms.add(hm1);
        hms.add(hm2);
        mHh.setHouseholdMemberList(hms);
        mDb.getHouseholdMemberDao().insert(hm1, hm2);

        FoodRecord fr1 = new FoodRecord(hm1.getUid());
        fr1.setFoodRecordId(1);
        FoodRecord fr2 = new FoodRecord(hm2.getUid());
        fr2.setFoodRecordId(2);
        mDb.getFoodRecordDao().insert(fr1, fr2);

        EatingOccasion eo1 = new EatingOccasion();
        eo1.setEatingOccasionId((long) 1);
        eo1.setFoodRecordId(fr1.getFoodRecordId());
        EatingOccasion eo2 = new EatingOccasion();
        eo2.setEatingOccasionId((long) 2);
        eo2.setFoodRecordId(fr2.getFoodRecordId());
        mDb.getEatingOccasionDao().insert(eo1, eo2);

        FoodItem fi1 = new FoodItem();
        fi1.setFoodItemId(1);
        fi1.setEatingOccasionId(eo1.getEatingOccasionId());
        fi1.setFinalized(true);
        FoodItem fi2 = new FoodItem();
        fi2.setFoodItemId(2);
        fi2.setEatingOccasionId(eo1.getEatingOccasionId());
        fi2.setFinalized(true);
        FoodItem fi3 = new FoodItem();
        fi3.setFoodItemId(3);
        fi3.setEatingOccasionId(eo2.getEatingOccasionId());
        fi3.setFinalized(true);
        FoodItem fi4 = new FoodItem();
        fi4.setFoodItemId(4);
        fi4.setEatingOccasionId(eo2.getEatingOccasionId());
        fi4.setFinalized(true);
        mDb.getFoodItemDao().insert(fi1, fi2, fi3, fi4);

        cleanDownloadsDirectory();
    }

    private void cleanDownloadsDirectory(){
        //Make sure the Downloads directory doesn't already have the output files
        File downloadsDir = Utilities.getDownloadsDirectory(context);
        downloadsDir.setReadable(true);
//        System.out.println("Read: " + downloadsDir.canRead());
//        System.out.println("Execute: " + downloadsDir.canExecute());
//        System.out.println("Write: " + downloadsDir.canWrite());
        for(File f : downloadsDir.listFiles()){
            if(f.getName().contains(AppConstants.ZIP_FILE)){
                Utilities.deleteDirectory(f);
            }
            else if(f.getName().equals(EXPORT_MEDIA_DIR)){
                Utilities.deleteDirectory(f);
            }
        }
    }

    @After
    public void closeDb() {
        mDb.close();
        cleanDownloadsDirectory();
    }


    @Test
    public void blank() {
        assertThat(true, is(true));
    }

    private CountingIdlingResource mIdlingResource;
    @Before
    public void registerIdlingResource(){
        mActivityRule.launchActivity(null);
        mIdlingResource = new CountingIdlingResource(AppConstants.DATAEXPORTER_RESOURCE_NAME);
        IdlingRegistry.getInstance().register(mIdlingResource);
        System.out.println(IdlingRegistry.getInstance().getResources().size());
    }
    @After
    public  void deregisterIdlingResource(){
        IdlingRegistry.getInstance().unregister(mIdlingResource);
    }
    @Test
    public void exportCreatesExportMediaAndZipFile(){
        //Click the export button
        Household hh = new HouseholdRepository(mActivityRule.getActivity().getApplication()).getHousehold();
        mIdlingResource.increment();
        DataExporter de = new DataExporter(mActivityRule.getActivity().getApplication(), mIdlingResource, new DataExporter.DataExporterCallbackHandler() {
            @Override
            public void onExportComplete() {
                //Check the directories exist
                File downloadDir = Utilities.getDownloadsDirectory(context);
                //Since the zip file now contains a time date stamp we can jsut check if a zip file has been created.
                //Since we are clearing the download directory in @Before. There should only be 1 file that contains
                //the ZIP_FILE string.
                int zipFileCount = 0;
                for(File f : downloadDir.listFiles()){
                    if(f.getName().contains(ZIP_FILE)){
                        zipFileCount++;
                    }
                }
                //Check that a zip file exists
                File exportFIle = Utilities.getExportDirectory(context);

                //Check that only 1 zip file has been created.
                assertThat(zipFileCount, is(1));
                assertThat(exportFIle.exists(), is(true));
                mIdlingResource.decrement();
            }
        });

        de.exportData();
        //Wait fot the data exporter to be completed
        while(!mIdlingResource.isIdleNow()){}

        //assertThat(false, is(true));
    }



    //@Test
    /*
    public void exportDataCreatesJsonFile() throws FileNotFoundException {
        //Set up Database with:
        // 1 hosuehold member no food records
        //Prepare the static class
        AppDatabase.setInstance(mDb);
        final Context context = mActivityRule.getActivity().getApplicationContext();

        final Application app = mActivityRule.getActivity().getApplication();
        DataExporter de = new DataExporter(app, new DataExporter.DataExporterCallbackHandler() {
            @Override
            public void onExportComplete() {
                File exportDir = Utilities.getExportDirectory(app);
                String[] files = exportDir.list();
                assertThat(files.length, greaterThan(0));

                Gson gson = new Gson();
                //Get the JSON File

                try {
                    Household hh = gson.fromJson(
                            new FileReader(new File(Utilities.getExportDirectory(context), JSON_FILE)),
                            Household.class);

                    assertThat(hh, is(mHh));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    assertThat(true, is(false));
                }
            }
        });
        de.exportData();


    }
    */
}
