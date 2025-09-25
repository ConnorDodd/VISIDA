package bo.db.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

import bo.typeconverter.TimestampConverter;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Created by jnc985 on 06-Mar-18.
 */

@Entity(indices = {
        @Index(value = "ingredientId", name="ingredientId"),
        @Index(value = "recipeId", name = "recipeId")},
        foreignKeys = @ForeignKey(
                entity = Recipe.class,
                parentColumns = "recipeId",
                childColumns = "recipeId",
                onDelete = CASCADE))
public class IngredientCapture implements ImageAudioProvider{

    @PrimaryKey(autoGenerate = true)
    private long ingredientId;
    private long recipeId;
    private String imageUrl;
    private String audioUrl;
    private String description;
    @TypeConverters(TimestampConverter.class)
    private Date captureTime;

    public IngredientCapture(){
        this.captureTime = new Date();
    }

    public long getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(long ingredientId) {
        this.ingredientId = ingredientId;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCaptureTime() {
        return captureTime;
    }

    public void setCaptureTime(Date captureTime) {
        this.captureTime = captureTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IngredientCapture that = (IngredientCapture) o;

        if (ingredientId != that.ingredientId) return false;
        if (recipeId != that.recipeId) return false;
        if (imageUrl != null ? !imageUrl.equals(that.imageUrl) : that.imageUrl != null)
            return false;
        return audioUrl != null ? audioUrl.equals(that.audioUrl) : that.audioUrl == null;
    }

    @Override
    public String getImageName() {
        return getImageUrl();
    }

    @Override
    public String getAudioName() {
        return getAudioUrl();
    }
}