package au.edu.newcastle.jnc985.visida.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import au.edu.newcastle.jnc985.visida.BuildConfig;
import au.edu.newcastle.jnc985.visida.R;
import bo.EatingOccasionRepository;
import bo.FoodItemRepository;
import bo.FoodRecordRepository;
import bo.NotificationRepository;
import bo.RecipeRepository;
import bo.Utilities;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.IReviewable;
import bo.db.entity.Recipe;
import bo.db.entity.ReviewNotification;
import notification.AlarmController;
import ui.HelpAudioButtonController;
import ui.RecordReviewAdapter;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.AUDIOONLY_FOODITEM_FILE_NAME_TEMPLATE;
import static bo.AppConstants.AUDIO_ONLY_FRAGMENT_TAG;
import static bo.AppConstants.EOID;
import static bo.AppConstants.FOODITEM_DESCRIPTION_MAX_LENGTH;
import static bo.AppConstants.FRID;
import static bo.AppConstants.NAVBAR;
import static bo.AppConstants.PPID;

public class RecordReviewActivity extends AppCompatActivity implements View.OnClickListener, AudioRecordingFragment.AudioRecorderHandler {


    private static final String TAG = "RecordReviewActivity";
    private NavigationBarFragment mNavBar;

    private RecordReviewAdapter mAdapter;
    private long mFrId;
    private FoodRecord mFr;

    //Eating Occasion to contain all of the Audio Only Records
    private EatingOccasion mEo;

    /**
     * Controller for Help Button
     */
    private HelpAudioButtonController helpAudioButtonController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Run this at the start of every activity
        Utilities.updateLanguage(getBaseContext());
        setTitle(R.string.title_review_day);
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Record Review Activity Created");
        if(BuildConfig.forceKhmer || BuildConfig.forceSwahili) {
            setContentView(R.layout.activity_record_review_audio);
        }
        else{
            setContentView(R.layout.activity_record_review);
        }

        FragmentManager fm = getSupportFragmentManager();
        mNavBar = (NavigationBarFragment) fm.findFragmentByTag(NAVBAR);

        if(mNavBar == null) {
            //Load the fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, NavigationBarFragment.newInstance(), NAVBAR)
                    .commit();
        }

        Button btnYes = findViewById(R.id.btnYes);
        btnYes.setOnClickListener(this);
        Button btnNo = findViewById(R.id.btnNo);
        btnNo.setOnClickListener(this);


        if(savedInstanceState != null){
            mFrId = savedInstanceState.getLong(FRID);
            long eoId = savedInstanceState.getLong(FRID);
            mEo = new EatingOccasionRepository(getApplication()).getEatingOccasion(eoId);
        }
        else {
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            if (extras != null) {
                //Try to get food record form FrId if provided
                long frId = extras.getLong(FRID, -1);
                if(frId <= 0){
                    String ppid = extras.getString(PPID);
                    mFr = new FoodRecordRepository(getApplication()).getFoodRecordFromPpId(ppid);
                    mFrId = mFr.getId();
                }
                else{
                    mFr = new FoodRecordRepository(getApplication()).getFoodRecord(frId);
                    mFrId = mFr.getId();
                }

            }
        }

        System.out.println("Food Record ID: " + mFr.getFoodRecordId());
        if(mFr != null){
            TextView txtHmName = findViewById(R.id.txtHmName);
            txtHmName.setText(mFr.getHouseholdMember().getName());

            List<EatingOccasion> eos = mFr.getEatingOccasions();
            //Delete any empty Eating Occasions
            EatingOccasionRepository eoRepo = new EatingOccasionRepository(getApplication());
            //Iterate through the EO's and delete them if they dont contain any food items
            Iterator<EatingOccasion> i = eos.iterator();
            while (i.hasNext()) {
                EatingOccasion eo = i.next();
                if(eo.getFoodItems() == null || eo.getFoodItems().size() == 0){
                    eoRepo.delete(eo);
                    i.remove();
                }
            }
            List<IReviewable> reviewables = (List<IReviewable>)(List<?>) eos;
            mAdapter = new RecordReviewAdapter(reviewables);

            RecyclerView list = (RecyclerView) findViewById(R.id.rvEatincOccasionsList);
            list.setLayoutManager(new LinearLayoutManager(this));
            list.setAdapter(mAdapter);

            lockRecipes();
        }
        else {
            //There was an error return to main activity
            Log.i(ACTIVITY_LOG_TAG, TAG + ": Error with FRID " + mFrId);
            startActivity(new Intent(this, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
        setHelpAudioButtonController(new HelpAudioButtonController(this, findViewById(R.id.imgHelp), R.raw.ab_rec_10));

    }

    public void setHelpAudioButtonController(HelpAudioButtonController controller){
        this.helpAudioButtonController = controller;
    }

    private void removeNotification(long mFrId, String ppid) {
        //Update the Notification to seen to remove the red umber on Main Activity
        NotificationRepository notificationRepo = new NotificationRepository(getApplication());
        List<ReviewNotification> rns = notificationRepo.getNotificationsForTodayAndPpId(ppid);
        AlarmController alarmController = new AlarmController(getApplication());
        if(rns.size() > 0) {
            for (ReviewNotification rn : rns) {
                if (!rn.isSeen()) {
                    rn.setSeen(true);
                    notificationRepo.update(rn);
                }
                alarmController.cancelRecordReviewNotification(rn.getNotificationId());
                Log.i(ACTIVITY_LOG_TAG, TAG + ": Review Notification cancelled id " + mFrId);
            }
        }
        else{
            Log.i(ACTIVITY_LOG_TAG, TAG + ": Reviewing day using an old review notification FoodRecordId " + mFrId + " ppid " + ppid);
            //Must be using an old review notification so get all and check
            rns = notificationRepo.getNotificationsForPpId(ppid);
            for (ReviewNotification rn : rns) {
                if (!rn.isSeen()) {
                    rn.setSeen(true);
                    notificationRepo.update(rn);
                }
                alarmController.cancelRecordReviewNotification(rn.getNotificationId());
                Log.i(ACTIVITY_LOG_TAG, TAG + ": Old Review Notification cancelled id " + mFrId);
            }

        }
        notificationRepo.setFoodRecordsEatingOccasionRemindersToSeen(mFrId);
    }

    //Updates the unlocked recipes and locks them so they cannot be edited
    private void lockRecipes() {
        RecipeRepository rRepo = new RecipeRepository(getApplication());
        List<Recipe> unlockedRecipes = rRepo.getAllUnlocked();
        for (Recipe r : unlockedRecipes) {
            r.setLocked(true);
        }
        rRepo.update(unlockedRecipes);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(FRID, mFrId);
        if(mEo != null){
            outState.putLong(EOID, mEo.getEatingOccasionId());
        }
    }

    @Override
    public void onClick(View v) {
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked " + getResources().getResourceEntryName(v.getId()));
        switch (v.getId()){
            case R.id.btnYes:
                captureAudioOnlyRecord();
                break;
            case R.id.btnNo:
                finalizeFoodRecord();
                break;
        }
    }

    private void captureAudioOnlyRecord() {
        //Create Eating Occasion for the FoodRecord to hold the audio only food Items
        //Eating Occasion will already be finalized since we are in review state.
        if(mEo == null) {
            mEo = new EatingOccasion();
            mEo.setFoodRecordId(mFrId);
            mEo.setFinalized(true);
        }

        //Add the eating occasion to the database (and set its id)
        EatingOccasionRepository eoRepo = new EatingOccasionRepository(getApplication());
        mEo.setEatingOccasionId(eoRepo.addEatingOccasion(mEo));

        SimpleDateFormat simpleDateFormat = Utilities.DATE_FORMAT;
        String timestamp = simpleDateFormat.format(new Date());

        //Get all of the values for the image name
        String hhId = mFr.getHouseholdMember().getParticipantHouseholdId();
        String ppId = mFr.getHouseholdMember().getParticipantHouseholdMemberId();
        long frId = mFr.getFoodRecordId();

        Locale currentLocal = getResources().getConfiguration().getLocales().get(0);
        //%s_%d_%d_%d_%s_AUDIOONLY.mp3
        String fileName = String.format(currentLocal, AUDIOONLY_FOODITEM_FILE_NAME_TEMPLATE, hhId, ppId, frId,mEo.getEatingOccasionId(), timestamp);
        File outputFile = new File(Utilities.getMediaDirectory(this), fileName);
        //Open up Audio recording fragment
        boolean allowText = !(BuildConfig.forceKhmer || BuildConfig.forceSwahili);
        AudioRecordingFragment frag;
        if(!allowText) {
            frag = AudioRecordingFragment.newInstance(outputFile.getAbsolutePath(), R.raw.ab_rec_41, true, allowText, FOODITEM_DESCRIPTION_MAX_LENGTH);
        }
        else{
            frag = AudioRecordingFragment.newInstance(outputFile.getAbsolutePath(), true, allowText, FOODITEM_DESCRIPTION_MAX_LENGTH);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .add(frag, AUDIO_ONLY_FRAGMENT_TAG)
                .commit();
    }

    private void finalizeFoodRecord() {
        //Loop through the eating occasions in the food record and make sure they are
        FoodRecordRepository frRepo = new FoodRecordRepository(getApplication());
        frRepo.finalizeReviewed(mFrId);
        //TODO Ask to confirm they are done

        //Find the record review notification and set to seen HERE
        removeNotification(mFrId, mFr.getHouseholdMember().getParticipantHouseholdMemberId());

        finish();
    }

    @Override
    public void onRecordComplete(File outputFile) {
        dismissFragment();

        //Create the Audio Only entity add to database
        FoodItem fi = new FoodItem();
        fi.setAudioUrls(outputFile.getName());
        fi.setEatingOccasionId(mEo.getEatingOccasionId());
        fi.setFinalized(true);

        FoodItemRepository fiRepo = new FoodItemRepository(getApplication());
        fiRepo.addFoodItem(fi);

        //Add the food Item to the Eating Occaison
        mEo.addFoodItem(fi);
        //Add the Eating Occasion to the Adapter
        mAdapter.addRecord(mEo);

        //Update the File Name nwo we have a FIID
        Utilities.renameMediaFile(this, outputFile.getName(), fi.getAudioName());
    }

    private void dismissFragment() {
        //Dismiss fragment
        FragmentManager fm = getSupportFragmentManager();
        Fragment f = fm.findFragmentByTag(AUDIO_ONLY_FRAGMENT_TAG);
        if (f != null) {
            fm.beginTransaction()
                    .remove(f)
                    .commit();
        }
    }

    @Override
    public void onTextComplete(String textDescription) {
        dismissFragment();

        //Create the Ttext Only entity add to database
        FoodItem fi = new FoodItem();
        fi.setDescription(textDescription);
        fi.setEatingOccasionId(mEo.getEatingOccasionId());

        FoodItemRepository fiRepo = new FoodItemRepository(getApplication());
        fiRepo.addFoodItem(fi);

        //Add the food Item to the Eating Occaison
        mEo.addFoodItem(fi);
        //Add the Eating Occasion to the Adapter
        mAdapter.addRecord(mEo);
    }
}
