package client.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Window Panel template for Card Layout
 *
 * @author Huw Jones
 * @since 23/04/2016
 */
public abstract class WindowPanel extends JPanel {

    private String title;
    private JPanel panel_title;
    private JLabel label_title;

    public WindowPanel(String title){
        super(new BorderLayout(6, 6));
        this.title = title;
        this.createTitlePanel();
    }

    public void setTitle(String title){
        this.title = title;
        this.label_title.setText(title);
    }

    public String getTitle(){
        return this.title;
    }

    private void createTitlePanel(){
        this.panel_title = new JPanel(new BorderLayout());
        this.panel_title.setBorder(new EmptyBorder(new Insets(6, 6, 6, 6)));
        this.label_title = new JLabel(this.title, JLabel.LEADING);
        this.label_title.setFont(this.label_title.getFont().deriveFont(18f));
        this.panel_title.add(this.label_title, BorderLayout.CENTER);
        this.add(this.panel_title, BorderLayout.PAGE_START);
    }

    /**
     * Sets the main panel for this window
     */
    public final void setMainPanel(Component panel){
        this.add(panel, BorderLayout.CENTER);
    }
}
