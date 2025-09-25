package bo.scheduler;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.util.SparseArray;

import java.util.List;

import bo.HouseholdMemberRepository;
import bo.db.entity.HouseholdMember;
import bo.db.entity.Reminder;
import notification.AlarmController;

import static bo.AppConstants.DEFAULT_REVIEW_HOUR;
import static bo.AppConstants.DEFAULT_REVIEW_MINUTE;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.REVIEWTIME_HOUR;
import static bo.AppConstants.REVIEWTIME_MIN;


/**
 * View model class to hold all the times chosen for the eating occasion
 * reminders.
 *
 * This calss encapsulates 9 (currently)
 * @see{bo.scheduler.ObservableCalendars} which contains two @bindable strings Time and Date
 * Each Observable Calendar can be accessed by using its index. There are 3 days to be scheduled.
 * When setting the date, using an integer flag to determine which day, this then updates ALL of the
 * dates fo the three corresponding Observable calendats so they all match.
 */
public class EOScheduleViewModel extends AndroidViewModel {

    public static final int DAY_ONE = 1;
    public static final int DAY_TWO = 4;
    public static final int DAY_THREE = 7;
//    public static final int SENSOR = 10;



    public static final int NUMBER_OF_TIMES  = 9;

    private int mCurrentTime;
    private int mCurrentDate;

    SparseArray<ObservableCalendar> timeSlots;
//    ObservableCalendar mSensorTIme;

    public EOScheduleViewModel(Application app){
        super(app);

        ReminderRepository remRepo = new ReminderRepository(app);
        List<Reminder> reminders = remRepo.getReminders();
        if(reminders.size() != NUMBER_OF_TIMES + 1){
            //If not all reminders set create a WHOLE NEW SET of Calendars
            timeSlots = new SparseArray<>();
            for(int i = 1; i <= NUMBER_OF_TIMES; i++){
                timeSlots.append(i, new ObservableCalendar());
            }
        }
        else{
            //Load the Reminders into ObservableCalendars
            timeSlots = new SparseArray<>();
            for(int i = 1; i <= NUMBER_OF_TIMES; i++){
                timeSlots.append(i, new ObservableCalendar(reminders.get(i-1)));
            }
        }

//        if(reminders.size() == NUMBER_OF_TIMES + 1) {
//            this.mSensorTIme = new ObservableCalendar(reminders.get(SENSOR - 1));
//        }
//        else{
//            this.mSensorTIme = new ObservableCalendar();
//        }
    }


    public void setCurrentTime(int slot){
        mCurrentTime = slot;
    }


    public ObservableCalendar getTime(int slot){
        return timeSlots.get(slot);
    }


    public void setTime(int hourOfDay, int minute) {
        //If we are changing a time in the top group, propogte that change
        //to all the time slots.
        if(mCurrentTime <= 3){
            for(int i = mCurrentTime; i <=NUMBER_OF_TIMES; i+=3){
                timeSlots.get(i).setTime(hourOfDay, minute);
            }
        }
//        else{
//            if(mCurrentTime != SENSOR) {
//                timeSlots.get(mCurrentTime).setTime(hourOfDay, minute);
//            }
//            else{
//                mSensorTIme.setTime(hourOfDay, minute);
//            }
//        }
    }


    /**
     * Set the date of all of the 3 groupd times. Using a flag to
     * determine which day is being changed.
     * @param year
     * @param month
     * @param dayOfMonth
     */
    public void setDate(int year, int month, int dayOfMonth) {
        for(int i = mCurrentDate; i < mCurrentDate + 3; i++){
            timeSlots.get(i).setDate(year, month, dayOfMonth);
        }
    }

    public int getNumberOfTimes() {
        return NUMBER_OF_TIMES;
    }

    public void setCurrentDate(int date) {
        mCurrentDate = date;
    }

    /**
     *
     * Returns the respective calendar of each time slot.
     * @param slot
     * @return
     */
    public Calendar getCalendar(int slot) {
        return timeSlots.get(slot).getCalendar();
    }

    /**
     * Iterate over the entire set of Times and check they are
     * all Set.
     * @return
     */
    public boolean allTimesSet() {
        for(int i = 0; i < timeSlots.size(); i++){
            int key = timeSlots.keyAt(i);
            if(!timeSlots.get(key).isSet()){
                return false;
            }
        }
//        if(!mSensorTIme.isTimeSet()){
//            return false;
//        }
        return true;
    }

    public int getCurrentDate() {
        return this.mCurrentDate;
    }

    public void attempt() {
        for(int i = 0; i < timeSlots.size(); i++){
            int key = timeSlots.keyAt(i);
            timeSlots.get(key).setAttempted(true);
        }
//        mSensorTIme.setAttempted(true);
    }

    public void saveTimes() {
        //Go through the List of Observable Calendars and Save
        //their corresponding reminders
        ReminderRepository repo = new ReminderRepository(getApplication());
        for(int i = 1; i <= NUMBER_OF_TIMES; i++){
            Calendar c = timeSlots.get(i).getCalendar();
            if(c != null) {
                Reminder r = new Reminder(i, c.getTime());
                repo.addReminder(r);
            }
        }

        //Save the Sensor reminder time
//        Reminder r = new Reminder(SENSOR, mSensorTIme.getTimeCalendar().getTime());
//        repo.addReminder(r);
        //Set all the alarms
        setAlarms();
    }

    private void setAlarms() {
        AlarmController ac = new AlarmController(getApplication());
        for (int i = 1; i <= NUMBER_OF_TIMES; i++) {
            Calendar c = getCalendar(i);
            //Schedule ALARM
            if(c != null) {
                ac.scheduleReminder(i, c);
            }
        }

//        scheduleSensorReminders();
    }

    /*
    private void scheduleSensorReminders() {
        AlarmController ac = new AlarmController(getApplication());
        Calendar c = mSensorTIme.getTimeCalendar();

        //Get the dates for the 3 Recording days.
        Calendar d1 = timeSlots.get(DAY_ONE).getCalendar();
        d1.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
        d1.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
        ac.scheduleSensorReminder(d1);

        Calendar d2 = timeSlots.get(DAY_TWO).getCalendar();
        d2.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
        d2.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
        ac.scheduleSensorReminder(d2);

        Calendar d3 = timeSlots.get(DAY_THREE).getCalendar();
        d3.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
        d3.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
        ac.scheduleSensorReminder(d3);

    }


    public ObservableCalendar getSensorTime() {
        return mSensorTIme;
    }
     */

    public void setDefaultReviewAlarms() {
        SharedPreferences sharedPref = getApplication().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        int hours = sharedPref.getInt(REVIEWTIME_HOUR, DEFAULT_REVIEW_HOUR);
        int minutes = sharedPref.getInt(REVIEWTIME_MIN, DEFAULT_REVIEW_MINUTE);

        //Get the 3 days for the review notifications
        Calendar day1 = timeSlots.get(DAY_ONE).getCalendar();
        Calendar day2 = timeSlots.get(DAY_TWO).getCalendar();
        Calendar day3 = timeSlots.get(DAY_THREE).getCalendar();
        //Set the times to when the review day reminder is scheduled
        day1.set(Calendar.HOUR_OF_DAY, hours);
        day1.set(Calendar.MINUTE, minutes);
        day2.set(Calendar.HOUR_OF_DAY, hours);
        day2.set(Calendar.MINUTE, minutes);
        day3.set(Calendar.HOUR_OF_DAY, hours);
        day3.set(Calendar.MINUTE, minutes);

        //Get all the household members
        List<HouseholdMember> hms = new HouseholdMemberRepository(getApplication()).getHouseholdMemberList();
        AlarmController ac = new AlarmController(getApplication());
        for(HouseholdMember hm : hms){
            String ppid = hm.getParticipantHouseholdMemberId();
            ac.scheduleDefaultRecordReviewNotification(ppid+"1", day1);
            ac.scheduleDefaultRecordReviewNotification(ppid+"2", day2);
            ac.scheduleDefaultRecordReviewNotification(ppid+"3", day3);
        }
    }
}
