package client.components;

import client.Client;

/**
 * Client side window template
 *
 * @author Huw Jones
 * @since 12/05/2016
 */
public abstract class WindowTemplate extends shared.utils.WindowTemplate {

    public WindowTemplate(String title) {
        super(title);
    }

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
        if(Client.getUser() != null) {
            super.setTitle(title + " | Biddr - [" + Client.getUser().getFullName() + "]");
        } else {
            super.setTitle(title + " | Biddr");
        }
    }
}
