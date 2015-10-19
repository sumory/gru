package com.sumory.gru.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //format.setTimeZone(TimeZone.getTimeZone("GMT"));
            return format;
        }
    };

    public static String toDateTimeString(Date datetime) {
        return DATE_FORMAT.get().format(datetime);
    }

    public static Date parseDateTimeFromString(String datetime) {
        Date date = tryToParse(datetime, DATE_FORMAT.get());
        return date;
    }

    public static long parseDateTimeToMilliseconds(String datetime) {
        Date date = parseDateTimeFromString(datetime);
        if (date != null) {
            return date.getTime();
        }
        return 0;
    }

    private static Date tryToParse(String datetime, SimpleDateFormat format) {
        try {
            return format.parse(datetime);
        }
        catch (ParseException e) {
            return null;
        }
    }
}
