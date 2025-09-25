package au.edu.newcastle.jnc985.visida.activity;

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
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.RecyclerViewMatcher;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;
import bo.db.entity.Recipe;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static au.edu.newcastle.jnc985.visida.RecyclerViewItemCountAssertion.withItemCount;
import static bo.AppConstants.FR;
import static org.hamcrest.core.Is.is;

/**
 * Created by jnc985 on 30-Nov-17.
 */

@RunWith(MockitoJUnitRunner.class)
public class EatingOccasionActivityAusTest {

    protected AppDatabase mDb;
    protected Intent mIntent;
    protected Context mContext;

    private HouseholdMember hm1;
    private String mHmName = "householdMemberName";
    private FoodRecord mFr;
    private EatingOccasion mEo;
    //Set up Rule
    @Rule
    public IntentsTestRule<EatingOccasionActivity> mIntentsRule = new IntentsTestRule<EatingOccasionActivity>(EatingOccasionActivity.class, true, false) {
        @Override
        protected Intent getActivityIntent() {
            //Put the EAT state in as default Each test will have to set the state accordingly
            Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
            mIntent = new Intent(targetContext, EatingOccasionActivity.class);
            mIntent.putExtra(FR, new FoodRecord(0));
            return mIntent;
        }
    };

    @Before
    public void createDb() throws Throwable {
        mContext = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).build();
        AppDatabase.setInstance(mDb);
        mIntent = new Intent(mContext, EatingOccasionActivity.class);
        hm1 = new HouseholdMember(1, "", mHmName, "", 25, false, false);
        mDb.getHouseholdMemberDao().insert(hm1);
        FoodRecord fr = new FoodRecord(hm1.getUid());
        fr.setFoodRecordId((long) 1);
        fr.setHouseholdMember(hm1);
        List<EatingOccasion> eos = new ArrayList<EatingOccasion>();
        EatingOccasion mEo = new EatingOccasion();
        mEo.setFoodRecordId(fr.getFoodRecordId());
        mEo.setEatingOccasionId((long)1);
        mEo.setStartTime(new Date());
        eos.add(mEo);
        fr.setEatingOccasions(eos);
        mIntent.putExtra(FR, fr);
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

   @After
    public void closeDb() {
       mDb.close();
    }

   protected void addFoodRecordAndEatingOccasionToDB(){
       //Add Eating Occasion and FoodRecord
       mFr = new FoodRecord(hm1.getUid());
       mFr.setFoodRecordId((long) 1);
       mFr.setHouseholdMember(hm1);
       mDb.getFoodRecordDao().insert(mFr);

       mEo = new EatingOccasion();
       mEo.setFoodRecordId(mFr.getFoodRecordId());
       mEo.setEatingOccasionId((long)1);
       mEo.setStartTime(new Date());
       mDb.getEatingOccasionDao().insert(mEo);
   }

    @Test
    public void clickLinkRecipeOpensDialog() {
        addRecipes();
        //Add Eating Occasion and FoodRecord
        addFoodRecordAndEatingOccasionToDB();

        mIntentsRule.launchActivity(mIntent);
        onView(withId(R.id.btnLinkRecipe)).perform(click());

        onView(withId(R.id.recipeListTitle)).check(matches(isDisplayed()));

        //Check that all the recipes are displayed
        onView(withId(R.id.rvRecipeList)).check(withItemCount(3));
    }

    @Test
    public void selectRecipesReturnsSelectedRecipeIds() {
        addRecipes();
        //Add Eating Occasion and FoodRecord
        addFoodRecordAndEatingOccasionToDB();

        mIntentsRule.launchActivity(mIntent);
        onView(withId(R.id.btnLinkRecipe)).perform(click());

        onView(withId(R.id.recipeListTitle)).check(matches(isDisplayed()));

        //Select the first and last recipe r1 r3.
        onView(new RecyclerViewMatcher(R.id.rvRecipeList).atPositionOnView(0, R.id.chkIsSelected)).perform(click());
        onView(new RecyclerViewMatcher(R.id.rvRecipeList).atPositionOnView(2, R.id.chkIsSelected)).perform(click());

        //Click Accept
        onView(withText(R.string.audio_accept)).perform(click());

        //Get the eating occasion and check the ids stored under recipes
        EatingOccasion eo = mDb.getEatingOccasionDao().getEatingOccasion(mEo.getEatingOccasionId()).get(0);
        List<Long> selectedRecipes = eo.getRecipeIds();
        assertThat(selectedRecipes.size(), is(2));
    }

    @Test
    public void reOpeningRecipeListAfterAcceptingRemembersSelection() {
        addRecipes();
        addFoodRecordAndEatingOccasionToDB();
        mIntentsRule.launchActivity(mIntent);
        onView(withId(R.id.btnLinkRecipe)).perform(click());

        onView(withId(R.id.recipeListTitle)).check(matches(isDisplayed()));
        int selection1 = 0;
        int selection2 = 2;
        //Select the first and last recipe r1 r3.
        onView(new RecyclerViewMatcher(R.id.rvRecipeList).atPositionOnView(selection1, R.id.chkIsSelected)).perform(click());
        onView(new RecyclerViewMatcher(R.id.rvRecipeList).atPositionOnView(selection2, R.id.chkIsSelected)).perform(click());

        //Click Accept
        onView(withText(R.string.audio_accept)).perform(click());

        //Get the eating occasion and check the ids stored under recipes
        EatingOccasion eo = mDb.getEatingOccasionDao().getEatingOccasion(mEo.getEatingOccasionId()).get(0);
        List<Long> selectedRecipes = eo.getRecipeIds();
        assertThat(selectedRecipes.size(), is(2));

        //Click the link button again
        onView(withId(R.id.btnLinkRecipe)).perform(click());

        //Check that r1 and r3 are checked
        onView(new RecyclerViewMatcher(R.id.rvRecipeList).atPositionOnView(selection1, R.id.chkIsSelected)).check(matches(isChecked()));
        onView(new RecyclerViewMatcher(R.id.rvRecipeList).atPositionOnView(selection2, R.id.chkIsSelected)).check(matches(isChecked()));
    }
}
