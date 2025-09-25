package au.edu.newcastle.jnc985.visida.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.edu.newcastle.jnc985.visida.BuildConfig;
import au.edu.newcastle.jnc985.visida.R;
import bo.EatingOccasionViewModel;
import bo.RecipeRepository;
import bo.Utilities;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.ImageAudioProvider;
import ui.ImageAudioRecyclerViewAdapter;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.ALLOW_TEXT;
import static bo.AppConstants.AUDIOFILE_NAME;
import static bo.AppConstants.AUDIOFILE_NAME_TEMPLATE;
import static bo.AppConstants.FIID_PLACEHOLDER;
import static bo.AppConstants.FR;
import static bo.AppConstants.IMAGE_NAME;
import static bo.AppConstants.NAVBAR;
import static bo.AppConstants.TEXT_DESCRIPTION;

public class EatingOccasionActivity extends AppCompatActivity implements View.OnClickListener, SelectRecipeDialog.OnSelectRecipeListener {
    private static final String TAG = "eatingoccasoinactivity";

    private EatingOccasionViewModel mEatingOccasionViewModel;
    private ImageAudioRecyclerViewAdapter mAdapter;
    private NavigationBarFragment mNavBar;
    private FoodRecord mFoodRecord;

    private FoodItem mFoodItem;
    private String mImageName;
    private String mAudioName;

    //Testing Method only
    public void setEatingOccasionViewModel(EatingOccasionViewModel eoVm){
        this.mEatingOccasionViewModel = eoVm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Eating Occasion Activity Created");
        super.onCreate(savedInstanceState);
        // Run this at the start of every activity
        Utilities.updateLanguage(getBaseContext());

        if(BuildConfig.forceKhmer || BuildConfig.forceSwahili) {
            setContentView(R.layout.activity_eating_occasion_audio);
        }
        else{
            setContentView(R.layout.activity_eating_occasion);
        }

        //Set the title
        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.title_eating_occasion);

        FragmentManager fm = getSupportFragmentManager();
        mNavBar = (NavigationBarFragment) fm.findFragmentByTag(NAVBAR);

        if(mNavBar == null) {
            //Load the fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, NavigationBarFragment.newInstance(), NAVBAR)
                    .commit();
        }

        //Create View Model
        if(mEatingOccasionViewModel == null){
            mEatingOccasionViewModel = new ViewModelProvider(this).get(EatingOccasionViewModel.class);
        }

        //Check the shared preferences to see if we are in EAT or Finalize
        if (null == savedInstanceState) {
            //Get the householdmember sent from the list
            Intent i = getIntent();
            Bundle extras = i.getExtras();
            if(extras != null) {
                mFoodRecord = (FoodRecord) extras.getParcelable(FR);
            }
        }
        else{
            this.mFoodRecord = savedInstanceState.getParcelable(FR);
            this.mAudioName = savedInstanceState.getString(AUDIOFILE_NAME);
            this.mImageName = savedInstanceState.getString(IMAGE_NAME);
        }

        Button btnFinish = findViewById(R.id.btnFinish);
        btnFinish.setOnClickListener(this);
        Button btnAddIngredient = findViewById(R.id.btnAddFoodItem);
        btnAddIngredient.setOnClickListener(this);
        Button btnLinkRecipe = findViewById((R.id.btnLinkRecipe));
        btnLinkRecipe.setOnClickListener(this);
        TextView txtHmName = findViewById(R.id.txtEoHmName);
        txtHmName.setText(mFoodRecord.getHouseholdMember().getName());
        setUpFoodItemList();
        loadAvatar(findViewById(R.id.imgHmAvatar));

    }

    private void loadAvatar(ImageView img) {
        //Get the household members image
        //Get Household member from food item
        if(mEatingOccasionViewModel != null) {
            File avatarImage = mEatingOccasionViewModel.getHouseholdMemberImage();
            Glide.with(this)
                    .load(avatarImage)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_default_person))
                    .into(img);
        }
    }

    private void setUpFoodItemList() {
        //Create Adapter/ Send this as context and Click listener implementation
        mAdapter = new ImageAudioRecyclerViewAdapter(this, new ImageAudioRecyclerViewAdapter.RecyclerViewItemClickListener() {
            @Override
            public void recyclerViewItemClicked(ImageAudioProvider item) {
                //Play the audio maybe here but really we dont want to do anything.
            }

            @Override
            public boolean recyclerViewItemLongClicked(final ImageAudioProvider item) {
                //Ask the user to confirm the delete
                new AlertDialog.Builder(EatingOccasionActivity.this)
                        .setTitle(R.string.delete_item)
                        .setMessage(R.string.delete_confirmation)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                Log.i(ACTIVITY_LOG_TAG, TAG + ": Deleted food Item " + item.getImageName());
                                Toast.makeText(EatingOccasionActivity.this, R.string.food_item_deleted, Toast.LENGTH_SHORT).show();
                                //Delete the HM
                                FoodItem fi = (FoodItem) item;
                                EatingOccasionActivity.this.mEatingOccasionViewModel.removefoodItem(fi);

                            }})
                        .setNegativeButton(R.string.no, null).show();
                return true;
            }
        });
        mEatingOccasionViewModel.setEatingOccasion(mFoodRecord.getCurrentEatingOccasion());

        //Subscribe to the view Model
        mEatingOccasionViewModel.getObservableFoodItems().observe(this, new Observer<List<FoodItem>>() {
            @Override
            public void onChanged(@Nullable List<FoodItem> ingredientCaptures) {
                mAdapter.setIngredientsList(ingredientCaptures);
            }
        });

        //Get Recycler View
        RecyclerView list = (RecyclerView) findViewById(R.id.rvFoodItemList);
        list.setAdapter(mAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(llm);
    }


    private static final int EATINGOCCASION_FOODITEM_IMAGE_REQUEST_CODE = 987;
    private static final int EATINGOCCASION_FOOD_ITEM_AUDIO_REQUEST_CODE = 989;
    @Override
    public void onClick(View v) {
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked " + getResources().getResourceEntryName(v.getId()));
        switch(v.getId()){
            case R.id.btnAddFoodItem:
                addFoodItem();
                break;
            case R.id.btnFinish:
                Toast.makeText(this, R.string.eatingoccasionrecorded, Toast.LENGTH_SHORT).show();
                Intent selectHmIntent = new Intent(this, MainActivity.class);
                selectHmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(selectHmIntent);
                break;
            case R.id.btnLinkRecipe:
                linkRecipes();
        }
    }

    /**
     * Show A dialog which lists the recipes saved on the device.
     */
    private void linkRecipes() {
        if(new RecipeRepository(getApplication()).recipeCount() <= 0){
            Toast.makeText(this, au.edu.newcastle.jnc985.visida.R.string.no_recipes, Toast.LENGTH_SHORT).show();
            return;
        }

        FragmentManager fm = getSupportFragmentManager();
        List<Long> recipeIds = mEatingOccasionViewModel.getEatingOccasion().getRecipeIds();
        long[] ids = recipeIds.stream().mapToLong(l -> l).toArray();
        SelectRecipeDialog editNameDialogFragment = SelectRecipeDialog.newInstance(ids);
        editNameDialogFragment.show(fm, "fragment_select_recipes");
    }

    private void addFoodItem() {
        Intent intent;
        intent = new Intent(this, CameraActivity.class);
        startActivityForResult(intent, EATINGOCCASION_FOODITEM_IMAGE_REQUEST_CODE);
    }


    private void updateFoodItemUrls(FoodItem fi) {
        //Update the image name after getting the fooditem id
        String newImageName = fi.getImageUrl();
        String newAudioName = fi.getAudioUrls();

        //Update the image
        if(newImageName != null) {
            Utilities.renameMediaFile(this, mImageName, newImageName);
        }
        if(newAudioName != null) {
            Utilities.renameMediaFile(this, mAudioName, newAudioName);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(EATINGOCCASION_FOODITEM_IMAGE_REQUEST_CODE == requestCode){
            if (RESULT_OK == resultCode) {
                //Get the image name and request the audio
                Bundle extras = data.getExtras();
                String imageName = extras.getString(IMAGE_NAME);
                String hhId = mFoodRecord.getHouseholdMember().getParticipantHouseholdId();
                String ppId = mFoodRecord.getHouseholdMember().getParticipantHouseholdMemberId();
                long frId = mFoodRecord.getFoodRecordId();
                long eoId = mEatingOccasionViewModel.getEatingOccasion().getEatingOccasionId();
                SimpleDateFormat simpleDateFormat = Utilities.DATE_FORMAT;
                String timestamp = simpleDateFormat.format(new Date());
                mImageName = String.format(Locale.ENGLISH, imageName, hhId, ppId, frId, eoId, FIID_PLACEHOLDER, timestamp);
                Utilities.renameMediaFile(this, imageName, mImageName);
                mFoodItem = new FoodItem();

                mFoodItem.setEatingOccasionId(eoId);
                mFoodItem.setImageUrl(mImageName);
                recordFoodItemAudio();
            }
        } else if (EATINGOCCASION_FOOD_ITEM_AUDIO_REQUEST_CODE == requestCode) {
            if (RESULT_OK == resultCode) {
                Bundle extras = data.getExtras();
                String audioFileName = extras.getString(AUDIOFILE_NAME);
                String ppId = "";
                if(audioFileName != null && !audioFileName.isEmpty()) {
                    //Format the file
                    SimpleDateFormat simpleDateFormat = Utilities.DATE_FORMAT;
                    String timestamp = simpleDateFormat.format(new Date());

                    String hhId = mFoodRecord.getHouseholdMember().getParticipantHouseholdId();
                    ppId = mFoodRecord.getHouseholdMember().getParticipantHouseholdMemberId();
                    long frId = mFoodRecord.getFoodRecordId();
                    long eoId = mEatingOccasionViewModel.getEatingOccasion().getEatingOccasionId();

                    mAudioName = String.format(Locale.ENGLISH, AUDIOFILE_NAME_TEMPLATE, hhId, ppId, frId, eoId, FIID_PLACEHOLDER, timestamp);

                    Utilities.renameMediaFile(this, audioFileName, mAudioName);
                    mFoodItem.setAudioUrls(mAudioName);
                }
                else{
                    //Add Text to the FoodItem
                    String textDescription = extras.getString(TEXT_DESCRIPTION);
                    mFoodItem.setDescription(textDescription);
                }
                mEatingOccasionViewModel.addFoodItem(ppId, mFoodItem);
                updateFoodItemUrls(mFoodItem);
                Log.i(ACTIVITY_LOG_TAG, TAG + ": Image Saved " + mFoodItem.getImageName());
            }
            else{
                //Most likely hit back during the audio recording.
                //Delete the image and audio files that may have been created.
                Utilities.deleteMediaFile(this, mImageName);
                Utilities.deleteMediaFile(this, mAudioName);
            }
        }
    }

    private void recordFoodItemAudio() {
        SimpleDateFormat simpleDateFormat = Utilities.DATE_FORMAT;
        String timestamp = simpleDateFormat.format(new Date());

        String hhId = mFoodRecord.getHouseholdMember().getParticipantHouseholdId();
        String ppId = mFoodRecord.getHouseholdMember().getParticipantHouseholdMemberId();
        long frId = mFoodRecord.getFoodRecordId();
        long eoId = mEatingOccasionViewModel.getEatingOccasion().getEatingOccasionId();

        mAudioName = String.format(Locale.ENGLISH, AUDIOFILE_NAME_TEMPLATE, hhId, ppId, frId, eoId, FIID_PLACEHOLDER, timestamp);

        //Call the audio activity to record the audio.
        Intent i = new Intent(this, AudioActivity.class);
        i.putExtra(IMAGE_NAME, mFoodItem.getImageUrl());
        i.putExtra(AUDIOFILE_NAME, mAudioName);
        //Only allow text in English Version
        boolean allowText = !(BuildConfig.forceKhmer || BuildConfig.forceSwahili);
        i.putExtra(ALLOW_TEXT, allowText);
        startActivityForResult(i, EATINGOCCASION_FOOD_ITEM_AUDIO_REQUEST_CODE);
    }

    @Override
    protected void onSaveInstanceState(Bundle state){
        super.onSaveInstanceState(state);
        state.putString(AUDIOFILE_NAME, mAudioName);
        state.putString(IMAGE_NAME, mImageName);
        state.putParcelable(FR, mFoodRecord);
    }

    @Override
    public void onDialogAccept(List<Long> recipeIds) {
        //Get the list of recipe Id's and store them in the db
        mEatingOccasionViewModel.addRecipes(recipeIds);
    }
}
