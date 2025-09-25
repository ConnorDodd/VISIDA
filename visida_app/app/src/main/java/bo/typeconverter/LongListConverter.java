package bo.typeconverter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jnc985 on 15-Dec-17.
 */

public class LongListConverter {

    @TypeConverter
    public static List<Long> fromJson(String value){
        Gson gson = new Gson();
        if(value == null){
            //return an empty list
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<Long>>() {}.getType();
        List<Long> result = gson.fromJson(value, listType);
        return result == null ? new ArrayList<Long>() : result;
    }

    @TypeConverter
    public static String toJson(List<Long> urls){
        Gson gson = new Gson();
        String result = gson.toJson(urls);
        return result == null ? "[]" : result;
    }
}
