import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 09/05/2016
 */
public class AnagramTest {

    private static final char[] letters;

    static {
        letters = new char[26];
        for(int i = 0; i < 26; i++){
            letters[i] = (char) (i + 65);
        }
    }

    public static boolean isAnagram(String str1, String str2) {

        if (str1.length() != str2.length()) {
            return false;
        }

        char[] a, b;
        Arrays.sort(a = str1.toCharArray());
        Arrays.sort(b = str2.toCharArray());
        return Arrays.equals(a, b);
    }

    /**
     * Ensures all the anagrams are unique
     *
     * @throws Exception
     */
    @Test
    public void generate_unique() throws Exception {
        String input = generateRandomInput();
        Anagram anagram = new Anagram(input);
        List output = anagram.generate();
        assertTrue("Output contains original string.", !output.contains(input));
    }

    /**
     * Ensures all the combinations are covered
     *
     * @throws Exception
     */
    @Test
    public void generate_allAnagrams() throws Exception {
        String input = generateRandomInput();
        long inputLength = input.length();
        Anagram anagram = new Anagram(input);
        List output = anagram.generate();
        if(inputLength != 0) {
            assertEquals("Not all anagrams were found.", factorial(inputLength), output.size());
        }
    }

    public String generateRandomInput(){
        int length = 4;//new Random().nextInt(16);
        char[] charArray = new char[length];
        for(int i = 0; i < length; i++){
            charArray[i] = letters[new Random().nextInt(26)];
        }
        return String.valueOf(charArray);
    }

    /**
     * Calculates x!
     * @param x value, x
     *
     * @return x!
     */
    public long factorial(long x){
        long result = 1;
        for(long y = x; y != 0; y--){
            result *= y;
        }
        return result;
    }
}