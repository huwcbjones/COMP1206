package client.windows;

import client.components.WindowPanel;
import shared.User;

import javax.swing.*;


/**
 * View User panel
 *
 * @author Huw Jones
 * @since 20/04/2016
 */
public class ViewUser extends WindowPanel {
    private User user;
    public ViewUser() {
        super("View User");
    }

    public void setUser(User user){
        this.user = user;
    }

    /**
     * Gets the default button for the panel
     *
     * @return Default button
     */
    @Override
    public JButton getDefaultButton() {
        return null;
    }
}
