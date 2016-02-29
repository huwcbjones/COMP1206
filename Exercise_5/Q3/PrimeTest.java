import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 26/02/2016
 */
public class PrimeTest {

    private Vector<Integer> primes;

    public PrimeTest(Vector<Integer> primes) {
        this.primes = primes;
    }

    public static void main(String args[]) {
        Vector<Integer> primes = new Vector<>();
        for (int i = 400; i < 600; i++) {
            primes.add(i);
        }
        PrimeTest test = new PrimeTest(primes);
        test.checkPrimes();
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

    public void checkPrimes() {
        ExecutorService es = Executors.newFixedThreadPool(10);
        for(int n: primes){
            es.execute(new PrimeChecker(n));
        }

        // Shutdown the thread pool after all threads have exited
        es.shutdown();
    }

    private class PrimeChecker implements Runnable {

        int n;

        public PrimeChecker(int n) {
            this.n = n;
        }

        @Override
        public void run() {
            if(checkPrime(n)){
                System.out.println("int '" + n +"' is a prime.");
            } else {
                System.out.println("int '" + n +"' is not a prime.");
            }
        }
    }
}
