package client.components;

import javax.swing.*;
import java.awt.*;

/**
 * Window Panel template for Card Layout
 *
 * @author Huw Jones
 * @since 23/04/2016
 */
public abstract class WindowPanel extends JPanel {

    private String title;

    public WindowPanel(String title) {
        super(new BorderLayout(6, 6));
        this.setBackground(Color.WHITE);
        this.title = title;
    }

    /**
     * Returns the title of this panel
     *
     * @return Panel title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Sets the title for this panel
     *
     * @param title Title of this panel
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the main panel for this window
     */
    public final void setMainPanel(Component panel) {
        this.add(panel, BorderLayout.CENTER);
    }

    /**
     * Gets the default button for the panel
     *
     * @return Default button
     */
    public abstract JButton getDefaultButton();
}
