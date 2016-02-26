import java.util.ArrayList;

/**
 * Simple Thread
 *
 * @author Huw Jones
 * @since 26/02/2016
 */
public class SimpleThread {

    public static void main(String[] args){
         SimpleThread t = new SimpleThread();
    }

    public SimpleThread(){
        ArrayList<NumberThread> threads = new ArrayList<>();
        for(int i = 1; i <=3; i++){
            threads.add(new NumberThread(i));
        }

        threads.forEach(NumberThread::start);
    }

    private class NumberThread extends Thread {

        private int number;

        public NumberThread(int number){
            this.setName("Thread " + number);
            this.number = number;
        }
        @Override
        public void run() {
            try {
                Thread.sleep(1000 * (4-number));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(number);
        }
    }


}
