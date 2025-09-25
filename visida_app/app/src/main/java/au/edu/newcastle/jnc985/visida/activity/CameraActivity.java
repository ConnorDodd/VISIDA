/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package au.edu.newcastle.jnc985.visida.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import java.io.File;

import au.edu.newcastle.jnc985.visida.R;
import bo.State;
import bo.Utilities;
import ui.ImagePreviewDialogFragment;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.IMAGE_NAME;
import static bo.AppConstants.IMAGE_NAME_FORMAT;
import static bo.AppConstants.INGREDIENT_IMAGE_NAME_TEMPLATE;
import static bo.AppConstants.NAVBAR;
import static bo.AppConstants.SHARED_DISH;
import static bo.AppConstants.SHARED_DISH_IMAGE_NAME_TEMPLATE;

/**
 * Camera Activity. Houses a {@link android.view.SurfaceView} in the form of
 * {@link TakePhotoFragment} to preview the camera. Has a single take image button.
 *
 * This Activity is able to read the following fields from the {@link Intent#getExtras()}
 * {@value bo.AppConstants#SHARED_DISH}: Flag whether the Eat image being captured is shared or individual
 * this flag changes the behaviour of the {@link CameraActivity#getImageName()} method.
 * {@value #ALLOW_NO_IMAGE}: Flag if the activity allows the user to skip the image and return
 * a placeholder for the image name.
 *
 * After taking the image the filename of the image is returned by this activity in the
 * result intent extras under {@link bo.AppConstants#IMAGE_NAME}.
 */
public class CameraActivity extends AppCompatActivity implements ImagePreviewDialogFragment.OnImagePreviewFragmentClickListener{

    public static final String ALLOW_NO_IMAGE = "allownoimage";
    private static final String TAKEPHOTOFRAGMENTTAG = "takephotofragment";
    private static final String TAG = "cameraactivity";
    private NavigationBarFragment mNavBar;
    private TakePhotoFragment mTakePhotoFragment;

    private boolean mSharedDish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Camera Activity Created");
        // Run this at the start of every activity
        Utilities.updateLanguage(getBaseContext());
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        //There is an issue when the camera is allowed to go to sleep. So for now just keep the camera awake.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        FragmentManager fm = getSupportFragmentManager();
        mNavBar = (NavigationBarFragment) fm.findFragmentByTag(NAVBAR);

        if(mNavBar == null) {
            //Load the fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, NavigationBarFragment.newInstance(), NAVBAR)
                    .commit();
        }

        boolean allowNoImageButton = false;
        //Check the shared preferences to see if we are in EAT or Finalize
        if (null == savedInstanceState) {
            //Get the householdmember sent from the list
            Intent i = getIntent();
            Bundle extras = i.getExtras();
            if(extras != null) {
                mSharedDish = extras.getBoolean(SHARED_DISH, false);
                allowNoImageButton = extras.getBoolean(ALLOW_NO_IMAGE, false);
            }
        }
        else{
            this.mSharedDish = savedInstanceState.getBoolean(SHARED_DISH, false);
        }

        Button btnNoImage = findViewById(R.id.btnNoPhoto);
        btnNoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Call positive click with a dummy image name
                onPositiveClick(getResources().getString(R.string.NO_PHOTO));
            }
        });
        if(!allowNoImageButton) {
            //Hide the no image button
            btnNoImage.setVisibility(View.GONE);
        }
    }

    private String getImageName() {
        State currentState = Utilities.getState(this);
        switch(currentState){
            case EAT:
            case FINALIZE:
            {
                if(mSharedDish){
                    return SHARED_DISH_IMAGE_NAME_TEMPLATE;
                }
                else{
                    return IMAGE_NAME_FORMAT;
                }
            }
            case COOK:{
                return INGREDIENT_IMAGE_NAME_TEMPLATE;
            }
            case MEAL:
                return SHARED_DISH_IMAGE_NAME_TEMPLATE;
        }
        return null;
    }

    @Override
    public void onResume(){
        super.onResume();

        //We want to completely replace the Camera Fragment here because there is an issue with the capture session
        //when the phone is locked/slept then awoken again. By replacing the fragment here we circumvent the issue
        //is returning the device to the correct state.
        FragmentManager fm = getSupportFragmentManager();
        String imageName = getImageName();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.cameraContainer, TakePhotoFragment.newInstance(imageName), TAKEPHOTOFRAGMENTTAG)
                .commit();
        getSupportFragmentManager().executePendingTransactions();
        mTakePhotoFragment = (TakePhotoFragment) fm.findFragmentByTag(TAKEPHOTOFRAGMENTTAG);
    }

    @Override
    protected void onSaveInstanceState(Bundle state){
        super.onSaveInstanceState(state);
        state.putBoolean(SHARED_DISH, mSharedDish);
    }

    @Override
    public void onPositiveClick(String imageName) {
        State currentState = Utilities.getState(this);
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Accepted Image in STATE " + currentState + " name " + imageName);

        //Return the image
        Intent result = new Intent();
        result.putExtra(IMAGE_NAME, imageName);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    @Override
    public void onNegativeClick(String imageName) {
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Declined Image");
        mTakePhotoFragment.setIsDialogOpen(false);
        File imageFile = new File(Utilities.getMediaDirectory(this), imageName);
        imageFile.delete();
    }
}
