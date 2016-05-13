import java.io.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 11/03/2016
 */
public class ByteBasedStream {

    public static void main(String[] args){
        try {
            ArrayList<Integer> ints = new ArrayList<>(10000);
            Random random = new Random();
            for(int i = 0; i < 10000; i++){
                ints.add(random.nextInt());
            }
            FileOutputStream fos = new FileOutputStream("byte_output_file.txt");
            ints.forEach(integer -> {
                try {
                    // Writes lower 8 bits to OS, upper 24 bits (3 bytes) are ignored
                    fos.write(integer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            fos.close();

            FileWriter fw = new FileWriter("char_output_file.txt");
            ints.forEach(integer -> {
                try {
                    // Writes lower 16 bits to OS, upper 16 bits (2 bytes) are ignored
                    fw.write((char)integer.intValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            fw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
