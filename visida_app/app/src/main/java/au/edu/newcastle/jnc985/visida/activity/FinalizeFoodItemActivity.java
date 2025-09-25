package au.edu.newcastle.jnc985.visida.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import bo.AppConstants;
import bo.EatingOccasionRepository;
import bo.NotificationRepository;
import bo.State;
import bo.Utilities;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import notification.AlarmController;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.EOID;
import static bo.AppConstants.FINALIZE_FRAGMENT;
import static bo.AppConstants.NAVBAR;

public class FinalizeFoodItemActivity extends AppCompatActivity {
    private static String TAG = "FinalizeFoodItemActivity";

    private EatingOccasion mEo;
    private FinalizeFoodItemFragment finalizeFoodItemFragment;
    private NavigationBarFragment mNavBar;
    private TextView txtTitle;

    @Override
    public void onRestart(){
        super.onRestart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Finalize Food Item Activity Created");
        // Run this at the start of every activity
        Utilities.updateLanguage(getBaseContext());
        setContentView(R.layout.activity_finalize_food_item);

        //Set up title bar
        txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.title_finalize_food_item);
        ImageView imgIcon = findViewById(R.id.imgPicture);
        imgIcon.setImageResource(R.drawable.ic_btn_finalize_eat);


        FragmentManager fm = getSupportFragmentManager();
        mNavBar = (NavigationBarFragment) fm.findFragmentByTag(NAVBAR);

        if(mNavBar == null) {
            //Load the fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, NavigationBarFragment.newInstance(), NAVBAR)
                    .commit();
        }

        long eoId = 0;
        //Intent will contain a food record. Food record has method getNonFinalizedEOs()
        if(savedInstanceState != null){
            eoId = savedInstanceState.getLong(EOID);
        }
        else{
            //Get the food record out of the intent
            Intent i = getIntent();
            Bundle extras = i.getExtras();
            if(extras != null){
                eoId = extras.getLong(EOID);
            }
        }

        //Get the eating occasion from the database (this will populate the food items
        EatingOccasionRepository eoRepo = new EatingOccasionRepository(this.getApplication());
        mEo = eoRepo.getEatingOccasion(eoId);

        if(!mEo.isFinalized()) {
            //Get the FoodItems from the EatingOccasion
            List<FoodItem> foodItems = getNonFinalizedFoodItems();
            if (foodItems.size() > 0) {
                //Load the fragment with first food item
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.finalizecontainer, FinalizeFoodItemFragment.newInstance(foodItems.get(0)), FINALIZE_FRAGMENT)
                        .commit();
            } else {
                cancelNotification(mEo);
                //Finalize the eating occasion and update the database
                mEo.finalise();
                //Remove the alarm for this eating occasion
                eoRepo.updateEatingOccasion(mEo);
                //Go back to the select household membmer activity
                Intent i = new Intent(this, SelectHouseholdMemberActivity.class);
                i.putExtra(AppConstants.STATE, State.FINALIZE);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        }
    }

    @Override
    public void setTitle(int resId){
        super.setTitle(resId);
        txtTitle.setText(R.string.title_finalize_food_item_shared);
    }
    private void cancelNotification(EatingOccasion mEo) {
        AlarmController alarmController = new AlarmController(getApplication());
        alarmController.cancelUnfinalizedEatingOccasionNotification(mEo.getEatingOccasionId());
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Notification cancelled id " + mEo.getFoodRecordId().intValue());
        //Mark EO notification as seen.
        NotificationRepository notificationRepository = new NotificationRepository(getApplication());
        notificationRepository.markSeen(mEo);
    }

    public List<FoodItem> getNonFinalizedFoodItems(){
        List<FoodItem> nonFinalizedFoodItems = new ArrayList<>();
        for(FoodItem fi : mEo.getFoodItems()){
            if(!fi.isFinalized()){
                nonFinalizedFoodItems.add(fi);
            }
        }
        return nonFinalizedFoodItems;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the state of the activity for when they return.
        outState.putLong(EOID, mEo.getEatingOccasionId());
    }
}
