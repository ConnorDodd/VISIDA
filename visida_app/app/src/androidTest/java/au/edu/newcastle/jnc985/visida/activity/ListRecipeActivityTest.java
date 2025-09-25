package au.edu.newcastle.jnc985.visida.activity;

import android.app.Activity;
import android.app.Instrumentation;
import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.RecyclerViewMatcher;
import bo.db.AppDatabase;
import bo.db.entity.Recipe;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static au.edu.newcastle.jnc985.visida.RecyclerViewItemCountAssertion.withItemCount;
import static bo.AppConstants.RECIPEID;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;

/**
 * Created by jnc985 on 09-Apr-18.
 */

@RunWith(AndroidJUnit4.class)
public class ListRecipeActivityTest {

    @Rule
    public IntentsTestRule<ListRecipesActivity> mActivityRule = new IntentsTestRule<ListRecipesActivity>(ListRecipesActivity.class, true, false);

    private AppDatabase mDb;
    private Context mContext;

    @Before
    public void setupDb() {
        mContext = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).build();
        AppDatabase.setInstance(mDb);
    }

    @After
    public void closeDb() {
        mDb.close();
    }

    @Test
    public void listCorrectNumberOfRecipesShowsAll() {
        //List only unlocked recipes
        Recipe lockedRecipe = new Recipe();
        lockedRecipe.setLocked(true);
        Recipe unlockedRecipe = new Recipe();
        unlockedRecipe.setLocked(false);
        Recipe unlockedRecipe2 = new Recipe();
        unlockedRecipe2.setLocked(false);
        mDb.getRecipeDao().insert(lockedRecipe, unlockedRecipe, unlockedRecipe2);

        //launch the activity
        mActivityRule.launchActivity(new Intent());

        onView(withId(R.id.rvRecipes)).check(withItemCount(3));
    }

    @Test
    public void clickEditOnUnlockedRecipeMovesToCreateRecipePageAndLockedRecipeRemains() {
        long lockedRecipeId = 1;
        long unlockedRecipeId = 2;
        //List only saved recipes
        Recipe lockedRecipe = new Recipe();
        lockedRecipe.setRecipeId(lockedRecipeId);
        lockedRecipe.setLocked(true);
        Recipe unlockedRecipe = new Recipe();
        unlockedRecipe.setRecipeId(unlockedRecipeId);
        unlockedRecipe.setLocked(false);
        mDb.getRecipeDao().insert(lockedRecipe, unlockedRecipe);

        //launch the activity
        mActivityRule.launchActivity(new Intent());

        //Mock the intent to edit the recipe
        Intent intent = new Intent();
        Instrumentation.ActivityResult intentResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, intent);
        intending(anyIntent()).respondWith(intentResult);

        //Click the locked recipe
        onView(new RecyclerViewMatcher(R.id.rvRecipes).atPositionOnView(0, R.id.btnFinalizeItem)).perform(click());

        //Click the unlocked recipe
        onView(new RecyclerViewMatcher(R.id.rvRecipes).atPositionOnView(1, R.id.btnFinalizeItem)).perform(click());

        intended(allOf(hasComponent(CreateRecipeActivity.class.getName()),
                hasExtra(RECIPEID, unlockedRecipeId)));

    }

    @Test
    public void longClickButtonDeletesRecipe(){
        long unlockedRecipeId = 1;
        //List only saved recipes
        Recipe unlockedRecipe = new Recipe();
        unlockedRecipe.setRecipeId(unlockedRecipeId);
        unlockedRecipe.setLocked(false);
        mDb.getRecipeDao().insert(unlockedRecipe);

        //launch the activity
        mActivityRule.launchActivity(new Intent());

        //Click the recipe
        onView(new RecyclerViewMatcher(R.id.rvRecipes).atPositionOnView(0, R.id.btnFinalizeItem)).perform(longClick());

        //Check the alert appears and click ok
        onView(withText(R.string.yes)).check(matches(isDisplayed()));
        onView(withText(R.string.yes)).perform(click());

        //Get the list of recipes
        List<Recipe> allRecipes = mDb.getRecipeDao().getAll();
        assertThat(allRecipes.size(), is(0));
    }

}
