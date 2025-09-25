package au.edu.newcastle.jnc985.visida.activity;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import au.edu.newcastle.jnc985.visida.R;
import bo.AppConstants;
import bo.HouseholdMembersViewModel;
import bo.HouseholdRepository;
import bo.Utilities;
import bo.db.entity.Household;
import bo.db.entity.HouseholdMember;
import ui.HouseholdMemberListAdapter;
import ui.TimePickerFragment;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.DEFAULT_REVIEW_HOUR;
import static bo.AppConstants.DEFAULT_REVIEW_MINUTE;
import static bo.AppConstants.HOUSEHOLDID;
import static bo.AppConstants.HOUSEHOLDMEMBER;
import static bo.AppConstants.NAVBAR;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.REVIEWTIME_HOUR;
import static bo.AppConstants.REVIEWTIME_MIN;
import static bo.AppConstants.SETUP;

public class SetupHouseholdActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, View.OnClickListener {

    private static final String TAG = "SetupHouseholdActivity";
    private static final String TIMEPICKERFRAGMENT = "timePickerfragment";

    private SharedPreferences mSharedPref;
    private NavigationBarFragment mNavBar;

    private HouseholdMembersViewModel mHouseholdMemberViewModel;
    private HouseholdMemberListAdapter mAdapter;

    private String mHouseholdId;
    private Household mHousehold;

    private Dialog mPromptDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Setup Household Activity Created");
        // Run this at the start of every activity
        Utilities.updateLanguage(getBaseContext());
        setTitle(R.string.title_setup_household);
        setContentView(R.layout.activity_setup_household);

        FragmentManager fm = getSupportFragmentManager();
        mNavBar = (NavigationBarFragment) fm.findFragmentByTag(NAVBAR);

        if(mNavBar == null) {
            //Load the fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, NavigationBarFragment.newInstance(), NAVBAR)
                    .commit();
        }

        mSharedPref = getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

        //Create household
        mHousehold = createHousehold();

        ImageView btnSetupTimes = findViewById(R.id.btnSetupTimes);
        btnSetupTimes.setOnClickListener(this);

        //Check if the household is already set up
        boolean isSetup = mSharedPref.getBoolean(AppConstants.SETUP, false);

        //If already set up require a pin code to move one
        //if(!isSetup){
        //    promptFirstUse();
        //}
        saveSetup();

        if(savedInstanceState != null){
            //restore the household id
            mHouseholdId = savedInstanceState.getString(HOUSEHOLDID);
        }

        //Create household member View Model
        if(mHouseholdMemberViewModel == null) {
            mHouseholdMemberViewModel = new ViewModelProvider(this).get(HouseholdMembersViewModel.class);
        }

        //Create the adapter for the list
        mAdapter = new HouseholdMemberListAdapter(this, new HouseholdMemberListAdapter.HouseholdMemberClickListener() {
            @Override
            public void householdMemberClicked(HouseholdMember hm) {
                Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked household member " + hm.getName());
                //Create intent to the create Household member activity
                Intent intent = new Intent(SetupHouseholdActivity.this, CreateHouseholdMemberActivity.class);
                intent.putExtra(HOUSEHOLDMEMBER, hm);
                startActivity(intent);
            }

            @Override
            public boolean householdMemberLongClicked(final HouseholdMember hm) {
                //Ask the user to confirm the delete
                new AlertDialog.Builder(SetupHouseholdActivity.this)
                        .setTitle(R.string.delete_item)
                        .setMessage(R.string.delete_confirmation)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                Log.i(ACTIVITY_LOG_TAG, TAG + ": Confirmed to delete household member");
                                Toast.makeText(SetupHouseholdActivity.this, R.string.householdmember_deleted, Toast.LENGTH_SHORT).show();
                                //Delete the HM
                                SetupHouseholdActivity.this.mHouseholdMemberViewModel.deleteHouseholdMember(hm);
                            }})
                        .setNegativeButton(R.string.no, null).show();
                return true;
            }
        });

        //Subscribe to the ViewModel
        mHouseholdMemberViewModel.getObservableHouseholdMembers().observe(this, new Observer<List<HouseholdMember>>(){
            @Override
            public void onChanged(List<HouseholdMember> hmList){
                if(hmList != null) {
                    mAdapter.setHouseholdMemberList(hmList);
                }
            }
        });

        //Get the recycler view
        RecyclerView list = findViewById(R.id.recyclelistHouseholdMembers);
        list.setAdapter(mAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(layoutManager);

        //Set onClickListenerOfTheFloatingActionButton
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked FAB to add household member");
                //Create Intent to move to the create household member
                Intent intent = new Intent(SetupHouseholdActivity.this, CreateHouseholdMemberActivity.class);
                startActivity(intent);
            }
        });

        setupTime();
    }

    private void setupTime(){
        //Default time 7:30pm.
        int hours = mSharedPref.getInt(REVIEWTIME_HOUR, DEFAULT_REVIEW_HOUR);
        int minutes = mSharedPref.getInt(REVIEWTIME_MIN, DEFAULT_REVIEW_MINUTE);
        TextView textView = findViewById(R.id.txtWhatTime);
        setTimeString(hours, minutes);
        textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_clock, 0, 0, 0);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked to set review notification time");
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getSupportFragmentManager(), TIMEPICKERFRAGMENT);
            }
        });
    }

    @Override
    public void onPause(){
        if(mPromptDialog != null){
            mPromptDialog.dismiss();
        }
        super.onPause();
    }

    private void promptFirstUse() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.popup_checkbox, null);
        CheckBox chkAccept = layout.findViewById(R.id.chkAccept);
        builder.setView(layout);
        builder.setTitle(R.string.consent_audio_transcribe_title)
                .setMessage(R.string.consent_audio_transcribe);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveSetup();
                mHousehold.setConsent(chkAccept.isChecked());
                HouseholdRepository hhRepo = new HouseholdRepository(getApplication());
                hhRepo.updateHousehold(mHousehold);
            }
        });
        mPromptDialog = builder.show();
    }

    private void saveSetup() {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putBoolean(SETUP, true);
        editor.commit();
    }

    private Household createHousehold() {
        //Check shared preferences to see if Household already created
        mHouseholdId = mSharedPref.getString(HOUSEHOLDID, null);
        Household hh = null;
        if(mHouseholdId == null){
            //Generate and save new unique id
            mHouseholdId = UUID.randomUUID().toString();
            //Get the country
            String country = getResources().getConfiguration().getLocales().get(0).getDisplayCountry();
            //Create a household in the database
            hh = new Household();
            hh.setHouseholdId(mHouseholdId);
            hh.setCountry(country);
            HouseholdRepository hhRepo = new HouseholdRepository(getApplication());
            hhRepo.addHousehold(hh);

            //Save the unique id in shared preferences
            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString(HOUSEHOLDID, mHouseholdId);
            editor.commit();
            Log.i(ACTIVITY_LOG_TAG, TAG + ": Household ID Created " + mHouseholdId);
        }
        else{
            HouseholdRepository hhRepo = new HouseholdRepository(getApplication());
            return hhRepo.getHousehold();
        }
        return hh;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the state of the activity for when they return.
        outState.putString(HOUSEHOLDID, mHouseholdId);
    }

    public void setTimeString(int hour, int minute){
        TextView txtWhatTime = findViewById(R.id.txtWhatTime);
        String defaultText = getResources().getString(R.string.whattime);
        defaultText += hour + ":";
        if(minute < 10){
            defaultText += "0";
        }
        defaultText += minute;
        txtWhatTime.setText(defaultText);
        txtWhatTime.setPaintFlags(txtWhatTime.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Review notification time set to " + hourOfDay + "-" + minute);
        setTimeString(view.getHour(), view.getMinute());
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        Date finalizeTime = new Date(c.getTime().getTime());
        mHousehold.setFinalizeTime(finalizeTime);
        HouseholdRepository hhRepo = new HouseholdRepository(getApplication());
        hhRepo.updateHousehold(mHousehold);

        //Save the hour and minute in shared preferences
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putInt(REVIEWTIME_HOUR, hourOfDay);
        editor.putInt(REVIEWTIME_MIN, minute);
        editor.apply();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked " + getResources().getResourceEntryName(id));
        switch (id) {
            case R.id.btnSetupTimes:
                Intent intent = new Intent(this, SetupTimesActivity.class);
                startActivity(intent);
        }
    }
}
