import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Simple Increment GUI
 *
 * @author Huw Jones
 * @since 04/02/2016
 */
public class IncrementGUI extends JFrame {

    private int numberPresses;
    private JButton btn_increment;
    private JButton btn_reset;
    private JTextField text_numPresses;

    public IncrementGUI() {
        super("Increment GUI");

        numberPresses = 0;

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {

        }

        Container contentPane = this.getContentPane();
        contentPane.setLayout(new FlowLayout());

        btn_increment = new JButton("Increment");
        btn_increment.addActionListener(new btn_incrementActionListener());
        contentPane.add(btn_increment);

        btn_reset = new JButton("Reset");
        btn_reset.addActionListener(new btn_resetActionListener());
        contentPane.add(btn_reset);

        text_numPresses = new JTextField();
        text_numPresses.setColumns(20);
        contentPane.add(text_numPresses);

        updateTextField();
        this.pack();

        this.setVisible(true);
    }

    public static void main(String[] args) {
        IncrementGUI gui = new IncrementGUI();
    }

    protected void updateTextField() {
        text_numPresses.setText(((Integer) numberPresses).toString());
    }

    protected class btn_incrementActionListener implements ActionListener {

        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            numberPresses++;
            updateTextField();
        }
    }

    protected class btn_resetActionListener implements ActionListener {

        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            numberPresses = 0;
            updateTextField();
        }
    }
}
