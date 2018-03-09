package me.yeon.hangmanandroid.comm;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by yeon on 2018-03-09 009.
 */

public class DateDeserializer implements JsonDeserializer<Calendar> {

    public static final SimpleDateFormat sServerDateDateFormat = new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss.SSSZ");

    @Override
    public Calendar deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json != null) {
            final String jsonString = json.getAsString();
            try {
                Calendar myCal = new GregorianCalendar();
                myCal.setTimeZone(TimeZone.getTimeZone("UTC"));
                myCal.setTime(sServerDateDateFormat.parse(jsonString));
                return myCal;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
