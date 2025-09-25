package au.edu.newcastle.jnc985.visida.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import au.edu.newcastle.jnc985.visida.BuildConfig;
import au.edu.newcastle.jnc985.visida.R;
import bo.EatingOccasionViewModel;
import bo.FoodRecordViewModel;
import bo.HouseholdMembersViewModel;
import bo.LoggerUtil;
import bo.MealViewModel;
import bo.MealViewModelFactory;
import bo.NotificationViewModel;
import bo.State;
import bo.Utilities;
import bo.db.entity.Deliverable;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;
import bo.db.entity.Meal;
import ui.AudioButton;

import static au.edu.newcastle.jnc985.visida.R.id.action_manage;
import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.HASBREASTFED;
import static bo.AppConstants.NAVBAR;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.SETUP;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private NavigationBarFragment mNavBar;
    private NotificationViewModel mNotificationViewModel;
    private TextView mNotificationBadge;
    private static int mNotificationCount = 0;

    //Button to handle the long click for the help audio
    private Button bellButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Main Activity Created");
        // Run this at the start of every activity
        Utilities.updateLanguage(getBaseContext());

        // Check if the app has expired
        GregorianCalendar expDate = new GregorianCalendar(BuildConfig.expiryYear, BuildConfig.expiryMonth - 1, BuildConfig.expiryDay);
        GregorianCalendar now = new GregorianCalendar();

        boolean isExpired = now.after(expDate);

        if (isExpired) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.expired);
            builder.setMessage(R.string.expired_message);
            builder.setPositiveButton(R.string.close_app, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }

        if(BuildConfig.forceKhmer) {
            setContentView(R.layout.activity_main_shared);
        }
        else if(BuildConfig.forceSwahili){
            setContentView(R.layout.activity_main_audio);
        }
        else{
            setContentView(R.layout.activity_main);
        }


        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        if(!LoggerUtil.getInstance().isOpen()) {
            LoggerUtil.getInstance().open(this);
        }

        FragmentManager fm = getSupportFragmentManager();
        mNavBar = (NavigationBarFragment) fm.findFragmentByTag(NAVBAR);

        if(mNavBar == null) {
            //Load the fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, NavigationBarFragment.newInstance(), NAVBAR)
                    .commit();
        }

        //Create View model for notifications.
        if (mNotificationViewModel == null) {
            mNotificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        }

        //register to observe the Notificaitons list. When the list changes update the counter.
        mNotificationViewModel.getObservableNotifications().observe(this, new Observer<List<? extends Deliverable>>() {
            @Override
            public void onChanged(@Nullable List<? extends Deliverable> reviewNotifications) {
                int i = mNotificationViewModel.countUnseen();
                updateNotificationCount(i);
            }
        });

        Button btnBreastfed = findViewById(R.id.btnBreastFeed);

        //Set the menu buttons placeholders.
        bellButton= new AudioButton(this);
        //bellButton.setAudioFileResId(R.raw.inst_audio_v23);

        //Check shared preferences if the household member have been set up
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        //Set state to INVALID since we are on the home screen.
        Utilities.setState(getApplicationContext(), State.INVALID);

        //Default to false for first run
        boolean setup = sharedPreferences.getBoolean(SETUP, false);
        //If not load the household member set up.
        if(!setup){
            //Create intent to go to the set up household member activity
            Intent i = new Intent(this, SetupHouseholdActivity.class);
            startActivity(i);
        }

        //if breastfed make the button visible.
        boolean containsBreastfed = sharedPreferences.getBoolean(HASBREASTFED, false);
        if (containsBreastfed) {
            btnBreastfed.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.actionbar_menu, menu);
        final MenuItem bell = menu.findItem(R.id.notification_list);
        final View menu_hotlist = bell.getActionView();
        mNotificationBadge = menu_hotlist.findViewById(R.id.notification_dot);

        //Since we are using a custom action layout the parent of the actionlayout (Relative layout)
        //will consume the click action. So we add an onclick listener here and manually call the
        //onOptionSelectedMethod sending the bell menu item.
        menu_hotlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.onOptionsItemSelected(bell);
            }
        });

        menu_hotlist.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //return bellButton.onLongClick(v);
                return true;
            }
        });

        //Get number of unseen notifications
        mNotificationCount = mNotificationViewModel.countUnseen();
        updateNotificationCount(mNotificationCount);

        //Manually make the settings icon white. For some reason we cant do this in XML??
        MenuItem settings = menu.findItem(R.id.action_manage);
        Drawable d = settings.getIcon();
        d.mutate();
        d.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);


        return super.onCreateOptionsMenu(menu);
    }

    public void updateNotificationCount(int count){
        //https://stackoverflow.com/a/25453979/4960314
        mNotificationCount = count;
        if (mNotificationBadge == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mNotificationCount == 0)
                    mNotificationBadge.setVisibility(View.INVISIBLE);
                else {
                    mNotificationBadge.setVisibility(View.VISIBLE);
                    mNotificationBadge.setText(Integer.toString(mNotificationCount));
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case action_manage: {
                Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked Settings");
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.notification_list:{
                Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked Bell");
                showNotificationsMenu();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        //Refresh the option menu to update the notifications
        //counter. If the user clicks the back button the menu
        //is not redrawn. This call will force the redraw.
        invalidateOptionsMenu();
    }

    private void showNotificationsMenu() {
        View v = findViewById(R.id.notification_list);
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked notification " + item.getTitle());
                Intent i = item.getIntent();
                if(i.getComponent().getClassName().equals(SelectEatingOccasionActivity.class.getName())){
                    // Set the state to FINALIZE
                    Utilities.setState(getApplicationContext(), State.FINALIZE);
                }
                startActivity(i);
                return true;
            }
        });

        List<? extends Deliverable> notifications = mNotificationViewModel.getObservableNotifications().getValue();
        Iterator<? extends Deliverable> it = notifications.iterator();
        while(it.hasNext()){
            Deliverable n = it.next();
            //String id = n.getPpid();
            String id = n.getId();
            if(id == null || id.isEmpty()) {
                it.remove();
            }
            else{
                Intent itemIntent;
                String msg = n.getMessage(this);
                //Create an intent for every notification to the Record Review Activity.
                itemIntent = n.getIntent(this);
                popupMenu.getMenu().add(msg).setIntent(itemIntent);
            }
        }

        //If there are no notifications dont show the menu. If you show an empty mneu you will
        //still have to tap outside the "invisible" menu to close it.
        if(notifications.size() > 0) {
            popupMenu.show();
        }
    }

    public void onClickEat(View view){
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked Eat");
        moveToSelectHouseholdMember(State.EAT);
    }

    public void onClickMeal(View v){
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked Meal");
        Utilities.setState(getApplicationContext(), State.MEAL);
        Intent i = new Intent(this, MealActivity.class);
        startActivity(i);
    }

    public void onClickCook(View view){
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked Cook");
        Utilities.setState(getApplicationContext(), State.COOK);
        Intent intent = new Intent(this, ListRecipesActivity.class);
        startActivity(intent);
    }

    public void onClickFinalizeEat(View view){
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked Finalize Eat");
        //Convert Meals to Eating occasions
        convertMeals();
        //Move to the SelectHosueholdMemberActivity
        moveToSelectHouseholdMember(State.FINALIZE);
    }

    private void convertMeals() {
        //Get a Meal View Model (use -1 for id to just create the view model).
        MealViewModel mealViewModel = new ViewModelProvider(this, new MealViewModelFactory(this.getApplication(), -1)).get(MealViewModel.class);
        FoodRecordViewModel foodRecordViewModel = new ViewModelProvider(this).get(FoodRecordViewModel.class);
        EatingOccasionViewModel eatingOccasionViewModel = new ViewModelProvider(this).get(EatingOccasionViewModel.class);
        HouseholdMembersViewModel householdMembersViewModel = new ViewModelProvider(this).get(HouseholdMembersViewModel.class);
        List<Meal> unFinalizedMeals = mealViewModel.getUnfinalizedMeals();
        List<HouseholdMember> householdMembers = householdMembersViewModel.getHouseholdMembers();
        //Tidy up any empty eating occasions
        eatingOccasionViewModel.clearEmpty();
        for (Meal m : unFinalizedMeals) {
            List<FoodItem> dishes = mealViewModel.getDishes(m.getMealId());
            //Go to every household member
            for (HouseholdMember hm : householdMembers) {
                //Get todays food record
                FoodRecord todaysFr = foodRecordViewModel.getTodaysFoodRecordFor(hm);
                EatingOccasion currentEo = todaysFr.getCurrentEatingOccasion();
                if(currentEo == null){
                    currentEo = new bo.db.entity.EatingOccasion();
                    currentEo.setFoodRecordId(todaysFr.getFoodRecordId());
                    //Moved notification so this should schedule a notification for each eating occasion.
                    eatingOccasionViewModel.addEatingOccasion(currentEo);
                }
                eatingOccasionViewModel.setEatingOccasion(currentEo);
                for(FoodItem fi : dishes){
                    FoodItem copy = fi.copy();
                    copy.setEatingOccasionId(currentEo.getEatingOccasionId());
                    eatingOccasionViewModel.addFoodItem(hm.getParticipantHouseholdMemberId(), copy);
                }
                //Link any recipes from the meal to the Eating Occasion
                eatingOccasionViewModel.addRecipes(m.getRecipeIds());
            }
            m.setFinalized(true);
            mealViewModel.update(m);
        }
    }

    public void onClickBreastfeed(View view){
        Log.i(ACTIVITY_LOG_TAG, TAG + ": Clicked Breastfeed");
        moveToSelectHouseholdMember(State.BREASTFEED);
    }

    private void moveToSelectHouseholdMember(State state){
        Utilities.setState(getApplicationContext(), state);
        Intent intent = new Intent(this, SelectHouseholdMemberActivity.class);
        startActivity(intent);
    }



//    @Override
//    public boolean play(AudioButton audioButton) {
//        if(mMediaPlayer == null || !mMediaPlayer.isPlaying()) {
//            mMediaPlayer = MediaPlayer.create(this, audioButton.getAudioFile());
//            mMediaPlayer.setOnCompletionListener(audioButton);
//            mMediaPlayer.start();
//            System.out.println("PLAYING AUDIO");
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        mMediaPlayer.start();
//                    } catch (IllegalStateException e) {
//                        e.printStackTrace();
//                        //Now tell the button to go back to normal
//                        audioButton.reset();
//                    }
//                }
//            }).start();
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public void stop() {
//        System.out.println("STOPPING AUDIO");
//    }
//
//    @Override
//    public void onPause(){
//        super.onPause();
//        if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
//            mMediaPlayer.stop();
//        }
//    }

}
