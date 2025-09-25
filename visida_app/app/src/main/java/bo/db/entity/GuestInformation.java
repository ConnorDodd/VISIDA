package bo.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity
public class GuestInformation implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private long geustInfoId;
    private int adultMaleGuests;
    private int adultFemaleGuests;
    private int childGuests;

    public GuestInformation(int adultMaleGuests, int adultFemaleGuests, int childGuests) {
        this.adultMaleGuests = adultMaleGuests;
        this.adultFemaleGuests = adultFemaleGuests;
        this.childGuests = childGuests;
    }

    public long getGeustInfoId() {
        return geustInfoId;
    }

    public void setGeustInfoId(long geustInfoId) {
        this.geustInfoId = geustInfoId;
    }

    public int getAdultMaleGuests() {
        return adultMaleGuests;
    }

    public void setAdultMaleGuests(int adultMaleGuests) {
        this.adultMaleGuests = adultMaleGuests;
    }

    public int getAdultFemaleGuests() {
        return adultFemaleGuests;
    }

    public void setAdultFemaleGuests(int adultFemaleGuests) {
        this.adultFemaleGuests = adultFemaleGuests;
    }

    public int getChildGuests() {
        return childGuests;
    }

    public void setChildGuests(int childGuests) {
        this.childGuests = childGuests;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GuestInformation> CREATOR = new Creator<GuestInformation>(){

        @Override
        public GuestInformation createFromParcel(Parcel source) {
            return new GuestInformation(source);
        }

        @Override
        public GuestInformation[] newArray(int size) {
            return new GuestInformation[size];
        }
    };
    public static Creator<GuestInformation> getCREATOR(){return CREATOR;}

    protected GuestInformation(Parcel in){
        this.adultMaleGuests = in.readInt();
        this.adultFemaleGuests= in.readInt();
        this.childGuests = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(adultMaleGuests);
        dest.writeInt(adultFemaleGuests);
        dest.writeInt(childGuests);
    }
}
