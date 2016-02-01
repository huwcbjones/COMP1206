import javax.swing.*;

/**
 * An mp3 Player Layout
 *
 * @author Huw Jones
 * @since 30/01/2016
 */
public class mp3Player {

    private JFrame window;
    private JPanel panel_artwork;
    private JPanel panel_controls;
    private JPanel panel_playlist;

    private JSlider slider_time;
    private JButton btn_previous;
    private JButton btn_playPause;
    private JButton btn_next;
    private JButton btn_stop;

    public static void main(String[] args){
        mp3Player mp3Player = new mp3Player();
    }

    public mp3Player(){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {

        }
    }
}
