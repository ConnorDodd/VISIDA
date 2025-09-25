package au.edu.newcastle.jnc985.visida.activity;

import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.recyclerview.widget.RecyclerView;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import au.edu.newcastle.jnc985.visida.R;
import bo.FoodRecordViewModel;
import bo.HouseholdMembersViewModel;
import bo.State;
import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.FoodRecord;
import bo.db.entity.HouseholdMember;
import bo.typeconverter.DateTypeConverter;
import bo.typeconverter.TimeTypeConverter;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;
import static bo.AppConstants.FR;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.STATE;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

/**
 * Created by jnc985 on 30-Nov-17.
 */

@RunWith(MockitoJUnitRunner.class)
public class SelectHouseholdMemberActivityTest {
    private final static long MINUTE = 60 * 1000;

    private AppDatabase mDb;

    @Mock
    private HouseholdMembersViewModel hmViewModel;
    @Mock
    private FoodRecordViewModel frViewModel;

    private Context mContext;

    private HouseholdMember hm1;

    private Intent mIntent;
    //Set up Rule
    @Rule
    public IntentsTestRule<SelectHouseholdMemberActivity> intentsTestRule = new IntentsTestRule<SelectHouseholdMemberActivity>(SelectHouseholdMemberActivity.class, true, false) {
        @Override
        protected Intent getActivityIntent() {
            mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
            mIntent = new Intent(mContext, SelectHouseholdMemberActivity.class);

            //Put the EAT state in as default Each test will have to set the state accordingly
            mIntent.putExtra(STATE, State.EAT);
            return mIntent;
        }
    };

    @Before
    public void createDb() throws Throwable {
        mContext = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).build();

        //Create the list of dummy household members
        final MutableLiveData<List<HouseholdMember>> hms = new MutableLiveData<>();
        hm1 = new HouseholdMember(1, "1", "HM1", "", 1, false, false);
        HouseholdMember hm2 = new HouseholdMember(2, "1", "HM2", "", 2, false, false);
        hm2.setBreastfed(true);
        HouseholdMember hm3 = new HouseholdMember(3, "1", "HM3", "", 3, false, false);
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                hms.setValue(new ArrayList<HouseholdMember>());
            }
        });

        hms.getValue().add(hm1);
        hms.getValue().add(hm2);
        hms.getValue().add(hm3);

        //Add hms to database
        mDb.getHouseholdMemberDao().insert(hm1, hm2, hm3);

        //Set up state to not be invalid
        SharedPreferences sharedPref = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(STATE, State.EAT.ordinal());
        editor.commit();

        AppDatabase.setInstance(mDb);

        intentsTestRule.launchActivity(new Intent());

        //Set up the ViewModel to provide the household members
        when(hmViewModel.getObservableHouseholdMembers()).thenReturn(hms);

        intentsTestRule.getActivity().setHouseholdMemberViewModel(hmViewModel);
        //Run the list setup code in activity to overwrite the listview after creation.
        try {
            intentsTestRule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    intentsTestRule.getActivity().setUpList();
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }


    }

    @After
    public void closeDb() {
        mDb.close();
    }

    private void addExtraHouseholdMembers(){

    }

    //Test with household members no food records
    @Test
    public void clickEATHouseholdMemberCreatesNewFoodRecordNoEatingOccasion() {
        //Inject the current state to EAT
        intentsTestRule.getActivity().setCurrentState(State.EAT);

        //Create a date object
        Date today = new Date();

        //Get this first item in the listView (to verify)
        RecyclerView lv = intentsTestRule.getActivity().findViewById(R.id.listViewHouseholdMembers);

        onView(withId(R.id.listViewHouseholdMembers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //Check the database now contains a new food record
        List<FoodRecord> todaysFodRecords = mDb.getFoodRecordDao().getTodaysFoodRecordForHouseholdMember(hm1.getUid(), DateTypeConverter.dateToTimestamp(today));
        assertThat(todaysFodRecords.size(), is(1));


        FoodRecord fr = todaysFodRecords.get(0);
        assertThat(fr.getEatingOccasions(), is(empty()));
        assertThat(DateTypeConverter.dateToTimestamp(fr.getDate()), is(equalTo(DateTypeConverter.dateToTimestamp(today))));

        //Clicking the button should also create a new Eating Occasion
        List<EatingOccasion> foodRecordsEatingOccasions = mDb.getEatingOccasionDao().getAllEatingOccasionsForFoodRecord(fr.getFoodRecordId());
        assertThat(foodRecordsEatingOccasions, is(not(empty())));
        assertThat(foodRecordsEatingOccasions.size(), is(1));

        //Make sure the eating occasion is created new with current time (+- 1min)
        EatingOccasion newEo = foodRecordsEatingOccasions.get(0);
        assertThat(TimeTypeConverter.isTimeWithinRange(today, newEo.getStartTime(), 1), is(true));

        //Click the add FoodItem Button
        onView(withId(R.id.btnAddFoodItem)).perform(click());

        //Check that the Intent that is created
        intended(hasComponent(CameraActivity.class.getName()));
    }

    //Test
    @Test
    public void clickEATHouseholdMemberWithFoodRecordWithNoEatingOccasion() {
        //Inject the current state to EAT
        intentsTestRule.getActivity().setCurrentState(State.EAT);

        //Create a date object
        Date today = new Date();

        //Add a Food Record to the database
        FoodRecord dummyYr = new FoodRecord(hm1.getUid());
        //dummyYr.setFoodRecordId(1);
        dummyYr.setHouseholdMemberId(hm1.getUid());
        mDb.getFoodRecordDao().insert(dummyYr);

        onView(withId(R.id.listViewHouseholdMembers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //Check the database contains the food record for today.
        List<FoodRecord> todaysFodRecords = mDb.getFoodRecordDao().getTodaysFoodRecordForHouseholdMember(hm1.getUid(), DateTypeConverter.dateToTimestamp(today));
        assertThat(1, is(todaysFodRecords.size()));

        FoodRecord fr = todaysFodRecords.get(0);
        assertThat(fr.getEatingOccasions(), is(empty()));
        assertThat(DateTypeConverter.dateToTimestamp(fr.getDate()), is(equalTo(DateTypeConverter.dateToTimestamp(today))));

        //Clicking the button should also create a new Eating Occasion
        List<EatingOccasion> foodRecordsEatingOccasions = mDb.getEatingOccasionDao().getAllEatingOccasionsForFoodRecord(fr.getFoodRecordId());
        assertThat(foodRecordsEatingOccasions, is(not(empty())));
        assertThat(foodRecordsEatingOccasions.size(), is(1));

        //Make sure the eating occasion is created new with current time (+- 1min)
        EatingOccasion newEo = foodRecordsEatingOccasions.get(0);
        assertThat(TimeTypeConverter.isTimeWithinRange(today, newEo.getStartTime(), 1), is(true));

        //Click the add FoodItem Button
        onView(withId(R.id.btnAddFoodItem)).perform(click());

        //Check that the Intent that is created contains a new Food Record (for today)
        intended(hasComponent(CameraActivity.class.getName()));
    }

    //Test with hm with food record no eating occasion (Something happened and no EOid was saved)
    @Test
    public void clickEATHouseholdMemberWithFoodRecordWithFinalizedOldEatingOccasion() {
        //Inject the current state to EAT
        intentsTestRule.getActivity().setCurrentState(State.EAT);

        //Create a date object
        Date today = new Date();

        //Add a Food Record to the database
        FoodRecord fr = new FoodRecord(1);
        fr.setFoodRecordId(1);
        fr.setHouseholdMember(hm1);
        mDb.getFoodRecordDao().insert(fr);

        //Add an eating Occasion for the Food Record to the database
        EatingOccasion eo = new EatingOccasion();
        eo.setFoodRecordId((long) 1);
        eo.setFinalized(true);
        //Set the start time for 2 hours ago and endTime 4 minutes after
        eo.setStartTime(new Date(today.getTime() - 120 * MINUTE));
        eo.setEndTime(new Date(eo.getStartTime().getTime() + 4 * MINUTE));
        fr.addEatingOccasion(eo);
        eo.setEatingOccasionId(mDb.getEatingOccasionDao().insert(eo));

        //Mock the FoodRecordViewModel to return the foodrecord with the eating occasion inserted
        try {
            when(frViewModel.getTodaysFoodRecordFor(any(HouseholdMember.class))).thenReturn(fr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        intentsTestRule.getActivity().setFoodRecordViewModel(frViewModel);

        onView(withId(R.id.listViewHouseholdMembers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //Clicking the button should also create a new Eating Occasion
        List<EatingOccasion> foodRecordsEatingOccasions = mDb.getEatingOccasionDao().getAllEatingOccasionsForFoodRecord(fr.getFoodRecordId());
        assertThat(foodRecordsEatingOccasions, is(not(empty())));
        assertThat(foodRecordsEatingOccasions.size(), is(2));

        //Make sure the eating occasion is created new with current time (+- 1min)
        EatingOccasion newEo = null;
        if (foodRecordsEatingOccasions.size() > 0) {
            //Search through all the eating occasions. Just chek all of them, not expected to have many in the list
            Date now = new Date();
            for (EatingOccasion eO : foodRecordsEatingOccasions) {
                if (TimeTypeConverter.isTimeWithinRange(now, eO.getStartTime(), 60)) {
                    //Check the EOid has not been finalized
                    if (!eO.isFinalized()) {
                        newEo = eO;
                        break;
                    }
                }
            }
        }
        assertThat(newEo, is(notNullValue()));
        assertThat(TimeTypeConverter.isTimeWithinRange(today, newEo.getStartTime(), 1), is(true));

        //Click the add FoodItem Button
        onView(withId(R.id.btnAddFoodItem)).perform(click());

        //Check that the Intent that is created.
        intended(hasComponent(CameraActivity.class.getName()));
    }


    //Test with hm with food record with finalized eo from > 60 minutes ago(already done breakfast now doing dinner)
    @Test
    public void clickEATHouseholdMemberWithFoodRecordWithFinalizedRecentEatingOccasion() {
        //Inject the current state to EAT
        intentsTestRule.getActivity().setCurrentState(State.EAT);

        //Create a date object
        Date today = new Date();

        //Add a Food Record to the database
        FoodRecord fr = new FoodRecord(1);
        fr.setFoodRecordId(1);
        fr.setHouseholdMember(hm1);
        mDb.getFoodRecordDao().insert(fr);

        //Add an eating Occasion for the Food Record to the database
        EatingOccasion eo = new EatingOccasion();
        eo.setFoodRecordId((long) 1);
        eo.setFinalized(true);
        //Set the start time for 30 minutes ago hand endTime 4 minutes after
        eo.setStartTime(new Date(today.getTime() - 30 * MINUTE));
        eo.setEndTime(new Date(eo.getStartTime().getTime() + 4 * MINUTE));
        fr.addEatingOccasion(eo);
        eo.setEatingOccasionId(mDb.getEatingOccasionDao().insert(eo));

        //Mock the FoodRecordViewModel to return the foodrecord with the eating occasion inserted
        try {
            when(frViewModel.getTodaysFoodRecordFor(any(HouseholdMember.class))).thenReturn(fr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        intentsTestRule.getActivity().setFoodRecordViewModel(frViewModel);

        onView(withId(R.id.listViewHouseholdMembers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //Clicking the button should also create a new Eating Occasion
        List<EatingOccasion> foodRecordsEatingOccasions = mDb.getEatingOccasionDao().getAllEatingOccasionsForFoodRecord(fr.getFoodRecordId());
        assertThat(foodRecordsEatingOccasions, is(not(empty())));
        assertThat(foodRecordsEatingOccasions.size(), is(2));

        //Make sure the eating occasion is created new with current time (+- 1min)
        EatingOccasion newEo = null;
        if (foodRecordsEatingOccasions.size() > 0) {
            //Search through all the eating occasions. Just chek all of them, not expected to have many in the list
            Date now = new Date();
            for (EatingOccasion eO : foodRecordsEatingOccasions) {
                if (TimeTypeConverter.isTimeWithinRange(now, eO.getStartTime(), 60)) {
                    //Check the EOid has not been finalized
                    if (!eO.isFinalized()) {
                        newEo = eO;
                        break;
                    }
                }
            }
        }
        assertThat(newEo, is(notNullValue()));
        assertThat(TimeTypeConverter.isTimeWithinRange(today, newEo.getStartTime(), 1), is(true));

        //Click the add FoodItem Button
        onView(withId(R.id.btnAddFoodItem)).perform(click());

        //Check that the Intent that is created contains a new Food Record (for today)
        intended(hasComponent(CameraActivity.class.getName()));
    }


    @Test
    public void clickEATHouseholdMemberWithFoodRecordWithSingleNonFinalizedRecentEatingOccasion() {

        //Inject the current state to EAT
        intentsTestRule.getActivity().setCurrentState(State.EAT);

        //Create a date object
        Date today = new Date();

        //Add a Food Record to the database
        FoodRecord fr = new FoodRecord(1);
        fr.setFoodRecordId(1);
        fr.setHouseholdMember(hm1);
        mDb.getFoodRecordDao().insert(fr);

        //Add an eating Occasion for the Food Record to the database
        EatingOccasion eo = new EatingOccasion();
        eo.setFoodRecordId((long) 1);
        eo.setFinalized(false);
        //Set the start time for 30 minutes ago hand endTime 4 minutes after
        eo.setStartTime(new Date(today.getTime() - 30 * MINUTE));
        fr.addEatingOccasion(eo);
        eo.setEatingOccasionId(mDb.getEatingOccasionDao().insert(eo));

        List<EatingOccasion> foodRecordsEatingOccasions2 = mDb.getEatingOccasionDao().getAllEatingOccasionsForFoodRecord(1);
        //Mock the FoodRecordViewModel to return the foodrecord with the eating occasion inserted
        try {
            when(frViewModel.getTodaysFoodRecordFor(any(HouseholdMember.class))).thenReturn(fr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        intentsTestRule.getActivity().setFoodRecordViewModel(frViewModel);

        onView(withId(R.id.listViewHouseholdMembers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //Clicking the button should also create a new Eating Occasion
        List<EatingOccasion> foodRecordsEatingOccasions = mDb.getEatingOccasionDao().getAllEatingOccasionsForFoodRecord(fr.getFoodRecordId());
        assertThat(foodRecordsEatingOccasions, is(not(empty())));
        assertThat(foodRecordsEatingOccasions.size(), is(1));

        //Make sure the non finalized eating occasion is found and used
        EatingOccasion newEo = null;
        if (foodRecordsEatingOccasions.size() > 0) {
            //Search through all the eating occasions. Just chek all of them, not expected to have many in the list
            Date now = new Date();
            for (EatingOccasion eO : foodRecordsEatingOccasions) {
                if (TimeTypeConverter.isTimeWithinRange(now, eO.getStartTime(), 60)) {
                    //Check the EOid has not been finalized
                    if (!eO.isFinalized()) {
                        newEo = eO;
                        break;
                    }
                }
            }
        }
        assertThat(newEo, is(notNullValue()));
        assertThat(TimeTypeConverter.isTimeWithinRange(today, newEo.getStartTime(), 60), is(true));

        //Click the add FoodItem Button
        onView(withId(R.id.btnAddFoodItem)).perform(click());

        //Check that the Intent that is created contains a new Food Record (for today)
        intended(hasComponent(CameraActivity.class.getName()));
    }

    @Test
    public void clickEATHouseholdMemberWithFoodRecordWithFinalzedEOAndNonFinalizedRecentEatingOccasion() {
        //Inject the current state to EAT
        intentsTestRule.getActivity().setCurrentState(State.EAT);

        //Create a date object
        Date today = new Date();

        //Add a Food Record to the database
        FoodRecord fr = new FoodRecord(1);
        fr.setFoodRecordId(1);
        fr.setHouseholdMember(hm1);
        mDb.getFoodRecordDao().insert(fr);

        //Add a FInalized EOid (breakfast)
        EatingOccasion eo1 = new EatingOccasion();
        eo1.setFoodRecordId((long) 1);
        eo1.setFinalized(true);
        //Set the start time for 2 hours ago hand endTime 4 minutes after
        eo1.setStartTime(new Date(today.getTime() - 120 * MINUTE));
        eo1.setEndTime(new Date(eo1.getStartTime().getTime() + 4 * MINUTE));
        fr.addEatingOccasion(eo1);
        eo1.setEatingOccasionId(mDb.getEatingOccasionDao().insert(eo1));

        //Add an unfinalized EOid (dinner, current meal)
        EatingOccasion eo2 = new EatingOccasion();
        eo2.setFoodRecordId((long) 1);
        eo2.setFinalized(false);
        //Set the start time for 30 minutes ago
        eo2.setStartTime(new Date(today.getTime() - 30 * MINUTE));
        fr.addEatingOccasion(eo2);
        eo2.setEatingOccasionId(mDb.getEatingOccasionDao().insert(eo2));

        //Mock the FoodRecordViewModel to return the foodrecord with the eating occasion inserted
        try {
            when(frViewModel.getTodaysFoodRecordFor(any(HouseholdMember.class))).thenReturn(fr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        intentsTestRule.getActivity().setFoodRecordViewModel(frViewModel);

        onView(withId(R.id.listViewHouseholdMembers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //Clicking the button should get the two eating occasions (picking up the non finalized one)
        List<EatingOccasion> foodRecordsEatingOccasions = mDb.getEatingOccasionDao().getAllEatingOccasionsForFoodRecord(fr.getFoodRecordId());
        assertThat(foodRecordsEatingOccasions, is(not(empty())));
        assertThat(foodRecordsEatingOccasions.size(), is(2));

        //Make sure the non finalized eating occasion is found and used
        EatingOccasion newEo = null;
        if (foodRecordsEatingOccasions.size() > 0) {
            //Search through all the eating occasions. Just chek all of them, not expected to have many in the list
            Date now = new Date();
            for (EatingOccasion eO : foodRecordsEatingOccasions) {
                if (TimeTypeConverter.isTimeWithinRange(now, eO.getStartTime(), 60)) {
                    //Check the EOid has not been finalized
                    if (!eO.isFinalized()) {
                        newEo = eO;
                        break;
                    }
                }
            }
        }
        assertThat(newEo, is(notNullValue()));
        assertThat(newEo, is(eo2));
        assertThat(TimeTypeConverter.isTimeWithinRange(today, newEo.getStartTime(), 60), is(true));

        //Click the add FoodItem Button
        onView(withId(R.id.btnAddFoodItem)).perform(click());

        //Check that the Intent that is created contains a new Food Record (for today)
        intended(hasComponent(CameraActivity.class.getName()));
    }

    // @Test
    public void clickEATHouseholdMemberWithFoodRecordWithNonFinalzedOldEO() {
        //Inject the current state to EAT
        intentsTestRule.getActivity().setCurrentState(State.EAT);

        //Create a date object
        Date today = new Date();

        //Add a Food Record to the database
        FoodRecord fr = new FoodRecord(1);
        fr.setFoodRecordId(1);
        fr.setHouseholdMember(hm1);
        mDb.getFoodRecordDao().insert(fr);

        //Add a finalized eating Occasion for the Food Record to the database
        EatingOccasion eo1 = new EatingOccasion();
        eo1.setFoodRecordId((long) 1);
        eo1.setFinalized(false);
        //Set the start time for 2 hours ago hand endTime 4 minutes after
        eo1.setStartTime(new Date(today.getTime() - 120 * MINUTE));
        fr.addEatingOccasion(eo1);
        eo1.setEatingOccasionId(mDb.getEatingOccasionDao().insert(eo1));

        //Mock the FoodRecordViewModel to return the foodrecord with the eating occasion inserted
        try {
            when(frViewModel.getTodaysFoodRecordFor(any(HouseholdMember.class))).thenReturn(fr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        intentsTestRule.getActivity().setFoodRecordViewModel(frViewModel);

        onView(withId(R.id.listViewHouseholdMembers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        assertThat(1, is(2));

    }

    /*
    with hm
        without fr - clickHouseholdMemberCreatesNewFoodRecordNoEatingOccasion
        with fr
            with no EOid - clickHouseholdMemberWithFoodRecordWithNoEatingOccasion
            with finalized old EOid - clickHouseholdMemberWithFoodRecordWithFinalizedOldEatingOccasion
            with finalized recent EOid - clickHouseholdMemberWithFoodRecordWithFinalizedRecentEatingOccasion
            with single non finalized recent EOid - clickHouseholdMemberWithFoodRecordWithSingleNonFinalizedRecentEatingOccasion
            with finalizedEO and non finalized recent EOid - clickHouseholdMemberWithFoodRecordWithFinalzedEOAndNonFinalizedRecentEatingOccasion
            with non finalized old EOid - clickHouseholdMemberWithFoodRecordWithNonFinalzedOldEO
     */
    @Test
    public void clickFINALIZEHouseholdMemberMovesToSelectEatingOccasion() {
        //Create a date object
        Date today = new Date();

        //Inject that we are in the Finalize state
        intentsTestRule.getActivity().setCurrentState(State.FINALIZE);

        //Add a Food Record to the database
        FoodRecord fr = new FoodRecord(1);
        fr.setFoodRecordId(1);
        fr.setHouseholdMember(hm1);
        mDb.getFoodRecordDao().insert(fr);

        //Add a FInalized EOid (breakfast)
        EatingOccasion eo1 = new EatingOccasion();
        eo1.setFoodRecordId((long) 1);
        eo1.setFinalized(false);
        //Set the start time for 30 minutes
        eo1.setStartTime(new Date(today.getTime() - 30 * MINUTE));
        fr.addEatingOccasion(eo1);
        eo1.setEatingOccasionId(mDb.getEatingOccasionDao().insert(eo1));

        onView(withId(R.id.listViewHouseholdMembers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //Check that the Intent that is created contains a new Food Record (for today)
        intended(hasComponent(SelectEatingOccasionActivity.class.getName()));
    }

    @Test
    public void clickFINALIZEHouseholdMemberWithNoEatingOccasions() {
        //Inject that we are in the Finalize state
        intentsTestRule.getActivity().setCurrentState(State.FINALIZE);

        //Create a date object
        Date today = new Date();

        onView(withId(R.id.listViewHouseholdMembers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //check that we are still on the select household member page ie nothing happened because there
        //are no eating occasions in the food record
        onView(withId(R.id.listViewHouseholdMembers)).check(matches(isDisplayed()));
    }

    @Test
    public void clickFINALIZEHouseholdMemberWithSingleNonFinalizedEatingOccasion() {
        //Inject that we are in the Finalize state
        intentsTestRule.getActivity().setCurrentState(State.FINALIZE);

        //Create a date object
        Date today = new Date();

        //Add a Food Record to the database
        FoodRecord fr = new FoodRecord(1);
        fr.setFoodRecordId(1);
        fr.setHouseholdMember(hm1);
        mDb.getFoodRecordDao().insert(fr);

        //Add an eating Occasion for the Food Record to the database
        EatingOccasion eo = new EatingOccasion();
        eo.setFoodRecordId((long) 1);
        eo.setFinalized(false);
        //Set the start time for 30 minutes ago
        eo.setStartTime(new Date(today.getTime() - 30 * MINUTE));
        fr.addEatingOccasion(eo);
        eo.setEatingOccasionId(mDb.getEatingOccasionDao().insert(eo));

        //Mock the FoodRecordViewModel to return the foodrecord with the eating occasion inserted
        try {
            when(frViewModel.getTodaysFoodRecordFor(any(HouseholdMember.class))).thenReturn(fr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        intentsTestRule.getActivity().setFoodRecordViewModel(frViewModel);

        onView(withId(R.id.listViewHouseholdMembers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //Check that the Intent that is created contains a new Food Record (for today)
        intended(allOf(
                hasComponent(SelectEatingOccasionActivity.class.getName()),
                hasExtra(FR, fr)));
    }

    @Test
    public void clickFINALIZEHouseholdMemberWithSingleNonFinalizedEatingOccasionAndSingleFinalizedEatingOccasion() {
        //Inject that we are in the Finalize state
        intentsTestRule.getActivity().setCurrentState(State.FINALIZE);

        //Create a date object
        Date today = new Date();

        //Add a Food Record to the database
        FoodRecord fr = new FoodRecord(1);
        fr.setFoodRecordId(1);
        fr.setHouseholdMember(hm1);
        mDb.getFoodRecordDao().insert(fr);

        //Add a FInalized EOid (breakfast)
        EatingOccasion eo1 = new EatingOccasion();
        eo1.setFoodRecordId((long) 1);
        eo1.setFinalized(true);
        //Set the start time for 2 hours ago hand endTime 4 minutes after
        eo1.setStartTime(new Date(today.getTime() - 120 * MINUTE));
        eo1.setEndTime(new Date(eo1.getStartTime().getTime() + 4 * MINUTE));
        fr.addEatingOccasion(eo1);
        eo1.setEatingOccasionId(mDb.getEatingOccasionDao().insert(eo1));

        //Add an unfinalized EOid (dinner, current meal)
        EatingOccasion eo2 = new EatingOccasion();
        eo2.setFoodRecordId((long) 1);
        eo2.setFinalized(false);
        //Set the start time for 30 minutes ago
        eo2.setStartTime(new Date(today.getTime() - 30 * MINUTE));
        fr.addEatingOccasion(eo2);
        eo2.setEatingOccasionId(mDb.getEatingOccasionDao().insert(eo2));

        //Mock the FoodRecordViewModel to return the foodrecord with the eating occasion inserted
        try {
            when(frViewModel.getTodaysFoodRecordFor(any(HouseholdMember.class))).thenReturn(fr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        intentsTestRule.getActivity().setFoodRecordViewModel(frViewModel);

        onView(withId(R.id.listViewHouseholdMembers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //Check that the Intent that is created contains a new Food Record (for today)
        intended(allOf(
                hasComponent(SelectEatingOccasionActivity.class.getName()),
                hasExtra(FR, fr)));
    }

    @Test
    public void clickFINALIZEHouseholdMemberWithOnlyFinalizedEatingOccasions() {
        //Inject that we are in the Finalize state
        intentsTestRule.getActivity().setCurrentState(State.FINALIZE);

        //Create a date object
        Date today = new Date();

        //Add a Food Record to the database
        FoodRecord fr = new FoodRecord(1);
        fr.setFoodRecordId(1);
        fr.setHouseholdMember(hm1);
        mDb.getFoodRecordDao().insert(fr);

        //Add a FInalized EOid (breakfast)
        EatingOccasion eo1 = new EatingOccasion();
        eo1.setFoodRecordId((long) 1);
        eo1.setFinalized(true);
        //Set the start time for 2 hours ago hand endTime 4 minutes after
        eo1.setStartTime(new Date(today.getTime() - 120 * MINUTE));
        eo1.setEndTime(new Date(eo1.getStartTime().getTime() + 4 * MINUTE));
        fr.addEatingOccasion(eo1);
        eo1.setEatingOccasionId(mDb.getEatingOccasionDao().insert(eo1));

        //Add an unfinalized EOid (dinner, current meal)
        EatingOccasion eo2 = new EatingOccasion();
        eo2.setFoodRecordId((long) 1);
        eo2.setFinalized(true);
        //Set the start time for 30 minutes ago
        eo2.setStartTime(new Date(today.getTime() - 30 * MINUTE));
        fr.addEatingOccasion(eo2);
        eo2.setEatingOccasionId(mDb.getEatingOccasionDao().insert(eo2));

        //Mock the FoodRecordViewModel to return the foodrecord with the eating occasion inserted
        try {
            when(frViewModel.getTodaysFoodRecordFor(any(HouseholdMember.class))).thenReturn(fr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        intentsTestRule.getActivity().setFoodRecordViewModel(frViewModel);

        onView(withId(R.id.listViewHouseholdMembers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //check that we are still on the select household member page ie nothing happened because there
        //are no eating occasions in the food record
        onView(withId(R.id.listViewHouseholdMembers)).check(matches(isDisplayed()));
    }


    @Test
    public void clickBREASTFEEDHouseholdMemberIsBreastfedMovesToBreastfeedActivity() {
        //Inject that we are in the Finalize state
        intentsTestRule.getActivity().setCurrentState(State.BREASTFEED);

        onView(withId(R.id.listViewHouseholdMembers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

        //Check that the Intent that is created contains a new Food Record (for today)
        intended(allOf(
                hasComponent(BreastfeedActivity.class.getName()),
                hasExtraWithKey(FR)));
    }

    @Test
    public void FINALIZEhouseholdMemberWithNonFinalizedEatingOccasionHasAsterix() {
        //Inject that we are in the Finalize state
        intentsTestRule.getActivity().setCurrentState(State.FINALIZE);

        //Create a date object
        Date today = new Date();

        //Add a Food Record to the database
        FoodRecord fr = new FoodRecord(1);
        fr.setFoodRecordId(1);
        fr.setHouseholdMember(hm1);
        mDb.getFoodRecordDao().insert(fr);

        //Add a FInalized EOid (breakfast)
        EatingOccasion eo1 = new EatingOccasion();
        eo1.setFoodRecordId((long) 1);
        eo1.setFinalized(false);
        fr.addEatingOccasion(eo1);
        eo1.setEatingOccasionId(mDb.getEatingOccasionDao().insert(eo1));

        //Add an unfinalized EOid (dinner, current meal)
        EatingOccasion eo2 = new EatingOccasion();
        eo2.setFoodRecordId((long) 1);
        eo2.setFinalized(false);
        //Set the start time for 30 minutes ago
        fr.addEatingOccasion(eo2);
        eo2.setEatingOccasionId(mDb.getEatingOccasionDao().insert(eo2));

        //Mock the FoodRecordViewModel to return the foodrecord with the eating occasion inserted
        try {
            when(frViewModel.getTodaysFoodRecordFor(any(HouseholdMember.class))).thenReturn(fr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        intentsTestRule.getActivity().setFoodRecordViewModel(frViewModel);

        //Check the First item in the recycler view has the asterix

    }

    @Test
    public void FINALIZEhouseholdMemberWithOnlyFinalizedEatingOccasionHasNoAsterix() {

    }
//
//    @Test
//    public void autoSelectHouseholdMemberIfOnlyOneExistsMEAL(){
//        //Straight up expect to see the Eating Occasion page
//        //Set up state to not be invalid
//        SharedPreferences sharedPref = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putInt(STATE, State.MEAL.ordinal());
//        editor.commit();
//
//        intentsTestRule.launchActivity(new Intent());
//        //Straight up expect to see the Eating Occasion page
//        onView(withId(R.id.rvDishList)).check(matches(isDisplayed()));
//    }
}
