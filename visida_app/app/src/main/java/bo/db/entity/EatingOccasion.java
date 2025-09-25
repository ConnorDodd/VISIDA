package bo.db.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bo.typeconverter.LongListConverter;
import bo.typeconverter.TimestampConverter;

/**
 * Created by jnc985 on 06-Dec-17.
 */

@Entity(indices = @Index(value = "foodRecordId", name="FrId"),
        foreignKeys = @ForeignKey(
        entity = FoodRecord.class,
        parentColumns = "foodRecordId",
        childColumns = "foodRecordId"))
public class EatingOccasion implements Parcelable, ImageListProvider, IReviewable{

    @PrimaryKey(autoGenerate = true)
    private long eatingOccasionId;

    private long foodRecordId;
    @TypeConverters({TimestampConverter.class})
    private Date startTime;

    @TypeConverters({TimestampConverter.class})
    private Date endTime;

    private boolean finalized;
    private boolean isBreastfeedOccasion;
    private boolean hasCondiments;

    @TypeConverters({LongListConverter.class})
    private List<Long> recipeIds;

    @Ignore
    private List<FoodItem> foodItems;

    public EatingOccasion(){
        this.startTime = new Date();
        //End Time cant be null due to the Parcelable and reading the date back in.
        //Make it the same so that the times still work when checking if dates are recent.
        this.endTime = new Date(startTime.getTime());
        this.finalized = false;
        this.recipeIds = new ArrayList<Long>();
    }

    //region Getters and Setters
    public Long getEatingOccasionId() {
        return eatingOccasionId;
    }

    public void setEatingOccasionId(Long eatingOccasionId) {
        this.eatingOccasionId = eatingOccasionId;
    }

    public Long getFoodRecordId() {
        return foodRecordId;
    }

    public void setFoodRecordId(Long foodRecordId) {
        this.foodRecordId = foodRecordId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the end time of the eating occasion. Only returns a valid date if the
     * Eating Occasion is ifnalized
     * @return Time the eating occasion was ended if finlized. If not finalised this will
     * be the same as the start time
     */
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public boolean isFinalized() {
        return finalized;
    }

    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }

    public boolean isBreastfeedOccasion() {
        return isBreastfeedOccasion;
    }

    public void setBreastfeedOccasion(boolean breastfeedOccasion) {
        isBreastfeedOccasion = breastfeedOccasion;
    }

    public boolean isHasCondiments() {
        return hasCondiments;
    }

    public void setHasCondiments(boolean hasCondiments) {
        this.hasCondiments = hasCondiments;
    }

    /**
     * Finalizes the Eating Occasion by setting the end time to NOW and the finalized flag to true.
     * This method does NOT update the database, it is not this objects job to do that.
     */
    public void finalise() {
        //Save the current date as the end time
        this.endTime = new Date();
        //Loop through each foodItem and finalizeItem
        if(foodItems != null) {
            for (FoodItem fi : foodItems) {
                if (!fi.isFinalized()) {
                    fi.finalizeItem();
                }
            }
        }
        this.finalized = true;
    }

    public List<Long> getRecipeIds() {
        return recipeIds;
    }

    public void setRecipeIds(List<Long> recipeIds) {
        this.recipeIds = recipeIds;
    }

    public List<FoodItem> getFoodItems() {
        return foodItems == null ? new ArrayList<FoodItem>() : foodItems;
    }

    public void setFoodItems(List<FoodItem> foodItems) {
        this.foodItems = foodItems;
    }

    public void addFoodItem(FoodItem fi){
        if(this.foodItems == null){
            this.foodItems = new ArrayList<>();
        }
        if(!this.foodItems.contains(fi)) {
            foodItems.add(fi);
        }
    }

    public static Creator<EatingOccasion> getCREATOR() {
        return CREATOR;
    }
    //endregion


    protected EatingOccasion(Parcel in) {
        this.eatingOccasionId = in.readLong();
        this.foodRecordId = in.readLong();
        this.startTime = new Date(in.readLong());
        this.endTime = new Date(in.readLong());
        this.finalized = in.readByte() != 0;
        this.isBreastfeedOccasion = in.readByte() != 0;
        this.hasCondiments = in.readByte() != 0;
        this.recipeIds = new ArrayList<>();
        in.readList(this.recipeIds, Long.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.eatingOccasionId);
        dest.writeLong(this.foodRecordId);
        dest.writeLong(this.startTime.getTime());
        dest.writeLong(this.endTime.getTime());
        dest.writeByte((byte) (this.finalized ? 1 : 0));
        dest.writeByte((byte) (this.isBreastfeedOccasion ? 1 : 0));
        dest.writeByte((byte) (this.hasCondiments ? 1 : 0));
        dest.writeList(this.recipeIds);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<EatingOccasion> CREATOR = new Creator<EatingOccasion>() {
        @Override
        public EatingOccasion createFromParcel(Parcel in) {
            return new EatingOccasion(in);
        }

        @Override
        public EatingOccasion[] newArray(int size) {
            return new EatingOccasion[size];
        }
    };

    @Override
    public boolean equals(Object obj){
        if(obj == null) return false;
        if(!(obj instanceof EatingOccasion)) return false;

        EatingOccasion other = (EatingOccasion) obj;
        if(this.eatingOccasionId != other.getEatingOccasionId()) return false;
        if(this.foodRecordId != other.getFoodRecordId()) return false;
        if(this.isBreastfeedOccasion != other.isBreastfeedOccasion()) return false;

        String thisStart = TimestampConverter.toTimestamp(this.startTime);
        String otherStart = TimestampConverter.toTimestamp(other.getStartTime());
        if(thisStart != null && otherStart != null) {
            if (!thisStart.equals(otherStart)) return false;
        }

        String thisEnd = TimestampConverter.toTimestamp(this.endTime);
        String otherEnd = TimestampConverter.toTimestamp(other.getEndTime());
        if(thisEnd != null && otherEnd != null) {
            if (!thisEnd.equals(otherEnd)) return false;
        }

        return true;
    }

    @Override
    public long getId() {
        return getEatingOccasionId();
    }

    @Override
    public List<String> getImageNames() {
        List<String> paths = new ArrayList<>();
        if(foodItems != null){
            for(FoodItem fi: foodItems){
                if(!fi.isFinalized()){
                    paths.add(fi.getImageUrl());
                }
            }
        }
        return paths;
    }

    @Override
    public List<? extends ImageAudioProvider> getReviewItems() {
        return foodItems;
    }

    @Override
    public String getHeaderString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE hh:mm a");
        return dateFormat.format(getStartTime());
    }

    @Override
    public boolean isValid() {
        return isFinalized();
    }
}
