package utils;

import java.io.PrintStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logs messages to System.out and System.err
 *
 * @author Huw Jones
 * @since 01/03/2016
 */
public class Log {
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static final SimpleDateFormat ms = new SimpleDateFormat("SSS");
    public static void Fatal(String message) {
        message("[FATAL]\t" + message, true);
        System.exit(-1);
    }

    public static void Warning(String message) {
        message("[WARN]\t" + message, true);
    }

    public static void Information(String message) {
        message("[INFO]\t" + message, false);
    }

    private static void message(String message, boolean isError) {
        PrintStream out = (isError) ? System.err : System.out;
        out.println("[" + getDateTime() + "]" + message);
    }

    private static String getDateTime(){
        return format.format(new Date()) + "." + String.format("%03.0f", new Timestamp(new Date().getTime()).getNanos() / 1000000d);
    }
}
