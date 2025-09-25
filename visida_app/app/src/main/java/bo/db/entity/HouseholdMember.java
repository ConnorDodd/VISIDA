package bo.db.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by jnc985 on 29-Nov-17.
 * POJO Busoness Object to represent a Household member.
 */
@Entity
        /*(foreignKeys = @ForeignKey(@ForeignKey(
        entity = Household.class,
        parentColumns = "householdId",
        childColumns = "householdId"))*/
public class HouseholdMember implements Parcelable{

    private static final String TAG = "HouseholdMember";

    @PrimaryKey(autoGenerate = true)
    private long uid;

    private String householdId;
    private String participantHouseholdId;
    private String name;
    private String participantHouseholdMemberId;
    private String avatar;      //String containing the path to the image used as avatar
    private float age;
    private boolean isFemale;
    private boolean isMother;
    private boolean isBreastfed;
    private String lifeStage;

    @Ignore
    private List<FoodRecord> foodRecords;

    public HouseholdMember(){
        this.householdId = "";      //REPLACE WITH HOUSEHOLD REFERENCE
        this.name = "";
        this.avatar = null;
        this.age = -1;
        this.isFemale = true;
        this.isMother = false;
        this.isBreastfed = false;
        this.lifeStage = "";
    }

    public HouseholdMember(int uid, String household, String name, String avatar, float age, boolean isMother, boolean isBreastfed) {
        this.uid = uid;
        this.householdId = household;
        this.name = name;
        this.avatar = avatar;
        this.age = age;
        this.isFemale = true;
        this.isMother = isMother;
        this.isBreastfed = isBreastfed;
        this.lifeStage = "";
    }

    public long getUid() { return uid; }

    public void setUid(long uid) { this.uid = uid; }

    public String getHouseholdId() {
        return householdId;
    }

    public void setHouseholdId(String householdId) {
        this.householdId = householdId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFemale() {
        return isFemale;
    }

    public void setFemale(boolean female) {
        isFemale = female;
    }

    public String getAvatar(){ return avatar; }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public float getAge() {
        return age;
    }

    public int getAgeInYears() {return (int)age;}

    public int getAgeInMonths(){
        float months = age % 1;
        months = months * 12;
        return Math.round(months);
    }

    public void setAge(float age) {
        this.age = age;
    }

    public boolean isMother() {
        return isMother;
    }

    public void setMother(boolean mother) {
        isMother = mother;
    }

    public boolean isBreastfed() {
        return isBreastfed;
    }

    public void setBreastfed(boolean breastfed) {
        isBreastfed = breastfed;
    }

    public String getLifeStage() {
        return lifeStage;
    }

    public void setLifeStage(String lifeStage) {
        this.lifeStage = lifeStage;
    }

    public List<FoodRecord> getFoodRecords() {
        return foodRecords;
    }

    public void setFoodRecords(List<FoodRecord> foodRecords) {
        this.foodRecords = foodRecords;
    }

    public String getParticipantHouseholdId() {
        return participantHouseholdId;
    }

    public void setParticipantHouseholdId(String participantHouseholdId) {
        this.participantHouseholdId = participantHouseholdId;
    }

    public String getParticipantHouseholdMemberId() {
        return participantHouseholdMemberId;
    }

    public void setParticipantHouseholdMemberId(String participantHouseholdMemberId) {
        this.participantHouseholdMemberId = participantHouseholdMemberId;
    }

    public static final Creator<HouseholdMember> CREATOR = new Creator<HouseholdMember>() {
        @Override
        public HouseholdMember createFromParcel(Parcel in) {
            return new HouseholdMember(in);
        }

        @Override
        public HouseholdMember[] newArray(int size) {
            return new HouseholdMember[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    protected HouseholdMember(Parcel in) {
        this.uid = in.readLong();
        householdId = in.readString();
        participantHouseholdId = in.readString();
        name = in.readString();
        participantHouseholdMemberId = in.readString();
        avatar = in.readString();
        age = in.readFloat();
        isFemale = in.readByte() != 0;
        isMother = in.readByte() != 0;
        isBreastfed = in.readByte() != 0;
        lifeStage = in.readString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.uid);
        out.writeString(this.householdId);
        out.writeString(this.participantHouseholdId);
        out.writeString(this.name);
        out.writeString(this.participantHouseholdMemberId);
        out.writeString(this.avatar);
        out.writeFloat(this.age);
        out.writeByte((byte) (this.isFemale ? 1 : 0));
        //https://stackoverflow.com/a/7089687
        out.writeByte((byte) (this.isMother ? 1 : 0));
        out.writeByte((byte) (this.isBreastfed ? 1 : 0));
        out.writeString(this.lifeStage);
    }

    @Override
    public boolean equals(Object obj){
        if(obj == null) return false;
        if(!(obj instanceof HouseholdMember)) return false;

        final HouseholdMember other = (HouseholdMember) obj;
        if(!this.getName().equals(other.getName())) return false;
        if(this.getAge() != other.getAge()) return false;
        if(this.isFemale != other.isFemale()) return false;
        if(!this.getHouseholdId().equals(other.getHouseholdId())) return false;
        if(!this.lifeStage.equals(other.getLifeStage())) return false;

        return true;
    }
}
