package au.edu.newcastle.jnc985.visida.instruction;

import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import androidx.test.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.activity.FinalizeFoodItemActivity;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;
import bo.db.entity.Meal;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static bo.AppConstants.EOID;

public class AudioButtonTest {

    @Rule
    public ActivityTestRule<FinalizeFoodItemActivity> mFinalizeActivityRule = new ActivityTestRule<>(FinalizeFoodItemActivity.class, false, false);


    private AppDatabase mDb;
    private Context mContext;

    private long mEoId = 1;

    @Before
    public void setupDB(){
        mContext = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).build();
        AppDatabase.setInstance(mDb);

        //Add Default data
        addDefaultData();
    }

    private void addDefaultData() {
        //Create a household member
        HouseholdMember hm = new HouseholdMember(1, "", "HM1", "", 1, false, true);
        mDb.getHouseholdMemberDao().insert(hm);

        //Create a food record
        FoodRecord fr = new FoodRecord(hm.getUid());
        fr.setFoodRecordId((long) 1);
        mDb.getFoodRecordDao().insert(fr);

        //Create Eating Occasion
        EatingOccasion eo = new EatingOccasion();
        eo.setEatingOccasionId(mEoId);
        eo.setFoodRecordId(fr.getFoodRecordId());
        mDb.getEatingOccasionDao().insert(eo);
    }

    @Test
    public void finalizeFoodItemPlayAudioFileThenAudioButton(){
        //Create Meal
        Meal m = new Meal();
        m.setMealId((long) 1);
        m.setGuestInfoCaptured(true);
        mDb.getMealDao().insert(m);

        //Create a shared food item
        FoodItem fi = new FoodItem();
        fi.setEatingOccasionId(mEoId);
        fi.setFoodItemId((long) 1);
        fi.setMealId(m.getMealId());
        fi.setImageUrl("");
        fi.setLeftoverImageUrl("Leftovers");
        mDb.getFoodItemDao().insert(fi);

        //Launch the activity
        Intent i = new Intent();
        i.putExtra(EOID, mEoId);
        mFinalizeActivityRule.launchActivity(i);

        //Accepts guests
        onView(withText(R.string.tick)).perform(click());

        //Click play audio
        onView(withId(R.id.imgvAudioFile)).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Now playt the audio button
        onView(withId(R.id.btnAteAll)).perform(longClick());
    }
}
