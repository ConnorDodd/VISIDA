package bo.db.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import bo.typeconverter.TimestampConverter;

/**
 * Created by jnc985 on 08-Jan-18.
 */

@Entity(indices = @Index(value = "foodItemId", name="FoodItemId"))
public class FoodItem implements Parcelable, ImageAudioProvider{

    @PrimaryKey(autoGenerate = true)
    private long foodItemId;
    private long eatingOccasionId;
    private long mealId;
    private String imageUrl;
    private String leftoverImageUrl;
    private String description;

    @TypeConverters({TimestampConverter.class})
    private Date captureTime;
    @TypeConverters({TimestampConverter.class})
    private Date finalizeTime;

    private boolean finalized;

    //TODO Change to list of urls for multiple descriptions. This will need a type converter to get the list. Store as Json?
    private String audioUrls;
    private String leftoverAudioUrls;

    private boolean didnteat;
    private String leftoverDescription;

    private long baseFoodItemId;
    private long guestInfoId;

    public FoodItem(){
        this.captureTime = new Date();
        this.finalizeTime = new Date(0);
        this.finalized = false;
        this.baseFoodItemId = -1;
        this.guestInfoId = -1;
    }

    public void finalizeItem(){
        this.finalizeTime = new Date();
        this.finalized = true;
    }

    public long getFoodItemId() {
        return foodItemId;
    }

    public void setFoodItemId(long foodItemId) {
        this.foodItemId = foodItemId;
    }

    public long getEatingOccasionId() {
        return eatingOccasionId;
    }

    public void setEatingOccasionId(long eatingOccasionId) {
        this.eatingOccasionId = eatingOccasionId;
    }

    public long getMealId() {
        return mealId;
    }

    public void setMealId(long mealId) {
        this.mealId = mealId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLeftoverImageUrl() {
        return leftoverImageUrl;
    }

    public void setLeftoverImageUrl(String leftoverImageUrl) {
        this.leftoverImageUrl = leftoverImageUrl;
    }

    public void setDescription(String description){
        this.description = description;
    }
    @Override
    public String getDescription(){
        return this.description;
    }


    public Date getCaptureTime() {
        return captureTime;
    }

    public void setCaptureTime(Date captureTime) {
        this.captureTime = captureTime;
    }

    public Date getFinalizeTime() {
        return finalizeTime;
    }

    public void setFinalizeTime(Date finalizeTime) {
        this.finalizeTime = finalizeTime;
    }

    public boolean isFinalized() {
        return finalized;
    }

    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }

    public String getAudioUrls() {
        return audioUrls;
    }

    public void setAudioUrls(String audioUrls) {
        this.audioUrls = audioUrls;
    }

    public String getLeftoverAudioUrls() {
        return leftoverAudioUrls;
    }

    public void setLeftoverAudioUrls(String leftoverAudioUrls) {
        this.leftoverAudioUrls = leftoverAudioUrls;
    }

    public boolean isDidnteat() {
        return didnteat;
    }

    public void setDidnteat(boolean didnteat) {
        this.didnteat = didnteat;
    }

    public String getLeftoverDescription() {
        return leftoverDescription;
    }

    public void setLeftoverDescription(String leftOverDescription) {
        this.leftoverDescription = leftOverDescription;
    }

    public long getBaseFoodItemId() {
        return baseFoodItemId;
    }

    public void setBaseFoodItemId(long baseFoodItemId) {
        this.baseFoodItemId = baseFoodItemId;
    }

    public long getGuestInfoId() {
        return guestInfoId;
    }

    public void setGuestInfoId(long guestInfoId) {
        this.guestInfoId = guestInfoId;
    }

    @Override
    public boolean equals(Object obj){
        if(obj == null) return false;
        if(!(obj instanceof FoodItem)) return false;

        FoodItem other = (FoodItem) obj;
        if(this.eatingOccasionId != other.getEatingOccasionId()) return false;


        String thisStart = TimestampConverter.toTimestamp(this.captureTime);
        String otherStart = TimestampConverter.toTimestamp(other.getCaptureTime());
        if(thisStart != null && otherStart != null) {
            if (!thisStart.equals(otherStart)) return false;
        }

        String thisEnd = TimestampConverter.toTimestamp(this.finalizeTime);
        String otherEnd = TimestampConverter.toTimestamp(other.getFinalizeTime());
        if(thisEnd != null && otherEnd != null) {
            if (!thisEnd.equals(otherEnd)) return false;
        }

        return true;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FoodItem> CREATOR = new Creator<FoodItem>() {
        @Override
        public FoodItem createFromParcel(Parcel in) {
            return new FoodItem(in);
        }

        @Override
        public FoodItem[] newArray(int size) {
            return new FoodItem[size];
        }
    };

    public static Creator<FoodItem> getCREATOR() {
        return CREATOR;
    }


    protected FoodItem(Parcel in) {
        this.foodItemId = in.readLong();
        this.eatingOccasionId = in.readLong();
        this.mealId = in.readLong();
        this.imageUrl = in.readString();
        this.leftoverImageUrl = in.readString();
        this.captureTime = new Date(in.readLong());
        this.finalizeTime = new Date(in.readLong());
        this.finalized = in.readByte() != 0;
        this.audioUrls = in .readString();
        this.leftoverAudioUrls = in.readString();
        this.didnteat = in.readByte() != 0;
        this.baseFoodItemId = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.foodItemId);
        dest.writeLong(this.eatingOccasionId);
        dest.writeLong(this.mealId);
        dest.writeString(imageUrl);
        dest.writeString(leftoverImageUrl);
        dest.writeLong(captureTime.getTime());
        dest.writeLong(finalizeTime.getTime());
        dest.writeByte((byte)(this.finalized ? 1 : 0));
        dest.writeString(this.audioUrls);
        dest.writeString(this.leftoverAudioUrls);
        dest.writeByte((byte)(this.didnteat ? 1 : 0));
        dest.writeLong(baseFoodItemId);
    }

    @Override
    public String getImageName() {
        return getImageUrl();
    }

    @Override
    public String getAudioName() {
        return getAudioUrls();
    }

    /**
     * Returns a new FoodItem with the same Image, Audio, Capture Time
     * EatingOccasionId and MealId as the current FoodItem
     * @return
     */
    public FoodItem copy(){
        FoodItem copy = new FoodItem();
        copy.setImageUrl(imageUrl);
        copy.setAudioUrls(audioUrls);
        copy.setCaptureTime(captureTime);
        copy.setEatingOccasionId(eatingOccasionId);
        copy.setMealId(mealId);
        copy.setDescription(description);
        copy.setBaseFoodItemId(foodItemId);
        return copy;
    }

    public boolean hasGuestInfo() {
        return guestInfoId != -1;
    }
}
