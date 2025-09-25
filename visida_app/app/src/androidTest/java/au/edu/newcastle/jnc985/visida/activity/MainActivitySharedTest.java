package au.edu.newcastle.jnc985.visida.activity;

import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import bo.State;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;
import bo.db.entity.Meal;
import au.edu.newcastle.jnc985.visida.R;

import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.SETUP;
import static bo.AppConstants.STATE;
import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.core.Is.is;

/**
 * Created by jnc985 on 30-Nov-17.
 */

@RunWith(AndroidJUnit4.class)
public class MainActivitySharedTest {

    //Intent to start activity
    Intent intent = new Intent();
    private Context mContext;
    //Set up Rule
    @Rule
    public ActivityTestRule<MainActivity> mIntentsTestRule = new ActivityTestRule<MainActivity>(MainActivity.class, true, false);

    @Before
    public void setUpState() {
        //Default to SETUP=true
        mContext = getInstrumentation().getTargetContext();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SETUP, true);
        editor.commit();
        Intents.init();
    }

    @After
    public void cleanUp() {
        Intents.release();
    }

//    @Test
//    public void clickMealTakesToMealPage() {
//        mIntentsTestRule.launchActivity(intent);
//        onView(ViewMatchers.withId(R.id.btnMeal)).perform(click());
//        //Check that the intent is create dand going to the next page
//        intended(hasComponent(MealActivity.class.getName()));
//        SharedPreferences sharedPref = mIntentsTestRule.getActivity().getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
//        State currentState = State.values()[sharedPref.getInt(STATE, State.INVALID.ordinal())];
//        assertThat(currentState, is(State.MEAL));
//    }

    @Test
    public void clickingFInalizeConvertsTheMeals() {
        AppDatabase db = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).build();
        AppDatabase.setInstance(db);

        //Create 3 houshold members
        HouseholdMember hm1 = new HouseholdMember(1, "HHID", "hm1", "", 1, false, false);
        HouseholdMember hm2 = new HouseholdMember(2, "HHID", "hm2", "", 1, false, false);
        HouseholdMember hm3 = new HouseholdMember(3, "HHID", "hm3", "", 1, false, false);
        hm1.setParticipantHouseholdMemberId("ppid1");
        hm2.setParticipantHouseholdMemberId("ppid2");
        hm3.setParticipantHouseholdMemberId("ppid3");
        db.getHouseholdMemberDao().insert(hm1, hm2, hm3);

        //Create a Meal
        Meal meal = new Meal();
        meal.setMealId((long) 1);
        db.getMealDao().insert(meal);

        //Create 3 FoodItems for this meal
        FoodItem fi1 = new FoodItem();
        fi1.setFoodItemId((long) 1);
        fi1.setMealId(meal.getMealId());
        fi1.setImageUrl("img1");
        fi1.setAudioUrls("aud1");
        FoodItem fi2 = new FoodItem();
        fi2.setFoodItemId((long) 2);
        fi2.setMealId(meal.getMealId());
        fi2.setImageUrl("img2");
        fi2.setAudioUrls("aud2");
        FoodItem fi3 = new FoodItem();
        fi3.setFoodItemId((long) 3);
        fi3.setMealId(meal.getMealId());
        fi3.setImageUrl("img3");
        fi3.setAudioUrls("aud3");
        db.getFoodItemDao().insert(fi1, fi2, fi3);

        List<FoodItem> allFisBefore = db.getFoodItemDao().getAll();
        assertThat(allFisBefore.size(), is(3));


        mIntentsTestRule.launchActivity(intent);

        onView(withId(R.id.btnFinalizeEat)).perform(click());

        List<FoodItem> allFisAfter = db.getFoodItemDao().getAll();
        assertThat(allFisAfter.size(), is(12));

        //Check Household member 1
        List<FoodRecord> foodRecords = db.getFoodRecordDao().getAllFoodRecordsForHouseholdMember(hm1.getUid());
        assertThat(foodRecords.size(), is(1));
        FoodRecord hm1Fr = foodRecords.get(0);
        List<EatingOccasion> hm1Eos = db.getEatingOccasionDao().getAllEatingOccasionsForFoodRecord(hm1Fr.getFoodRecordId());
        assertThat(hm1Eos.size(), is(1));
        List<FoodItem> hm1Fis = db.getFoodItemDao().getAllFoodItemsForEatingOccasion(hm1Eos.get(0).getId());
        assertThat(hm1Fis.size(), is(3));

    }
}

