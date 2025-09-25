package bo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import androidx.test.espresso.idling.CountingIdlingResource;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import bo.db.entity.Household;

import static bo.AppConstants.JSON_FILE;
import static bo.AppConstants.LOGFILENAME;
import static bo.AppConstants.PARTICIPANTHOUSEHOLDID;
import static bo.AppConstants.PREFERENCES;
import static bo.AppConstants.READMEFILENAME;
import static bo.AppConstants.ZIP_FILE;
import static bo.Utilities.getVersionCode;
import static bo.Utilities.getVersionName;

/**
 * Created by jnc985 on 05-Feb-18.
 */

public class DataExporter{


    private Application context;
    private DataExporterCallbackHandler callbackHandler;

    private final CountingIdlingResource countingIdlingResource;

    public interface DataExporterCallbackHandler {
        public void onExportComplete();
    }

    public DataExporter(Application context, CountingIdlingResource idlingResource, DataExporterCallbackHandler handler){
        this.context = context;
        this.countingIdlingResource = idlingResource;
        this.callbackHandler = handler;
    }

    public void exportData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Write the regular Export Media Directory
                File data = buildExportDirectory();

                //Pass this directory to be copied to a zip file
                String now = Utilities.DATE_FORMAT.format(Calendar.getInstance().getTime());
                SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                String hhid = sharedPreferences.getString(PARTICIPANTHOUSEHOLDID, "");
                String fileName= String.format("%s_%s_%s", now, hhid, ZIP_FILE);
                File zipFile = new File(Utilities.getDownloadsDirectory(context), fileName);
                exportToZip(data, zipFile.getAbsolutePath());
                //Tell Handler we have finished exporting the data
                String[] newFiles = new String[]{data.toString(), zipFile.toString()};
                MediaScannerConnection.scanFile(context, newFiles, null, null);
                callbackHandler.onExportComplete();
            }
        }).start();
    }

    private void exportToZip(File contents, String zipFile) {
        int BUFFER = 2048;
        try{
            BufferedInputStream origin = null;
            //Create output file stream
            FileOutputStream dest = new FileOutputStream(zipFile);
            //Buffer the outputfil stream and pass to zipoutput stream
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte data[] = new byte[BUFFER];
            for(File file : contents.listFiles()){
                FileInputStream fi = new FileInputStream(file);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(file.getName());
                out.putNextEntry(entry);
                int count;
                while((count = origin.read(data, 0, BUFFER)) != -1){
                    out.write(data, 0, count);
                }
                origin.close();

            }
            out.finish();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File buildExportDirectory() {
        File exportDir = Utilities.getExportDirectory(context);
        //Clear out any old data
        Utilities.deleteDirectory(exportDir);
        exportDir.mkdirs();
        //Copy the media directory to a publicly accessiblt directory
        try{
            //Get the database file and serialize it to JSON
            copyDataBase();
            copyMediaDirectory(Utilities.getMediaDirectory(context), exportDir);
            copyLogFile();
            createReadme();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
        return exportDir;
    }

    private void createReadme() {
        File readmeFile = new File(Utilities.getExportDirectory(context), READMEFILENAME);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(readmeFile));
            writer.write("Version Name: " + getVersionName(context));
            writer.write("\n");
            writer.write("Version Number: " + getVersionCode(context));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyLogFile() throws IOException {
        File logFile = new File(Utilities.getHouseholdDirectory(context), LOGFILENAME);
        File dest = new File(Utilities.getExportDirectory(context), LOGFILENAME);
        if(logFile.exists()) {
            copyBytes(logFile, dest);
        }
    }

    private void copyDataBase() {
        //Create Gson
        Gson gson = new Gson();

        //Get App versio info
        int versionCode = getVersionCode(context);
        String version = getVersionName(context);

        //Create repo objects
        HouseholdRepository hhRepo = new HouseholdRepository(context);
        Household hh = hhRepo.getFullHousehold();
        //Set the version name an dnumber
        hh.setVersionName(version);
        hh.setVersionCode(versionCode);
        //Set and save the export time.
        hh.setExportTime(new Date());
        hhRepo.updateHousehold(hh);

        //Write to file
        try(FileWriter writer = new FileWriter(new File(Utilities.getExportDirectory(context), JSON_FILE))) {
            gson.toJson(hh, writer);
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    }

    public void copyMediaDirectory(File src, File dest) throws IOException{
        if(src.isDirectory()){
            if(!dest.exists()){
                dest.mkdir();
            }

            //Get all the child files and copy them over to dest
            String files[] = src.list();
            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                copyMediaDirectory(srcFile,destFile);
            }

        } else {
            copyBytes(src, dest);
        }
    }

    private void copyBytes(File src, File dest) throws IOException{
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dest)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
        }
    }
}
