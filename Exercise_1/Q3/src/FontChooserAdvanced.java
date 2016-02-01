import javax.swing.*;
import java.awt.*;

/**
 * A Font Chooser GUI
 *
 * @author Huw Jones
 * @since 29/01/2016
 */
public class FontChooserAdvanced {

    private JFrame window;

    private JComboBox<String> combo_font;

    private JPanel panel_style;
    private JCheckBox checkbox_bold;
    private JCheckBox checkbox_italic;

    private JTextField text_input;

    private JButton btn_ok;

    public static void main (String[] args) {
        FontChooserAdvanced fontChooserAdvanced = new FontChooserAdvanced();
    }

    public FontChooserAdvanced () {
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

        // Font ComboBox
        combo_font = new JComboBox<>(_loadFonts());
        window.add(combo_font);

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

    /**
     * Loads font names from system.
     * Snippet from <a href="https://docs.oracle.com/javase/tutorial/2d/text/fonts.html">https://docs.oracle.com/javase/tutorial/2d/text/fonts.html</a>.
     *
     * @return String[] of available font names
     */
    private String[] _loadFonts () {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return graphicsEnvironment.getAvailableFontFamilyNames();
    }
}
