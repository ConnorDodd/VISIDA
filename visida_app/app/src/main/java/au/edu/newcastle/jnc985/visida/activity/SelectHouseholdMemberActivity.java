package au.edu.newcastle.jnc985.visida.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutionException;

import au.edu.newcastle.jnc985.visida.R;
import bo.AppConstants;
import bo.EatingOccasionRepository;
import bo.FoodRecordViewModel;
import bo.HouseholdMembersViewModel;
import bo.State;
import bo.Utilities;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;
import ui.HouseholdMemberListAdapter;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.FR;
import static bo.AppConstants.NAVBAR;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.STATE;
import static bo.AppConstants.UNFINALIZEDEOIDS;

public class SelectHouseholdMemberActivity extends AppCompatActivity implements HouseholdMemberListAdapter.HouseholdMemberClickListener {
    private static String TAG = "SelectHouseholdMemberActivity";

    private NavigationBarFragment mNavBar;

    private HouseholdMembersViewModel mHouseholdMemberViewModel;
    private HouseholdMemberListAdapter mHouseholdMemberAdapter;
    private FoodRecordViewModel foodRecordViewModel;

    /**
     * State enum to track which use case the activity is in.
     */
    private State mCurrentState;

    /**
     * Methods for testing the viewModels
     * @param hmViewModel
     */
    public void setHouseholdMemberViewModel(HouseholdMembersViewModel hmViewModel){
        this.mHouseholdMemberViewModel = hmViewModel;
    }
    public void setFoodRecordViewModel(FoodRecordViewModel frViewModel){
        this.foodRecordViewModel = frViewModel;
    }
    public void setCurrentState(State state){
        this.mCurrentState = state;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the state of the activity for when they return.
        outState.putSerializable(AppConstants.STATE, mCurrentState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Select Household Member Activity Created");
        // Run this at the start of every activity
        Utilities.updateLanguage(getBaseContext());
        setContentView(R.layout.activity_select_household_member);

        FragmentManager fm = getSupportFragmentManager();
        mNavBar = (NavigationBarFragment) fm.findFragmentByTag(NAVBAR);

        if(mNavBar == null) {
            //Load the fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, NavigationBarFragment.newInstance(), NAVBAR)
                    .commit();
        }

        //Set the state from shared preferences
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        mCurrentState = State.values()[sharedPref.getInt(STATE, State.INVALID.ordinal())];
        if(mCurrentState == State.INVALID){
            //Something went wring return to main screen.
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
        
        setupToolbar();

        //Create food Record view model
        if(foodRecordViewModel == null) {
            foodRecordViewModel = new ViewModelProvider(this).get(FoodRecordViewModel.class);
        }

        //Create household member View Model
        if(mHouseholdMemberViewModel == null) {
            mHouseholdMemberViewModel = new ViewModelProvider(this).get(HouseholdMembersViewModel.class);
        }

        List<HouseholdMember> hms = mHouseholdMemberViewModel.getHouseholdMembers();
        if(hms.size() == 1){
            HouseholdMember onlyMember = hms.get(0);
            householdMemberClicked(onlyMember);
        }
        setUpList();

    }

    private void setupToolbar() {
        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.title_select_household_member);
        ImageView imgIcon = findViewById(R.id.imgPicture);

        switch(mCurrentState){
            case EAT:
                imgIcon.setImageResource(R.drawable.ic_btn_eat);
                break;
            case FINALIZE:
                imgIcon.setImageResource(R.drawable.ic_btn_finalize_eat);
                break;
            case BREASTFEED:
                imgIcon.setImageResource(R.drawable.breastfeeding);
        }
    }

    public void setUpList(){
        //Create Adapter/ Send this as context and Click listener implementation
        mHouseholdMemberAdapter = new HouseholdMemberListAdapter(this, this);

        //Subscribe to the view model to get the data and populate list. This updates on data changed
        mHouseholdMemberViewModel.getObservableHouseholdMembers().observe(this, new Observer<List<HouseholdMember>>() {
            @Override
            public void onChanged(@Nullable List<HouseholdMember> householdMembers) {
                mHouseholdMemberAdapter.setHouseholdMemberList(householdMembers);
            }
        });

        //Get Recycler View
        RecyclerView list = (RecyclerView) findViewById(R.id.listViewHouseholdMembers);
        list.setAdapter(mHouseholdMemberAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(llm);
    }

    @Override
    public void onDestroy(){
        mHouseholdMemberViewModel = null;
        mHouseholdMemberAdapter = null;
        foodRecordViewModel = null;
        super.onDestroy();
    }

    @Override
    public void householdMemberClicked(HouseholdMember hm) {
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked Household Member " + hm.getName());
        // //Check to see if there is a current food record for this person.. Should be 1 or 0.
        FoodRecord todaysFoodRecord = null;
        try {
            todaysFoodRecord = foodRecordViewModel.getTodaysFoodRecordFor(hm);
            //Behave according to the state of the activity.
            switch(SelectHouseholdMemberActivity.this.mCurrentState){
                case EAT: //Start the record eating occasion process
                    startEating(todaysFoodRecord);
                    break;
                case FINALIZE:
                case MEAL:
                    moveToNextActivity(todaysFoodRecord);
                    break;
                case BREASTFEED:
                    moveToNextActivity(todaysFoodRecord);
                    break;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean householdMemberLongClicked(HouseholdMember hm) {
        //Do nothing
        return false;
    }

    private void startEating(FoodRecord todaysFoodRecord) throws ExecutionException, InterruptedException {
        //Check the eating occasions in the food record
        List<EatingOccasion> eatingOccasions = todaysFoodRecord.getEatingOccasions();

        //If no eating occasions create a new one
        EatingOccasion currentEo = null;
        if(eatingOccasions.size() > 0){
            //Search through all the eating occasions. Just chek all of them, not expected to have many in the list
            currentEo = todaysFoodRecord.getCurrentEatingOccasion();
        }
        if(currentEo == null){
            //If no recent EOs then create a new one to use and add it to the FoodRecord
            currentEo = new EatingOccasion();
            currentEo.setFoodRecordId(todaysFoodRecord.getFoodRecordId());
            //Add the eating occasion to the database
            EatingOccasionRepository eoRepo = new EatingOccasionRepository(SelectHouseholdMemberActivity.this.getApplication());
            currentEo.setEatingOccasionId(eoRepo.addEatingOccasion(currentEo));
            todaysFoodRecord.addEatingOccasion(currentEo);
        }

        //Post the notification
        moveToNextActivity(todaysFoodRecord);
    }

    /**
     * Method for creating the Intent and starting the next activity.
     * This method determines if we are in EAT or FINALIZE EAT and
     * creates the intent accordingly.
     * @param todaysFoodRecord
     */
    private void moveToNextActivity(FoodRecord todaysFoodRecord){
        Intent intent;
        switch(this.mCurrentState){
            case EAT: //Start the record eating occasion process
                intent = new Intent(this, EatingOccasionActivity.class);
                break;
            case FINALIZE:
                //Get all of the non finalized eating occasions for this household member
                EatingOccasionRepository eoRepo = new EatingOccasionRepository(getApplication());
                List<EatingOccasion> unFinalizedEatingOccasions = eoRepo.getNonFinalizedEatingOccasionsForHouseholdMember(todaysFoodRecord.getHouseholdMemberId());
                //If there are no eating occasions not finalized then dont do anything.
                if(unFinalizedEatingOccasions.size() <= 0){
                    Toast.makeText(this, R.string.noeatingoccasions, Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    //Otherwise create intent to the select eating occasion activity.
                    intent = new Intent(SelectHouseholdMemberActivity.this, SelectEatingOccasionActivity.class);
                    Long[] eoIds = new Long[unFinalizedEatingOccasions.size()];
                    for(int i = 0; i < eoIds.length; i++) {
                        eoIds[i] = unFinalizedEatingOccasions.get(i).getEatingOccasionId();
                    }
                    intent.putExtra(UNFINALIZEDEOIDS, eoIds);
                }
                break;
            case BREASTFEED:
                if(!todaysFoodRecord.getHouseholdMember().isBreastfed()) {
                    Toast.makeText(this, R.string.hmnotbreastfed, Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    intent = new Intent(SelectHouseholdMemberActivity.this, BreastfeedActivity.class);
                }
                break;
            case MEAL:
                intent = new Intent(this, MealActivity.class);
                break;
            default:
                Log.e(TAG, "Select householdmember activity in unknown state");
                intent = new Intent();
        }

        //Add the food record to the Intent to pass to the next activity
        intent.putExtra(FR, todaysFoodRecord);
        startActivity(intent);

    }
}


