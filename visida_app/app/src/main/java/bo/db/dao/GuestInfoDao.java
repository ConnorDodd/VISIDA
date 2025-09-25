package bo.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import bo.db.entity.GuestInformation;

@Dao
public interface GuestInfoDao {
    @Insert
    long insert(GuestInformation guestInformation);

    @Query("SELECT * FROm GuestInformation")
    List<GuestInformation> all();
}
