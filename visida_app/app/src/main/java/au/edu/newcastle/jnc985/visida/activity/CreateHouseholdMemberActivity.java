package au.edu.newcastle.jnc985.visida.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import au.edu.newcastle.jnc985.visida.R;
import bo.HouseholdMemberRepository;
import bo.Utilities;
import bo.db.entity.HouseholdMember;
import bo.typeconverter.TimestampConverter;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.HASBREASTFED;
import static bo.AppConstants.HOUSEHOLDID;
import static bo.AppConstants.HOUSEHOLDMEMBER;
import static bo.AppConstants.NAVBAR;
import static bo.AppConstants.PARTICIPANTHOUSEHOLDID;
import static bo.AppConstants.PREFERENCES;

public class CreateHouseholdMemberActivity extends AppCompatActivity implements View.OnClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback, RadioGroup.OnCheckedChangeListener{

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = "CreateHouseholdMemberActivity";
    private static final String IS_FEMALE = "gender";
    private static final String FILENAME = "filename";
    private static final String PARTICIPANTID = "participantid";
    private static final String NAME = "name";
    private static final String AGE = "age";
    private static final String BREASTFED = "breastfed";
    private static final String DIALOG = "dialog";

    private File mImageFile;

    private ImageView mImageView;
    private TextInputEditText mTxtName;
    private TextInputEditText mTxtHouseholdId;
    private TextInputEditText mTxtParticipantId;
    private NumberPicker mAgeYears;
    private NumberPicker mAgeMonths;
    private TextView mTvAgeMonths;
    private CheckBox mIsBreastfed;
    private RadioButton mRbMale;
    private RadioButton mRbFemale;
    private Spinner mSpinnerLifeStage;

    private LinearLayout lifeStageLayout;

    private HouseholdMember currentHm;

    private NavigationBarFragment mNavBar;

    private String mParticipantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Run this at the start of every activity
        Utilities.updateLanguage(getBaseContext());
        setTitle(R.string.title_create_householdmember);
        setContentView(R.layout.activity_create_household_member);

        FragmentManager fm = getSupportFragmentManager();
        mNavBar = (NavigationBarFragment) fm.findFragmentByTag(NAVBAR);

        if(mNavBar == null) {
            //Load the fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, NavigationBarFragment.newInstance(), NAVBAR)
                    .commit();
        }

        this.mImageView = findViewById(R.id.imgViewAvatar);
        this.mTxtHouseholdId = findViewById(R.id.txtHouseholdId);
        this.mTxtParticipantId = findViewById(R.id.txtParticipantId);
        this.mTxtName = findViewById(R.id.txtName);
        this.mAgeYears = findViewById(R.id.ageYears);
        this.mAgeMonths = findViewById(R.id.ageMonths);
        this.mTvAgeMonths = findViewById(R.id.tvAgeMonths);
        this.mIsBreastfed = findViewById(R.id.chkIsBreastfed);
        this.mRbMale = findViewById(R.id.radioBtnMale);
        this.mRbFemale = findViewById(R.id.radioBtnFemale);
        this.mSpinnerLifeStage = findViewById(R.id.spinnerLifeStage);

        // Setup age picker
        // Age can be 0 - 100
        this.mAgeYears.setMaxValue(100);
        this.mAgeYears.setMinValue(0);
        // Months will only go up to 11
        this.mAgeMonths.setMaxValue(11);
        this.mAgeMonths.setMinValue(0);

        lifeStageLayout = findViewById(R.id.layout_lifeStage);

        RadioGroup radioGroup = findViewById(R.id.radioGender);
        radioGroup.setOnCheckedChangeListener(this);
        mImageView.setOnClickListener(this);
        Button submitButton = findViewById(R.id.btnSaveHouseholdMember);
        submitButton.setOnClickListener(this);

        this.mAgeYears.setOnValueChangedListener(new NumberListener());
        this.mAgeMonths.setOnValueChangedListener(new NumberListener());

        if(savedInstanceState != null){
            String fileName = savedInstanceState.getString(FILENAME);
            if(fileName != null) {
                mImageFile = new File(fileName);
            }
            setImageView();
            this.mTxtHouseholdId.setText(savedInstanceState.getString(PARTICIPANTHOUSEHOLDID));
            this.mTxtParticipantId.setText(savedInstanceState.getString(PARTICIPANTID));
            this.mTxtName.setText(savedInstanceState.getString(NAME));
            // TODO this.mTxtYears.setText(savedInstanceState.getString(AGE));
            this.mIsBreastfed.setChecked(savedInstanceState.getBoolean(BREASTFED));
            this.currentHm = savedInstanceState.getParcelable(HOUSEHOLDMEMBER);
            boolean isFemale = savedInstanceState.getBoolean(IS_FEMALE);
            if(isFemale){
                mRbFemale.setChecked(true);
            }
            else{
                mRbMale.setChecked(true);
            }

        }
        else {
            //Get the intent.
            Intent i = getIntent();
            Bundle extras = i.getExtras();
            if (extras != null) {
                //get the household member from the intent and set the fields
                currentHm = extras.getParcelable(HOUSEHOLDMEMBER);
                this.mTxtParticipantId.setText(currentHm.getParticipantHouseholdMemberId());
                this.mTxtName.setText(currentHm.getName());
                // TODO this.mTxtYears.setText(String.valueOf(currentHm.getAge()));
                this.mIsBreastfed.setChecked(currentHm.isBreastfed());
                boolean isFemale = currentHm.isFemale();
                if(isFemale){
                    mRbFemale.setChecked(true);
                }
                else{
                    mRbMale.setChecked(true);
                }
                String avatarPath = currentHm.getAvatar();
                Glide.with(this)
                        .load(avatarPath)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.ic_default_person))
                        .into(mImageView);
                if(avatarPath != null && !avatarPath.isEmpty()) {
                    this.mImageFile = new File(currentHm.getAvatar());
                }
                this.mSpinnerLifeStage.setSelection(getIntForLifeStage(currentHm.getLifeStage()));
            }
        }
        //Read in Participant Household Id from shared preferences
        SharedPreferences sharedPreferences = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        String participantHouseholdId = sharedPreferences.getString(PARTICIPANTHOUSEHOLDID, null);
        this.mTxtHouseholdId.setText(participantHouseholdId);
        checkParticpantFields();
    }

    /**
     * Aus Version. To Allow pre set up household members we dont want the user to be able to
     * edit the Household id, or the Participant ID. So if editing we disable the two text boxes.
     */
    private void checkParticpantFields() {
        if(!isEmpty(mTxtHouseholdId)){
            mTxtHouseholdId.setEnabled(false);
        }
        if(!isEmpty(mTxtParticipantId)){
            mParticipantId = mTxtParticipantId.getText().toString();
            mTxtParticipantId.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked " + getResources().getResourceEntryName(v.getId()));
        switch (v.getId()) {
            case R.id.imgViewAvatar: {
                captureAvatar();
                break;
            }
            case R.id.btnSaveHouseholdMember:{
                boolean saved = saveHouseholdMember();
                if(saved) {
                    checkHosueholdForBreastfed();
                    //Return to the list activity
                    Intent intent = new Intent(this, SetupHouseholdActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        }
    }

    private void checkHosueholdForBreastfed() {
        //Cheak all of the household members currently saved.
        HouseholdMemberRepository hmRepo = new HouseholdMemberRepository(this.getApplication());
        boolean hasBreastfed = hmRepo.hasBrestfedMember();
        SharedPreferences sharedPreferences = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(HASBREASTFED, hasBreastfed);
        editor.apply();
    }

    private boolean saveHouseholdMember() {
        boolean valid = validInput();
        if(valid){
            //Save the householdmember
            HouseholdMember newHm = new HouseholdMember();
            SharedPreferences sharedPreferences = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String householdId = sharedPreferences.getString(HOUSEHOLDID, null);
            //If we are editing a current household member then take the Uid.
            if(currentHm != null){
                newHm.setUid(currentHm.getUid());
            }
            newHm.setHouseholdId(householdId);

            String name = mTxtName.getText().toString();
            newHm.setName(name);

            String participantHouseholdId = mTxtHouseholdId.getText().toString();
            if(!participantHouseholdId.isEmpty()) {
                //Save to shared preferences
                editor = sharedPreferences.edit();
                editor.putString(PARTICIPANTHOUSEHOLDID, participantHouseholdId);
                newHm.setParticipantHouseholdId(participantHouseholdId);
                Log.i(ACTIVITY_LOG_TAG, TAG + ": Participant household id created " + participantHouseholdId);
            }

            String participantId = mTxtParticipantId.getText().toString();
            newHm.setParticipantHouseholdMemberId(participantId);

            float age = getAge();
            TextView tv = findViewById(R.id.textViewAge);
            if(age >= 0){
                newHm.setAge(age);
                tv.setError(null);
            }
            else{
                tv.setError(getResources().getString(R.string.error_age));
                valid = false;
            }

            boolean isFemale = mRbFemale.isChecked();
            if(mRbFemale.isChecked() != mRbMale.isChecked()){
                newHm.setFemale(isFemale);
            }

            if(mImageFile != null) {
                newHm.setAvatar(mImageFile.getAbsolutePath());
            }
            boolean isBreastfed = mIsBreastfed.isChecked();
            newHm.setBreastfed(isBreastfed);

            //Get the life stage
            String lifeStage = getEnglishStringForLifeStage(mSpinnerLifeStage.getSelectedItemPosition());
            //String lifeStage = mSpinnerLifeStage.getSelectedItem().toString();
            newHm.setLifeStage(lifeStage);

            HouseholdMemberRepository hmRepo = new HouseholdMemberRepository(this.getApplication());
            hmRepo.addHouseholdMember(newHm);

            editor.apply();
        }

        return valid;
    }

    private String getEnglishStringForLifeStage(int selectedItemPosition) {
        //Take the index of the selected lifestage and return the english string
        /*
        <item>None</item>
        <item>Pregnant</item>
        <item>Breastfeeding</item>
        <item>Pregnant &amp; Breastfeeding </item>
         */
        switch (selectedItemPosition){
            case 0: return "None";
            case 1: return "Pregnant";
            case 2: return "Breastfeeding";
            case 3: return "Pregnant & Breastfeeding";
            default: return "ERROR";
        }
    }
    private int getIntForLifeStage(String lifestage) {
        //Take the index of the selected lifestage and return the english string
        /*
        <item>None</item>
        <item>Pregnant</item>
        <item>Breastfeeding</item>
        <item>Pregnant &amp; Breastfeeding </item>
         */
        switch (lifestage){
            case "None": return 0;
            case "Pregnant": return 1;
            case "Breastfeeding": return 2;
            case "Pregnant & Breastfeeding": return 3;
            default: return 0;
        }
    }

    private boolean validInput() {
        boolean isValid = true;
        //Make sure they have taken an image
        /*
        Aus Version to allow users to be pre set up we allow no Image to be taken hence this code is
        commented out.
        */
        if(mImageFile == null) {
            Toast.makeText(this, getResources().getString(R.string.no_image), Toast.LENGTH_SHORT).show();
            isValid = false;
        }


        //Check the participant household id
        TextInputLayout til = findViewById(R.id.txtHouseholdIdLayout);
        if(isEmpty(mTxtHouseholdId)){
            til.setError(getResources().getString(R.string.error_participant_household_id));
            isValid = false;
        }
        else{
            til.setError(null);
        }

        //Check the participant id
        til = findViewById(R.id.txtParticipantIdLayout);
        if(isEmpty(mTxtParticipantId)){
            til.setError(getResources().getString(R.string.error_participant_id));
            isValid = false;
        }
        else{
            //Check and make sure its unique
            HouseholdMemberRepository hmRepo = new HouseholdMemberRepository(getApplication());
            String participantId = mTxtParticipantId.getText().toString();
            if(!participantId.equals(mParticipantId) && hmRepo.participantIdExists(participantId)){
                til.setError(getResources().getString(R.string.error_participant_id_already_exists));
                isValid = false;
            }
            else {
                til.setError(null);
            }
        }

        til = findViewById(R.id.txtNameLayout);
        if(isEmpty(mTxtName)){
            til.setError(getResources().getString(R.string.error_name));
            isValid = false;
        }
        else{
            til.setError(null);
        }

        TextView tv = findViewById(R.id.textViewAge);
        if(getAge() <= 0){
            tv.setError(getResources().getString(R.string.error_age));
            isValid = false;
        }
        else{
            tv.setError(null);
        }
        if(!mRbFemale.isChecked() && !mRbMale.isChecked()){
            mRbMale.setError(getResources().getString(R.string.error_gender));
            isValid = false;
        }
        else{
            mRbMale.setError(null);
        }

        return isValid;
    }

    /**
     * Gets the age from the Age Text box. If The age is invalid
     * It sets the error on the age box and returns -1
     * @return
     */
    private float getAge(){
        float age;
        try {
            int years = mAgeYears.getValue();
            float months = mAgeMonths.getValue();
            if(years != 0 || months != 0) {
                age = years + (months/12);
                Log.i("getAge: ", String.valueOf(age));
                return age;
            }
            return -1;
        }
        catch (NumberFormatException ex){
            return -1;
        }
    }

    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        //Put the thumbnail in the imgview
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            setImageView();
        }
    }

    private void setImageView() {
        if(mImageFile != null) {
            Glide.with(this)
                    .load(mImageFile)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_thinking))
                    .into(mImageView);
        }
    }

    private int REQUEST_CAMERA_PERMISSION = 1;

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            new ConfirmationDialog().show(getSupportFragmentManager(), DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                TakePhotoFragment.ErrorDialog.newInstance(getString(R.string.request_permission_camera))
                        .show(getSupportFragmentManager(), DIALOG);
            }
            else{
                captureAvatar();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }

    private void captureAvatar() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }

        //Create Intent to the camera app to take a photo
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            //Create file for the image to be saved into
            try{
                mImageFile = createImageFile();
            }
            catch (IOException ex){
                Log.e(TAG, ex.getMessage());
            }

            if(mImageFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this, "au.edu.newcastle.jnc985.visida.fileprovider", mImageFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                //Start activity for result and get the thumbnail back.
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = TimestampConverter.toTimestamp(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Utilities.getHouseholdDirectory(this);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if(checkedId == R.id.radioBtnFemale){
            //Check the age
            float age = getAge();
            if(age >= 0){
                if(age >= 18){
                    //Make the LifeStage visible
                    lifeStageLayout.setVisibility(View.VISIBLE);
                }
            }
        }
        else{
            //Make the LifeStage gone
            lifeStageLayout.setVisibility(View.GONE);
        }
    }


    /**
     * Shows OK/Cancel confirmation dialog about camera permission.
     */
    public static class ConfirmationDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity parent = getActivity();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.request_permission_camera)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            parent.requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    REQUEST_IMAGE_CAPTURE);
                        }
                    })
                    .setNegativeButton(R.string.no,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //Activity activity = parent.getActivity();
                                    if (parent != null) {
                                        parent.finish();
                                    }
                                }
                            })
                    .create();
        }
    }

    private class NumberListener implements NumberPicker.OnValueChangeListener {

        @Override
        public void onValueChange(NumberPicker numberPicker, int i, int i1) {
            float age = getAge();
            if (age < 5) {
                //Make the breastfeed checkbox appear
                CreateHouseholdMemberActivity.this.mIsBreastfed.setVisibility(View.VISIBLE);
                CreateHouseholdMemberActivity.this.lifeStageLayout.setVisibility(View.GONE);
            } else {
                CreateHouseholdMemberActivity.this.mIsBreastfed.setVisibility(View.GONE);
                //Make sure its false
                CreateHouseholdMemberActivity.this.mIsBreastfed.setChecked(false);
            }
            if(age >= 18){
                //Check if female is checked
                if(mRbFemale.isChecked()){
                    //Make the spinner visible
                    lifeStageLayout.setVisibility(View.VISIBLE);
                }
            }
            else{
                //Check if female is checked
                if(mRbFemale.isChecked()){
                    //Make the spinner invisible
                    lifeStageLayout.setVisibility(View.INVISIBLE);
                }
            }
            if(age >= 2) {
                // Hide months and set it to zero
                mTvAgeMonths.setVisibility(View.INVISIBLE);
                mAgeMonths.setVisibility(View.INVISIBLE);
                mAgeMonths.setValue(0);
            }
            else {
                // Show months
                mTvAgeMonths.setVisibility(View.VISIBLE);
                mAgeMonths.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mImageFile != null) {
            outState.putString(FILENAME, mImageFile.getAbsolutePath());
        }
        Editable editTxt = mTxtName.getText();
        if(editTxt != null) {
            outState.putString(NAME, editTxt.toString());
        }
        editTxt = mTxtHouseholdId.getText();
        if(editTxt != null){
            outState.putString(PARTICIPANTHOUSEHOLDID, editTxt.toString());
        }
        editTxt = mTxtParticipantId.getText();
        if(editTxt != null){
            outState.putString(PARTICIPANTID, editTxt.toString());
        }
        // TODO editTxt = mTxtYears.getText();
        if(editTxt != null) {
            outState.putString(AGE, editTxt.toString());
        }
        outState.putBoolean(BREASTFED, mIsBreastfed.isChecked());
        if(currentHm != null){
            outState.putParcelable(HOUSEHOLDMEMBER, currentHm);
        }
        outState.putBoolean(IS_FEMALE, mRbFemale.isChecked());

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }


}
