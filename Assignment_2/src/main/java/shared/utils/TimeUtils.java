package shared.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * Time Utilities
 *
 * @author Huw Jones
 * @since 03/05/2016
 */
public class TimeUtils {

    public static Calendar DateToCalendar(Date d){
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c;
    }
}
