import javafx.scene.control.TextFormatter;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A Font Chooser GUI
 *
 * @author Huw Jones
 * @since 29/01/2016
 */
public class FontChooserAdvanced {

    private JFrame window;

    private JComboBox<String> combo_font;
    private JSpinner spinner_size;

    private JPanel panel_style;
    private JCheckBox checkbox_bold;
    private JCheckBox checkbox_italic;

    private JTextField text_input;

    private JButton btn_ok;

    public FontChooserAdvanced() {
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
        checkbox_bold.addActionListener(new fontUpdateListener());
        panel_style.add(checkbox_bold);

        checkbox_italic = new JCheckBox("Italic");
        checkbox_italic.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkbox_italic.addActionListener(new fontUpdateListener());
        panel_style.add(checkbox_italic);

        window.add(panel_style);

        // Font ComboBox
        combo_font = new JComboBox<>(_loadFonts());
        combo_font.addActionListener(new fontUpdateListener());
        window.add(combo_font);

        // Font Size
        SpinnerModel spinnerModel = new SpinnerNumberModel(12, 1, 32, 1);
        spinner_size = new JSpinner(spinnerModel);
        spinner_size.addChangeListener(new fontUpdateListener());
        window.add(spinner_size);

        // Input
        text_input = new JTextField(20);
        window.add(text_input);

        // Buttons
        btn_ok = new JButton("OK");
        btn_ok.addActionListener(new fontUpdateListener());
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
    private String[] _loadFonts() {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return graphicsEnvironment.getAvailableFontFamilyNames();
    }

    public static void main(String[] args) {
        FontChooserAdvanced fontChooserAdvanced = new FontChooserAdvanced();
    }

    protected void displayFont(String fontName) {
        int style = getStyle();
        int size = getFontSize();
        this.text_input.setText(fontName);
        this.text_input.setFont(new Font(fontName, style, size));
    }

    protected int getStyle() {
        int style = 0;
        if (checkbox_bold.isSelected()) {
            style += Font.BOLD;
        }
        if (checkbox_italic.isSelected()) {
            style += Font.ITALIC;
        }
        return style;
    }

    protected int getFontSize() {
        return (int)spinner_size.getModel().getValue();
    }

    protected class fontUpdateListener implements ActionListener, ChangeListener {

        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            String font = (String) combo_font.getSelectedItem();
            displayFont(font);
        }

        /**
         * Invoked when the target of the listener has changed its state.
         *
         * @param e a ChangeEvent object
         */
        @Override
        public void stateChanged(ChangeEvent e) {
            String font = (String) combo_font.getSelectedItem();
            displayFont(font);
        }
    }
}
