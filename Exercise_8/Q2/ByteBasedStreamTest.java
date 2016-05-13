import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 09/05/2016
 */
public class ByteBasedStreamTest {

    @Test
    public void main() throws Exception {
        ByteBasedStream.main(new String[0]);

        try {
            FileInputStream fis = new FileInputStream("byte_output_file.txt");
            int inputInt;
            ArrayList<Integer> intArray = new ArrayList<>(1000);
            while((inputInt = fis.read()) != -1){
                intArray.add(inputInt);
            }
            fis.close();
            assertEquals("File did not contain correct amount of ints.", 10000, intArray.size());

            FileReader fr = new FileReader("char_output_file.txt");
            int inputChar;
            ArrayList<Integer> charArray = new ArrayList<>(1000);
            while((inputChar = fr.read()) != -1){
                charArray.add(inputChar);
            }
            fr.close();
            assertEquals("File did not contain correct amount of ints.", 10000, charArray.size());

            assertTrue("File contents do not match.", Arrays.equals(intArray.toArray(new Integer[0]), charArray.toArray(new Integer[0])));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}