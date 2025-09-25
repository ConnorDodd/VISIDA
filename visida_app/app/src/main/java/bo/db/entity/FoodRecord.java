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

import bo.AppConstants;
import bo.typeconverter.TimestampConverter;

/**
 * Created by jnc985 on 06-Dec-17.
 */

@Entity(indices = @Index(value = "householdMemberId", name="hmId"),
        foreignKeys = @ForeignKey(
        entity = bo.db.entity.HouseholdMember.class,
        parentColumns = "uid",
        childColumns = "householdMemberId"))
public class FoodRecord implements Parcelable, IReviewable{

    @Ignore
    private static final long HOUR = 60 * 60 * 1000;

    //REF: https://www.bignerdranch.com/blog/room-data-storage-for-everyone/
    @PrimaryKey(autoGenerate = true)
    private long foodRecordId;

    private long householdMemberId;

    private boolean reviewed;

    @TypeConverters({TimestampConverter.class})
    private Date date;

    @Ignore
    private List<bo.db.entity.EatingOccasion> eatingOccasions;

    @Ignore
    private HouseholdMember householdMember;


    public FoodRecord(long householdMemberId) {
        this.householdMemberId = householdMemberId;
        this.date = new Date();
        eatingOccasions = new ArrayList<>();
        this.reviewed = false;
    }

    //region Getters and Setters
    public long getFoodRecordId() {
        return foodRecordId;
    }

    public void setFoodRecordId(long foodRecordId) {
        this.foodRecordId = foodRecordId;
    }

    public long getHouseholdMemberId() {
        return householdMemberId;
    }

    public void setHouseholdMemberId(long householdMemberId) {
        this.householdMemberId = householdMemberId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<EatingOccasion> getEatingOccasions() {
        return eatingOccasions;
    }

    public void setEatingOccasions(List<EatingOccasion> eatingOccasions) {
        this.eatingOccasions = eatingOccasions;
    }

    public void addEatingOccasion(EatingOccasion eo){
        if(this.eatingOccasions == null){
            this.eatingOccasions = new ArrayList<>();
        }
        this.eatingOccasions.add(eo);
    }

    public HouseholdMember getHouseholdMember() {
        return householdMember;
    }

    public void setHouseholdMember(HouseholdMember householdMember) {
        this.householdMember = householdMember;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    //endregion

    protected FoodRecord(Parcel in) {
        this.foodRecordId = in.readLong();
        this.householdMemberId = in.readLong();
        this.date = new Date(in.readLong());
        this.eatingOccasions = new ArrayList<>();
        in.readList(this.eatingOccasions, EatingOccasion.class.getClassLoader());
        this.householdMember = in.readParcelable(HouseholdMember.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.foodRecordId);
        dest.writeLong(this.householdMemberId);
        //Write data long to as Date is not parcelable.
        dest.writeLong(this.date.getTime());
        dest.writeList(this.eatingOccasions);
        dest.writeParcelable(this.householdMember, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FoodRecord> CREATOR = new Creator<FoodRecord>() {
        @Override
        public FoodRecord createFromParcel(Parcel in) {
            return new FoodRecord(in);
        }

        @Override
        public FoodRecord[] newArray(int size) {
            return new FoodRecord[size];
        }
    };

    @Override
    public boolean equals(Object obj){
        boolean equal = true;
        if(obj == null) {
            equal = false;//return false;
        }
        if(!(obj instanceof FoodRecord)) {
           equal = false;
        }

        FoodRecord other = (FoodRecord) obj;

        String thisDate = TimestampConverter.toTimestamp(this.date);
        String otherDate = TimestampConverter.toTimestamp(other.getDate());
        if(!thisDate.equals(otherDate)){
            equal = false;
        }
        if(this.getHouseholdMemberId() != other.getHouseholdMemberId()) {
            equal = false;
        }
        if(this.getFoodRecordId() != other.getFoodRecordId()) {
            equal = false;
        }

        return equal;
    }

    /**
     * Helper method to find the current eating occasion if one exists.
     * Searches through the list of eating occasions and returns the first
     * eating occasion which was started within the last hourh and has not
     * been finalised.
     * @return Current active eating occasion. Null if none are found.
     */
    public EatingOccasion getCurrentEatingOccasion(){
        //Search through all the eating occasions. Just chek all of them, not expected to have many in the list
        Date now = new Date();
        for(EatingOccasion eo : eatingOccasions){
            long diff = now.getTime() - eo.getStartTime().getTime();
            if(now.getTime() - eo.getStartTime().getTime() <= AppConstants.EATINGOCCASIONDURATION_HOURS * HOUR){
                //Check the EOid has not been finalized
                if(!eo.isFinalized()){
                    return eo;
                }
            }
        }
        return null;
    }

    /**
     * Method to return all NON Finalized eating occasions
     */
    public List<EatingOccasion> getNonFinlizedEatingOccasions(){
        List<EatingOccasion> nonFinalized = new ArrayList<>();
        for(EatingOccasion eo : this.eatingOccasions){
            if(!eo.isFinalized()){
                nonFinalized.add(eo);
            }
        }
        return nonFinalized;
    }


    @Override
    public String getHeaderString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE hh:mm a");
        return dateFormat.format(getDate());
    }

    @Override
    public boolean isValid() {
        return isReviewed();
    }

    @Override
    public List<? extends ImageAudioProvider> getReviewItems() {
        List<FoodItem> fis = new ArrayList<>();
        for(EatingOccasion eo : eatingOccasions){
            fis.addAll(eo.getFoodItems());
        }
        return fis;
    }

    @Override
    public long getId(){
        return getFoodRecordId();
    }
}
