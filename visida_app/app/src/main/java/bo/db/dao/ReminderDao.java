package bo.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import bo.db.entity.Reminder;

@Dao
public interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Reminder r);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Reminder... r);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Reminder r);

    @Query("SELECT * FROM Reminder")
    List<Reminder> getReminders();

    @Query("SELECT * FROM Reminder ORDER BY reminderDay ASC")
    LiveData<List<Reminder>> getObservableReminders();

    @Query("SELECT * FROM Reminder WHERE reminderDay IS :day LIMIT 1")
    Reminder getReminder(int day);



}
