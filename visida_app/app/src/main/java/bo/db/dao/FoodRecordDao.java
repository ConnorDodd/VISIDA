package bo.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import bo.db.entity.FoodRecord;


/**
 * Created by jnc985 on 15-Dec-17.
 */

@Dao
public interface FoodRecordDao {

    @Insert
    Long[] insert(FoodRecord... foodRecords);

    @Update
    void update(FoodRecord[] foodRecords);

    @Query("SELECT * FROM foodrecord WHERE foodRecordId IS :frId")
    List<FoodRecord> getFoodRecord(long frId);

    @Query("SELECT * FROM foodrecord where householdMemberId IS :hmId")
    List<FoodRecord> getAllFoodRecordsForHouseholdMember(long hmId);

    @Query("SELECT * FROM foodrecord where householdMemberId IS :hmId")
    LiveData<List<FoodRecord>> getObservableAllFoodRecordsForHouseholdMember(long hmId);

    @Query("SELECT * FROM foodrecord WHERE householdMemberId is :hmId AND strftime('%Y-%m-%d', date) IS strftime('%Y-%m-%d',:date)")
    List<FoodRecord> getTodaysFoodRecordForHouseholdMember(long hmId, String date);

    @Query("SELECT * FROM FoodRecord")
    List<FoodRecord> getAllFoodRecords();

    @Query("SELECT a.* FROM FoodRecord AS a LEFT JOIN  HouseholdMember " +
            "ON a.householdMemberId = HouseholdMember.uid " +
            "WHERE " +
            "strftime('%Y-%m-%d', a.date) IS strftime('%Y-%m-%d',datetime('now','localtime'))" +
            "AND " +
            "HouseholdMember.participantHouseholdMemberId IS :ppid")
    FoodRecord getTodaysFoodRecordForPpId(String ppid);
}
