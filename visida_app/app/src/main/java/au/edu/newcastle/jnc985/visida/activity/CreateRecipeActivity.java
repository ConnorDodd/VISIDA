package au.edu.newcastle.jnc985.visida.activity;

import android.app.Dialog;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.edu.newcastle.jnc985.visida.BuildConfig;
import au.edu.newcastle.jnc985.visida.R;
import bo.RecipeViewModel;
import bo.State;
import bo.Utilities;
import bo.db.entity.ImageAudioProvider;
import bo.db.entity.IngredientCapture;
import notification.AlarmController;
import ui.HelpAudioButtonController;
import ui.ImageAudioRecyclerViewAdapter;
import ui.MediaPlayerManager;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.ALLOW_TEXT;
import static bo.AppConstants.AUDIOFILE_NAME;
import static bo.AppConstants.FOODITEM_DESCRIPTION_MAX_LENGTH;
import static bo.AppConstants.IMAGE_NAME;
import static bo.AppConstants.INGREDIENT_AUDIOFILE_TEMPLATE;
import static bo.AppConstants.NAVBAR;
import static bo.AppConstants.PARTICIPANTHOUSEHOLDID;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.RECIPE_FINALE_IMAGE_DIALOG;
import static bo.AppConstants.RECIPEID;
import static bo.AppConstants.RECIPE_NAME_AUDIOFILE_TEMPLATE;
import static bo.AppConstants.TEXT_DESCRIPTION;

public class CreateRecipeActivity extends AppCompatActivity implements AudioRecordingFragment.AudioRecorderHandler, View.OnClickListener {
    private static final String TAG = "CreaterecipeActivity";
    private static final int INGREDIENT_IMAGE_REQUEST_CODE = 456;
    private static final int AUDIO_REQUEST_CODE = 457;
    private static final int RECIPE_IMAGE_REQUEST_CODE = 458;

    private NavigationBarFragment mNavBar;
    private static final String TITLE_AUDIO_FRAGMENT = "titleaudiofragment";
    private static final String INGREDIENT_AUDIO_FRAGMENT = "titleaudiofragment";

    private ImageAudioRecyclerViewAdapter mIngredientAdapter;
    private RecipeViewModel mRecipeViewModel;

    private ImageView mImgViewPlayAudio;
    private IngredientCapture mIc;

    private boolean mIsAudioRecorded;
    private String mAudioFlieName;


    /*
    Testing method only.
     */
    public void setAudioRecorded(boolean recorded) {
        this.mIsAudioRecorded = recorded;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Create Recipe Activity Created");
        // Run this at the start of every activity
        Utilities.updateLanguage(getBaseContext());
        setTitle(R.string.title_create_recipe);

        if(BuildConfig.forceKhmer || BuildConfig.forceSwahili){
            setContentView(R.layout.activity_create_recipe_audio);
        }
        else {
            setContentView(R.layout.activity_create_recipe);
        }

        FragmentManager fm = getSupportFragmentManager();
        mNavBar = (NavigationBarFragment) fm.findFragmentByTag(NAVBAR);

        //Set up title bar
        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.title_create_recipe);
        ImageView imgIcon = findViewById(R.id.imgPicture);
        imgIcon.setImageResource(R.drawable.ic_btn_cook);

        if(mNavBar == null) {
            //Load the fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, NavigationBarFragment.newInstance(), NAVBAR)
                    .commit();
        }

        //Set the state to cook incase we end up here via notification not MainActivity.
        Utilities.setState(getApplicationContext(), State.COOK);
        // Set up UI
        Button btnFinish = findViewById(R.id.btnFinish);
        btnFinish.setOnClickListener(this);
        Button btnAddIngredient = findViewById(R.id.btnAddIngredient);
        btnAddIngredient.setOnClickListener(this);
        ImageView imgViewRecord = findViewById(R.id.imgRecordAudioBtn);
        imgViewRecord.setOnClickListener(this);
        mImgViewPlayAudio = findViewById(R.id.imgAudioFile);
        mImgViewPlayAudio.setOnClickListener(this);


        //Create View Model
        if(mRecipeViewModel == null){
            mRecipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
        }

        long recipeId;
        if(savedInstanceState != null){
            //Restore state
            recipeId = savedInstanceState.getLong(RECIPEID);
        }
        else{
            Bundle extras = getIntent().getExtras();
            if(extras != null){
                recipeId = extras.getLong(RECIPEID);
            }
            else{
                //Arrived by clicking the FAB. Send -1 to signify we want a new Recipe
                recipeId = -1;
            }
        }

        //Set the Recipe
        mRecipeViewModel.setRecipe(recipeId);
        String recipeNameFileName = mRecipeViewModel.getRecipeNameAudioFileName();
        if(recipeNameFileName != null) {
            File recipeName = new File(Utilities.getMediaDirectory(this), recipeNameFileName);
            if (recipeName.exists()) {
                setAudioFileIconVisible(true);
            }
        }
        else{
            //If there was no audio file check to see if there was a txt name.
            String recipeName = mRecipeViewModel.getRecipeNameText();
            if(recipeName != null && !recipeName.isEmpty()){
                setAudioFileIconVisible(true);
            }
        }

        setUpIngredientList();
    }

    public void setUpIngredientList(){
        //Create Adapter/ Send this as context and Click listener implementation
        mIngredientAdapter = new ImageAudioRecyclerViewAdapter(this, new ImageAudioRecyclerViewAdapter.RecyclerViewItemClickListener() {
            @Override
            public void recyclerViewItemClicked(ImageAudioProvider item) {
                //Do nothing yet
            }

            @Override
            public boolean recyclerViewItemLongClicked(final ImageAudioProvider item) {
                //Ask the user to confirm the delete
                new AlertDialog.Builder(CreateRecipeActivity.this)
                        .setTitle(R.string.delete_item)
                        .setMessage(R.string.delete_confirmation)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                Log.i(ACTIVITY_LOG_TAG, TAG + ": Deleted Ingredient");
                                Toast.makeText(CreateRecipeActivity.this, R.string.ingredient_deleted, Toast.LENGTH_SHORT).show();
                                //Delete the HM
                                IngredientCapture ingredient = (IngredientCapture) item;
                                CreateRecipeActivity.this.mRecipeViewModel.deleteIngredient(ingredient);

                            }})
                        .setNegativeButton(R.string.no, null).show();
                return true;
            }
        });

        // Subscribe to the View Model
        // A callback is used here in case this is a new recipe and the list is not yet ready to be returned by the View Model
        mRecipeViewModel.getObservableIngredients((LiveData<List<IngredientCapture>> ingredients) -> {
            ingredients.observe(this, new Observer<List<IngredientCapture>>() {
                @Override
                public void onChanged(@Nullable List<IngredientCapture> ingredientCaptures) {
                    mIngredientAdapter.setIngredientsList(ingredientCaptures);
                }
            });
        });

        //Get Recycler View
        RecyclerView list = (RecyclerView) findViewById(R.id.rvIngredientList);
        list.setAdapter(mIngredientAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(llm);
    }

    private void setAudioFileIconVisible(boolean visible){
        if(visible) {
            mImgViewPlayAudio.setVisibility(View.VISIBLE);
        }
        else{
            mImgViewPlayAudio.setVisibility(View.INVISIBLE);
        }
        mIsAudioRecorded = visible;
    }

    @Override
    public void onClick(View v) {
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked " + getResources().getResourceEntryName(v.getId()));
        switch(v.getId()){
            case R.id.imgRecordAudioBtn:
                recordRecipeName(TITLE_AUDIO_FRAGMENT);
                break;
            case R.id.btnAddIngredient:
                addIngredient();
                break;
            case R.id.btnFinish:
                submitRecipe();
                break;
            case R.id.imgAudioFile:
                playRecipeNameAudio();
                break;
        }
    }

    /**
     * Sets the saved bit in the database and returns to the
     * Recipe List Activity.
     */
    private void submitRecipe() {
        if(!mIsAudioRecorded){
            LayoutInflater factory = LayoutInflater.from(this);
            final View view = factory.inflate((R.layout.popup_recipe_name_required), null);
            if(BuildConfig.forceSwahili) {
                //Need to get the audio file recorded then set it here.
                setHelpButton(view, R.raw.aa_rec_78);
            }
            Button btnConfirm = view.findViewById(R.id.btnConfirm);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(view);

            AlertDialog dialog = builder.create();

            btnConfirm.setOnClickListener(v -> {
                Log.i(ACTIVITY_LOG_TAG, TAG + ": Didn't record recipe name. Prompting to do so.");
                dialog.dismiss();
                recordRecipeName(TITLE_AUDIO_FRAGMENT);
            });
            dialog.show();
            return;
        }

        //If they have not captured the final image aske the user if they would like to
        //Skip if already taken image.
        if(mRecipeViewModel.getRecipeImage() == null || mRecipeViewModel.getRecipeImage().isEmpty()) {
            promptForFinalImage();
        }
        else{
            saveAndReturn();
        }
    }

    private MediaPlayer mHelpMediaPlayer;
    private void setHelpButton(View view, int resId){
        //If Khmer version add the help buttons
        if(BuildConfig.forceKhmer) {
            //TODO Add actual Audio Clip for instructions
            ImageView imgHelp = view.findViewById(R.id.imgHelp);
            imgHelp.setVisibility(View.VISIBLE);
            mHelpMediaPlayer = MediaPlayer.create(this, resId);

            //Set the Help button to play the audio file
            imgHelp.setOnClickListener(v -> {
                if (mHelpMediaPlayer.isPlaying()) {
                    //Stop playing. Inorder to continue playing on subsequent presses
                    //We need to prepare the player. See State Diagram (https://developer.android.com/reference/android/media/MediaPlayer)
                    try {
                        Log.i(ACTIVITY_LOG_TAG, TAG + ": Help audio button Stopped");
                        mHelpMediaPlayer.stop();
                        mHelpMediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.i(ACTIVITY_LOG_TAG, TAG + ": Help audio button Started");
                    mHelpMediaPlayer.start();
                }
            });
        }
    }

    private void promptForFinalImage(){
        //prompt the user that they need to take the leftover image.
        FinalImageDialogFragment.newInstance().show(this.getSupportFragmentManager(), RECIPE_FINALE_IMAGE_DIALOG);
    }


    private void playRecipeNameAudio() {
        //MediaPlayer mediaPlayer = new MediaPlayer();
        MediaPlayer mediaPlayer = MediaPlayerManager.getInstance();
        //If its not already playing something then start playing.
        if(!mediaPlayer.isPlaying()) {
            try {
                File mediaDirectory = Utilities.getMediaDirectory(this);
                String audioFileName = mRecipeViewModel.getRecipeNameAudioFileName();
                if(audioFileName != null) {
                    File dataSource = new File(mediaDirectory, audioFileName);
                    mediaPlayer.setDataSource(dataSource.getAbsolutePath());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }
                else{
                    String recipeName = mRecipeViewModel.getRecipeNameText();
                    if(recipeName != null && !recipeName.isEmpty()){
                        Toast.makeText(this, getString(R.string.recipe_name) + ": " + recipeName, Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
            }
        }
    }

    private void addIngredient() {
        //Create Ingredient
        mIc = new IngredientCapture();
        //Add the recipe id to the ingredient
        mIc.setRecipeId(mRecipeViewModel.getRecipeId());
        mRecipeViewModel.addIngredient(mIc);

        //Get image
        Intent imageIntent = new Intent(this, CameraActivity.class);
        startActivityForResult(imageIntent, INGREDIENT_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == INGREDIENT_IMAGE_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                String imageName = extras.getString(IMAGE_NAME);
                SharedPreferences sharedPreferences = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                String participantHouseholdId = sharedPreferences.getString(PARTICIPANTHOUSEHOLDID, "");
                long recipeId = mRecipeViewModel.getRecipeId();
                long ingredientId = mIc.getIngredientId();
                String timestamp = Utilities.DATE_FORMAT.format(new Date());
                //Fill out the image details
                String fileName = String.format(Locale.ENGLISH, imageName, participantHouseholdId, recipeId, ingredientId, timestamp);

                //Rename the image file
                Utilities.renameMediaFile(this, imageName, fileName);

                mIc.setImageUrl(fileName);
                mRecipeViewModel.updateIngredient(mIc);
                recordIngredientDescription();
            }
            else{
                mRecipeViewModel.deleteIngredient(mIc);
            }
        }
        else if(requestCode == AUDIO_REQUEST_CODE){
            //Check if we have an audio file or Text
            //If audio file or Text was good update ingredient. Else delete it.
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                String audioFileName = extras.getString(AUDIOFILE_NAME);
                if (audioFileName != null && !audioFileName.isEmpty()) {
                    //Set the audio file
                    mIc.setAudioUrl(mAudioFlieName);
                } else {
                    //Text description
                    String textDescription = extras.getString(TEXT_DESCRIPTION);
                    mIc.setDescription(textDescription);
                }
                mRecipeViewModel.updateIngredient(mIc);
                Toast.makeText(this, R.string.ingredient_added, Toast.LENGTH_SHORT).show();
                Log.i(ACTIVITY_LOG_TAG, TAG + ": Ingredient Added");
            }
            else {
                mRecipeViewModel.deleteIngredient(mIc);
            }
        }
        else if(requestCode == RECIPE_IMAGE_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                Bundle extras = data.getExtras();
                String imageName = extras.getString(IMAGE_NAME);
                SharedPreferences sharedPreferences = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                String participantHouseholdId = sharedPreferences.getString(PARTICIPANTHOUSEHOLDID, "");
                long recipeId = mRecipeViewModel.getRecipeId();
                long imageId = 0;
                String timestamp = Utilities.DATE_FORMAT.format(new Date());
                //Fill out the image details
                String fileName = String.format(Locale.ENGLISH, imageName, participantHouseholdId, recipeId, imageId, timestamp);

                //Rename the image file
                Utilities.renameMediaFile(this, imageName, fileName);

                //Add the image to the Recipe in the database
                mRecipeViewModel.addRecipeFinalImage(fileName);

                //Cancel final image reminder
                cancelReminder();

                //Notify the user has completed the recipe.
                saveAndReturn();
            }
        }
    }

    private void saveAndReturn() {
        Toast.makeText(this,R.string.recipe_saved , Toast.LENGTH_SHORT).show();
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Recipe Saved");
        Intent intent = new Intent(this, ListRecipesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void cancelReminder() {
        AlarmController ac = new AlarmController(getApplication());
        ac.cancelRecipeFinalImageReminder(mRecipeViewModel.getRecipeId());
    }

    private void recordIngredientDescription() {
        //Call the audio activity to record the audio.
        String timestamp = Utilities.DATE_FORMAT.format(new Date());
        SharedPreferences sharedPreferences = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        String hhid = sharedPreferences.getString(PARTICIPANTHOUSEHOLDID, "");
        this.mAudioFlieName = String.format(INGREDIENT_AUDIOFILE_TEMPLATE, hhid, mRecipeViewModel.getRecipeId(), mIc.getIngredientId(), timestamp);

        Intent i = new Intent(this, AudioActivity.class);
        i.putExtra(IMAGE_NAME, mIc.getImageUrl());
        i.putExtra(AUDIOFILE_NAME, mAudioFlieName);
        i.putExtra(ALLOW_TEXT, false);
        startActivityForResult(i, AUDIO_REQUEST_CODE);
    }

    private void recordRecipeName(String fragmentTag) {
        SimpleDateFormat simpleDateFormat = Utilities.DATE_FORMAT;
        SharedPreferences sharedPreferences = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        String participantHouseholdId = sharedPreferences.getString(PARTICIPANTHOUSEHOLDID, "");
        long recipeId = mRecipeViewModel.getRecipeId();
        String timestamp = simpleDateFormat.format(new Date());
        String fileName = String.format(Locale.ENGLISH, RECIPE_NAME_AUDIOFILE_TEMPLATE, participantHouseholdId, recipeId, timestamp);

        //Open up Audio recording fragment withthe new audio file
        File outputFile = new File(Utilities.getMediaDirectory(this), fileName);
        boolean allowText = !(BuildConfig.forceKhmer || BuildConfig.forceSwahili);
        AudioRecordingFragment frag = AudioRecordingFragment.newInstance(outputFile.getAbsolutePath(), true, allowText, FOODITEM_DESCRIPTION_MAX_LENGTH);
        getSupportFragmentManager()
                .beginTransaction()
                .add(frag, fragmentTag)
                .commit();
    }


    @Override
    public void onRecordComplete(File outputFile) {
        //Check if a recipe name already exists and delete it if so.
        String recipeName = mRecipeViewModel.getRecipeNameAudioFileName();
        if (recipeName != null) {
            Utilities.deleteMediaFile(this, recipeName);
        }
        //Set the name of the audio file for the name of the recipe
        mRecipeViewModel.setRecipeNameAudio(outputFile.getName());
        //If its the title audio show the file
        setAudioFileIconVisible(true);
        closeAudioFragment();
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Recipe Name recording complete");

    }

    @Override
    public void onTextComplete(String textDescription) {
        //Check if a recipe name already exists and delete it if so.
        //If they already recorded an audio file but want to have text instead.
        String recipeName = mRecipeViewModel.getRecipeNameAudioFileName();
        if (recipeName != null) {
            //REmove the audio file from the recipe
            mRecipeViewModel.setRecipeNameAudio(null);
            Utilities.deleteMediaFile(this, recipeName);
        }
        mRecipeViewModel.setRecipeNameText(textDescription);
        setAudioFileIconVisible(true);
        closeAudioFragment();
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Text Recipe name complete");
    }

    public void closeAudioFragment(){
        FragmentManager fm = getSupportFragmentManager();
        Fragment f = fm.findFragmentByTag(TITLE_AUDIO_FRAGMENT);
        //Remove the fragment
        getSupportFragmentManager()
                .beginTransaction()
                .remove(f)
                .commit();
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the state of the activity for when they return.
        outState.putLong(RECIPEID, mRecipeViewModel.getRecipeId());
    }

    public static class FinalImageDialogFragment extends DialogFragment {

        private HelpAudioButtonController helpAudioButtonController;

        public static FinalImageDialogFragment newInstance() {
            FinalImageDialogFragment dialog = new FinalImageDialogFragment();
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final CreateRecipeActivity activity = (CreateRecipeActivity) getActivity();
            AlertDialog finalImagePopup= new AlertDialog.Builder(activity).create();
            LayoutInflater inflater = LayoutInflater.from(activity);
            finalImagePopup.setTitle(R.string.take_final_image);
            //noinspection InflateParams
            final View view = inflater.inflate(R.layout.fragment_final_image_dialog, null);

            //Set the text
            String message = getResources().getString(R.string.take_final_image_message);
            finalImagePopup.setMessage(message);

            //Set the help button if required
            setHelpAudioButtonController(new HelpAudioButtonController(activity, view.findViewById(R.id.imgHelp), R.raw.ab_rec_8));

            //Custom Buttons
            Button btnCancel = view.findViewById(R.id.btnCancel);
            Button btnConfirm = view.findViewById(R.id.btnConfirm);
            btnCancel.setText(R.string.no);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(ACTIVITY_LOG_TAG, TAG + ": Did not take final image.");
                    activity.saveAndReturn();
                    finalImagePopup.dismiss();
                }
            });
            btnConfirm.setText(R.string.yes);
            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(ACTIVITY_LOG_TAG, TAG + ": Accepted to take final image.");
                    Intent imageIntent = new Intent(activity, CameraActivity.class);
                    activity.startActivityForResult(imageIntent, RECIPE_IMAGE_REQUEST_CODE);
                    finalImagePopup.dismiss();
                }
            });
            finalImagePopup.setView(view);
            finalImagePopup.setCancelable(false);
            return finalImagePopup;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            helpAudioButtonController.destroy();
        }

        public void setHelpAudioButtonController(HelpAudioButtonController helpAudioButtonController) {
            this.helpAudioButtonController = helpAudioButtonController;
        }
    }
}
