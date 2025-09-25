package bo.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import bo.db.entity.Household;

/**
 * Created by jnc985 on 14-Dec-17.
 */

@Dao
public interface HouseholdDao {
    @Query("SELECT * FROM Household where householdId IS :hhId")
    Household getHousehold(String hhId);

    @Query("SELECT * FROM household")
    List<Household> getAll();

    @Insert
    void insert(Household household);

    @Update
    void update(Household... hhs);
}
