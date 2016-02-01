import javax.swing.*;
import java.awt.*;

/**
 * An mp3 Player Layout
 *
 * @author Huw Jones
 * @since 30/01/2016
 */
public class mp3Player {

    private JFrame window;
    private JPanel panel_artwork;
    private JPanel panel_controller;
    private JPanel panel_controls;
    private JPanel panel_controls_buttons;
    private JPanel panel_playlist;

    private JSlider slider_time;
    private JLabel label_time;
    private JSlider slider_volume;
    private JButton btn_previous;
    private JButton btn_playPause;
    private JButton btn_next;
    private JButton btn_stop;

    public static void main (String[] args) {
        mp3Player mp3Player = new mp3Player();
    }

    public mp3Player () {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {

        }

        window = new JFrame("mp3 Player");
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setLayout(new BorderLayout());

        // Artwork panel
        panel_artwork = new JPanel();
        window.add(panel_artwork, BorderLayout.CENTER);

        // Controls
        panel_controller = new JPanel(new BorderLayout(4, 4));
        window.add(panel_controller, BorderLayout.SOUTH);

        slider_time = new JSlider();
        slider_time.setMinimum(0);
        slider_time.setValue(0);
        slider_time.setMaximum(0);
        panel_controller.add(slider_time, BorderLayout.CENTER);

        panel_controls = new JPanel(new BorderLayout(4, 4));
        panel_controller.add(panel_controls, BorderLayout.SOUTH);

        panel_controls_buttons = new JPanel(new FlowLayout());
        panel_controls.add(panel_controls_buttons, BorderLayout.CENTER);

        btn_stop = new JButton("STOP");
        panel_controls_buttons.add(btn_stop);

        btn_previous = new JButton("<< Previous");
        panel_controls_buttons.add(btn_previous);

        btn_playPause = new JButton("Play/Pause");
        panel_controls_buttons.add(btn_playPause);

        btn_next = new JButton("Next >>");
        panel_controls_buttons.add(btn_next);

        label_time = new JLabel("00:00/00:00");
        panel_controls.add(label_time, BorderLayout.LINE_START);

        slider_volume = new JSlider();
        slider_volume.setMinimum(0);
        slider_volume.setMaximum(16);
        slider_volume.setValue(16);
        slider_volume.setMajorTickSpacing(4);
        slider_volume.setMinorTickSpacing(1);
        slider_volume.setPaintTicks(true);
        slider_volume.setPreferredSize(new Dimension(128, 20));
        panel_controls.add(slider_volume, BorderLayout.LINE_END);

        window.setSize(new Dimension(640, 480));
        window.setVisible(true);
    }
}
