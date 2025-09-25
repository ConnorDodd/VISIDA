package bo.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

import bo.typeconverter.TimestampConverter;

@Entity
public class Reminder {

    @PrimaryKey
    private int reminderDay;

    @TypeConverters(TimestampConverter.class)
    private Date date;

    public Reminder(){
    }

    public Reminder(int day, Date date){
        this.reminderDay = day;
        this.date = date;
    }

    public int getReminderDay() {
        return reminderDay;
    }

    public void setReminderDay(int reminderDay) {
        this.reminderDay = reminderDay;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
