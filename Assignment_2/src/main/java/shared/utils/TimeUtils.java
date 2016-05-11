package shared.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Time Utilities
 *
 * @author Huw Jones
 * @since 03/05/2016
 */
public class TimeUtils {

    public static Calendar DateToCalendar(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c;
    }

    public static String getTimeString(long time, boolean isMilliseconds) {
        TimeUnit format = TimeUnit.SECONDS;

        if (isMilliseconds) format = TimeUnit.MILLISECONDS;

        if (time <= (isMilliseconds ? 60000 : 60)) {
            // Seconds
            return String.format("%d seconds", format.toSeconds(time));

        } else if (time <= (isMilliseconds ? 3600000 : 3600)) {
            long minutes = format.toMinutes(time);
            long seconds = format.toSeconds(time) - TimeUnit.MINUTES.toSeconds(minutes);

            // Minutes
            if (seconds == 0) {
                return String.format("%d mins",
                    minutes
                );
            } else {

            }
            return String.format("%d mins, %d seconds",
                minutes,
                seconds
            );

        } else if (time <= (isMilliseconds ? 86400000 : 86400)) {
            long hours = format.toHours(time);
            long minutes = format.toMinutes(time) - TimeUnit.HOURS.toMinutes(hours);

            // Hours
            if (minutes == 0) {
                return String.format("%d hours",
                    hours
                );
            } else {
                return String.format("%d hours, %d mins",
                    hours,
                    minutes
                );
            }

        } else {
            long days = format.toDays(time);
            long hours = format.toHours(time) - TimeUnit.DAYS.toHours(days);

            // Hours
            if (hours == 0) {
                return String.format("%d days",
                    days
                );
            } else {
                return String.format("%d days, %d hours",
                    days,
                    hours
                );
            }
        }
    }
}
