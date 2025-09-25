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
public class TimestampConverter {
    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
    public static String toTimestamp(Date date){
        return date == null ? null : df.format(date);
    }
}
