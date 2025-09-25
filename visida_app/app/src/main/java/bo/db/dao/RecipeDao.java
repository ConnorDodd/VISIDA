package bo.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import bo.db.entity.Recipe;

/**
 * Created by jnc985 on 15-Dec-17.
 */

@Dao
public interface RecipeDao {

    @Query("SELECT * FROM Recipe r LEFT JOIN IngredientCapture i ON r.recipeId = i.recipeId")
    LiveData<List<Recipe>> getAll();

    @Query("SELECT * FROM Recipe")
    LiveData<List<Recipe>> getAllObservable();

    @Query("SELECT * FROM Recipe WHERE recipeId IS :recipeId")
    Recipe get(Long recipeId);

    @Delete
    void deleteRecipe(Recipe recipe);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Long insert(Recipe recipe);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Long[] insert(Recipe... recipe);

    @Update
    void update(Recipe recipe);

    @Query("SELECT * FROM Recipe where isLocked IS 0")
    List<Recipe> getAllUnlocked();

    @Query("SELECT * FROM Recipe where isLocked IS 0")
    LiveData<List<Recipe>> getAllObservableUnlocked();

    @Query("SELECT COUNT(*) FROM Recipe")
    int count();
}
