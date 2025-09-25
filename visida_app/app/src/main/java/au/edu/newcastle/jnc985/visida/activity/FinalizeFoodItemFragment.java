package au.edu.newcastle.jnc985.visida.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import au.edu.newcastle.jnc985.visida.BuildConfig;
import au.edu.newcastle.jnc985.visida.R;
import bo.EatingOccasionRepository;
import bo.FinalizeFoodItemViewModel;
import bo.FoodItemRepository;
import bo.FoodRecordRepository;
import bo.Utilities;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.GuestInformation;
import ui.AudioButton;
import ui.CounterItemListAdapter;

import static android.app.Activity.RESULT_OK;
import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.ALLOW_TEXT;
import static bo.AppConstants.AUDIOFILE_NAME;
import static bo.AppConstants.AUDIOFILE_NAME_TEMPLATE;
import static bo.AppConstants.EOID;
import static bo.AppConstants.IMAGE_NAME;
import static bo.AppConstants.PARTICIPANTHOUSEHOLDID;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.SHARED_DISH;
import static bo.AppConstants.SHARED_DISH_AUDIOFILE_TEMPLATE;
import static bo.AppConstants.TEXT_DESCRIPTION;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FinalizeFoodItemFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FinalizeFoodItemFragment extends Fragment implements  View.OnClickListener{
    private static final String FOOD_ITEM = "fooditem";
    private static final String TAG = "FinalizeFoodItemFragment";
    private static final int SHARED_LEFTOVER_IMAGE_REQUEST_CODE = 113;
    private static final int SHARED_LEFTOVER_AUDIO_REQUEST_CODE = 114;
    private static final int INDIVIDUAL_LEFTOVER_IMAGE_REQUEST_CODE = 115;
    private static final int INDIVIDUAL_LEFTOVER_AUDIO_REQUEST_CODE = 116;
    private static final String FLAG = "flag";

    private MediaPlayer mMediaPlayer;
    private boolean mIsAudioPaused = false;
    private boolean mMediaPlayerReady = false;

    /*
    Media player for playing the help audio file.
    This field has a setter for testing purposes.
     */
    private MediaPlayer helpMediaPlayer;
    private MediaPlayer helpMediaPlayerGuest;

    /*
    Current food item being finalized.
     */
    private FoodItem mFoodItem;

    /*
    String to hold the image name after taking the photo while we
    record the audio. Once the audio is returned we can use this and
    the returned audio file to save the fooditem.
    */
    private String mImageFileName;
    private String mAudioFileName;

    public FinalizeFoodItemFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param fi The food item to be finlaized
     * @return A new instance of fragment FinalizeFoodItemFragment.
     */
    public static FinalizeFoodItemFragment newInstance(FoodItem fi) {
        FinalizeFoodItemFragment fragment = new FinalizeFoodItemFragment();
        Bundle args = new Bundle();
        args.putParcelable(FOOD_ITEM, fi);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFoodItem = getArguments().getParcelable(FOOD_ITEM);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(BuildConfig.forceKhmer ||BuildConfig.forceSwahili) {
            return inflater.inflate(R.layout.fragment_finalize_food_item_audio, container, false);
        }
        else {
            return inflater.inflate(R.layout.fragment_finalize_food_item, container, false);
        }
    }

    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstance){
        Button mBtnAteAll = view.findViewById(R.id.btnAteAll);
        mBtnAteAll.setOnClickListener(this);
        Button mBtnAteMost = view.findViewById(R.id.btnAteSome);
        mBtnAteMost.setOnClickListener(this);
        Button mBtnDidntEat = view.findViewById(R.id.btnAteNone);
        mBtnDidntEat.setOnClickListener(this);
        ImageView mBtnPlayAudio = view.findViewById(R.id.imgvAudioFile);
        mBtnPlayAudio.setOnClickListener(this);

        //TextView title = view.getRootView().findViewById(R.id.titleText);
        //title.setText(R.string.title_finalize_food_item);
        if(BuildConfig.forceKhmer || BuildConfig.forceSwahili) {
            helpMediaPlayer = MediaPlayer.create(getContext(), R.raw.ab_rec_57);
            setHelpButton(view, helpMediaPlayer);
        }

        //If the food Item comes from a meal
        if(mFoodItem.getMealId() != 0){
            //If there hasnt been any leftovers taken yet
            if (mFoodItem.getLeftoverImageUrl() == null || mFoodItem.getLeftoverImageUrl().isEmpty()) {
                //capture shared dish leftovers
                sharedDishLeftovers();
            }
            else{
                //MealRepository fiREpo = new MealRepository(Objects.requireNonNull(getActivity()).getApplication());
                //Meal m = fiREpo.getMeal(mFoodItem.getMealId());
                //if(!m.isGuestInfoCaptured()){
                captureGuests();
                //}
            }
            //Hide the ate all button
            mBtnAteMost.setVisibility(View.INVISIBLE);
            mBtnAteAll.setText(R.string.ate);
            //Set the titel to add Shared
            Objects.requireNonNull(getActivity()).setTitle(R.string.title_finalize_food_item_shared);
            //Set the Drawables of the buttons
            mBtnAteAll.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_btn_ate, 0, 0);
            mBtnDidntEat.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_btn_didnt_eat, 0, 0);

            //Change the audio file associated with the ate button.
            if(BuildConfig.forceKhmer) {
                AudioButton ateAll = (AudioButton) mBtnAteAll;
                ateAll.setAudioFileResId(R.raw.ab_rec_14);
            }
        }

        //Load the image into the image view
        File newImageFile = getImageFile();
        ImageView imgView = view.findViewById(R.id.imgFoodItem);
        Glide.with(this)
                .load(newImageFile)
                .apply(new RequestOptions()
                .placeholder(R.drawable.ic_thinking))
                .into(imgView);

        ImageView avatar = view.getRootView().findViewById(R.id.imgProfilePic);
        loadAvatar(avatar);
    }

    private void loadAvatar(ImageView img) {
        //Get the household members image
        //Get Household member from food item
        FinalizeFoodItemViewModel vm = new ViewModelProvider(this).get(FinalizeFoodItemViewModel.class);
        File avatarImage = vm.getHouseholdMemberImage(this.mFoodItem);
        Glide.with(this)
                .load(avatarImage)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_default_person))
                .into(img);
    }


    @VisibleForTesting
    public void setHelpMediaPlayer(MediaPlayer mp){
        this.helpMediaPlayer = mp;
    }
    private void sharedDishLeftovers() {
        //prompt the user that they need to take the leftover image.
        //AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        AlertDialog leftoverPopup = new AlertDialog.Builder(Objects.requireNonNull(getContext())).create();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        //noinspection InflateParams
        final View view = inflater.inflate(R.layout.fragment_image_text_option_dialog, null);
        ImageView dialogImgv = view.findViewById(R.id.dialog_imageview);
        TextView txtView = view.findViewById(R.id.dialog_text);
        txtView.setText(R.string.leftover_prompt);

        helpMediaPlayerGuest = MediaPlayer.create(getContext(), R.raw.ab_rec_37);
        setHelpButton(view, helpMediaPlayerGuest);

        //Custom Buttons
        Button btnCancel = view.findViewById(R.id.btnCancel);
        btnCancel.setText(R.string.no_leftovers);
        btnCancel.setOnClickListener(v -> {
            Log.i(ACTIVITY_LOG_TAG, TAG + ": No leftovers");
            stopAudio();
            if(helpMediaPlayerGuest.isPlaying()){
                helpMediaPlayerGuest.stop();
            }
            //Set the string for leftover image to placeholder
            String noAudio = getResources().getString(R.string.NO_AUDIO);
            mImageFileName = getResources().getString(R.string.NO_PHOTO);
            mFoodItem.setLeftoverImageUrl(mImageFileName);
            mFoodItem.setLeftoverAudioUrls(noAudio);
            FoodItemRepository fiRepo = new FoodItemRepository(Objects.requireNonNull(getActivity()).getApplication());
            fiRepo.addMealLeftoverAudio(mFoodItem.getImageName(), mImageFileName, noAudio);
            captureGuests();
            leftoverPopup.dismiss();
        });
        Button btnConfirm = view.findViewById(R.id.btnConfirm);
        btnConfirm.setText(R.string.yes_leftovers);
        btnConfirm.setOnClickListener(v -> {
            Log.i(ACTIVITY_LOG_TAG, TAG + ": Yes leftovers");
            stopAudio();
            //Get the leftover image and audio
            Intent imageIntent = new Intent(FinalizeFoodItemFragment.this.getContext(), CameraActivity.class);
            //imageIntent.putExtra(ALLOW_NO_IMAGE, true);
            imageIntent.putExtra(SHARED_DISH, true);
            //Dismiss the pop up
            startActivityForResult(imageIntent, SHARED_LEFTOVER_IMAGE_REQUEST_CODE);
            leftoverPopup.dismiss();
        });


        File imageFile = getImageFile();
        Glide.with(this)
                .load(imageFile)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_food_placeholder_100))
                .into(dialogImgv);
        leftoverPopup.setView(view);
        leftoverPopup.setCancelable(false);
        leftoverPopup.show();


    }

    public File getImageFile(){
        File hmDirectory = Utilities.getMediaDirectory(getContext());
        return new File(hmDirectory, mFoodItem.getImageUrl());
    }

    /**
     * Move to the {@link CameraActivity} to take an image of the leftovers.
     */
    private void captureLeftovers() {
        //Start the Camera Activity to capture the image.
        Intent i = new Intent(getContext(), CameraActivity.class);
        startActivityForResult(i, INDIVIDUAL_LEFTOVER_IMAGE_REQUEST_CODE);
    }

    /**
     * Handle the result form either the Camera or Audio activities.
     * The result could be from a shared or individual dish.
     * @param requestCode Request code of the activity started for result
     * @param resultCode Result code of the returning activity
     * @param data Intent containing the result data
     */
    @SuppressLint("DefaultLocale")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String formattedFileName = "";
        String originalFileName = "";
        if(SHARED_LEFTOVER_IMAGE_REQUEST_CODE == requestCode){
            if(RESULT_OK == resultCode){
                Bundle extras = data.getExtras();
                originalFileName = mImageFileName = Objects.requireNonNull(extras).getString(IMAGE_NAME);

                //Format the image name
                SharedPreferences sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                String hhid = sharedPreferences.getString(PARTICIPANTHOUSEHOLDID, "");
                String timestamp = Utilities.DATE_FORMAT.format(new Date());
                formattedFileName = String.format(mImageFileName, hhid, mFoodItem.getMealId(), timestamp);
                mImageFileName = formattedFileName;
                mFoodItem.setLeftoverImageUrl(mImageFileName);

                //Request the audio
                mAudioFileName = String.format(SHARED_DISH_AUDIOFILE_TEMPLATE, hhid, mFoodItem.getMealId(), timestamp);
                Intent i = new Intent(this.getContext(), AudioActivity.class);
                i.putExtra(IMAGE_NAME, mImageFileName);
                i.putExtra(AUDIOFILE_NAME, mAudioFileName);
                i.putExtra(SHARED_DISH, true);
                boolean allowText = !(BuildConfig.forceKhmer || BuildConfig.forceSwahili);
                i.putExtra(ALLOW_TEXT, allowText);
                startActivityForResult(i, SHARED_LEFTOVER_AUDIO_REQUEST_CODE);
            }
            else{
                //Set the image URL to a flag to show that the result of taking
                //the image was not OK. The flag is checked in the onResume method.
                mFoodItem.setLeftoverImageUrl(FLAG);
            }
        }
        else if(SHARED_LEFTOVER_AUDIO_REQUEST_CODE == requestCode){
            if(RESULT_OK == resultCode){
                //Save the audio name
                Bundle extras = data.getExtras();
                originalFileName = Objects.requireNonNull(extras).getString(AUDIOFILE_NAME);
                if(originalFileName != null && !originalFileName.isEmpty()) {
                    String timestamp = Utilities.DATE_FORMAT.format(new Date());

                    formattedFileName = String.format(originalFileName, mFoodItem.getMealId(), timestamp);
                    mAudioFileName = formattedFileName;
                    mFoodItem.setLeftoverAudioUrls(formattedFileName);

                    //Save the food item.
                    FoodItemRepository fiRepo = new FoodItemRepository(Objects.requireNonNull(getActivity()).getApplication());
                    fiRepo.addMealLeftoverAudio(mFoodItem.getImageName(), mImageFileName, formattedFileName);

                }
                else{
                    String leftOverDescription = extras.getString(TEXT_DESCRIPTION);
                    mFoodItem.setLeftoverDescription(leftOverDescription);
                    //Save the food item.
                    FoodItemRepository fiRepo = new FoodItemRepository(Objects.requireNonNull(getActivity()).getApplication());
                    fiRepo.addMealLeftoverText(mFoodItem.getImageName(), mImageFileName, leftOverDescription);
                }
                captureGuests();
            }
            else{
                //Get the file name
                Utilities.deleteMediaFile(getContext(), mAudioFileName);
                Utilities.deleteMediaFile(getContext(), mImageFileName);
                mFoodItem.setLeftoverImageUrl(FLAG);
            }
        }
        else if (INDIVIDUAL_LEFTOVER_IMAGE_REQUEST_CODE == requestCode) {
            if (RESULT_OK == resultCode) {
                Bundle extras = data.getExtras();
                originalFileName = Objects.requireNonNull(extras).getString(IMAGE_NAME);

                //Format the image name
                SimpleDateFormat simpleDateFormat = Utilities.DATE_FORMAT;
                String timestamp = simpleDateFormat.format(new Date());

                //Get the sending foodRecord
                FoodRecord fr = getFoodRecord();
                String hhId = Objects.requireNonNull(fr).getHouseholdMember().getParticipantHouseholdId();
                String ppId = fr.getHouseholdMember().getParticipantHouseholdMemberId();
                long frId = fr.getFoodRecordId();
                long eoId = mFoodItem.getEatingOccasionId();

                //Set the name of the image (Use English local since data will be consumed in English)
                assert originalFileName != null;
                formattedFileName = String.format(Locale.ENGLISH, originalFileName, hhId, ppId, frId, eoId, mFoodItem.getFoodItemId(), timestamp);
                mImageFileName = formattedFileName;
                mFoodItem.setLeftoverImageUrl(mImageFileName);


                //Create the audio file name.
                timestamp = simpleDateFormat.format(new Date());
                mAudioFileName = String.format(Locale.ENGLISH, AUDIOFILE_NAME_TEMPLATE, hhId, ppId, frId, eoId, mFoodItem.getFoodItemId(), timestamp);

                //Request the audio
                Intent i = new Intent(this.getContext(), AudioActivity.class);
                i.putExtra(IMAGE_NAME, formattedFileName);
                i.putExtra(AUDIOFILE_NAME, mAudioFileName);
                boolean allowText = !(BuildConfig.forceKhmer || BuildConfig.forceSwahili);
                i.putExtra(ALLOW_TEXT, allowText);
                startActivityForResult(i, INDIVIDUAL_LEFTOVER_AUDIO_REQUEST_CODE);
            }
        }
        else if (INDIVIDUAL_LEFTOVER_AUDIO_REQUEST_CODE == requestCode) {
            if (RESULT_OK == resultCode) {
                Bundle extras = data.getExtras();
                originalFileName = extras.getString(AUDIOFILE_NAME);

                if(originalFileName != null && !originalFileName.isEmpty()) {
                    //Store the leftover Audio Url in the food item
                    mFoodItem.setLeftoverAudioUrls(mAudioFileName);
                }
                else{
                    //Store the leftover text description in the food item
                    String description = extras.getString(TEXT_DESCRIPTION);
                    mFoodItem.setLeftoverDescription(description);
                }
                mFoodItem.finalizeItem();
                saveFoodItem(mFoodItem);
                fragmentReturn();
            }
            else{
                //Delete the image file
                Utilities.deleteMediaFile(getContext(), mImageFileName);
                Utilities.deleteMediaFile(getContext(), mAudioFileName);
                mFoodItem.setLeftoverImageUrl(null);
            }
        }
        //Rename the media file.
        Utilities.renameMediaFile(getContext(), originalFileName, formattedFileName);
    }

    private void setHelpButton(View view, MediaPlayer player){
        //If Khmer version add the help buttons
        if(BuildConfig.forceKhmer || BuildConfig.forceSwahili) {
            //TODO Add actual Audio Clip for instructions
            ImageView imgHelp = view.findViewById(R.id.imgHelp);
            imgHelp.setVisibility(View.VISIBLE);


            //Set the Help button to play the audio file
            MediaPlayer finalPlayer = player;
            imgHelp.setOnClickListener(v -> {
                if (finalPlayer.isPlaying()) {
                    //Stop playing. Inorder to continue playing on subsequent presses
                    //We need to prepare the player. See State Diagram (https://developer.android.com/reference/android/media/MediaPlayer)
                    try {
                        Log.i(ACTIVITY_LOG_TAG, TAG + ": Help audio button Stopped");
                        finalPlayer.stop();
                        finalPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.i(ACTIVITY_LOG_TAG, TAG + ": Help audio button Started");
                    finalPlayer.start();
                }
            });
        }
    }

    private void captureGuests() {
        /*
        MealViewModel mealViewModel = ViewModelProviders.of(FinalizeFoodItemFragment.this,
                new MealViewModelFactory(Objects.requireNonNull(FinalizeFoodItemFragment.this.getActivity()).getApplication(), mFoodItem.getMealId())).get(MealViewModel.class);
        */
        //Only continue if we haven't already captured the guest information for htis meal
        //if(mealViewModel.hasGuestInfo()) return;
        if(mFoodItem.hasGuestInfo()) return;

        //Open the dialog for the geusts
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.fragment_recyclerview_dialog, null);

        helpMediaPlayerGuest = MediaPlayer.create(getContext(), R.raw.ab_rec_38);
        setHelpButton(v, helpMediaPlayerGuest);

        RecyclerView recyclerView = v.findViewById(R.id.rvCheckBoxList);

        //Create adapter
        String adultMale = getResources().getString(R.string.adultmale);
        String adultFemale = getResources().getString(R.string.adultfemale);
        String child = getResources().getString(R.string.child);

        String[] guestLabels = new String[]{adultMale, adultFemale, child};
        int maleIcon = R.drawable.ic_adultmale;
        int femaleIcon = R.drawable.ic_adultfemale;
        int childIcon = R.drawable.ic_childicon;
        Integer[] icons = new Integer[]{maleIcon, femaleIcon, childIcon};

        CounterItemListAdapter adapter = new CounterItemListAdapter(Arrays.asList(guestLabels), Arrays.asList(icons));
        recyclerView.setAdapter(adapter);

        //Create layout manager
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);

        builder.setPositiveButton(R.string.tick, (dialog, which) -> {
            if(helpMediaPlayerGuest.isPlaying()){
                helpMediaPlayerGuest.stop();
            }
            Log.i(ACTIVITY_LOG_TAG, TAG + ": Guest info captured");
            //Load the foodItems meal into a meal view model
            //mealViewModel.setGeusts(adapter.getmItemCounts());
            int[] counts = adapter.getmItemCounts();
            int male = counts[0];
            int female = counts[1];
            int childCount = counts[2];
            FoodItemRepository fiRepo = new FoodItemRepository(getActivity().getApplication());
            GuestInformation gi = new GuestInformation(male, female, childCount);
            gi.setGeustInfoId(fiRepo.addNewGuestInfo(gi));
            mFoodItem.setGuestInfoId(gi.getGeustInfoId());

            fiRepo.updateFoodItem(mFoodItem);
            fiRepo.addGuestInfo(mFoodItem.getBaseFoodItemId(), gi);
            Log.i(ACTIVITY_LOG_TAG, TAG + ": Guest info added to food item");
        })
        .setCancelable(false)
        .setView(v)
        .show();


    }

    private void saveFoodItem(FoodItem fi) {
        //Add the foodItem to the database
        try {
            FoodItemRepository fiRepo = new FoodItemRepository(Objects.requireNonNull(getActivity()).getApplication());
            //Adding here will replace and FIID_PLACEHOLDERS in the image urls.
            fiRepo.addFoodItem(fi);
            //Notify the user the record has been saved
            Toast.makeText(getContext(), R.string.food_item_added, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            //TODO Proper Error handling
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked " + getResources().getResourceEntryName(view.getId()));
        switch (view.getId()) {
            case R.id.btnAteAll: {
                finalizeFoodItem();
                fragmentReturn();
                break;
            }
            case R.id.btnAteSome:{
                captureLeftovers();
                break;
            }
            case R.id.btnAteNone:{
                updateFoodItem();
                fragmentReturn();
                break;
            }
            case R.id.imgvAudioFile:{
                playAudio();
                break;
            }
        }
    }

    private void stopAudio(){
        if(mMediaPlayer != null) {
            Log.i(ACTIVITY_LOG_TAG, TAG + ": Stop Audio");
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mIsAudioPaused = false;
            mMediaPlayerReady = false;
            Drawable icon = Objects.requireNonNull(getContext()).getDrawable(R.drawable.ic_btn_play);
            ((ImageView) Objects.requireNonNull(this.getView()).findViewById(R.id.imgvAudioFile)).setImageDrawable(icon);
        }
    }

    private void playAudio() {
        //Set up the Media player with the file if it hasn't been set up yet.
        if(!mMediaPlayerReady) {
            mMediaPlayer = new MediaPlayer();
            try {
                String audioFileName = mFoodItem.getAudioName();
                if(audioFileName != null) {
                    File dataSource = new File(Utilities.getMediaDirectory(getContext()), audioFileName);
                    mMediaPlayer.setDataSource(dataSource.getAbsolutePath());
                    mMediaPlayer.prepare();
                }
                else{
                    //No Audio for this food item. Display toast with the Description.
                    //Toast.makeText(getContext(), getString(R.string.toast_food_item_description) + mFoodItem.getDescription(), Toast.LENGTH_SHORT).show();
                }
            } catch (IOException ex) {
                Log.e("TAG", ex.getMessage());
            }
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopAudio();
                }
            });
            mIsAudioPaused = true;
            mMediaPlayerReady = true;
        }

        //Start or pause th eplayer accoridng to the state. Update the image
        //in the image view to show the correct button.
        if(mIsAudioPaused){
            Log.i(ACTIVITY_LOG_TAG, TAG + ": Playing Audio");
            mMediaPlayer.start();
            Drawable icon = Objects.requireNonNull(getContext()).getDrawable(R.drawable.ic_btn_replay);
            ((ImageView) Objects.requireNonNull(this.getView()).findViewById(R.id.imgvAudioFile)).setImageDrawable(icon);
            mIsAudioPaused = false;
        }
        else{
            Log.i(ACTIVITY_LOG_TAG, TAG + ": Pausing Audio");
            mMediaPlayer.pause();
            Drawable icon = Objects.requireNonNull(getContext()).getDrawable(R.drawable.ic_btn_play);
            ((ImageView) Objects.requireNonNull(this.getView()).findViewById(R.id.imgvAudioFile)).setImageDrawable(icon);
            mIsAudioPaused = true;
        }
    }

    /**
     * Gets the Food Record for the class mFoodItem
     * @return FoodRecord the Current food item belongs to
     */
    private FoodRecord getFoodRecord() {
        long eoId = mFoodItem.getEatingOccasionId();
        //Get the eating occasion this foodItem belongs to
        EatingOccasionRepository eoRepo = new EatingOccasionRepository(Objects.requireNonNull(getActivity()).getApplication());
        EatingOccasion eo = eoRepo.getEatingOccasion(eoId);
        //Get the FoodRecord for this eating occasion
        FoodRecordRepository frRepo = new FoodRecordRepository(getActivity().getApplication());
        try {
            FoodRecord fr = frRepo.getFoodRecord(eo.getFoodRecordId());
            return fr;
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Deletes the food item from the Database.
     */
    private void updateFoodItem() {
        //Set the food item to didnt eat
        mFoodItem.setDidnteat(true);

        //Update in database
        FoodItemRepository fiRepo = new FoodItemRepository(Objects.requireNonNull(getActivity()).getApplication());
        fiRepo.updateFoodItem(mFoodItem);
    }

    /**
     * Finlaizes the food item
     */
    private void finalizeFoodItem(){
        mFoodItem.finalizeItem();
        //Update the database
        FoodItemRepository foodItemRepo = new FoodItemRepository(Objects.requireNonNull(getActivity()).getApplication());
        foodItemRepo.updateFoodItem(mFoodItem);
    }

    /**
     * Mimics the return keyword for this fragment. Returns to
     * the Finalize food item activity to finalizeItem the next food item.
     */
    protected void fragmentReturn(){
        Intent i = new Intent(getContext(), FinalizeFoodItemActivity.class);
        i.putExtra(EOID, mFoodItem.getEatingOccasionId());
        //Need to start new activity her otherise the list doesnt get updated so name may still contain asterix
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause(){
        super.onPause();
        if(mMediaPlayerReady) {
            stopAudio();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        //Check the current food item. If the leftover image has been set to the FLAG an error has occured
        //most likely user hot back during camera or audio recording. So reload the fragment.
        //Doing this makes the Leftover Prompt for Shared dishes re appear if they hit back.
        if(mFoodItem.getLeftoverImageUrl() != null && mFoodItem.getLeftoverImageUrl().equals(FLAG)){
            //Reset the image URL to allow the fragment to re show the leftover dialog
            mFoodItem.setLeftoverImageUrl(null);
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }
}
