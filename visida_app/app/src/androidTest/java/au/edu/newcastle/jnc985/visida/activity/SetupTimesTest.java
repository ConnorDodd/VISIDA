package au.edu.newcastle.jnc985.visida.activity;

import android.app.Activity;
import androidx.lifecycle.ViewModelProviders;
import androidx.room.Room;
import android.content.Context;
import android.content.Intent;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.rule.ActivityTestRule;
import android.widget.DatePicker;
import android.widget.TimePicker;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Calendar;

import au.edu.newcastle.jnc985.visida.R;
import bo.db.AppDatabase;
import bo.db.entity.Reminder;
import bo.scheduler.EOScheduleViewModel;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.ActivityResultMatchers.hasResultCode;
import static androidx.test.espresso.contrib.PickerActions.setTime;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static au.edu.newcastle.jnc985.visida.EspressoTestMatchers.withDrawable;
import static bo.scheduler.EOScheduleViewModel.SENSOR;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;

/**
 * Created by jnc985 on 30-Nov-17.
 */

@RunWith(MockitoJUnitRunner.class)
public class SetupTimesTest {

    //Intent to start activity
    Intent intent = new Intent();
    //Set up Rule
    @Rule
    public ActivityTestRule<SetupTimesActivity> mActivityTestRule = new ActivityTestRule<SetupTimesActivity>(SetupTimesActivity.class, true, false);

    private Context mContext;
    private AppDatabase mDb;

    @Before
    public void setUp() throws Throwable {
        mContext = getInstrumentation().getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(mContext, AppDatabase.class).build();
        AppDatabase.setInstance(mDb);
    }

    @After
    public void cleanUp() {
        mDb.close();
    }

    @Test
    public void clickSetupButtonsPopsUpDatTimePicker(){
        mActivityTestRule.launchActivity(new Intent());
        onView(withId(R.id.btnDateEO1)).perform(click());

        onView(withText(android.R.string.ok)).check(matches(isDisplayed()));
        onView(withText(android.R.string.ok)).perform(click());
    }

    @Test
    public void clickTimeButtonsUpdateCorrespondingTextView(){
        mActivityTestRule.launchActivity(new Intent());
        int hours = 1;
        int min = 30;

        //EO1 Time 1
        onView(withTagValue(is("1"))).perform(click());
        //Set the time
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hours, min));
        onView(withText(android.R.string.ok)).perform(click());

        onView(allOf(
                withParent(withTagValue(is("1"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hours++ + ":" + min ))));

        onView(withTagValue(is("2"))).perform(click());
        //Set the time
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hours, min));
        onView(withText(android.R.string.ok)).perform(click());
        onView(allOf(
                withParent(withTagValue(is("2"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hours++ + ":" + min ))));

        onView(withTagValue(is("3"))).perform(click());
        //Set the time
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hours, min));
        onView(withText(android.R.string.ok)).perform(click());
        onView(allOf(
                withParent(withTagValue(is("3"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hours++ + ":" + min ))));

        onView(withTagValue(is("4"))).perform(click());
        //Set the time
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hours, min));
        onView(withText(android.R.string.ok)).perform(click());
        onView(allOf(
                withParent(withTagValue(is("4"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hours++ + ":" + min ))));

        onView(withTagValue(is("5"))).perform(click());
        //Set the time
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hours, min));
        onView(withText(android.R.string.ok)).perform(click());
        onView(allOf(
                withParent(withTagValue(is("5"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hours++ + ":" + min ))));

        onView(withTagValue(is("6"))).perform(click());
        //Set the time
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hours, min));
        onView(withText(android.R.string.ok)).perform(click());
        onView(allOf(
                withParent(withTagValue(is("6"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hours++ + ":" + min ))));

        onView(withTagValue(is("7"))).perform(click());
        //Set the time
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hours, min));
        onView(withText(android.R.string.ok)).perform(click());
        onView(allOf(
                withParent(withTagValue(is("7"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hours++ + ":" + min ))));

        onView(withTagValue(is("8"))).perform(click());
        //Set the time
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hours, min));
        onView(withText(android.R.string.ok)).perform(click());
        onView(allOf(
                withParent(withTagValue(is("8"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hours++ + ":" + min ))));

        onView(withTagValue(is("9"))).perform(click());
        //Set the time
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hours, min));
        onView(withText(android.R.string.ok)).perform(click());
        onView(allOf(
                withParent(withTagValue(is("9"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hours + ":" + min ))));

    }

    @Test
    public void clickCalendarOpensDatePicker(){
        mActivityTestRule.launchActivity(new Intent());
        int year = 2018;
        int month = 10;
        int day = 10;
        //Make sure imageview has calendar no tick
        onView(withId(R.id.btnDateEO1)).check(matches(withDrawable(R.drawable.ic_calendar)));
        onView(withId(R.id.btnDateEO1)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(year, month, day));
        onView(withText(android.R.string.ok)).perform(click());
        onView(withId(R.id.btnDateEO1)).check(matches(withDrawable(R.drawable.ic_calendar_tick)));

    }

    @Test
    public void clickSetShowsErrorIfNotAllSet(){
        mActivityTestRule.launchActivity(new Intent());
        onView(withId(R.id.btnSet)).perform(click());
        //Check that the button is still visible to indicate we haven't left the page
        onView(withId(R.id.btnSet)).check(matches(isDisplayed()));
    }

    @Test
    public void clickSetWithAllDataSetReturnsToSetupHouseholdActivity(){
        mActivityTestRule.launchActivity(new Intent());
        EOScheduleViewModel vm = ViewModelProviders.of(mActivityTestRule.getActivity()).get(EOScheduleViewModel.class);
        int year = 2018;
        int month = 10;
        int day = 21;
        int hour = 7;
        int minute = 30;
        //Set all the times
        for(int i = 1; i <= vm.getNumberOfTimes(); i++){
            vm.setCurrentTime(i);
            vm.setTime(hour, minute);
        }
        //Set the dates
        vm.setCurrentDate(EOScheduleViewModel.DAY_ONE);
        vm.setDate(year, month, day);
        vm.setCurrentDate(EOScheduleViewModel.DAY_TWO);
        vm.setDate(year, month, day);
        vm.setCurrentDate(EOScheduleViewModel.DAY_THREE);
        vm.setDate(year, month, day);
        vm.setCurrentTime(SENSOR);
        vm.setTime(hour, minute);

        onView(withId(R.id.btnSet)).perform(click());

        //Check the activity is finished
        assertThat(mActivityTestRule.getActivityResult(), hasResultCode(Activity.RESULT_OK));
    }

    @Test
    public void viewsShowDataIfTimesAlreadySet(){
        int hours = 0;
        int min = 0;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, min);

        Reminder r1 = new Reminder();
        cal.set(Calendar.HOUR_OF_DAY, hours + 1);
        r1.setReminderDay(1);
        r1.setDate(cal.getTime());

        Reminder r2 = new Reminder();
        cal.set(Calendar.HOUR_OF_DAY, hours + 2);
        r2.setReminderDay(2);
        r2.setDate(cal.getTime());

        Reminder r3 = new Reminder();
        cal.set(Calendar.HOUR_OF_DAY, hours + 3);
        r3.setReminderDay(3);
        r3.setDate(cal.getTime());

        Reminder r4 = new Reminder();
        cal.set(Calendar.HOUR_OF_DAY, hours + 4);
        r4.setReminderDay(4);
        r4.setDate(cal.getTime());

        Reminder r5 = new Reminder();
        cal.set(Calendar.HOUR_OF_DAY, hours + 5);
        r5.setReminderDay(5);
        r5.setDate(cal.getTime());

        Reminder r6 = new Reminder();
        cal.set(Calendar.HOUR_OF_DAY, hours + 6);
        r6.setReminderDay(6);
        r6.setDate(cal.getTime());

        Reminder r7 = new Reminder();
        cal.set(Calendar.HOUR_OF_DAY, hours + 7);
        r7.setReminderDay(7);
        r7.setDate(cal.getTime());

        Reminder r8 = new Reminder();
        cal.set(Calendar.HOUR_OF_DAY, hours + 8);
        r8.setReminderDay(8);
        r8.setDate(cal.getTime());

        Reminder r9 = new Reminder();
        cal.set(Calendar.HOUR_OF_DAY, hours + 9);
        r9.setReminderDay(9);
        r9.setDate(cal.getTime());

        Reminder r10 = new Reminder();
        cal.set(Calendar.HOUR_OF_DAY, hours + 10);
        r10.setReminderDay(10);
        r10.setDate(cal.getTime());

        mDb.getReminderDao().insert(r1,r2,r3,r4,r5,r6,r7,r8,r9, r10);

        //Launch the activity
        mActivityTestRule.launchActivity(new Intent());

        //Check the dates have been set
        onView(withId(R.id.btnDateEO1)).check(matches(withDrawable(R.drawable.ic_calendar_tick)));
        onView(withId(R.id.btnDateEO2)).check(matches(withDrawable(R.drawable.ic_calendar_tick)));
        onView(withId(R.id.btnDateEO3)).check(matches(withDrawable(R.drawable.ic_calendar_tick)));

        //Check the times
        for(int i = 1; i < EOScheduleViewModel.NUMBER_OF_TIMES; i++) {
            onView(allOf(
                    withParent(withTagValue(is("" + (hours + i)))),
                    withId(R.id.txtTime)))
                    .check(matches(withText(containsString(hours+i + ":00"))));
        }
    }

    @Test
    public void addTimeToFirstGroupPropogatesToOtherTwo(){
        int hours = 1;
        int min = 30;
        //Start Activity
        mActivityTestRule.launchActivity(new Intent());

        //Click the First Time and set it
        onView(withTagValue(is("1"))).perform(click());
        //Set the time
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hours, min));
        onView(withText(android.R.string.ok)).perform(click());

        //Check each of the top time slots 1, 4, 7
        onView(allOf(
                withParent(withTagValue(is("1"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hours + ":" + min ))));
        onView(allOf(
                withParent(withTagValue(is("4"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hours + ":" + min ))));
        onView(allOf(
                withParent(withTagValue(is("7"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hours + ":" + min ))));


        //Set the time of the 2nd time
        onView(withTagValue(is("2"))).perform(click());
        //Set the time
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(++hours, min));
        onView(withText(android.R.string.ok)).perform(click());

        //Check the middle slots 2, 5, 8
        onView(allOf(
                withParent(withTagValue(is("2"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hours + ":" + min ))));
        onView(allOf(
                withParent(withTagValue(is("5"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hours + ":" + min ))));
        onView(allOf(
                withParent(withTagValue(is("8"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hours + ":" + min ))));


        //Set the time of the 2nd time
        onView(withTagValue(is("3"))).perform(click());
        //Set the time
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(++hours, min));
        onView(withText(android.R.string.ok)).perform(click());

        //Check the middle slots 2, 5, 8
        onView(allOf(
                withParent(withTagValue(is("3"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hours + ":" + min ))));
        onView(allOf(
                withParent(withTagValue(is("6"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hours + ":" + min ))));
        onView(allOf(
                withParent(withTagValue(is("9"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hours + ":" + min ))));

    }

    @Test
    public void changing2nd3rdGroupsDoesntAffectOthers(){
        //Launch Activity
        mActivityTestRule.launchActivity(new Intent());

        int hoursGroup2 = 1;
        int minGroup2 = 30;

        int hoursGroup3 = 2;
        int minGroup3 = 40;

        //Change 4 check 1 and 7
        //Set the time of the 2nd time
        onView(withTagValue(is("4"))).perform(click());
        //Set the time
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hoursGroup2, minGroup2));
        onView(withText(android.R.string.ok)).perform(click());

        onView(allOf(
                withParent(withTagValue(is("1"))),
                withId(R.id.txtTime)))
                .check(matches(withText(isEmptyOrNullString())));
        onView(allOf(
                withParent(withTagValue(is("7"))),
                withId(R.id.txtTime)))
                .check(matches(withText(isEmptyOrNullString())));

        //Change 5 check 2 and 8
        onView(withTagValue(is("5"))).perform(click());
        //Set the time
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hoursGroup2, minGroup2));
        onView(withText(android.R.string.ok)).perform(click());

        onView(allOf(
                withParent(withTagValue(is("2"))),
                withId(R.id.txtTime)))
                .check(matches(withText(isEmptyOrNullString())));
        onView(allOf(
                withParent(withTagValue(is("8"))),
                withId(R.id.txtTime)))
                .check(matches(withText(isEmptyOrNullString())));

        //Change 6 check 3 and 9
        onView(withTagValue(is("6"))).perform(click());
        //Set the time
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hoursGroup2, minGroup2));
        onView(withText(android.R.string.ok)).perform(click());

        onView(allOf(
                withParent(withTagValue(is("3"))),
                withId(R.id.txtTime)))
                .check(matches(withText(isEmptyOrNullString())));
        onView(allOf(
                withParent(withTagValue(is("9"))),
                withId(R.id.txtTime)))
                .check(matches(withText(isEmptyOrNullString())));

        //Change 7 check 1 and 4
        onView(withTagValue(is("7"))).perform(click());
        //Set the time
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hoursGroup3, minGroup3));
        onView(withText(android.R.string.ok)).perform(click());

        onView(allOf(
                withParent(withTagValue(is("1"))),
                withId(R.id.txtTime)))
                .check(matches(withText(isEmptyOrNullString())));
        onView(allOf(
                withParent(withTagValue(is("4"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hoursGroup2 + ":" + minGroup2))));

        //Change 8 check 2 and 5
        onView(withTagValue(is("8"))).perform(click());
        //Set the time
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hoursGroup3, minGroup3));
        onView(withText(android.R.string.ok)).perform(click());

        onView(allOf(
                withParent(withTagValue(is("2"))),
                withId(R.id.txtTime)))
                .check(matches(withText(isEmptyOrNullString())));
        onView(allOf(
                withParent(withTagValue(is("5"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hoursGroup2 + ":" + minGroup2))));


        //Change 9 check 3 and 6
        onView(withTagValue(is("9"))).perform(click());
        //Set the time
        onView(withClassName(equalTo(TimePicker.class.getName()))).perform(setTime(hoursGroup3, minGroup3));
        onView(withText(android.R.string.ok)).perform(click());

        onView(allOf(
                withParent(withTagValue(is("3"))),
                withId(R.id.txtTime)))
                .check(matches(withText(isEmptyOrNullString())));
        onView(allOf(
                withParent(withTagValue(is("6"))),
                withId(R.id.txtTime)))
                .check(matches(withText(containsString(hoursGroup2 + ":" + minGroup2))));
    }
}
