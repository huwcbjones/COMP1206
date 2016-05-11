import java.io.*;
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
            FileOutputStream fos = new FileOutputStream("byte_output_file.txt");
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
