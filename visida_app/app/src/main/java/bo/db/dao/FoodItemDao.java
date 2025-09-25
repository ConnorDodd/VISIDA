package bo.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import bo.db.entity.FoodItem;

import static androidx.room.OnConflictStrategy.REPLACE;

/**
 * Created by jnc985 on 15-Dec-17.
 */

@Dao
public interface FoodItemDao {
    @Query("SELECT * FROM fooditem")
    List<FoodItem> getAll();

    @Delete
    void deleteFoodItem(FoodItem fi);

    @Insert(onConflict = REPLACE)
    Long insert(FoodItem fi);

    @Insert
    Long[] insert(FoodItem... fi);

    @Update
    int update(FoodItem fi);

    @Query("SELECT * FROM fooditem where eatingOccasionId IS :eoId AND didnteat IS 0")
    List<FoodItem> getAllFoodItemsForEatingOccasion(long eoId);

    @Query("SELECT * FROM fooditem where eatingOccasionId IS :eoId")
    LiveData<List<FoodItem>> getAllObservableFoodItemsForEatingOccasion(long eoId);

    @Query("SELECT * FROM FoodItem WHERE mealId IS :mealId")
    LiveData<List<FoodItem>> getObservableDishesForMeal(long mealId);

    @Query("SELECT * FROM fooditem where foodItemId IS :fiId")
    List<FoodItem> getFoodItem(Long fiId);

    @Query("UPDATE FoodItem SET leftoverImageUrl = :leftoverImage, " +
            "leftoverAudioUrls = :leftoverAudio "+
            "WHERE imageUrl IS :imageName")
    void updateMealLeftoversAudio(String imageName, String leftoverImage, String leftoverAudio);

    @Query("UPDATE FoodItem SET leftoverImageUrl = :leftoverImage, " +
            "leftoverDescription = :leftoverdescription "+
            "WHERE imageUrl IS :imageName")
    void updateMealLeftoversText(String imageName, String leftoverImage, String leftoverdescription);

    @Query("UPDATE FoodItem SET guestInfoId = :guestInfoId " +
            "WHERE baseFoodItemId IS :baseId")
    void updateGuestInfo(long baseId, long guestInfoId);
}
