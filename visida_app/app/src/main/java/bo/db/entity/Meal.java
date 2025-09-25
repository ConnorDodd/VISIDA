package bo.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bo.typeconverter.LongListConverter;
import bo.typeconverter.TimestampConverter;

@Entity
public class Meal {

    @PrimaryKey(autoGenerate = true)
    private long mealId;

    private boolean finalized;
    @TypeConverters({TimestampConverter.class})
    private Date startTime;
//    private int adultMaleGuests;
//    private int adultFemaleGuests;
//    private int childGuests;

    private boolean guestInfoCaptured;

    @TypeConverters({LongListConverter.class})
    private List<Long> recipeIds;

    public List<Long> getRecipeIds() {
        return recipeIds;
    }

    public void setRecipeIds(List<Long> recipeIds) {
        this.recipeIds = recipeIds;
    }

    public boolean isGuestInfoCaptured() {
        return guestInfoCaptured;
    }

    public void setGuestInfoCaptured(boolean guestInfoCaptured) {
        this.guestInfoCaptured = guestInfoCaptured;
    }

    public Meal(){
        this.startTime = new Date();
        this.recipeIds = new ArrayList<Long>();
    }

    public boolean isFinalized() {
        return finalized;
    }

    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public long getMealId() {
        return mealId;
    }

    public void setMealId(long mealId) {
        this.mealId = mealId;
    }

//    public int getAdultMaleGuests() {
//        return adultMaleGuests;
//    }
//
//    public void setAdultMaleGuests(int adultMaleGuests) {
//        this.adultMaleGuests = adultMaleGuests;
//    }
//
//    public int getAdultFemaleGuests() {
//        return adultFemaleGuests;
//    }
//
//    public void setAdultFemaleGuests(int adultFemaleGuests) {
//        this.adultFemaleGuests = adultFemaleGuests;
//    }
//
//    public int getChildGuests() {
//        return childGuests;
//    }
//
//    public void setChildGuests(int childGuests) {
//        this.childGuests = childGuests;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meal meal = (Meal) o;
        return mealId == meal.mealId &&
                finalized == meal.finalized;
                //&&
//                adultMaleGuests == meal.adultMaleGuests &&
//                adultFemaleGuests == meal.adultFemaleGuests &&
//                childGuests == meal.childGuests;
    }

}
