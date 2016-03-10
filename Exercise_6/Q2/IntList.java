import java.util.ArrayList;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 10/03/2016
 */
public class IntList {
    private final ArrayList<Integer> ints = new ArrayList<>();

    public void add(Integer o) {
        ints.add(o);
        synchronized (ints) {
            ints.notify();
        }
    }

    public Integer get() {
        int i;
        synchronized (ints) {
            if (ints.isEmpty()) {
                try {
                    ints.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            i = ints.get(0);
            ints.remove(0);
        }
        return i;
    }

    public boolean isEmpty() {
        synchronized (ints) {
            return ints.isEmpty();
        }
    }
}
