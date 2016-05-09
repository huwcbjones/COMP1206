import org.junit.Test;

import java.io.*;
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
            for(int i = 0; i< 10000; i++){
                fos.write(new Random(100000).nextInt());
            }
            fos.close();

            FileWriter fw = new FileWriter("char_output_file.txt");
            for(int i = 0; i< 10000; i++){
                fw.write(new Random(100000).nextInt());
            }
            fw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}