package au.edu.newcastle.jnc985.visida.activity;

import androidx.lifecycle.ViewModelProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.edu.newcastle.jnc985.visida.BuildConfig;
import au.edu.newcastle.jnc985.visida.R;
import bo.AppConstants;
import bo.MealViewModel;
import bo.MealViewModelFactory;
import bo.RecipeRepository;
import bo.Utilities;
import bo.db.entity.FoodItem;
import bo.db.entity.ImageAudioProvider;
import ui.ImageAudioRecyclerViewAdapter;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.ALLOW_TEXT;
import static bo.AppConstants.AUDIOFILE_NAME;
import static bo.AppConstants.IMAGE_NAME;
import static bo.AppConstants.PARTICIPANTHOUSEHOLDID;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.SHARED_DISH_AUDIOFILE_TEMPLATE;
import static bo.AppConstants.SHARED_DISH_IMAGE_NAME_TEMPLATE;
import static bo.AppConstants.TEXT_DESCRIPTION;

public class MealActivity extends AppCompatActivity implements View.OnClickListener, SelectRecipeDialog.OnSelectRecipeListener {

    private static final String TAG = "Mealactivity";
    private static final int SHARED_DISH_IMAGE_REQUEST_CODE = 111;
    private static final int SHARED_DISH_AUDIO_REQUEST_CODE = 112;
    private NavigationBarFragment mNavBar;

    private MealViewModel mMealViewModel;

    private ImageAudioRecyclerViewAdapter mAdapter;
    private String mImageName;
    private String mAudioName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Meal Activity Created");
        // Run this at the start of every activity
        Utilities.updateLanguage(getBaseContext());
        if(BuildConfig.forceKhmer){
            setContentView(R.layout.activity_meal_audio);
        }
        else {
            setContentView(R.layout.activity_meal);
        }

        //Set up title bar
        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.meal);
        ImageView imgIcon = findViewById(R.id.imgPicture);
        imgIcon.setImageResource(R.drawable.ic_btn_shared_dish_dark_grey);


        FragmentManager fm = getSupportFragmentManager();
        mNavBar = (NavigationBarFragment) fm.findFragmentByTag(AppConstants.NAVBAR);

        if(mNavBar == null) {
            //Load the fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, NavigationBarFragment.newInstance(), AppConstants.NAVBAR)
                    .commit();
        }

        if(mMealViewModel == null){
            long mealId = 0;
            mMealViewModel = new ViewModelProvider(this, new MealViewModelFactory(this.getApplication(), mealId)).get(MealViewModel.class);
        }

        Button btnAddDish = findViewById(R.id.btnAddDish);
        Button btnSubmit = findViewById(R.id.btnFinish);
        Button btnLinkRecipes = findViewById(R.id.btnLinkRecipe);
        btnAddDish.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        btnLinkRecipes.setOnClickListener(this);

        setUpDishesList();
    }

    private void setUpDishesList() {
        mAdapter = new ImageAudioRecyclerViewAdapter(this, new ImageAudioRecyclerViewAdapter.RecyclerViewItemClickListener() {
            @Override
            public void recyclerViewItemClicked(ImageAudioProvider item) {
                //Possibly play the audio file here
            }
            @Override
            public boolean recyclerViewItemLongClicked(ImageAudioProvider item) {
                return deleteDish(item);
            }
        });

        //Set the meal once we have saving and return ing to a meal
        //mMealViewModel.setMeal(mealId);

        //Observe the list of food items for this meal
        mMealViewModel.getObservableDishes().observe(this, (dishes) -> {
            mAdapter.setIngredientsList(dishes);
        });

        //Set up the recycler view
        RecyclerView list = findViewById(R.id.rvDishList);
        list.setAdapter(mAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        list.setLayoutManager(llm);

    }

    private boolean deleteDish(ImageAudioProvider item) {
        //Ask the user to confirm the delete
        new AlertDialog.Builder(MealActivity.this)
                .setTitle(R.string.delete_item)
                .setMessage(R.string.delete_confirmation)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.yes, (dialog, whichButton) -> {
                    Log.i(ACTIVITY_LOG_TAG, TAG + ": Deleted food Item from meal " + item.getImageName());
                    Toast.makeText(MealActivity.this, R.string.food_item_deleted, Toast.LENGTH_SHORT).show();
                    //Delete the HM
                    FoodItem fi = (FoodItem) item;
                    MealActivity.this.mMealViewModel.removefoodItem(fi);
                })
                .setNegativeButton(R.string.no, null).show();
        return true;
    }


    @Override
    public void onClick(View v){
        int id = v.getId();
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked " + getResources().getResourceEntryName(id));
        switch(id){
            case R.id.btnAddDish:
                //Go to camera in Meal state
                Intent imageIntent;
                imageIntent = new Intent(this, CameraActivity.class);
                //Add the food record to the Intent to pass to the next activity
                startActivityForResult(imageIntent, SHARED_DISH_IMAGE_REQUEST_CODE);
                break;
            case R.id.btnFinish:
                //Return to the main screen
                if(mMealViewModel.foodItemCount() <= 0){
                    mMealViewModel.deleteMeal();
                }
                Toast.makeText(this, R.string.mealrecorded, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
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
        List<Long> recipeIds = mMealViewModel.getLinkedRecipes();
        long[] ids = recipeIds.stream().mapToLong(l -> l).toArray();
        SelectRecipeDialog editNameDialogFragment = SelectRecipeDialog.newInstance(ids);
        editNameDialogFragment.show(fm, "fragment_select_recipes");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SHARED_DISH_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //Get the image name
                Bundle extras = data.getExtras();
                String unformatedImageName = extras.getString(IMAGE_NAME);

                //Format the image name and rename the image file
                SharedPreferences sharedPreferences = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                String hhid = sharedPreferences.getString(PARTICIPANTHOUSEHOLDID, "");
                long mealId = mMealViewModel.getMeal().getMealId();
                String timestamp = Utilities.DATE_FORMAT.format(new Date());
                mImageName = String.format(Locale.ENGLISH, SHARED_DISH_IMAGE_NAME_TEMPLATE, hhid, mealId, timestamp);
                Utilities.renameMediaFile(this, unformatedImageName, mImageName);

                //Get the file name for the audio file
                mAudioName = String.format(Locale.ENGLISH, SHARED_DISH_AUDIOFILE_TEMPLATE, hhid, mealId, timestamp);

                //Go Record the Audio for the Image
                Intent audioIntent = new Intent(this, AudioActivity.class);
                audioIntent.putExtra(IMAGE_NAME, mImageName);
                audioIntent.putExtra(AUDIOFILE_NAME, mAudioName);
                boolean allowText = !BuildConfig.forceKhmer;
                audioIntent.putExtra(ALLOW_TEXT, allowText);
                startActivityForResult(audioIntent, SHARED_DISH_AUDIO_REQUEST_CODE);
            }
            else{
                //TODO When you get around to refactoring the image you will need to delete it here
            }
        } else if (requestCode == SHARED_DISH_AUDIO_REQUEST_CODE) {
            //Now we have the audio and the image save the food item.
            if(RESULT_OK == resultCode) {
                Bundle extras = data.getExtras();
                mAudioName = extras.getString(AUDIOFILE_NAME);

                if(mAudioName != null && !mAudioName.isEmpty()) {
                    mMealViewModel.addDish(mImageName, mAudioName);
                }
                else{
                    String description = extras.getString(TEXT_DESCRIPTION);
                    mMealViewModel.addDishTextOnly(mImageName, description);
                }
            }
            else{
                //Delete the image and audio file
                Utilities.deleteMediaFile(this, mImageName);
                Utilities.deleteMediaFile(this, mAudioName);
                mImageName = null;
                mAudioName = null;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outstate) {
        super.onSaveInstanceState(outstate);
        outstate.putString(IMAGE_NAME, mImageName);
    }

    @Override
    public void onDialogAccept(List<Long> recipeIds) {
        mMealViewModel.linkRecipes(recipeIds);
    }

}
