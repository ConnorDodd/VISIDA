package au.edu.newcastle.jnc985.visida.activity;

import android.app.Instrumentation;
import androidx.lifecycle.ViewModelProviders;
import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.intent.rule.IntentsTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Calendar;
import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.RecyclerViewMatcher;
import bo.MealViewModel;
import bo.MealViewModelFactory;
import bo.db.AppDatabase;
import bo.db.entity.Household;
import bo.db.entity.HouseholdMember;
import bo.db.entity.Meal;
import bo.db.entity.Recipe;

import static android.app.Activity.RESULT_OK;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static au.edu.newcastle.jnc985.visida.RecyclerViewItemCountAssertion.withItemCount;
import static bo.AppConstants.AUDIOFILE_NAME;
import static bo.AppConstants.IMAGE_NAME;
import static bo.AppConstants.SHARED_DISH_AUDIOFILE_TEMPLATE;
import static bo.AppConstants.SHARED_DISH_IMAGE_NAME_TEMPLATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

/**
 * Created by jnc985 on 30-Nov-17.
 * Test class for Meal Activity.
 */

@RunWith(MockitoJUnitRunner.class)
public class MealActivityTest {
    private AppDatabase mDb;

    @Mock
    private MealViewModel mViewModel;

    private Context mContext;

    //Set up Rule
    @Rule
    public IntentsTestRule<MealActivity> mIntentsRule = new IntentsTestRule<>(MealActivity.class, true, false);
    private int mHm1Uid = 1;
    private int mHm2Uid = 2;
    private int mHm3Uid = 3;
    private String mHm1Name = "hm1";
    private String mHm2Name = "hm2";
    private String mHm3Name = "hm3";
    private String HHID = "HHID";


    @Before
    public void createDb() throws Throwable {
        mContext = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).build();
        AppDatabase.setInstance(mDb);

        Household hh = new Household();
        hh.setHouseholdId(HHID);
        mDb.getHouseholdDao().insert(hh);
        HouseholdMember hm1 = new HouseholdMember(mHm1Uid, HHID, mHm1Name, "", 1, false, false);
        HouseholdMember hm2 = new HouseholdMember(mHm2Uid, HHID, mHm2Name, "", 1, false, false);
        HouseholdMember hm3 = new HouseholdMember(mHm3Uid, HHID, mHm3Name, "", 1, false, false);
        mDb.getHouseholdMemberDao().insert(hm1, hm2, hm3);

    }

    @After
    public void closeDb() {
        mDb.close();
    }

    @Test
    public void newMealIsCreatedWhenPageOpensWithNoRecentMeals() {
        List<Meal> meals = AppDatabase.getInstance(mContext).getMealDao().getAll();
        mIntentsRule.launchActivity(new Intent());
        List<Meal> mealsAfter = AppDatabase.getInstance(mContext).getMealDao().getAll();

        assertThat(mealsAfter.size(), is(meals.size() + 1));
        Meal newMeal = mealsAfter.get(0);

        MealViewModel mealViewModel = ViewModelProviders.of(
                mIntentsRule.getActivity(), new MealViewModelFactory(mIntentsRule.getActivity().getApplication(), 0))
                .get(MealViewModel.class);

        Meal m = mealViewModel.getMeal();
        assertThat(newMeal, is(m));
    }

    @Test
    public void mealViewModelUsesSameMealIfLessThanOneHourOld() {
        //Create a meal
        Meal currentMeal = new Meal();
        //Add to the database
        currentMeal.setMealId(mDb.getMealDao().insert(currentMeal)[0]);

        //Launch the activity
        mIntentsRule.launchActivity(new Intent());

        MealViewModel mealViewModel = ViewModelProviders.of(
                mIntentsRule.getActivity(), new MealViewModelFactory(mIntentsRule.getActivity().getApplication(), 0))
                .get(MealViewModel.class);

        Meal viewModelsMeal = mealViewModel.getMeal();
        assertThat(viewModelsMeal, is(currentMeal));
    }

    @Test
    public void mealViewModelCreatesANewMealIfOnlyOldMealsExist() {
        Meal oldMeal = new Meal();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -2);
        oldMeal.setStartTime(cal.getTime());

        oldMeal.setMealId(mDb.getMealDao().insert(oldMeal)[0]);

        //Launch the activity
        mIntentsRule.launchActivity(new Intent());

        MealViewModel mealViewModel = ViewModelProviders.of(
                mIntentsRule.getActivity(), new MealViewModelFactory(mIntentsRule.getActivity().getApplication(), 0))
                .get(MealViewModel.class);

        Meal viewModelsMeal = mealViewModel.getMeal();
        assertThat(mealViewModel, not(is(oldMeal)));

        List<Meal> allMeals = mDb.getMealDao().getAll();
        assertThat(allMeals.size(), is(2));

    }

    @Test
    public void clickAddDishMovesToCameraActivity() {
        mIntentsRule.launchActivity(new Intent());

        //Mock the intent so we dont actually open the camera
        Intent i = new Intent();
        i.putExtra(IMAGE_NAME, SHARED_DISH_IMAGE_NAME_TEMPLATE);
        i.putExtra(AUDIOFILE_NAME, SHARED_DISH_AUDIOFILE_TEMPLATE);
        Instrumentation.ActivityResult intentResult = new Instrumentation.ActivityResult(RESULT_OK, i);

        intending(anyIntent()).respondWith(intentResult);

        //Click add dish
        onView(withId(R.id.btnAddDish)).perform(click());

        //Verify we are going to the camera
        intended(hasComponent(CameraActivity.class.getName()));
    }

    @Test
    public void longClickDishDeletesTheDish() {
        //Add a dish to the view model
        mIntentsRule.launchActivity(new Intent());

        mViewModel = ViewModelProviders.of(mIntentsRule.getActivity()).get(MealViewModel.class);

        mViewModel.addDish("", "");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Check the dish is present
        onView(withId(R.id.rvDishList)).check(withItemCount(1));

        //Long click the dish
        onView(new RecyclerViewMatcher(R.id.rvDishList).atPositionOnView(0, R.id.imgIngredientImage)).perform(longClick());

        //Confirm the delete
        onView(withText(R.string.yes)).perform(click());

        //Check dish is gone
        onView(withId(R.id.rvDishList)).check(withItemCount(0));
    }

    public void addRecipes() {
        Recipe r1 = new Recipe();
        r1.setRecipeId(1);
        r1.setRecipeNameText("RECIPE1");
        Recipe r2 = new Recipe();
        r2.setRecipeId(2);
        r2.setRecipeNameText("RECIPE 2");
        Recipe r3 = new Recipe();
        r3.setRecipeId(3);
        r3.setRecipeNameText("RECIPE 3");
        r3.setLocked(true);

        mDb.getRecipeDao().insert(r1, r2, r3);

    }
    @Test
    public void clickLinkRecipeOpensDialog(){
        addRecipes();

        mIntentsRule.launchActivity(new Intent());
        onView(withId(R.id.btnLinkRecipe)).perform(click());

        onView(withId(R.id.recipeListTitle)).check(matches(isDisplayed()));

        //Check that all the recipes are displayed
        onView(withId(R.id.rvRecipeList)).check(withItemCount(3));
    }

    @Test
    public void addRecipeStoresIdInMeal(){
        addRecipes();

        mIntentsRule.launchActivity(new Intent());

        //Get the view model
        MealViewModel mealViewModel = ViewModelProviders.of(
                mIntentsRule.getActivity(), new MealViewModelFactory(mIntentsRule.getActivity().getApplication(), 0))
                .get(MealViewModel.class);

        //Check the recipes Id list is empty
        List<Long> recipeIdsBefore = mealViewModel.getMeal().getRecipeIds();
        assertThat(recipeIdsBefore.size(), is(0));

        //Click link recipe
        onView(withId(R.id.btnLinkRecipe)).perform(click());
        onView(new RecyclerViewMatcher(R.id.rvRecipeList).atPositionOnView(1, R.id.chkIsSelected)).perform(click());

        //Click accept
        onView(withText(R.string.audio_accept)).perform(click());

        //Get the meal and check the recipe id is stored
        Meal m = mealViewModel.getMeal();
        List<Long> recipeIdsAfter = m.getRecipeIds();
        assertThat(recipeIdsAfter.size(), is(1));
    }
}
