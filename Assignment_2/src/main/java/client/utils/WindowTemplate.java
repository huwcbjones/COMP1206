package client.utils;

import client.Client;
import client.Config;
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
    protected final Config config;

    public WindowTemplate(String title) {
        super(title + " | Biddr");
        this.setImageIcon();
        this.config = Client.getConfig();

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
}
