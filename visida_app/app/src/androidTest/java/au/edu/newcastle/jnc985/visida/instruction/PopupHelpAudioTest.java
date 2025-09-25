//package au.edu.newcastle.jnc985.visida.instruction;
//
//import android.arch.persistence.room.Room;
//import android.content.Context;
//import android.content.Intent;
//import android.media.MediaPlayer;
//import android.support.test.InstrumentationRegistry;
//import android.support.test.rule.ActivityTestRule;
//
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//
//import au.edu.newcastle.jnc985.visida.R;
//import au.edu.newcastle.jnc985.visida.activity.AudioRecordingFragment;
//import au.edu.newcastle.jnc985.visida.activity.BreastfeedActivity;
//import au.edu.newcastle.jnc985.visida.activity.CreateRecipeActivity;
//import au.edu.newcastle.jnc985.visida.activity.FinalizeFoodItemActivity;
//import au.edu.newcastle.jnc985.visida.activity.FinalizeFoodItemFragment;
//import au.edu.newcastle.jnc985.visida.activity.RecordReviewActivity;
//import bo.db.AppDatabase;
//import bo.db.entity.EatingOccasion;
//import bo.db.entity.FoodItem;
//import bo.db.entity.FoodRecord;
//import bo.db.entity.HouseholdMember;
//import bo.db.entity.Meal;
//import bo.db.entity.Recipe;
//import ui.HelpAudioButtonController;
//
//import static android.support.test.espresso.Espresso.onView;
//import static android.support.test.espresso.action.ViewActions.click;
//import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
//import static android.support.test.espresso.matcher.ViewMatchers.withId;
//import static bo.AppConstants.AUDIO_ONLY_FRAGMENT_TAG;
//import static bo.AppConstants.EOID;
//import static bo.AppConstants.FINALIZE_FRAGMENT;
//import static bo.AppConstants.FRID;
//import static bo.AppConstants.PPID;
//import static bo.AppConstants.RECIOE_FINALE_IMAGE_DIALOG;
//import static bo.AppConstants.RECIPEID;
//import static org.hamcrest.CoreMatchers.is;
//
//public class PopupHelpAudioTest {
//
//    @Rule
//    public ActivityTestRule<FinalizeFoodItemActivity> mFinaliseFoodItemActivityTestRule = new ActivityTestRule<FinalizeFoodItemActivity>(FinalizeFoodItemActivity.class, true, false);
//
//    @Rule
//    public ActivityTestRule<BreastfeedActivity> mBreastfeedActivityTestRule = new ActivityTestRule<BreastfeedActivity>(BreastfeedActivity.class, true, false);
//
//    @Rule
//    public ActivityTestRule<CreateRecipeActivity> mCreateRecipeActivityTestRule = new ActivityTestRule<CreateRecipeActivity>(CreateRecipeActivity.class, true, false);
//
//    @Rule
//    public ActivityTestRule<RecordReviewActivity> mRecordReviewActivityTestRule = new ActivityTestRule<RecordReviewActivity>(RecordReviewActivity.class, true, false);
//
//    private Context mContext;
//    private AppDatabase mDb;
//    private String ppid = "ppid";
//
//    @Before
//    public void setupDb(){
//        mContext = InstrumentationRegistry.getTargetContext();
//        mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).build();
//        AppDatabase.setInstance(mDb);
//    }
//
//    @Test
//    public void onClickLeftoverPopupPlaysAudio(){
//        //Set up A household member
//        HouseholdMember hm = new HouseholdMember(1, "1", "HM1", "", 1, false, true);
//        hm.setParticipantHouseholdMemberId(ppid);
//        mDb.getHouseholdMemberDao().insert(hm);
//
//        //Set up a FoodRecord
//        FoodRecord fr = new FoodRecord(hm.getUid());
//        fr.setFoodRecordId((long) 1);
//        mDb.getFoodRecordDao().insert(fr);
//
//        //Set up a Shared Eating Occasion
//        EatingOccasion eo = new EatingOccasion();
//        eo.setEatingOccasionId((long)1);
//        eo.setFoodRecordId(fr.getFoodRecordId());
//        mDb.getEatingOccasionDao().insert(eo);
//
//        //Set up the Shared Food Item
//        FoodItem fi = new FoodItem();
//        fi.setFoodItemId(1);
//        fi.setEatingOccasionId(eo.getEatingOccasionId());
//        fi.setMealId(1);
//        fi.setImageUrl("");
//        fi.setAudioUrls("");
//        mDb.getFoodItemDao().insert(fi);
//
//        //Launch the finalise Activity
//        Intent i = new Intent();
//        i.putExtra(EOID, (long)1);
//        mFinaliseFoodItemActivityTestRule.launchActivity(i);
//
//        //Inject the Media Player into the fragment
//        MediaPlayer mp = MediaPlayer.create(mContext, R.raw.inst_audio_v1);
//        FinalizeFoodItemFragment frag = (FinalizeFoodItemFragment) mFinaliseFoodItemActivityTestRule.getActivity().getSupportFragmentManager().findFragmentByTag(FINALIZE_FRAGMENT);
//        frag.setHelpMediaPlayer(mp);
//
//        //Click the Help button
//        onView(withId(R.id.imgHelp)).perform(click());
//        //Check its playing
//        assertThat(mp.isPlaying(), is(true));
//
//        //Click the button again
//        onView(withId(R.id.imgHelp)).perform(click());
//        //Check it has stopped
//        assertThat(mp.isPlaying(), is(false));
//    }

//    @Test
//    public void clickHelpOnGuestPopupPlaysAudio(){
//        //Set up A household member
//        HouseholdMember hm = new HouseholdMember(1, "1", "HM1", "", 1, false, true);
//        hm.setParticipantHouseholdMemberId(ppid);
//        mDb.getHouseholdMemberDao().insert(hm);
//
//        //Create a meal to hold the Guest info
//        Meal meal = new Meal();
//        meal.setMealId((long) 1);
//        mDb.getMealDao().insert(meal);
//
//        //Set up a FoodRecord
//        FoodRecord fr = new FoodRecord(hm.getUid());
//        fr.setFoodRecordId((long) 1);
//        mDb.getFoodRecordDao().insert(fr);
//
//        //Set up a Shared Eating Occasion
//        EatingOccasion eo = new EatingOccasion();
//        eo.setEatingOccasionId((long)1);
//        eo.setFoodRecordId(fr.getFoodRecordId());
//        mDb.getEatingOccasionDao().insert(eo);
//
//        //Set up the Shared Food Item
//        FoodItem fi = new FoodItem();
//        fi.setFoodItemId(1);
//        fi.setEatingOccasionId(eo.getEatingOccasionId());
//        fi.setMealId(meal.getMealId());
//        fi.setImageUrl("");
//        fi.setAudioUrls("");
//        fi.setLeftoverImageUrl("NO IMAGE"); //Prescence of Leftover image is used to check if leftovers are captured. Setting this will skip the leftover popup.
//        mDb.getFoodItemDao().insert(fi);
//
//        //Launch the finalise Activity
//        Intent i = new Intent();
//        i.putExtra(EOID, (long)1);
//        mFinaliseFoodItemActivityTestRule.launchActivity(i);
//
//        //Inject the Media Player into the fragment
//        MediaPlayer mp = MediaPlayer.create(mContext, R.raw.inst_audio_v1);
//        FinalizeFoodItemFragment frag = (FinalizeFoodItemFragment) mFinaliseFoodItemActivityTestRule.getActivity().getSupportFragmentManager().findFragmentByTag(FINALIZE_FRAGMENT);
//        frag.setHelpMediaPlayer(mp);
//
//        //Click the Help button
//        onView(withId(R.id.imgHelp)).perform(click());
//        //Check its playing
//        assertThat(mp.isPlaying(), is(true));
//
//        //Click the button again
//        onView(withId(R.id.imgHelp)).perform(click());
//        //Check it has stopped
//        assertThat(mp.isPlaying(), is(false));
//    }
//
//    @Test
//    public void clickHelpOnBreastfeedPagePlaysAudio(){
//        //Set up A household member
//        HouseholdMember hm = new HouseholdMember(1, "1", "HM1", "", 1, false, true);
//        hm.setParticipantHouseholdMemberId(ppid);
//        mDb.getHouseholdMemberDao().insert(hm);
//
//        mBreastfeedActivityTestRule.launchActivity(new Intent());
//
//        //Inject the Media Player into the fragment
//        MediaPlayer mp = MediaPlayer.create(mContext, R.raw.inst_audio_v1);
//        BreastfeedActivity activity = (BreastfeedActivity) mBreastfeedActivityTestRule.getActivity();
//        HelpAudioButtonController helpAudioButtonController = new HelpAudioButtonController(mContext, activity.findViewById(R.id.imgHelp), R.raw.inst_audio_v1);
//        activity.setHelpAudioButtonController(helpAudioButtonController);
//
//        //Click the Help button
//        onView(withId(R.id.imgHelp)).perform(click());
//        //Check its playing
//        assertThat(helpAudioButtonController.isPlaying(), is(true));
//
//        //Click the button again
//        onView(withId(R.id.imgHelp)).perform(click());
//        //Check it has stopped
//        assertThat(helpAudioButtonController.isPlaying(), is(false));
//    }
//
//    @Test
//    public void clickHelpOnRecipeFinalImagePopupPlaysAudio(){
//        Recipe recipe = new Recipe();
//        recipe.setRecipeId((long) 1);
//        recipe.setRecipeNameText("TITLE");
//        mDb.getRecipeDao().insert(recipe);
//
//        //Launch the activity
//        Intent i = new Intent();
//        i.putExtra(RECIPEID, recipe.getRecipeId());
//        mCreateRecipeActivityTestRule.launchActivity(i);
//
//        //Click the submit button to bring up the popup
//        onView(withId(R.id.btnFinish)).perform(click());
//
//        //Get the fragment
//        CreateRecipeActivity.FinalImageDialogFragment frag = (CreateRecipeActivity.FinalImageDialogFragment) mCreateRecipeActivityTestRule.getActivity().getSupportFragmentManager().findFragmentByTag(RECIOE_FINALE_IMAGE_DIALOG);
//        HelpAudioButtonController helpAudioButtonController = new HelpAudioButtonController(mContext, frag.getDialog().findViewById(R.id.imgHelp), R.raw.inst_audio_v1);
//        frag.setHelpAudioButtonController(helpAudioButtonController);
//
//        //Click the Help button
//        onView(withId(R.id.imgHelp)).perform(click());
//        //Check its playing
//        //assertThat(mp.isPlaying(), is(true));
//        assertThat(helpAudioButtonController.isPlaying(), is(true));
//
//        //Click the button again
//        onView(withId(R.id.imgHelp)).perform(click());
//        //Check it has stopped
//        //assertThat(mp.isPlaying(), is(false));
//        assertThat(helpAudioButtonController.isPlaying(), is(false));
//
//    }
//
//    @Test
//    public void clickHelpOnRecordReviewAudioPlaysAudio(){
//        //Set up A household member
//        HouseholdMember hm = new HouseholdMember(1, "1", "HM1", "", 1, false, true);
//        hm.setParticipantHouseholdMemberId(ppid);
//        mDb.getHouseholdMemberDao().insert(hm);
//
//        //Set up a FoodRecord
//        FoodRecord fr = new FoodRecord(hm.getUid());
//        fr.setFoodRecordId((long) 1);
//        mDb.getFoodRecordDao().insert(fr);
//
//        Intent i = new Intent();
//        i.putExtra(FRID, fr.getFoodRecordId());
//        i.putExtra(PPID, ppid);
//        mRecordReviewActivityTestRule.launchActivity(i);
//
//       onView(withId(R.id.btnYes)).perform(click());
//
//        //Inject the Media Player into the fragment
//        MediaPlayer mp = MediaPlayer.create(mContext, R.raw.inst_audio_v1);
//        AudioRecordingFragment frag = (AudioRecordingFragment) mRecordReviewActivityTestRule.getActivity().getSupportFragmentManager().findFragmentByTag(AUDIO_ONLY_FRAGMENT_TAG);
//        HelpAudioButtonController helpAudioButtonController = new HelpAudioButtonController(mContext, frag.getView(), R.raw.inst_audio_v1);
//        frag.setHelpAudioButtonController(helpAudioButtonController);
//
//        //Click the Help button
//        onView(withId(R.id.imgHelp)).perform(click());
//
//        //Check its playing
//        assertThat(helpAudioButtonController.isPlaying(), is(true));
//
//        //Click the button again
//        onView(withId(R.id.imgHelp)).perform(click());
//        //Check it has stopped
//        assertThat(helpAudioButtonController.isPlaying(), is(false));
//    }
//
//    @Test
//    public void clickHelpOnRecordReviewPlaysAudio(){
//        //Set up A household member
//        HouseholdMember hm = new HouseholdMember(1, "1", "HM1", "", 1, false, true);
//        hm.setParticipantHouseholdMemberId(ppid);
//        mDb.getHouseholdMemberDao().insert(hm);
//
//        //Set up a FoodRecord
//        FoodRecord fr = new FoodRecord(hm.getUid());
//        fr.setFoodRecordId((long) 1);
//        mDb.getFoodRecordDao().insert(fr);
//
//        Intent i = new Intent();
//        i.putExtra(FRID, fr.getFoodRecordId());
//        i.putExtra(PPID, hm.getParticipantHouseholdMemberId());
//        mRecordReviewActivityTestRule.launchActivity(i);
//
//        //Inject the Media Player into the fragment
//        MediaPlayer mp = MediaPlayer.create(mContext, R.raw.inst_audio_v1);
//        RecordReviewActivity activity = mRecordReviewActivityTestRule.getActivity();
//        HelpAudioButtonController helpAudioButtonController = new HelpAudioButtonController(mContext, activity.findViewById(R.id.imgHelp), R.raw.inst_audio_v1);
//        activity.setHelpAudioButtonController(helpAudioButtonController);
//
//        //Click the Help button
//        onView(withId(R.id.imgHelp)).perform(click());
//
//        //Check its playing
//        assertThat(helpAudioButtonController.isPlaying(), is(true));
//
//        //Click the button again
//        onView(withId(R.id.imgHelp)).perform(click());
//        //Check it has stopped
//        assertThat(helpAudioButtonController.isPlaying(), is(false));
//    }
//}
