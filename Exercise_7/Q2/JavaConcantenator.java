import java.io.*;
import java.util.Arrays;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 11/03/2016
 */
public class JavaConcantenator {

    public static void main(String[] args){
        if(args.length != 2){
            System.err.println("Not enough arguments passed");
            return;
        }

        File directory = new File(args[0]);

        if(!directory.isDirectory()){
            System.err.println("Argument 1 must be a directory!");
            return;
        }

        File newFile = new File(args[1]);
        if(!newFile.canWrite()){
            try {
                if(!newFile.createNewFile()){
                    System.err.println("Cannot write to file, " + newFile.getName());
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            PrintWriter pw = new PrintWriter(newFile);
            File[] files = directory.listFiles((dir, name) -> {
                return name.substring(name.length() - 5).equals(".java");
            });

            String fileContents = "";
            String line;
            BufferedReader reader ;
            for(File f: files){
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                try {
                    while((line = reader.readLine()) != null){
                        fileContents += line + "\n";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            pw.write(fileContents);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }


    }
}
