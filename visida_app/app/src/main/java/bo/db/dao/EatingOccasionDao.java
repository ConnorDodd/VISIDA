package bo.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import bo.db.entity.EatingOccasion;

/**
 * Created by jnc985 on 15-Dec-17.
 */

@Dao
public interface EatingOccasionDao {
    @Query("SELECT * FROM eatingoccasion")
    List<EatingOccasion> getAll();

    @Query("SELECT * FROM eatingoccasion WHERE eatingOccasionId IN (:eoIds)")
    List<EatingOccasion> get(Long[] eoIds);

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    Long insert(EatingOccasion eatingOccasion);

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    Long[] insert(EatingOccasion... eatingOccasion);

    @Delete
    void delete(EatingOccasion... eos);

    @Update
    int update(EatingOccasion eo);

    @Query("SELECT * FROM eatingoccasion WHERE foodRecordId IS :frId")
    List<EatingOccasion> getAllEatingOccasionsForFoodRecord(long frId);

    @Query("SELECT * FROM eatingoccasion WHERE foodRecordId IS :frId AND finalized = 0")
    List<EatingOccasion> getAllNonFinalizedEatingOccasionsForFoodRecord(long frId);

    @Query("SELECT EatingOccasion.* FROM EatingOccasion INNER JOIN FoodRecord " +
            "ON FoodRecord.householdMemberId = :hmId " +
            "AND FoodRecord.foodRecordId = EatingOccasion.foodRecordId " +
            "AND EatingOccasion.finalized = 0")
    List<EatingOccasion> getAllNonFinalizedEatingOccasionsForHouseholdMember(long hmId);

    @Query("SELECT * from eatingoccasion WHERE eatingOccasionId is :eoId")
    List<EatingOccasion> getEatingOccasion(Long eoId);

   @Query("SELECT EatingOccasion.* FROM EatingOccasion LEFT OUTER JOIN FoodItem ON EatingOccasion.eatingOccasionId = FoodItem.eatingOccasionId " +
           "WHERE FoodItem.eatingOccasionId IS NULL")
    List<EatingOccasion> getEmpty();
}
