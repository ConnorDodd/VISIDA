package bo.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import bo.db.entity.IngredientCapture;

/**
 * Created by jnc985 on 15-Dec-17.
 */

@Dao
public interface IngredientDao {
    @Query("SELECT * FROM IngredientCapture")
    List<IngredientCapture> getAll();

    @Delete
    void delete(IngredientCapture ingredient);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Long insert(IngredientCapture ingredient);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Long[] insert(IngredientCapture... condiment);

    @Update
    void update(IngredientCapture ingredient);

    @Query("SELECT * from ingredientcapture WHERE recipeId IS :recipeId")
    LiveData<List<IngredientCapture>> getObservableIngredientsForRecipe(long recipeId);

    @Query("SELECT * from ingredientcapture WHERE recipeId IS :recipeId")
    List<IngredientCapture> getIngredientsForRecipe(long recipeId);

    @Query("DELETE FROM IngredientCapture WHERE imageUrl IS NULL")
    void deleteEmptyIngredients();
}
