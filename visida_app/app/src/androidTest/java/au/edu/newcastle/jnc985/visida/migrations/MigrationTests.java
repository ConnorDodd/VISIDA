package au.edu.newcastle.jnc985.visida.migrations;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.room.Room;
import androidx.room.testing.MigrationTestHelper;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import bo.db.AppDatabase;
import bo.db.entity.EatingOccasion;
import bo.db.entity.EatingOccasionNotification;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.Household;
import bo.db.entity.IngredientCapture;
import bo.db.entity.Meal;
import bo.db.entity.Recipe;
import bo.db.entity.ReviewNotification;
import bo.typeconverter.TimestampConverter;

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static bo.db.AppDatabase.MIGRATION_1_2;
import static bo.db.AppDatabase.MIGRATION_2_3;
import static bo.db.AppDatabase.MIGRATION_3_4;
import static bo.db.AppDatabase.MIGRATION_4_5;
import static bo.db.AppDatabase.MIGRATION_5_6;
import static bo.db.AppDatabase.MIGRATION_6_7;
import static bo.db.AppDatabase.MIGRATION_7_8;
import static bo.db.AppDatabase.MIGRATION_8_9;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.is;

@RunWith(AndroidJUnit4.class)
public class MigrationTests {
    private static final String TEST_DB = "migration-test";

    @Rule
    public MigrationTestHelper helper = new MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
            AppDatabase.class.getCanonicalName(),
            new FrameworkSQLiteOpenHelperFactory());

    public MigrationTests() {
    }

    @Test
    public void migrate1To3() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 1);

        // db has schema version 1. insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.
        ContentValues hmValues = new ContentValues();
        long hmId = 1;
        String hmName = "hmname";
        hmValues.put("uid", hmId);
        hmValues.put("name", hmName);
        hmValues.put("age", 1);
        hmValues.put("isFemale", true);
        hmValues.put("isMother", false);
        hmValues.put("isBreastfed", false);
        db.insert("HouseholdMember", SQLiteDatabase.CONFLICT_REPLACE, hmValues);

        ContentValues foodRecordValues = new ContentValues();
        long frId = 1;
        foodRecordValues.put("foodRecordId", frId);
        foodRecordValues.put("householdMemberId", hmId);
        db.insert("FoodRecord", SQLiteDatabase.CONFLICT_REPLACE, foodRecordValues);

        ContentValues eoValues = new ContentValues();
        long eoId = 1;
        eoValues.put("eatingOccasionId", eoId);
        eoValues.put("foodRecordId", frId);
        eoValues.put("finalized", false);
        eoValues.put("isBreastfeedOccasion", false);
        eoValues.put("hasCondiments", false);
        db.insert("EatingOccasion", SQLiteDatabase.CONFLICT_REPLACE, eoValues);

        ContentValues fiValues = new ContentValues();
        long fiid = 1;
        long mealId = 1;
        fiValues.put("foodItemId", fiid);
        fiValues.put("eatingOccasionId", eoId);
        fiValues.put("imageUrl", "");
        fiValues.put("audioUrls", "");
        fiValues.put("mealId", mealId);
        fiValues.put("finalized", false);
        fiValues.put("didnteat", false);
        db.insert("FoodItem", SQLiteDatabase.CONFLICT_REPLACE, fiValues);

        ContentValues recipeValues = new ContentValues();
        long recipeId = 1;
        recipeValues.put("recipeId", recipeId);
        recipeValues.put("recipeNameAudioUrl", "");
        recipeValues.put("finalImageUrl", "");
        recipeValues.put("methodAudioUrl", "");
        recipeValues.put("isLocked", true);
        db.insert("Recipe", SQLiteDatabase.CONFLICT_REPLACE, recipeValues);

        ContentValues icValues = new ContentValues();
        long icId = 1;
        icValues.put("ingredientId", icId);
        icValues.put("recipeId", recipeId);
        icValues.put("imageUrl", "");
        icValues.put("imageUrl", "");
        db.insert("IngredientCapture", SQLiteDatabase.CONFLICT_REPLACE, icValues);

        // Prepare for the next version.
        db.close();

        // Re-open the database with version 2 and provide
        // MIGRATION_1_2 as the migration process. And Migration_2_3
        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_1_2, MIGRATION_2_3);

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
        FoodItem fi = getMigratedDatabase().getFoodItemDao().getFoodItem(fiid).get(0);
        assertThat(fi.getFoodItemId(), is(fiid));
        assertThat(fi.getEatingOccasionId(), is(eoId));
        //Check that the new String "textDescription" is empty.
        assertThat(fi.getDescription().isEmpty(), is(true));
        assertThat(fi.getLeftoverDescription().isEmpty(), is(true));

        //Check the Recipe
        Recipe recipe = getMigratedDatabase().getRecipeDao().get(recipeId);
        assertThat(recipe.getRecipeId(), is(recipeId));
        assertThat(recipe.getRecipeNameText().isEmpty(), is(true));

        //Check Ingredient Capture
        IngredientCapture ic = getMigratedDatabase().getIngredientDao().getAll().get(0);
        assertThat(ic.getIngredientId(), is(icId));
        assertThat(ic.getRecipeId(), is(recipeId));
        assertThat(ic.getDescription().isEmpty(), is(true));

        EatingOccasion eo = getMigratedDatabase().getEatingOccasionDao().getEatingOccasion(eoId).get(0);
        assertNotNull(eo.getRecipeIds());
    }

    @Test
    public void migrate2To3() throws IOException{
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 2);

        Cursor cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='Reminder'");
        assertThat(cursor.getCount(), is(0));

        db.close();

        //Re open db with updated version running the migration.
        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_2_3);

        cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='Reminder'");
        assertThat(cursor.getCount(), is(1));
    }

    @Test
    public void migrate3To4() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 3);

        //Insert a household member
        ContentValues hmValues = new ContentValues();
        long hmId = 1;
        String hmName = "hmname";
        hmValues.put("uid", hmId);
        hmValues.put("name", hmName);
        hmValues.put("age", 1);
        hmValues.put("isFemale", true);
        hmValues.put("isMother", false);
        hmValues.put("isBreastfed", false);
        db.insert("HouseholdMember", SQLiteDatabase.CONFLICT_REPLACE, hmValues);

        //Add Foodrecord > 24hrs old
        ContentValues oldFoodRecordValues = new ContentValues();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -2);
        String timeStamp = TimestampConverter.toTimestamp(cal.getTime());
        long oldFrid = 1;
        oldFoodRecordValues.put("foodRecordId", oldFrid);
        oldFoodRecordValues.put("householdMemberId", hmId);
        oldFoodRecordValues.put("date", timeStamp);
        db.insert("FoodRecord", SQLiteDatabase.CONFLICT_REPLACE, oldFoodRecordValues);

        //Add Foodrecord 10minutes old
        ContentValues foodRecordValues = new ContentValues();
        cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -10);
        timeStamp = TimestampConverter.toTimestamp(cal.getTime());
        long newFrId = 2;
        foodRecordValues.put("foodRecordId", newFrId);
        foodRecordValues.put("householdMemberId", hmId);
        foodRecordValues.put("date", timeStamp);
        db.insert("FoodRecord", SQLiteDatabase.CONFLICT_REPLACE, foodRecordValues);

        // Prepare for the next version.
        db.close();

        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_3_4);

        //Get the two food record from the database and check their reviewed status
        FoodRecord oldFr = getMigratedDatabase().getFoodRecordDao().getFoodRecord(oldFrid).get(0);
        assertThat(oldFr.isReviewed(), is(true));

        FoodRecord newFr =  getMigratedDatabase().getFoodRecordDao().getFoodRecord(newFrId).get(0);
        assertThat(newFr.isReviewed(), is(false));


    }

    @Test
    public void migrate4To5() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 4);

        //Insert a household
        ContentValues hhValues = new ContentValues();
        String hhID = "hhid";
        String country = "country";
        Date time = Calendar.getInstance().getTime();
        hhValues.put("householdId", hhID);
        hhValues.put("country", country);
        db.insert("Household", SQLiteDatabase.CONFLICT_REPLACE, hhValues);

        // Prepare for the next version.
        db.close();

        db = helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5);

        Household hh =  getMigratedDatabase().getHouseholdDao().getHousehold(hhID);
        assertThat(hh.isConsent(), is(false));

    }

    @Test
    public void migrate5To6() throws IOException {
        //Adds the foodRecordId column
        //Rename recordId to reviewNotificationId
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 5);

        //Insert a household
        ContentValues notificationValues = new ContentValues();
        notificationValues.put("recordId", 1L);
        notificationValues.put("seen", true);
        notificationValues.put("ppid", "ppid");
        db.insert("ReviewNotification", SQLiteDatabase.CONFLICT_REPLACE, notificationValues);

        ContentValues notificationValues2 = new ContentValues();
        notificationValues2.put("recordId", 2L);
        notificationValues2.put("seen", false);
        notificationValues2.put("ppid", "ppid2");
        db.insert("ReviewNotification", SQLiteDatabase.CONFLICT_REPLACE, notificationValues2);

        // Prepare for the next version.
        db.close();

        db = helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6);

        ReviewNotification rn1 = getMigratedDatabase().getNotificationDao().getReviewNotification(1L);
        assertThat(rn1.getReviewNotificationId(), is(1L));
        assertThat(rn1.getFoodRecordId(), is(-1L));
    }

    @Test
    public void migrate6To7() throws IOException {
        //Adds the foodRecordId column
        //Rename recordId to reviewNotificationId
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 6);

        /*

                    "ADD COLUMN notificationId INTEGER NOT NULL default 1");
                    "ADD COLUMN ppid TEXT");
                    "ADD COLUMN issueDate TEXT");
                    "ADD COLUMN deliveryDate TEXT");

         */
        //Insert a household
        ContentValues eon = new ContentValues();
        eon.put("eatingOccasionId", 1L);
        eon.put("seen", true);
        db.insert("EatingOccasionNotification", SQLiteDatabase.CONFLICT_REPLACE, eon);

        // Prepare for the next version.
        db.close();

        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7);

        List<EatingOccasionNotification> eons = getMigratedDatabase().getNotificationDao().getEatingOccasionNotifications(1l);
        EatingOccasionNotification eo1 = eons.get(0);
        assertThat(eo1.getNotificationId(), is(1));
    }

    @Test
    public void migrate7To8() throws IOException {
        //Adds the foodRecordId column
        //Rename recordId to reviewNotificationId
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 7);


        //Insert an old Meal
        ContentValues meal = new ContentValues();
        meal.put("adultMaleGuests", 1);
        meal.put("adultFemaleGuests", 1);
        meal.put("childGuests", 1);
        meal.put("finalized", false);
        meal.put("guestInfoCaptured", false);
        db.insert("Meal", SQLiteDatabase.CONFLICT_REPLACE, meal);

        // Prepare for the next version.
        db.close();

        db = helper.runMigrationsAndValidate(TEST_DB, 8, true, MIGRATION_7_8);

        List<Meal> meals = getMigratedDatabase().getMealDao().getAll();
        assertThat(meals.size(), is(1));
    }


    private AppDatabase getMigratedDatabase(){
        AppDatabase appDb = Room.databaseBuilder(InstrumentationRegistry.getTargetContext(),
                AppDatabase.class, TEST_DB)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                .build();
        helper.closeWhenFinished(appDb);
        return appDb;
    }
}
