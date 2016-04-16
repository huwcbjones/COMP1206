package client.windows;

import client.utils.WindowTemplate;

import javax.swing.*;
import java.awt.*;

/**
 * Main Application Window
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
public final class Main extends WindowTemplate {

    public Main() {
        super("View Items");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(800, 600));

    }

    /**
     * Initialises the GUI components
     */
    @Override
    protected void initComponents() {

    }
}
