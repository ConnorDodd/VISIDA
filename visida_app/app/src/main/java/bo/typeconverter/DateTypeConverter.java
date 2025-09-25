package bo.typeconverter;

import androidx.room.TypeConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jnc985 on 15-Dec-17.
 */

//REF: http://androidkt.com/datetime-datatype-sqlite-using-room/
public class DateTypeConverter {
    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

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
    public static String dateToTimestamp(Date date){
        return date == null ? null : df.format(date);
    }

    /**
     * Returns true if the two dates are on the same day
     * @param d1
     * @param d2
     * @return
     */
    public static boolean sameDay(Date d1, Date d2){
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return fmt.format(d1).equals(fmt.format(d2));
    }
}
