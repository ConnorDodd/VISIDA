package bo;

public class AppConstants {
    public static final String ACTIVITY_LOG_TAG = "VISIDA";
    public static final String NAVBAR = "navbar";
    public static final String LOGFILENAME = "logcat.txt";
    public static final String READMEFILENAME = "About.txt";

    /**
     * Constant Values
     */
    public static final int DEFAULT_REVIEW_HOUR = 19;                       //Hours in Day for record review default time
    public static final int DEFAULT_REVIEW_MINUTE = 30;                     //Minutes of Hour for record review time
    public static final int FOODITEM_DESCRIPTION_MAX_LENGTH = 90;          // Maximum length a food item audio record can be. In seconds.
    public static final int EATINGOCCASIONDURATION_HOURS = 1;              //Duration of an Eaing Occasion
    public static final String FINALIZE_FRAGMENT = "finalisefooditemfragment";
    public static final String RECIPE_FINALE_IMAGE_DIALOG = "finalimagedialog";
    public static final String DATAEXPORTER_RESOURCE_NAME = "dataexporterresource";

    /**
     * Shared Preference Fields
     */
    public static final String PREFERENCES = "appPreferences";
    public static final String SETUP = "setup";
    public static final String STATE = "state";
    public static final String REMINDERS_SET = "remindersSet";
    public static final String REVIEWTIME_HOUR = "reviewtimehour";
    public static final String REVIEWTIME_MIN = "reviewtimeminute";
    public static final String HASBREASTFED = "hasbreastfed";
    public static final String PARTICIPANTHOUSEHOLDID= "participanthouseholdid";

    /**
     * File Names
     */
    public static final String EXPORT_MEDIA_DIR = "ExportMedia";
    public static final String ZIP_FILE = "ExportMedia.zip";
    public static final String JSON_FILE = "database.json";
    public static final String FIID_PLACEHOLDER = "#FIID#";                                   //Unique string to act as placeholder for FoodItem Id in image name
    public static final String IMAGE_NAME_FORMAT = "%s_%s_%d_%d_%s_%s.jpg";                   //HouseholdID_PPID_FRID_EOID_FIID_TIMESTAMP.jpg
    public static final String AUDIOONLY_FOODITEM_FILE_NAME_TEMPLATE = "%s_%s_%d_%d_" + FIID_PLACEHOLDER + "_%s_AUDIOONLY.mp3";     //hhId, ppId, frId, EoId, timestamp
    public static final String AUDIOFILE_NAME_TEMPLATE = "%s_%s_%d_%d_%s_%s.mp3";            //HouseholdID_PPID_FRID_EOID_FIID_TIMESTAMP.jpg
    public static final String INGREDIENT_AUDIOFILE_TEMPLATE = "%s_%d_%d_%s.mp3";            //HHID_RecipeID_IngredientID_timestamp.mp3
    public static final String INGREDIENT_IMAGE_NAME_TEMPLATE = "%s_%d_%d_%s.jpg";           //hhid_recipeID_IngredientID_timestamp.jpg
    public static final String SHARED_DISH_AUDIOFILE_TEMPLATE = "%s_%d_%s.mp3";              //hhid_MealID_Timestamp.mp3
    public static final String SHARED_DISH_IMAGE_NAME_TEMPLATE = "%s_%d_%s.jpg";             //hhid_MealID_Timestamp.jpg
    public static final String RECIPE_NAME_AUDIOFILE_TEMPLATE = "RECIPE_%s_%d_%s.mp3";       //RECIPE_hhid_RecipeID_Timestamp.mp3


    /**
     * Notification Constants
     */
    public static final String RECORDREVIEWCHANNEL = "recordreciewchannelId";
    public static final String FINALIZEEOCHANNELID = "finalizeEoChannelId";
    public static final String REMINDERCHANNELID = "reminderChannelId";
    public static final String SENSORREMINDERCHANNEL = "sensorReminderChannelId";
    public static final String RECIPEIMAGEREMINDERCHANNEL = "recipeImageReminderChannelId";
    public static String NOTIFICATION_ID = "notificationid";
    public static String NOTIFICATION = "notification";
    public static String CHANNELIDKEY = "channelId";


    /**
     * Entity Reference Strings
     */
    public static final String FR = "foodrecord";
    public static final String FRID = "frId";
    public static final String PPID = "ppid";
    public static final String EOID = "eoId";
    public static final String FI = "fooditem";
    public static final String DELIVERYDATE = "deliverydate";
    public static final String RECIPEID = "recipeId";
    public static final String HOUSEHOLDMEMBER = "householdMember";
    public static final String UNFINALIZEDEOIDS = "unfinalizedEatingOccasionIds";
    public static final String HOUSEHOLDID = "householdId";
    public static final String IMAGE_NAME = "imageName";
    public static final String SHARED_DISH = "shareddish";
    public static final String AUDIOFILE_NAME = "audiofile";
    public static final String TEXT_DESCRIPTION = "textdescription";
    public static final String DIALOG_MODE = "audioonly";
    public static final String ALLOW_TEXT = "allowtext";
    public static final String HELP_AUDIO = "helpaudioresid";
    public static final String AUDIO_ONLY_FRAGMENT_TAG = "audioonlyfragmenttag";

}
