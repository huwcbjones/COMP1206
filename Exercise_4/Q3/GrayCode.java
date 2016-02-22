import java.util.ArrayList;
import java.util.Arrays;

/**
 * Outputs gray code for a given N
 *
 * @author Huw Jones
 * @since 22/02/2016
 */
public class GrayCode {

    public static void main(String[] args) {
        // Make sure we get a number
        if (args.length != 1) {
            System.err.println("Please specify the N-bit Cray Code.");
            return;
        }

        try {
            int n = Integer.parseInt(args[0]);
            printGrayCode(n);
        } catch (NumberFormatException ex) {
            System.err.println("Please specify a valid N-bit Cray Code.");
        }
    }


    public static void printGrayCode(int n) {
        // Lamba that prints each string on a newline
        getGrayCode(n).forEach(System.out::println);
    }


    private static ArrayList<String> getGrayCode(int n) {
        // If n = 1, return {0, 1}
        if (n == 1) {
            return new ArrayList<>(Arrays.asList("0", "1"));
        }

        // Get Gray Code for n -1
        ArrayList<String> grayCodes = getGrayCode(n - 1);

        // Loop forwards and append "0"
        ArrayList<String> newCodes = new ArrayList<>();
        for (String s : grayCodes) {
            newCodes.add("0" + s);
        }

        // Loop backwards and append "1"
        for (int i = grayCodes.size() - 1; i >= 0; i--) {
            newCodes.add("1" + grayCodes.get(i));
        }

        return newCodes;
    }
}
