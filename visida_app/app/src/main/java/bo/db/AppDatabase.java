package bo.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import bo.db.dao.EatingOccasionDao;
import bo.db.dao.FoodItemDao;
import bo.db.dao.FoodRecordDao;
import bo.db.dao.GuestInfoDao;
import bo.db.dao.HouseholdDao;
import bo.db.dao.HouseholdMemberDao;
import bo.db.dao.IngredientDao;
import bo.db.dao.MealDao;
import bo.db.dao.NotificationDoa;
import bo.db.dao.RecipeDao;
import bo.db.dao.ReminderDao;
import bo.db.entity.EatingOccasion;
import bo.db.entity.EatingOccasionNotification;
import bo.db.entity.FoodItem;
import bo.db.entity.FoodRecord;
import bo.db.entity.GuestInformation;
import bo.db.entity.Household;
import bo.db.entity.HouseholdMember;
import bo.db.entity.IngredientCapture;
import bo.db.entity.Meal;
import bo.db.entity.Recipe;
import bo.db.entity.Reminder;
import bo.db.entity.ReviewNotification;

/**
 * Created by jnc985 on 14-Dec-17.
 */

@Database(entities = {Household.class, HouseholdMember.class, FoodRecord.class,
                        EatingOccasion.class, FoodItem.class, Recipe.class, IngredientCapture.class,
                        EatingOccasionNotification.class, ReviewNotification.class, Meal.class,
                        Reminder.class, GuestInformation.class},
        version = 9)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DB_NAME = "appdatabase.db";
    private static AppDatabase INSTANCE;

    public static final Executor executor = Executors.newFixedThreadPool(3); // change according to your requirements

    public abstract HouseholdDao getHouseholdDao();
    public abstract HouseholdMemberDao getHouseholdMemberDao();
    public abstract FoodRecordDao getFoodRecordDao();
    public abstract EatingOccasionDao getEatingOccasionDao();
    public abstract FoodItemDao getFoodItemDao();
    public abstract IngredientDao getIngredientDao();
    public abstract RecipeDao getRecipeDao();
    public abstract NotificationDoa getNotificationDao();
    public abstract MealDao getMealDao();
    public abstract ReminderDao getReminderDao();
    public abstract GuestInfoDao getGuestInfoDao();



    /**
     * Get the instance of AppDatabase.
     * Iplemented as a singleton class.
     * @param context
     * @return
     */
    public static AppDatabase getInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context, AppDatabase.class, DB_NAME)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    .build();
        }
        return INSTANCE;
    }

    public static void setInstance(AppDatabase db){
        INSTANCE = db;
    }

    @VisibleForTesting
    public static final Migration MIGRATION_1_2 = new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            //Add the new columns
            database.execSQL("ALTER TABLE FoodItem "
                    + "ADD COLUMN description TEXT");
            database.execSQL("ALTER TABLE FoodItem "
                    + "ADD COLUMN leftoverDescription TEXT");
            database.execSQL("ALTER TABLE Recipe "
                    + "ADD COLUMN recipeNameText TEXT");
            database.execSQL("ALTER TABLE IngredientCapture "
                    + "ADD COLUMN description TEXT");
            //Add Empty Strings to the new descriptions
            database.execSQL("UPDATE FoodItem SET description= ''");
            database.execSQL("UPDATE FoodItem SET leftoverDescription = ''");
            database.execSQL("UPDATE Recipe SET recipeNameText = ''");
            database.execSQL("UPDATE IngredientCapture SET description = ''");
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2,3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `Reminder`" +
                    "('reminderDay' INTEGER NOT NULL, 'date' TEXT, " +
                    "PRIMARY KEY('reminderDay'))");
	        database.execSQL("ALTER TABLE EatingOccasion "
                    + "ADD COLUMN recipeIds TEXT");
            database.execSQL("UPDATE EatingOccasion SET recipeIds = ''");
            database.execSQL("ALTER TABLE Meal "
                    + "ADD COLUMN recipeIds TEXT");
        }
    };

    public static final Migration MIGRATION_3_4 = new Migration(3,4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE FoodRecord "
                    + "ADD COLUMN reviewed INTEGER NOT NULL default 0");

            //Update the table if the FoodRecord is older than 24hrs
            //(less than 24hrs before now) then set reviewed to true (1)
            database.execSQL("UPDATE FoodRecord " +
                    "SET reviewed = CASE " +
                    "WHEN datetime(date) < datetime('now', 'localtime', '-24 hour') " +
                    "THEN 1 " +
                    "ELSE 0 END");
        }
    };

    public static final Migration MIGRATION_4_5 = new Migration(4,5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Household "
                    + "ADD COLUMN consent INTEGER NOT NULL default 0");
            database.execSQL("ALTER TABLE ReviewNotification "
                    + "ADD COLUMN ppid TEXT");
        }
    };

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE ReviewNotification "
                        + "ADD COLUMN foodRecordId INTEGER NOT NULL default -1");
            database.execSQL("ALTER TABLE ReviewNotification "
                    + "ADD COLUMN deliveryDate TEXT default -1");
            database.execSQL("ALTER TABLE ReviewNotification "
                    + "ADD COLUMN notificationId INTEGER NOT NULL default -1");

            //Rename old table
            database.execSQL("ALTER TABLE ReviewNotification RENAME TO ReviewNotification_Old");
            database.execSQL("DROP INDEX notificationId");

            //Create a new table with all the columns
            database.execSQL("CREATE TABLE IF NOT EXISTS ReviewNotification " +
                    "(reviewNotificationId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "seen INTEGER NOT NULL, " +
                    "ppid TEXT, " +
                    "issueDate TEXT, " +
                    "deliveryDate TEXT, " +
                    "foodRecordId INTEGER NOT NULL, " +
                    "notificationId INTEGER NOT NULL)");
            database.execSQL("CREATE INDEX notificationId ON ReviewNotification (`reviewNotificationId`)");

            //Insert old data into new table
            database.execSQL("INSERT INTO ReviewNotification(reviewNotificationId, seen, ppid, issueDate, deliveryDate, foodRecordId, notificationId)" +
                    " SELECT recordId, seen, ppid, issueDate, deliveryDate, foodRecordId, notificationId " +
                    " FROM ReviewNotification_Old");

            //Drop old table
            database.execSQL("DROP TABLE ReviewNotification_Old");
        }
    };

    public static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            //Update EatingOccasionNotification Table
            database.execSQL("ALTER TABLE EatingOccasionNotification " +
                    "ADD COLUMN notificationId INTEGER NOT NULL default 1");
            database.execSQL("ALTER TABLE EatingOccasionNotification " +
                    "ADD COLUMN ppid TEXT");
            database.execSQL("ALTER TABLE EatingOccasionNotification " +
                    "ADD COLUMN issueDate TEXT");
            database.execSQL("ALTER TABLE EatingOccasionNotification " +
                    "ADD COLUMN deliveryDate TEXT");
        }

    };

    public static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            //Update the FoodItem table
            database.execSQL("ALTER TABLE FoodItem " +
                             "ADD COLUMN baseFoodItemId INTEGER NOT NULL default -1");
            database.execSQL("ALTER TABLE FoodItem " +
                    "ADD COLUMN guestInfoId INTEGER NOT NULL default -1");

            //Rename old table
            database.execSQL("ALTER TABLE Meal RENAME TO Meal_Old");

            //Create a new table with all the columns
            database.execSQL("CREATE TABLE IF NOT EXISTS Meal " +
                    "(mealId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "finalized INTEGER NOT NULL, " +
                    "startTime TEXT, " +
                    "guestInfoCaptured INTEGER NOT NULL, " +
                    "recipeIds TEXT)");

            //Insert old data into new table
            database.execSQL("INSERT INTO Meal(mealId, finalized, startTime, guestInfoCaptured, recipeIds)" +
                    " SELECT mealId, finalized, startTime, guestInfoCaptured, recipeIds " +
                    " FROM Meal_Old");

            //Drop old table
            database.execSQL("DROP TABLE Meal_Old");

            //Create GuestInfo Table
            database.execSQL("CREATE TABLE IF NOT EXISTS GuestInformation " +
                    "(geustInfoId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "adultMaleGuests INTEGER NOT NULL, " +
                    "adultFemaleGuests INTEGER NOT NULL, " +
                    "childGuests INTEGER NOT NULL)");
        }

    };



    public static final Migration MIGRATION_8_9 = new Migration(8,9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            // Create the new table
            database.execSQL("CREATE TABLE hm_new (uid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "householdId TEXT, participantHouseholdId TEXT, name TEXT, " +
                            "participantHouseholdMemberId TEXT, avatar TEXT, age REAL NOT NULL, " +
                            "isFemale INTEGER NOT NULL, isMother INTEGER NOT NULL, isBreastfed " +
                            "INTEGER NOT NULL, lifeStage TEXT)");

            // Copy the data
            database.execSQL("INSERT INTO hm_new (uid, householdId, participantHouseholdId, name, participantHouseholdMemberId, avatar, age, isFemale, isMother, isBreastfed, lifeStage) " +
                    "SELECT uid, householdId, participantHouseholdId, name, participantHouseholdMemberId, avatar, age, isFemale, isMother, isBreastfed, lifeStage " +
                    "FROM householdmember");
            // Remove the old table
            database.execSQL("DROP TABLE householdmember");
            // Change the table name to the correct one
            database.execSQL("ALTER TABLE hm_new RENAME TO householdmember");
        }
    };
}
