package au.edu.newcastle.jnc985.visida.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import au.edu.newcastle.jnc985.visida.R;
import bo.EatingOccasionRepository;
import bo.Utilities;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodRecord;
import ui.HelpAudioButtonController;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.FR;
import static bo.AppConstants.NAVBAR;

public class BreastfeedActivity extends AppCompatActivity {
    private static final String TAG = "breastfeedActivity";

    private NavigationBarFragment mNavBar;

    private FoodRecord mFr;

    private HelpAudioButtonController helpAudioButtonController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Breastfeeding Activity Created");
        // Run this at the start of every activity
        Utilities.updateLanguage(getBaseContext());
        setContentView(R.layout.activity_breastfeed);

        //Set up title bar
        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText(R.string.title_breastfeed);
        ImageView imgIcon = findViewById(R.id.imgPicture);
        imgIcon.setImageResource(R.drawable.breastfeeding);

        FragmentManager fm = getSupportFragmentManager();
        mNavBar = (NavigationBarFragment) fm.findFragmentByTag(NAVBAR);

        if(mNavBar == null) {
            //Load the fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, NavigationBarFragment.newInstance(), NAVBAR)
                    .commit();
        }

        if(savedInstanceState != null){
            this.mFr = savedInstanceState.getParcelable(FR);
        }
        else {
            Intent i = getIntent();
            Bundle extras = i.getExtras();
            if (extras != null) {
                this.mFr = extras.getParcelable(FR);
            }
        }

        setHelpAudioButtonController(new HelpAudioButtonController(this, findViewById(R.id.imgHelp), R.raw.ab_rec_42));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the state of the activity for when they return.
        outState.putParcelable(FR, mFr);
    }

    public void onClickBreastfeed(View v){
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked submit Breastfeed occasion");
        //Create an eating occasion for breastfeeding
        EatingOccasion breastFeedOccasion = new EatingOccasion();
        breastFeedOccasion.setFoodRecordId(mFr.getFoodRecordId());
        //Breastfeeding Occasions are simply counted so automatically finalise
        breastFeedOccasion.finalise();
        EatingOccasionRepository eoRepo = new EatingOccasionRepository(this.getApplication());
        eoRepo.addEatingOccasion(breastFeedOccasion);

        Toast.makeText(this, R.string.breastfeedcaptured, Toast.LENGTH_SHORT).show();

        //Return to the Main activity
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    public void setHelpAudioButtonController(HelpAudioButtonController controller){
        this.helpAudioButtonController = controller;
    }
}
