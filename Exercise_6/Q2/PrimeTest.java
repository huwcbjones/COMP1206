import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 26/02/2016
 */
public class PrimeTest {

    private IntList ints;
    private int numberThreads;
    private int numberNumbers;

    public PrimeTest(int numberNumbers, int numberThreads) {
        this.numberNumbers = numberNumbers;
        this.numberThreads = numberThreads;
        ints = new IntList();
        for (int i = 0; i < numberNumbers; i++) {
            ints.add(new Random().nextInt(1000));
        }
    }

    public static void main(String args[]) {
        int numberNumbers;
        int numberThreads;

        if(args.length != 2){
            System.err.println("Wrong number of args.");
            return;
        }

        try {
            numberNumbers = Integer.parseInt(args[0]);
            numberThreads = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            System.err.println("Invalid number format.");
            return;
        }

        PrimeTest test = new PrimeTest(numberNumbers, numberThreads);
        test.checkPrimes();
    }

    public void checkPrimes() {
        ExecutorService es = Executors.newFixedThreadPool(numberThreads);
        for (int i = 0; i < numberThreads; i++) {
            es.execute(new PrimeChecker());
        }

        // Shutdown the thread pool after all threads have exited
        es.shutdown();
    }

    private static boolean checkPrime(int n) {
        int primes55[] = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47,
                53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107,
                109, 113, 127, 131, 137, 139, 149, 151, 157, 163,
                167, 173, 179, 181, 191, 193, 197, 199, 211, 223,
                227, 229, 233, 239, 241, 251, 257};

        for (int i = 0; i < 55; i++) {
            if (n % primes55[i] == 0) {
                if (n == primes55[i]) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        int maxtest = n / 16;

        for (int i = 259; i < maxtest; i += 2)
            if (n % i == 0)
                return false;

        return true;
    }

    private class PrimeChecker implements Runnable {

        @Override
        public void run() {
            int i;
            while (!ints.isEmpty()) {
                i = ints.get();
                if (checkPrime(i)) {
                    System.out.println("int '" + i + "' is a prime.");
                } else {
                    System.out.println("int '" + i + "' is not a prime.");
                }
            }
        }
    }
}
