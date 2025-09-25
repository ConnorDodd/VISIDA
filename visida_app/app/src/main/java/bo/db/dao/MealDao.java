package bo.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import bo.db.entity.FoodItem;
import bo.db.entity.Meal;

@Dao
public interface MealDao {
    @Query("DELETE FROM Meal WHERE mealId IS :id")
    void delete(long id);

    @Query("SELECT * FROM Meal")
    List<Meal> getAll();

    @Query("SELECT * FROM Meal WHERE mealId IS :mealId")
    Meal getMeal(long mealId);

    @Insert
    long[] insert(Meal... meals);

    @Update
    void update(Meal meal);

    @Query("SELECT * FROM Meal WHERE finalized IS 0")
    List<Meal> getAllUnfinalized();

    @Query("SELECT * FROM FoodItem WHERE mealId IS :id " +
            "AND eatingOccasionId IS 0")
    List<FoodItem> getDishes(Long id);

    @Query("SELECT * FROM Meal WHERE finalized IS 0 " +
            "AND datetime(startTime) > datetime('now', 'localtime', '-1 hours')")
    Meal getRecentMeal();
}
