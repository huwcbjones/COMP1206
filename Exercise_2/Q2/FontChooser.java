import com.sun.scenario.effect.impl.sw.java.JSWBlend_COLOR_BURNPeer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A Font Chooser GUI
 *
 * @author Huw Jones
 * @since 29/01/2016
 */
public class FontChooser extends JFrame {

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
        super("Font Chooser");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {

        }

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(new FlowLayout());

        Container window = this.getContentPane();

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

        // Radio Buttons
        panel_font = new JPanel();
        panel_font.setLayout(new BoxLayout(panel_font, BoxLayout.Y_AXIS));

        group_font = new ButtonGroup();

        radio_times = new JRadioButton("Times");
        radio_times.setActionCommand("Times New Roman");
        radio_times.addActionListener(new fontUpdateListener());
        group_font.add(radio_times);
        panel_font.add(radio_times);

        radio_helvetica = new JRadioButton("Helvetica");
        radio_helvetica.setActionCommand("Helvetica");
        radio_helvetica.addActionListener(new fontUpdateListener());
        group_font.add(radio_helvetica);
        panel_font.add(radio_helvetica);

        radio_courier = new JRadioButton("Courier");
        radio_courier.setActionCommand("Courier New");
        radio_courier.addActionListener(new fontUpdateListener());
        group_font.add(radio_courier);
        panel_font.add(radio_courier);

        group_font.setSelected(radio_times.getModel(), true);
        window.add(panel_font);

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
        this.pack();
        this.setSize(new Dimension(600, this.getHeight()));
        this.setVisible(true);
    }

    protected void displayFont(String fontName){
        int style = getStyle();
        this.text_input.setText(fontName);
        this.text_input.setFont(new Font(fontName, style, 12));
    }

    protected int getStyle(){
        int style = 0;
        if(checkbox_bold.isSelected()){
            style += Font.BOLD;
        }
        if(checkbox_italic.isSelected()){
            style += Font.ITALIC;
        }
        return style;
    }

    protected class fontUpdateListener implements ActionListener{

        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            String font = group_font.getSelection().getActionCommand();
            displayFont(font);
        }
    }
}
