package bo.db.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bo.typeconverter.TimestampConverter;

/**
 * Created by jnc985 on 06-Mar-18.
 */

@Entity
public class Recipe implements ImageListProvider{

    @PrimaryKey (autoGenerate = true)
    private long recipeId;
    @TypeConverters(TimestampConverter.class)
    private Date captureTime;
    private String recipeNameAudioUrl;
    private String recipeNameText;
    private String finalImageUrl;
    private String methodAudioUrl;
    private boolean isLocked;

    @Ignore
    private List<IngredientCapture> ingredients;

    public Recipe() {
        this.captureTime = new Date();
    }

    public long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    public Date getCaptureTime() {
        return captureTime;
    }

    public void setCaptureTime(Date captureTime) {
        this.captureTime = captureTime;
    }

    public void setRecipeNameAudioUrl(String recipeNameAudioUrl) {
        this.recipeNameAudioUrl = recipeNameAudioUrl;
    }

    public String getRecipeNameAudioUrl() {
        return this.recipeNameAudioUrl;
    }

    public String getRecipeNameText() {
        return recipeNameText;
    }

    public void setRecipeNameText(String recipeNameText) {
        this.recipeNameText = recipeNameText;
    }
    public String getFinalImageUrl() {
        return finalImageUrl;
    }

    public void setFinalImageUrl(String finalImageUrl) {
        this.finalImageUrl = finalImageUrl;
    }

    public List<IngredientCapture> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<IngredientCapture> ingredients) {
        this.ingredients = ingredients;
    }

    public String getMethodAudioUrl() {
        return methodAudioUrl;
    }

    public void setMethodAudioUrl(String methodAudioUrl) {
        this.methodAudioUrl = methodAudioUrl;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    @Override
    public long getId() {
        return recipeId;
    }

    @Override
    public List<String> getImageNames() {
        List<String> imageNames = new ArrayList<>();
        //Add the final image first
        String finalImage = this.finalImageUrl == null ? "" : finalImageUrl;
        imageNames.add(finalImage);
        for(IngredientCapture ic : ingredients){
            imageNames.add(ic.getImageUrl());
        }
        return imageNames;
    }
}
