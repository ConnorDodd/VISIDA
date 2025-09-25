package bo.scheduler;

import android.icu.util.Calendar;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import bo.db.entity.Reminder;

public class ObservableCalendar extends BaseObservable{
    protected static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    protected static final SimpleDateFormat niceDateFormat = new SimpleDateFormat("EEE-dd-MMM-yy");
    protected static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    protected String date;
    protected boolean dateSet;
    protected String time;
    protected boolean timeSet;
    protected boolean attempted;

    public ObservableCalendar(){
        this.dateSet = false;
        this.timeSet = false;
        this.attempted = false;
    }

    public ObservableCalendar(Reminder r){
        //Get the Date from the reminder
        Date d = r.getDate();
        //Convert the date into the two strings
        this.time = timeFormat.format(d);
        this.date = dateFormat.format(d);

        this.dateSet = true;
        this.timeSet = true;
        this.attempted = false;
    }


    @Bindable
    public String getDate(){
        return date;
    }

    @Bindable
    public String getTime(){
        return time;
    }

    @Bindable
    public String getFullTime(){
        String result = "";
        if(dateSet){
            Date d = null;
            try {
                d = dateFormat.parse(date);
                result += niceDateFormat.format(d) + "  ";
            } catch (ParseException e) {
                result += date + " ";
            }

        }
        if(timeSet){
            result += time;
        }
        return result;
    }

    @Bindable
    public boolean isTimeAttemptedSet(){return attempted && !timeSet;}

    @Bindable
    public boolean isTimeSet(){return timeSet;}

    @Bindable
    public boolean isDateSet(){return dateSet;}

    @Bindable
    public boolean isDateAttemptedSet(){
        return attempted && !dateSet;
    }

    @Bindable
    public boolean isSet() {
        return dateSet && timeSet;
    }

    public void setAttempted(boolean attempted) {
        this.attempted = attempted;
        notifyChange();
    }

    public void setDate(int year, int month, int dayOfMonth){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        this.date = dateFormat.format(c.getTime());
        this.dateSet = true;
        notifyChange();
    }

    public void setTime(int hourOfDay, int minute){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        this.time = timeFormat.format(c.getTime());
        this.timeSet = true;
        notifyChange();
    }

    public Calendar getCalendar(){
        this.attempted = true;
        if(timeSet && dateSet) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm");
            Date d = null;
            try {
                d = sdf.parse(date + "-" + time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            return cal;
        }
        return null;
    }


    public Calendar getTimeCalendar() {
        //Create a calendar with today but with the time
        try {
            if(this.time != null) {
                Date time = timeFormat.parse(this.time);
                Calendar c = Calendar.getInstance();
                c.setTime(time);
                return c;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
