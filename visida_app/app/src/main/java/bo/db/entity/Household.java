package bo.db.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.annotation.NonNull;

import java.util.Date;
import java.util.List;

import bo.typeconverter.TimeTypeConverter;
import bo.typeconverter.TimestampConverter;

/**
 * Created by jnc985 on 29-Jan-18.
 */

@Entity
public class Household {

    @PrimaryKey
    @NonNull
    private String householdId;
    private String country;
    @TypeConverters(TimestampConverter.class)
    private Date creationTime;                      //Time the household is created
    @TypeConverters(TimestampConverter.class)
    private Date exportTime;                        //Timestamp of when the household data is exported
    @TypeConverters(TimeTypeConverter.class)
    private Date finalizeTime;                      //The time the user would like to be notified to finalize their day
    private boolean consent;


    @Ignore
    private int versionCode;
    @Ignore
    private String versionName;
    @Ignore
    private List<HouseholdMember> householdMembers;
    @Ignore
    private long latitude;
    @Ignore
    private long longitude;
    @Ignore
    private boolean contextualInfoEnabled;
    @Ignore
    private boolean textDescriptionEnabled;
    @Ignore
    private List<Recipe> householdRecipes;
    @Ignore
    private List<GuestInformation> householdGuestInformation;

    public List<Meal> getHouseholdMeals() {
        return householdMeals;
    }

    public void setHouseholdMeals(List<Meal> householdMeals) {
        this.householdMeals = householdMeals;
    }

    @Ignore
    private List<Meal> householdMeals;

    public List<HouseholdMember> getHouseholdMemberList() {
        return householdMembers;
    }

    public Household(){
        this.creationTime = new Date();
    }

    public void setHouseholdMemberList(List<HouseholdMember> householdMemberList) {
        this.householdMembers = householdMemberList;
    }

    public String getHouseholdId() {
        return householdId;
    }

    public void setHouseholdId(String householdId) {
        this.householdId = householdId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getExportTime() {
        return exportTime;
    }

    public void setExportTime(Date ExportTime) {
        this.exportTime = ExportTime;
    }

    public Date getFinalizeTime() {
        return finalizeTime;
    }

    public void setFinalizeTime(Date finalizeTime) {
        this.finalizeTime = finalizeTime;
    }

    public long getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    public void setLongitude(long longitude) {
        this.longitude = longitude;
    }

    public boolean isContextualInfoEnabled() {
        return contextualInfoEnabled;
    }

    public void setContextualInfoEnabled(boolean contextualInfoEnabled) {
        this.contextualInfoEnabled = contextualInfoEnabled;
    }

    public boolean isTextDescriptionEnabled() {
        return textDescriptionEnabled;
    }

    public void setTextDescriptionEnabled(boolean textDescriptionEnabled) {
        this.textDescriptionEnabled = textDescriptionEnabled;
    }

    public List<Recipe> getHouseholdRecipes() {
        return householdRecipes;
    }

    public List<HouseholdMember> getHouseholdMembers() {
        return householdMembers;
    }

    public void setHouseholdMembers(List<HouseholdMember> householdMembers) {
        this.householdMembers = householdMembers;
    }

    public List<GuestInformation> getHouseholdGuestInformation() {
        return householdGuestInformation;
    }

    public void setHouseholdGuestInformation(List<GuestInformation> householdGuestInformation) {
        this.householdGuestInformation = householdGuestInformation;
    }

    public void setHouseholdRecipes(List<Recipe> householdRecipes) {
        this.householdRecipes = householdRecipes;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versoioCode) {
        this.versionCode = versoioCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Household household = (Household) o;

        if (!householdId.equals(household.householdId)) return false;
        return householdMembers != null ? householdMembers.equals(household.householdMembers) : household.householdMembers == null;
    }

    public void setConsent(boolean consent) {
        this.consent = consent;
    }

    public boolean isConsent(){
        return this.consent;
    }
}
