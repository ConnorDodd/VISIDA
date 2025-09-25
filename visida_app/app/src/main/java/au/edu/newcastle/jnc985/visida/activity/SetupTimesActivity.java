package au.edu.newcastle.jnc985.visida.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import au.edu.newcastle.jnc985.visida.R;
import au.edu.newcastle.jnc985.visida.databinding.ActivitySetupTimesBinding;
import bo.Utilities;
import bo.scheduler.EOScheduleViewModel;
import bo.scheduler.ObservableCalendar;
import ui.DatePickerFragment;
import ui.TimePickerFragment;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.NAVBAR;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.REMINDERS_SET;
import static bo.scheduler.EOScheduleViewModel.DAY_ONE;
import static bo.scheduler.EOScheduleViewModel.DAY_THREE;
import static bo.scheduler.EOScheduleViewModel.DAY_TWO;

public class SetupTimesActivity extends AppCompatActivity implements View.OnClickListener, TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    private static final String TAG = "SetupTimesActivity";

    private EOScheduleViewModel mScheduleViewModel;
    private NavigationBarFragment mNavBar;

    private ImageView btnDateEO1;
    private ImageView btnDateEO2;
    private ImageView btnDateEO3;
//    private ImageView btnSensor;

    private Button btnSet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Setup Times Activity Created");
        // Run this at the start of every activity
        Utilities.updateLanguage(getBaseContext());
        setContentView(R.layout.activity_setup_times);

        FragmentManager fm = getSupportFragmentManager();
        mNavBar = (NavigationBarFragment) fm.findFragmentByTag(NAVBAR);

        if(mNavBar == null) {
            //Load the fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, NavigationBarFragment.newInstance(), NAVBAR)
                    .commit();
        }

        //Set the view model
        if(mScheduleViewModel == null){
            mScheduleViewModel = new ViewModelProvider(this).get(EOScheduleViewModel.class);
        }


        bindViewModel();
        assignButtons();
    }

    private void bindViewModel() {
        ActivitySetupTimesBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_setup_times);
        ObservableCalendar time = mScheduleViewModel.getTime(1);
        binding.setDate1(time);
        binding.time1.setTime(time);
        binding.time1.layout.setTag("1");
        binding.time1.layout.setOnClickListener(this);

        time = mScheduleViewModel.getTime(2);
        binding.time2.setTime(time);
        binding.time2.layout.setTag("2");
        binding.time2.layout.setOnClickListener(this);

        time = mScheduleViewModel.getTime(3);
        binding.time3.setTime(time);
        binding.time3.layout.setTag("3");
        binding.time3.layout.setOnClickListener(this);

        time = mScheduleViewModel.getTime(4);
        binding.setDate2(time);
        binding.time4.setTime(time);
        binding.time4.layout.setTag("4");
        binding.time4.layout.setOnClickListener(this);

        time = mScheduleViewModel.getTime(5);
        binding.time5.setTime(time);
        binding.time5.layout.setTag("5");
        binding.time5.layout.setOnClickListener(this);

        time = mScheduleViewModel.getTime(6);
        binding.time6.setTime(time);
        binding.time6.layout.setTag("6");
        binding.time6.layout.setOnClickListener(this);

        time = mScheduleViewModel.getTime(7);
        binding.setDate3(time);
        binding.time7.setTime(time);
        binding.time7.layout.setTag("7");
        binding.time7.layout.setOnClickListener(this);

        time = mScheduleViewModel.getTime(8);
        binding.time8.setTime(time);
        binding.time8.layout.setTag("8");
        binding.time8.layout.setOnClickListener(this);

        time = mScheduleViewModel.getTime(9);
        binding.time9.setTime(time);
        binding.time9.layout.setTag("9");
        binding.time9.layout.setOnClickListener(this);

        // Time for the sensor reminder
        // No longer in use
        /*
        time = mScheduleViewModel.getSensorTime();
        binding.setSensorTime(time);
        binding.inputTimeSensor.setTime(time);
        binding.inputTimeSensor.layout.setTag(""+SENSOR);
        binding.inputTimeSensor.layout.setOnClickListener(this);
         */
    }

    private void assignButtons() {
        btnDateEO1 = findViewById(R.id.btnDateEO1);
        btnDateEO2 = findViewById(R.id.btnDateEO2);
        btnDateEO3 = findViewById(R.id.btnDateEO3);
//        btnSensor = findViewById(R.id.btnSensor);

        btnDateEO1.setOnClickListener(this);
        btnDateEO2.setOnClickListener(this);
        btnDateEO3.setOnClickListener(this);
//        btnSensor.setOnClickListener(this);
//        btnSensor.setTag(SENSOR);

        btnSet = findViewById(R.id.btnSet);
        btnSet.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked " + getResources().getResourceEntryName(id));
        switch(id){
            case R.id.btnDateEO1:
                //Open Date picker
                openDatePicker(DAY_ONE);
                break;
            case R.id.btnDateEO2:
                //Open Date picker
                openDatePicker(DAY_TWO);
                break;
            case R.id.btnDateEO3:
                //Open Date picker
                openDatePicker(DAY_THREE);
                break;

            case R.id.btnSet:
                scheduleAlarms();
                break;

            default:
                //Catch the case where they clicked a time.
                //Get the tag. Cast to Int and open the picker with that number
                try {
                    int tag = Integer.parseInt(v.getTag().toString());
                    openTimePicker(tag);
                }
                catch (NumberFormatException ex){
                    ex.printStackTrace();
                }

        }

    }

    private void scheduleAlarms() {
        if(!mScheduleViewModel.allTimesSet()){
            mScheduleViewModel.attempt();
            Toast.makeText(this, R.string.not_all_times_set, Toast.LENGTH_SHORT).show();
        }
        else {
            mScheduleViewModel.saveTimes();
            mScheduleViewModel.setDefaultReviewAlarms();
            updateSharedPreferences();
            setResult(RESULT_OK);
            finish();
        }
    }

    private void updateSharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(REMINDERS_SET, true);
        editor.commit();
    }

    private void openTimePicker(int slot) {
        mScheduleViewModel.setCurrentTime(slot);
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    private void openDatePicker(int date) {
        mScheduleViewModel.setCurrentDate(date);
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mScheduleViewModel.setTime(hourOfDay, minute);
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mScheduleViewModel.setDate(year, month, dayOfMonth);
    }
}
