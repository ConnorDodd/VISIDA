package bo.typeconverter;

import androidx.room.TypeConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jnc985 on 15-Dec-17.
 */

public class TimeTypeConverter {
    static DateFormat df = new SimpleDateFormat("HH:mm:ss");
    private static final long MINUTE = 60 * 1000;
    @TypeConverter
    public static Date fromTimestamp(String value){
        if(value != null) {
            try{
                return df.parse(value);
            }
            catch (ParseException e){
                //TODO Proper error handling
                e.printStackTrace();
            }
        }
        return null;
    }

    @TypeConverter
    public static String timeToTimestamp(Date date){
        return date == null ? null : df.format(date);
    }

    public static boolean isTimeWithinRange(Date d1, Date d2, int minutes){
        //Strip the days off
        Date time1 = fromTimestamp(timeToTimestamp(d1));
        Date time2 = fromTimestamp(timeToTimestamp(d2));

        long timeDifference = time1.getTime() - time2.getTime();
        return Math.abs(timeDifference) < minutes * MINUTE ? true : false;
    }
}
