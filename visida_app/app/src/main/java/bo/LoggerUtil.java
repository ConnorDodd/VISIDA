package bo;

import android.content.Context;

import java.io.IOException;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static bo.AppConstants.LOGFILENAME;

public class LoggerUtil {
    private boolean logOpen = false;
    private Process proc;

    private static LoggerUtil INSTANCE;

    public static LoggerUtil getInstance() {
        if(INSTANCE == null){
            INSTANCE = new LoggerUtil();
        }
        return INSTANCE;
    }

    public boolean isOpen(){
        return logOpen;
    }

    public boolean open(Context context){
        if(!logOpen){
            //Set up logging
            String filePath = Utilities.getHouseholdDirectory(context) + "/" + LOGFILENAME;
            try {
                //Open the log and store a reference to the process it spawns
                proc = Runtime.getRuntime().exec(new String[]{"logcat", "-f", filePath, "-v time" ,"*:S" , ACTIVITY_LOG_TAG + ":V"});
                logOpen = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return logOpen;
    }

    public boolean close(){
        //Destroy the Process we created when we started LogCat.
        //There is possible a better way to handle this but "logcat -d" didnt work.
        proc.destroy();
        logOpen = false;
        return logOpen;
    }
}
