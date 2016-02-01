import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

/**
 * A Simple GUI
 *
 * @author Huw Jones
 * @since 29/01/2016
 */
public class SimpleGUI {

    private JFrame window;
    private JTextField input;
    private JButton submit;
    private JButton cancel;


    // Already separated the GUI junk from the main method by default
    public static void main(String[] args){
        SimpleGUI GUI = new SimpleGUI();
    }

    public SimpleGUI(){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {

        }

        window = new JFrame("Simple Submit Cancel Form");
        window.setLayout(new FlowLayout());

        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setSize(new Dimension(300, 100));

        input = new JTextField(20);
        window.add(input);

        submit = new JButton("Submit");
        window.add(submit);

        cancel = new JButton("Cancel");
        cancel.addActionListener(new CancelButtonHandler());
        window.add(cancel);

        window.setVisible(true);
    }


    /**
     * Event Handler for Cancel Button
     * Closes the window on click
     */
    protected class CancelButtonHandler implements ActionListener {
        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
        }
    }
}
