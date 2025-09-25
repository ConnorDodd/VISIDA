package bo;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.text.Html;
import android.text.Spanned;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import au.edu.newcastle.jnc985.visida.BuildConfig;
import bo.typeconverter.DateTypeConverter;

import static bo.AppConstants.EXPORT_MEDIA_DIR;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.STATE;

/**
 * Created by Josh on 19-Dec-17.
 */

public class Utilities {

    private static final String TAG = "Utilities";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    /**
     * Returns true if the two dates are on the same day. False otherwise.
     * @param d1
     * @param d2
     * @return True if d1 is on the same day as d2
     */
    public static boolean sameDay(Date d1, Date d2){
        //User the date converte to check of the dates have the same format yyyy-mm-dd
        String d1Day = DateTypeConverter.dateToTimestamp(d1);
        String d2Day = DateTypeConverter.dateToTimestamp(d2);

        return d1Day.equals(d2Day);
    }

    private static final String MEDIA_DIR = "/Media";
    private static final String HOUSEHOLD_DIR = "/Household";
    private static File mMediaDir;
    private static File mHouseholdDir;
    /**
     * Retuns the Media Directory for the Application
     * @param context
     * @return
     */
    public static File getMediaDirectory(Context context){
        if(mMediaDir == null){
            mMediaDir = new File(context.getExternalFilesDir(null), MEDIA_DIR);
            if(!mMediaDir.exists()){
                mMediaDir.mkdirs();
            }
        }
        return mMediaDir;
    }

    /**
     * Returns the directory to Export media in the public Downloads directory of the device.
     * @param context
     * @return
     */
    public static File getExportDirectory(Context context){
        File downloadDir = getDownloadsDirectory(context);
        File exportDirectory = new File(downloadDir, EXPORT_MEDIA_DIR);
        if(!exportDirectory.exists()){
            exportDirectory.mkdirs();
        }
        return exportDirectory;
    }

    public static File getDownloadsDirectory(Context context){
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return downloadDir;
    }

    /**
     * Retuns the Media Directory for the Application
     * @param context
     * @return
     */
    public static File getHouseholdDirectory(Context context){
        if(mHouseholdDir == null){
            mHouseholdDir = new File(context.getExternalFilesDir(null), HOUSEHOLD_DIR);
            if(!mHouseholdDir.exists()){
                mHouseholdDir.mkdirs();
            }
        }
        return mHouseholdDir;
    }

    public static void setState(Context context, State state) {
        //Save the state in shared preferences
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(STATE, state.ordinal());
        editor.commit();
    }

    public static State getState(Context context){
        //Check the state we are in
        SharedPreferences sharedPref = context.getSharedPreferences(AppConstants.PREFERENCES, Context.MODE_PRIVATE);
        State currentState = State.values()[sharedPref.getInt(AppConstants.STATE, State.INVALID.ordinal())];
        return currentState;
    }

    /**
     *
     * @param context Context of the application to find the directory
     * @param from The original name of the file
     * @param to The new name of the filen
     */
    public static void renameMediaFile(Context context, String from, String to){
        if(from == null || to == null) return;

        //Rename the image file
        File mediaDirectory = Utilities.getMediaDirectory(context);
        new RenameFileAsync(mediaDirectory).execute(from, to);
    }

    private static class RenameFileAsync extends AsyncTask<String, Void, Boolean> {

        private File mMediaDir;

        public RenameFileAsync(File mediaDirectory){
            super();
            this.mMediaDir = mediaDirectory;
        }
        @Override
        protected Boolean doInBackground(String... strings) {
            File fromFile = new File(mMediaDir, strings[0]);
            File toFile = new File(mMediaDir, strings[1]);
            if(fromFile.exists()) {
                return fromFile.renameTo(toFile);
            }
            return false;
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    public static void deleteMediaFile(Context context, String fileName) {
        File mediaDirectory = Utilities.getMediaDirectory(context);
        File fileToDelete = new File(mediaDirectory, fileName);
        if(fileToDelete.exists()){
            fileToDelete.delete();
        }
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    public static void deleteDirectory(File folder) {
        File[] files = folder.listFiles();
        if(files != null) {
            for (File f : files) {
                if (folder.isDirectory()) {
                    deleteDirectory(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    public static String getVersionName(Application context){
        String version = "";
        //Get the app version
        try{
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        }
        catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        return version;
    }

    public static int getVersionCode(Application context){
        int versionCode = 0;
        try{
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionCode = pInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        return versionCode;
    }

    public static Uri getUri(Context context, int resId){
        Resources resources = context.getResources();
        Uri uri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(resId))
                .appendPath(resources.getResourceTypeName(resId))
                .appendPath(resources.getResourceEntryName(resId))
                .build();
        return uri;
    }

    public static int[] typedArrayToIds(TypedArray typedArray) {
        int[] ids = new int[typedArray.length()];
        for(int i = 0; i < typedArray.length(); i++) {
            ids[i] = typedArray.getResourceId(i, -1);
        }
        return ids;
    }

    public static void updateLanguage(Context context) {
        if(BuildConfig.forceKhmer) {
            forceKhmer(context);
        }
        else if(BuildConfig.forceSwahili){
            forceSwahili(context);
        }
    }

    private static void forceSwahili(Context context) {
        String languageToLoad  = "sw";
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());
    }

    private static void forceKhmer(Context context) {
        String languageToLoad  = "km";
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());
    }
}
