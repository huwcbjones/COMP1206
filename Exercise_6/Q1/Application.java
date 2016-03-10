import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 10/03/2016
 */
public class Application extends JFrame {

    private ArrayList<Dice> dice;
    private JPanel panel_dice;
    private JButton btn_roll;
    private int total = 0;

    public Application() {
        Dimension size = new Dimension(800, 190);
        this.setSize(size);
        this.setMinimumSize(size);
        this.setMaximumSize(size);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        panel_dice = new JPanel(new GridLayout(1, 5));
        this.dice = new ArrayList<>();

        Dice die;
        for (int i = 0; i < 5; i++) {
            die = new Dice();

            dice.add(die);
            panel_dice.add(die);
        }

        this.add(panel_dice, BorderLayout.CENTER);

        this.btn_roll = new JButton("Roll");
        this.btn_roll.setMnemonic(KeyEvent.VK_R);
        this.btn_roll.addActionListener(e -> {
            this.total = 0;
            Thread runner = new Thread() {
                @Override
                public void run() {
                    ArrayList<SynchRollDie> threads = new ArrayList<>();
                    Thread t;
                    SynchRollDie srd;
                    for (Dice d : dice) {
                        srd = new SynchRollDie(d);
                        threads.add(srd);

                        t = new Thread(srd);
                        t.start();
                    }
                    for (SynchRollDie s : threads) {
                        total += s.getVal();
                    }
                    System.out.println("Total is: " + total);
                }
            };
            runner.start();

        });

        this.add(btn_roll, BorderLayout.PAGE_END);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Application app = new Application();
        });
    }
}