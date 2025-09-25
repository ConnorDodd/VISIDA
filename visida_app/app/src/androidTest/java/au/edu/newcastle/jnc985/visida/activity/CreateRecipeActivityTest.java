package au.edu.newcastle.jnc985.visida.activity;

import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.intent.rule.IntentsTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.RecyclerViewMatcher;
import au.edu.newcastle.jnc985.visida.TestUtilities;
import bo.State;
import bo.Utilities;
import bo.db.AppDatabase;
import bo.db.entity.IngredientCapture;
import bo.db.entity.Recipe;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static au.edu.newcastle.jnc985.visida.RecyclerViewItemCountAssertion.withItemCount;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.RECIPEID;
import static bo.AppConstants.STATE;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;

/**
 * Created by jnc985 on 30-Nov-17.
 */


@RunWith(MockitoJUnitRunner.class)
public class CreateRecipeActivityTest {

    private AppDatabase mDb;
    private Context mContext;

    private Intent mIntent;

    private File mIcImage;
    private File mIcAudio;

    private final long completeRecipeId = 1;
    private final long nameAudioOnlyId = 2;
    private final long nameAudioSingleIngredientOnlyId = 3;
    private final long nameAudioIngredientFinalImageId = 4;
    private final long noAudioId = 5;
    private String recipeName = "RECIPE NAME";
    //private final long nameAudioOnlyId = 5;


    @Rule
    public IntentsTestRule<CreateRecipeActivity> mIntentsRule = new IntentsTestRule<CreateRecipeActivity>(CreateRecipeActivity.class, true, false) {
        @Override
        protected Intent getActivityIntent() {
            return mIntent;
        }
    };


    @Before
    public void createDb() {
        mContext = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).build();
        //Add the mock database to the app
        AppDatabase.setInstance(mDb);

        createRecipes();

        //Set state to COOK
        //Set state to EAT
        SharedPreferences sharedPref = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(STATE, State.COOK.ordinal());
        editor.commit();

    }

    private void createRecipes() {
        //Creat the recipe and add it to the database
        String imageName = "sampleIcImage.jpg";
        String audioName = "sampleIcAudio.mp3";
        mIcImage = new File(Utilities.getMediaDirectory(mContext), imageName);
        mIcImage.mkdir();
        mIcAudio = new File(Utilities.getMediaDirectory(mContext), audioName);
        mIcAudio.mkdir();

        //Create Recipe object
        Recipe r1 = new Recipe();
        r1.setRecipeId(completeRecipeId);
        //r1.setSaved(true);
        r1.setFinalImageUrl("");
        r1.setRecipeNameAudioUrl("");
        Recipe r2 = new Recipe();
        r2.setRecipeId(nameAudioIngredientFinalImageId);
        //r2.setSaved(true);
        r2.setFinalImageUrl("");
        r2.setRecipeNameAudioUrl("");
        Recipe r3 = new Recipe();
        r3.setRecipeId(nameAudioSingleIngredientOnlyId);
        //r3.setSaved(true);
        r3.setFinalImageUrl("");
        r3.setRecipeNameAudioUrl("");
        Recipe r4 = new Recipe();
        r4.setRecipeId(nameAudioOnlyId);
        //r4.setSaved(true);
        r4.setFinalImageUrl("IMAGE_PLACEHOLDER");
        r4.setRecipeNameAudioUrl("");
        Recipe r5 = new Recipe();
        r5.setRecipeId(noAudioId);
        r5.setRecipeNameText(recipeName);

        mDb.getRecipeDao().insert(r1, r2, r3, r4, r5);

        //Create Ingredients
        IngredientCapture ic1 = new IngredientCapture();
        ic1.setAudioUrl(audioName);
        ic1.setImageUrl(imageName);
        ic1.setRecipeId(completeRecipeId);
        ic1.setIngredientId(completeRecipeId);
        IngredientCapture ic2 = new IngredientCapture();
        ic2.setAudioUrl(audioName);
        ic2.setImageUrl(imageName);
        ic2.setRecipeId(nameAudioIngredientFinalImageId);
        ic2.setIngredientId(nameAudioIngredientFinalImageId);
        IngredientCapture ic3 = new IngredientCapture();
        ic3.setAudioUrl(audioName);
        ic3.setImageUrl(imageName);
        ic3.setRecipeId(nameAudioSingleIngredientOnlyId);
        ic3.setIngredientId(nameAudioSingleIngredientOnlyId);

        mDb.getIngredientDao().insert(ic1, ic2, ic3);


    }

    @After
    public void closeDb() {
        mDb.close();
        if (mIcAudio.exists()) {
            mIcAudio.delete();
        }
        if (mIcImage.exists()) {
            mIcImage.delete();
        }
    }

    @Test
    public void recipeOpensWithNameAudioFile() {
        mIntent = new Intent();
        mIntent.putExtra(RECIPEID, nameAudioOnlyId);
        mIntentsRule.launchActivity(mIntent);
        onView(withId(R.id.imgAudioFile)).check(matches(isDisplayed()));
    }

    @Test
    public void recipeOpensWithTextNameIfNoAudio(){
        //When run amongst other test, if the previous test showed a toast message, we must
        //wait for it to clear before checking this one.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mIntent = new Intent();
        mIntent.putExtra(RECIPEID, noAudioId);
        mIntentsRule.launchActivity(mIntent);
        onView(withId(R.id.imgAudioFile)).check(matches(isDisplayed()));

        onView(withId(R.id.imgAudioFile)).perform(click());
        onView(withText(containsString(recipeName)))
                .inRoot(withDecorView(not(mIntentsRule.getActivity().getWindow().getDecorView())))
                .check(matches(isDisplayed()));
    }

    @Test
    public void audioFileInvisibleOnStart() {
        mIntentsRule.launchActivity(null);
        onView(withId(R.id.imgAudioFile)).check(matches(not(isDisplayed())));
    }

    @Test
    public void clickRecordButtonOpensRecordFragmentFileAppearsOnComplete() throws InterruptedException {
        //Get the number of recipes before launching activity
        List<Recipe> recipesList = TestUtilities.getValue(mDb.getRecipeDao().getAllObservable());
        int recipes = recipesList.size();

        //Start activity (will create a new recipe)
        mIntentsRule.launchActivity(null);
        onView(withId(R.id.imgRecordAudioBtn)).perform(click());

        //Wait to load fragment
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Check the record button is there and click it
        onView(withId(R.id.btnRecordAudio)).check(matches(isDisplayed())).perform(click());

        //Wait to record some audio
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Stop recording
        onView(withId(R.id.btnAudioFile)).perform(click());

        //Click accept
        onView(withId(R.id.btnAudioAccept)).perform(click());

        //Make sure fragment is gone
        onView(withId(R.id.btnRecordAudio)).check(doesNotExist());

        //Check that file is now visible
        onView(withId(R.id.imgAudioFile)).check(matches(isDisplayed()));

        //Check the audio file has been created
        List<Recipe> allRecipes = TestUtilities.getValue(AppDatabase.getInstance(mContext).getRecipeDao().getAllObservable());
        assertThat(allRecipes.size(), greaterThan(0));

        //Check the audio name of the recipe is correct
        Recipe r = allRecipes.get(recipes);
        File mediaDir = Utilities.getMediaDirectory(mContext);
        String audioFileName = r.getRecipeNameAudioUrl();
        File recipeNameAudio = new File(mediaDir, audioFileName);

        assertThat(recipeNameAudio.exists(), is(true));
    }

    @Test
    public void openingActivityCreatesNewRecipe() throws InterruptedException {
        //Get the number of recipes before starting the activity.
        List<Recipe> recipesList = TestUtilities.getValue(mDb.getRecipeDao().getAllObservable());
        int recipeCount = recipesList.size();

        //Start the Activity
        mIntentsRule.launchActivity(null);

        //Get the number of recipes after opening the activity.
        List<Recipe> allRecipes = TestUtilities.getValue(AppDatabase.getInstance(mContext).getRecipeDao().getAllObservable());
        assertThat(allRecipes.size(), is(recipeCount + 1));
    }

    @Test
    public void clickSubmitPromptsToTakeFinalImageIfNoImageTaken() {
        mIntentsRule.launchActivity(null);
        mIntentsRule.getActivity().setAudioRecorded(true);
        onView(withId(R.id.btnFinish)).perform(click());

        onView(withText(R.string.take_final_image)).check(matches(isDisplayed()));
    }

    @Test
    public void clickSubmitPromptsSkipsFinalImageIfAlreadyTaken() {
        mIntent = new Intent();
        mIntent.putExtra(RECIPEID, nameAudioOnlyId);
        mIntentsRule.launchActivity(mIntent);

        onView(withId(R.id.btnFinish)).perform(click());

        onView(withText(R.string.take_final_image)).check(doesNotExist());
    }


    @Test
    public void clickYesOnSubmitPromptMovesToCameraActivity() throws InterruptedException {
        List<Recipe> recipesList = TestUtilities.getValue(mDb.getRecipeDao().getAllObservable());
        int numOfRecipes = recipesList.size();

        mIntentsRule.launchActivity(null);
        mIntentsRule.getActivity().setAudioRecorded(true);
        onView(withId(R.id.btnFinish)).perform(click());

        onView(withText(R.string.yes)).perform(click());

        //Get the recipe
        List<Recipe> allRecipes = TestUtilities.getValue(AppDatabase.getInstance(mContext).getRecipeDao().getAllObservable());
        assertThat(allRecipes.size(), is(numOfRecipes + 1));
        Recipe r = allRecipes.get(numOfRecipes);
        intended(hasComponent(CameraActivity.class.getName()));

        //Let the camera focus
        Thread.sleep(1500);

        //Take the image
        onView(withId(R.id.btnTakePicture)).perform(click());

        //Wait for the preview box
        Thread.sleep(1500);

        onView(withId(R.id.btnImageAccept)).perform(click());

        onView(withId(R.id.rvRecipes)).check(matches(isDisplayed()));


    }

    @Test
    public void clickNoOnSubmitPromptMovesToCameraActivity() throws InterruptedException {
        List<Recipe> recipesList = TestUtilities.getValue(mDb.getRecipeDao().getAllObservable());
        int recipeCount = recipesList.size();

        mIntentsRule.launchActivity(null);
        mIntentsRule.getActivity().setAudioRecorded(true);
        onView(withId(R.id.btnFinish)).perform(click());

        onView(withText(R.string.no)).perform(click());

        //Get the recipe
        intended(hasComponent(ListRecipesActivity.class.getName()));
    }

    @Test
    public void clickAddIngredientAddsToRecyclerView() {
        mIntentsRule.launchActivity(null);
        onView(withId(R.id.rvIngredientList)).check(withItemCount(0));
        onView(withId(R.id.btnAddIngredient)).perform(click());
        intended(hasComponent(CameraActivity.class.getName()));

        //Click take picture
        onView(withId(R.id.btnTakePicture)).perform(click());

        //Wait for the dialog to open
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Click accept Picture
        onView(withId(R.id.btnImageAccept)).perform(click());

        //Record audio
        onView(withId(R.id.btnRecordAudio)).perform(click());

        //Stop Recording
        onView(withId(R.id.btnAudioFile)).perform(click());

        //Accept Audio
        onView(withId(R.id.btnAudioAccept)).perform(click());

        //Check that the recycler view has an extra item
        onView(withId(R.id.rvIngredientList)).check(withItemCount(1));
    }

    @Test
    public void clickAddIngredientMovesToCameraActivity() {
        mIntentsRule.launchActivity(null);
        onView(withId(R.id.rvIngredientList)).check(withItemCount(0));
        onView(withId(R.id.btnAddIngredient)).perform(click());
        intended(hasComponent(CameraActivity.class.getName()));
    }

    @Test
    public void longClickIngredientDeletesIngredient() {
        mIntent = new Intent();
        mIntent.putExtra(RECIPEID, completeRecipeId);
        mIntentsRule.launchActivity(mIntent);

        //Long click the ingredient
        //Click the first entry in the recycler view
        onView(new RecyclerViewMatcher(R.id.rvIngredientList).atPositionOnView(0,R.id.imgIngredientImage)).perform(longClick());

        //Click the ok button
        onView(withText(R.string.yes)).perform(click());

        //Check the image and audio files are deleted
        assertThat(mIcImage.exists(), is(false));
        assertThat(mIcAudio.exists(), is(false));

        List<IngredientCapture> ics = mDb.getIngredientDao().getIngredientsForRecipe(completeRecipeId);
        assertThat(ics.size(), is(0));
    }

    @Test
    public void clickSubmitWithNoAudioFilePromptsToRecordRecipeName() {
        mIntentsRule.launchActivity(null);
        mIntentsRule.getActivity().setAudioRecorded(false);
        onView(withId(R.id.btnFinish)).perform(click());

        onView(withText(R.string.recipe_name_required)).check(matches(isDisplayed()));
        onView(withText(R.string.yes)).perform(click());

        onView(withId(R.id.btnRecordAudio)).check(matches(isDisplayed()));
    }
}
