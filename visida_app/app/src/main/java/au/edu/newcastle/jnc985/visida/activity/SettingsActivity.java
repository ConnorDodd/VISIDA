package au.edu.newcastle.jnc985.visida.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.test.espresso.idling.CountingIdlingResource;

import au.edu.newcastle.jnc985.visida.BuildConfig;
import au.edu.newcastle.jnc985.visida.R;
import bo.AppConstants;
import bo.DataExporter;
import bo.Utilities;
import recordverification.RecordVerificationActivity;
import upload.UploadCallback;
import upload.UploadFragment;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.NAVBAR;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, DataExporter.DataExporterCallbackHandler, UploadCallback {

    private static final int WRITE_STORAGE_PERMISSION_EXPORT = 1;
    private static final String TAG = "SettingsActivity";
    private NavigationBarFragment mNavBar;



    private UploadFragment mUploadFragment;
    private boolean uploading = false;
    private boolean needToUpload;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Settings Activity Created");
        // Run this at the start of every activity
        Utilities.updateLanguage(getBaseContext());
        setTitle(R.string.title_settings);
        setContentView(R.layout.activity_settings);

        FragmentManager fm = getSupportFragmentManager();
        mNavBar = (NavigationBarFragment) fm.findFragmentByTag(NAVBAR);

        if(mNavBar == null) {
            //Load the fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, NavigationBarFragment.newInstance(), NAVBAR)
                    .commit();
        }

        Button btnSetup = findViewById(R.id.btnSetupHousehold);
        btnSetup.setOnClickListener(this);
        Button btnExportData = findViewById(R.id.btnExportData);
        btnExportData.setOnClickListener(this);
        Button btnVerify = findViewById(R.id.btnVerify);
        btnVerify.setOnClickListener(this);
        Button btnUpload;
        btnUpload = findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(this);


        Button btnAudioOnly = findViewById(R.id.btnAbout);
        btnAudioOnly.setOnClickListener(this);

        //Ask for pin
        if(BuildConfig.releasebuild) {
            checkPin();
        }
        //if(BuildConfig.forceSwahili){
          mUploadFragment = UploadFragment.getInstance(getSupportFragmentManager(), "https://visida-api-us.azurewebsites.net/api/Portion");
        //}
    }

    private void checkPin() {
        //Open a dialog for a pin number and check it matches
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.fragment_pin_dialog, null);
        final EditText txtPin = view.findViewById(R.id.txtPin);
        builder.setTitle(R.string.enter_pin)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Check the value is correct
                        String pinInput = txtPin.getText().toString();
                        String expectedPin = getResources().getString(R.string.pin_value);
                        if (!pinInput.equals(expectedPin)) {
                            returnToMain();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        returnToMain();
                    }
                })
                .setCancelable(false)
                .setView(view).show();


    }

    private void returnToMain(){
        //if not return to main screen
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    @Override
    public void onClick(View v) {
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked " + getResources().getResourceEntryName(v.getId()));
        switch(v.getId()){
            case R.id.btnSetupHousehold:
                Intent intent = new Intent(this, SetupHouseholdActivity.class);
                startActivity(intent);
                break;

            case R.id.btnExportData:
                exportData();
                break;

            case R.id.btnAbout: {
                displayInformation();
                break;
            }
            case R.id.btnVerify:
                //Move to verification
                Intent i = new Intent(this, RecordVerificationActivity.class);
                startActivity(i);
                break;
            case R.id.btnUpload:
                uploadData();
                break;
        }
    }

    /***
     * Exports the data but also sets a flag which is read in
     * {@link SettingsActivity#onExportComplete} to see if we
     * need to upload the files aswell.
     */
    private void uploadData() {
        System.out.println("Exporting Files");
        exportData();
        needToUpload = true;
    }

    private void displayInformation() {
        //"Icon made by Freepik from www.flaticon.com"
        //https://icons8.com/
        String aboutMsg = "Icon made by Freepik from www.flaticon.com \nand \nwww.icons8.com\n";
        aboutMsg += "Version Name: " + Utilities.getVersionName(getApplication()) + "\n";
        aboutMsg += "Version Number: " + BuildConfig.versionId + "\n";
        new AlertDialog.Builder(SettingsActivity.this)
                .setTitle(R.string.about)
                .setMessage(aboutMsg)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void exportData() {
        //Check permission to write
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_STORAGE_PERMISSION_EXPORT);
        }
        else {
            DataExporter de = new DataExporter(getApplication(), new CountingIdlingResource(AppConstants.DATAEXPORTER_RESOURCE_NAME),this);
            de.exportData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_STORAGE_PERMISSION_EXPORT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    exportData();
                } else {
                    // permission denied
                }
            }
        }
    }

    @Override
    public void onExportComplete() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SettingsActivity.this, "Data Export Complete", Toast.LENGTH_SHORT).show();
                if(needToUpload){
                    mUploadFragment.startUpload();
                    needToUpload = false;
                }
            }
        });
    }

    @Override
    public void updateFromUpload(Object result) {
        System.out.println("Update UI Stuff {Progress bar or something)");
        if(result == null){
            System.out.println("UPDATE RESUT IS NULL PROBS NO CONNECTION OR NOT WIFI");
        }
        else{
            System.out.println("Result: " + result);
        }
    }

    @Override
    public NetworkCapabilities getActiveNetworkCapabilities() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkInfo = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        return networkInfo;
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        switch(progressCode) {
            // You can add UI behavior for progress updates here.
            case UploadCallback.Progress.ERROR:
                break;
            case UploadCallback.Progress.CONNECT_SUCCESS:
                System.out.println("CONENCT SUCCESS IN UPDATE");
                break;
            case UploadCallback.Progress.GET_INPUT_STREAM_SUCCESS:
                System.out.println("GET INPUT SREAM SUCCESS IN UPDATE");
                break;
            case UploadCallback.Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                break;
            case UploadCallback.Progress.PROCESS_INPUT_STREAM_SUCCESS:
                break;
        }
    }

    @Override
    public void finishUploading() {

    }
}
