package shared.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.net.URL;

/**
 * Template for Windows
 *
 * @author Huw Jones
 * @since 07/04/2016
 */
public abstract class WindowTemplate extends JFrame {

    protected static final Logger log = LogManager.getLogger(WindowTemplate.class);

    public WindowTemplate(){
        super();
        this.setImageIcon();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            log.debug("Failed to set System Look and Feel. {}", ex.getMessage());
            log.trace(ex);
        }
        this.initComponents();
    }
    public WindowTemplate(String title) {
        super(title + " | Biddr");
        this.setImageIcon();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            log.debug("Failed to set System Look and Feel. {}", ex.getMessage());
            log.trace(ex);
        }
        this.initComponents();
    }

    private void setImageIcon(){
        URL imgIconURL = WindowTemplate.class.getResource("/img/biddr_logo.png");
        if(imgIconURL == null){
            log.warn("Could not find biddr_logo.png");
            return;
        }
        ImageIcon img = new ImageIcon(imgIconURL);
        this.setIconImage(img.getImage());
    }

    /**
     * Initialises the GUI components
     */
    protected abstract void initComponents();

    /**
     * Sets the title for this frame to the specified string.
     *
     * @param title the title to be displayed in the frame's border.
     *              A <code>null</code> value
     *              is treated as an empty string, "".
     * @see #getTitle
     */
    @Override
    public void setTitle(String title) {
        super.setTitle(title + " | Biddr");
    }
}
