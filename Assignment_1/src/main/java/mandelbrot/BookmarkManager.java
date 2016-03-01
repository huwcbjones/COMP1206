package mandelbrot;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

/**
 * Manages Bookmarks
 *
 * @author Huw Jones
 * @since 01/03/2016
 */
public class BookmarkManager {
    private Main mainWindow;

    private JPanel panel_bookmarks;

    public BookmarkManager(Main mainWindow){
        this.mainWindow = mainWindow;

        panel_bookmarks = new JPanel();
        panel_bookmarks.setLayout(new BorderLayout());
        panel_bookmarks.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Bookmarks"));

    }

    public JPanel getBookmarkPanel(){
        return this.panel_bookmarks;
    }
}
