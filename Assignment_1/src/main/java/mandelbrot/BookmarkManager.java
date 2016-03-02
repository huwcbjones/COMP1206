package mandelbrot;

import mandelbrot.events.DocumentAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utils.Bookmark;
import utils.HintTextFieldUI;
import utils.Log;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Manages Bookmarks
 *
 * @author Huw Jones
 * @since 01/03/2016
 */
public class BookmarkManager {
    private Main mainWindow;
    private ConfigManager config;

    private JPanel panel_bookmarks;

    private HashMap<String, Bookmark> bookmarks;

    private JList list_bookmarks;
    private DefaultListModel<Bookmark> lm_bookmarks;

    private JPanel panel_add;
    private JTextField text_name;
    private JButton btn_add;
    private JButton btn_delete;

    public BookmarkManager(Main mainWindow){
        this.mainWindow = mainWindow;
        this.config = mainWindow.getConfigManager();

        loadBookmarks();

        panel_bookmarks = new JPanel(new BorderLayout());
        panel_bookmarks.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Bookmarks"),
                new EmptyBorder(6, 6, 6, 6)
        ));

        init();
    }

    private void loadBookmarks() {
        Log.Information("Loading bookmarks...");
        bookmarks = new HashMap<>();

        InputStream inputStream = this.getClass().getResourceAsStream("/mandelbrot/bookmarks.json");
        if (inputStream == null) {
            Log.Warning("Failed to find bookmarks.json!");
            return;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        JSONParser parser = new JSONParser();

        try {
            JSONObject root = (JSONObject) parser.parse(reader);
            JSONArray bookmarkArray = (JSONArray) root.get("bookmarks");

            Iterator jsonIterator = bookmarkArray.iterator();
            JSONObject bookmark;
            while (jsonIterator.hasNext()) {
                bookmark = (JSONObject) jsonIterator.next();
                String name = (String) bookmark.get("name");
                double real = Double.parseDouble((String) bookmark.get("real"));
                double imaginary = Double.parseDouble((String) bookmark.get("imaginary"));

                bookmarks.put(name, new Bookmark(name, real, imaginary));
            }

        } catch (IOException e) {
            Log.Error(e.getMessage());
        } catch (ParseException e) {
            Log.Error("Failed to parse JSON." + e.getMessage());
        }
    }

    //region Saving/Loading

    private void init() {
        // Create list model and add elements to it
        lm_bookmarks = new DefaultListModel<>();
        bookmarks.values().forEach(lm_bookmarks::addElement);

        list_bookmarks = new JList(lm_bookmarks);
        list_bookmarks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list_bookmarks.setLayoutOrientation(JList.VERTICAL);
        list_bookmarks.addListSelectionListener(new bookmarkSelectionHandler());

        JScrollPane listScroller = new JScrollPane(list_bookmarks);
        panel_bookmarks.add(listScroller, BorderLayout.CENTER);

        panel_add = new JPanel(new GridBagLayout());
        panel_add.setBorder(new EmptyBorder(6, 0, 6, 0));
        panel_bookmarks.add(panel_add, BorderLayout.PAGE_END);


        text_name = new JTextField();
        text_name.setUI(new HintTextFieldUI("Bookmark Name", true, Color.lightGray));
        text_name.getDocument().addDocumentListener(new textChangeHandler());

        btn_add = new JButton("Add");
        btn_add.setMnemonic(KeyEvent.VK_A);
        btn_add.setEnabled(false);
        btn_add.addActionListener(new addClickHandler());


        btn_delete = new JButton("Delete");
        btn_delete.setMnemonic(KeyEvent.VK_D);
        btn_delete.setEnabled(false);
        //btn_delete.addActionListener(new deleteClickHandler()); //TODO: Implement delete button

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 3);
        constraints.weightx = 0.8;

        panel_add.add(text_name, constraints);

        constraints.gridx = 1;
        constraints.weightx = 0.2;
        constraints.insets = new Insets(0, 3, 0, 3);
        panel_add.add(btn_add, constraints);
        constraints.gridx = 2;
        constraints.insets = new Insets(0, 3, 0, 0);
        panel_add.add(btn_delete, constraints);
    }

    //TODO: Save bookmarks to JSON file
    public void saveBookmarks() {

    }

    //endregion

    public JPanel getBookmarkPanel(){
        return this.panel_bookmarks;
    }

    public void addBookmark(Bookmark bookmark) {
        this.bookmarks.put(bookmark.getName(), bookmark);
    }

    //region Event Handling
    private class textChangeHandler extends DocumentAdapter {

        /**
         * Gives notification that an attribute or set of attributes changed.
         *
         * @param e the document event
         */
        @Override
        public void allUpdate(DocumentEvent e) {
            boolean btn_enabled = !text_name.getText().equals("");
            btn_enabled &= !bookmarks.containsKey(text_name.getText());
            btn_enabled &= config.getSelectedPoint() != null;
            btn_add.setEnabled(btn_enabled);
        }
    }

    private class bookmarkSelectionHandler implements ListSelectionListener {

        /**
         * Called whenever the value of the selection changes.
         *
         * @param e the event that characterizes the change.
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {
            List selectedObj = list_bookmarks.getSelectedValuesList();
            btn_delete.setEnabled(selectedObj.size() == 1);
            if (selectedObj.size() != 1) return;

            Bookmark bookmark = (Bookmark) selectedObj.get(0);
            config.setSelectedPoint(bookmark.getComplex());
        }
    }

    private class addClickHandler implements ActionListener {

        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            Bookmark bookmark = new Bookmark(text_name.getText(), config.getSelectedPoint());
            addBookmark(bookmark);
            lm_bookmarks.addElement(bookmark);
            text_name.setText("");
            saveBookmarks();
        }
    }


    //endregion
}
