import com.sun.scenario.effect.impl.sw.java.JSWBlend_COLOR_BURNPeer;

import javax.swing.*;
import java.awt.*;

/**
 * A Font Chooser GUI
 *
 * @author Huw Jones
 * @since 29/01/2016
 */
public class FontChooser {

    private JFrame window;

    private JPanel panel_font;
    private ButtonGroup group_font;
    private JRadioButton radio_times;
    private JRadioButton radio_helvetica;
    private JRadioButton radio_courier;

    private JPanel panel_style;
    private JCheckBox checkbox_bold;
    private JCheckBox checkbox_italic;

    private JTextField text_input;

    private JButton btn_ok;

    public static void main(String[] args) {
        FontChooser fontChooser = new FontChooser();
    }

    public FontChooser() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {

        }

        window = new JFrame("Font Chooser");
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setLayout(new FlowLayout());

        // Checkboxes
        panel_style = new JPanel();
        panel_style.setLayout(new BoxLayout(panel_style, BoxLayout.Y_AXIS));


        checkbox_bold = new JCheckBox("Bold");
        checkbox_bold.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel_style.add(checkbox_bold);

        checkbox_italic = new JCheckBox("Italic");
        checkbox_italic.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel_style.add(checkbox_italic);

        window.add(panel_style);

        // Radio Buttons
        panel_font = new JPanel();
        panel_font.setLayout(new BoxLayout(panel_font, BoxLayout.Y_AXIS));

        group_font = new ButtonGroup();

        radio_times = new JRadioButton("Times");
        group_font.add(radio_times);
        panel_font.add(radio_times);

        radio_helvetica = new JRadioButton("Helvetica");
        group_font.add(radio_helvetica);
        panel_font.add(radio_helvetica);

        radio_courier = new JRadioButton("Courier");
        group_font.add(radio_courier);
        panel_font.add(radio_courier);

        window.add(panel_font);

        // Input
        text_input = new JTextField(20);
        window.add(text_input);

        // Buttons
        btn_ok = new JButton("OK");
        window.add(btn_ok);

        // A little bit to make the layout the same size as the image given in the exercise sheet
        // Pack the components to set the size
        // Then use that height with 600px as the width
        window.pack();
        window.setSize(new Dimension(600, window.getHeight()));
        window.setVisible(true);
    }

}
