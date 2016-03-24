/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class StringUtils {

    /**
     * Takes a string and ensures the first character is upper case
     * @param text
     * @return
     */
    public static String CapitaliseString(String text){
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
