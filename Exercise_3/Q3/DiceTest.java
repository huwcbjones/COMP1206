import javax.swing.*;
import java.awt.*;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 22/02/2016
 */
public class DiceTest extends JFrame {
    public Dice dice;

    public DiceTest() {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setMaximumSize(new Dimension(600, 600));
        this.setMinimumSize(new Dimension(600, 600));
        this.setSize(new Dimension(600, 600));
        dice = new Dice();
        this.getContentPane().add(dice);
        this.setVisible(true);
        NumberThread updater = new NumberThread();
        updater.start();
    }

    public static void main(String[] args) {
        DiceTest diceTest = new DiceTest();
    }

    private class NumberThread extends Thread {

        public NumberThread() {
            this.setName("DiceIncrementThread");
        }

        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()) {
                for (int i = 0; i <= 6; i++) {
                    dice.updateVal(i);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }
    }
}
