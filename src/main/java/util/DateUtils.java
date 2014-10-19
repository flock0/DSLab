package util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static final String FORMAT = "yyyyMMdd_HHmmss.SSS";

    private static ThreadLocal<SimpleDateFormat> tl = new ThreadLocal<SimpleDateFormat>();
        
    public static String formatDate(Date d) {
        SimpleDateFormat sdf = tl.get();
        if(sdf == null) {
            sdf = new SimpleDateFormat(FORMAT);
            tl.set(sdf);
        }
        return sdf.format(d);
    }
}
