package shared.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class StringUtils {

    /**
     * Takes a string and ensures the first character is upper case
     *
     * @param text
     * @return
     */
    public static String CapitaliseString (String text) {
        if(text.length() == 0) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    /**
     * Securely takes an array of chars and returns an array of bytes
     * See <a href="http://stackoverflow.com/a/30561039>http://stackoverflow.com/a/30561039</a>.
     *
     * @param chars Array to convert
     * @return Array of bytes
     */
    public static byte[] charsToBytes (char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }
}
