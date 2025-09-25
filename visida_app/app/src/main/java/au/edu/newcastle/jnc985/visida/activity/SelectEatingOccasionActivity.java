package au.edu.newcastle.jnc985.visida.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import bo.EatingOccasionViewModel;
import bo.Utilities;
import bo.db.entity.EatingOccasion;
import bo.db.entity.ImageListProvider;
import ui.EatingOccasionListAdapter;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.EOID;
import static bo.AppConstants.NAVBAR;
import static bo.AppConstants.UNFINALIZEDEOIDS;

public class SelectEatingOccasionActivity extends AppCompatActivity {
    private static final String TAG = "SelectEOActivity";

    private Long[] mEoIds;

    private NavigationBarFragment mNavBar;
    private final long FRIDDEFAULT = -1;
    private EatingOccasion mSelectedEo;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(UNFINALIZEDEOIDS, mEoIds);
        super.onSaveInstanceState(outState);
        //Save the state of the activity for when they return.
        //outState.putParcelable(SelectHouseholdMemberActivity.FR, mFoodRecord);
    }

    @Override
    protected void onPause(){
        /*
        //Save the id of the current food record so if navigated back we can restore the state.
        SharedPreferences sharedPref = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        //editor.putLong(FRID, mFoodRecord.getEatingOccasionId());
        //editor.put
        editor.commit();
        */
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Run this at the start of every activity
        Utilities.updateLanguage(getBaseContext());
        setTitle(R.string.title_activity_select_eating_occasion);
        setContentView(R.layout.activity_select_eating_occasion);
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Select Eating Occasion Activity Created");

        FragmentManager fm = getSupportFragmentManager();
        mNavBar = (NavigationBarFragment) fm.findFragmentByTag(NAVBAR);

        if(mNavBar == null) {
            //Load the fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, NavigationBarFragment.newInstance(), NAVBAR)
                    .commit();
        }

        if(savedInstanceState != null){
            mEoIds = (Long[])savedInstanceState.get(UNFINALIZEDEOIDS);
        }
        else{
            //Get the food record out of the intent
            Intent i = getIntent();
            Bundle extras = i.getExtras();
            if(extras != null){
                //mFoodRecord = extras.getParcelable(SelectHouseholdMemberActivity.FR);
                mEoIds = (Long[])extras.get(UNFINALIZEDEOIDS);
            }
        }
        setUpList();
    }

    public void setUpList(){
        RecyclerView list = findViewById(R.id.rvEatingOccasions);
        EatingOccasionViewModel eatingOccasionViewModel = new ViewModelProvider(this).get(EatingOccasionViewModel.class);
        //Clear out any empty eating occasions
        eatingOccasionViewModel.clearEmpty();
        //Get the remaining eating occasions
        List<EatingOccasion> eatingOccasions = eatingOccasionViewModel.getEatingOccasions(mEoIds);

        String buttonText = getResources().getString(R.string.finalize);
        EatingOccasionListAdapter eoListAdapter = new EatingOccasionListAdapter(eatingOccasions, buttonText, new EatingOccasionListAdapter.onImageProviderClickListener() {
            @Override
            public void onItemClick(ImageListProvider item) {
                mSelectedEo = (EatingOccasion) item;
                moveToFinalize();
            }

            @Override
            public boolean onItemLongClick(ImageListProvider item) {
                return true;
            }
        });
        list.setAdapter(eoListAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(llm);
    }

    /**
     * TESTING ONLY
     * @param eo
     */
    public void setSelectedEatingOccasion(EatingOccasion eo){
        this.mSelectedEo = eo;
    }

    private void moveToFinalize() {
        if(mSelectedEo != null){
            Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked Eating Occasion " + mSelectedEo.getEatingOccasionId());
            //Create the intent to the finalizeItem activity
            Intent i = new Intent(SelectEatingOccasionActivity.this, FinalizeFoodItemActivity.class);
            i.putExtra(EOID, mSelectedEo.getEatingOccasionId());
            startActivity(i);
        }
    }

}
