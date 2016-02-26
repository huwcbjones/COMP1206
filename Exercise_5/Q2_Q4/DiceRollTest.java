import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 26/02/2016
 */
public class DiceRollTest extends JFrame {

    private Dice dice;

    public DiceRollTest() {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setMaximumSize(new Dimension(600, 600));
        this.setMinimumSize(new Dimension(600, 600));
        this.setSize(new Dimension(600, 600));
        dice = new Dice();
        this.getContentPane().add(dice);
        this.setVisible(true);

        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Runnable r= new RollDie(dice);
        Thread t = new Thread(r);

        t.start();
    }

    public static void main(String[] args){
        DiceRollTest d = new DiceRollTest();
    }

    private class clickHandler extends MouseAdapter {

    }
}
