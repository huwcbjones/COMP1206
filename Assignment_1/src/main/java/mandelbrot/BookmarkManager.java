package mandelbrot;

import mandelbrot.events.ConfigChangeAdapter;
import mandelbrot.events.DocumentAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utils.Bookmark;
import utils.Complex;
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
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Manages Bookmarks
 *
 * @author Huw Jones
 * @since 01/03/2016
 */
public class BookmarkManager {

    private Main mainWindow;
    private ConfigManager config;

    private HashMap<String, Bookmark> bookmarks;

    private JList list_bookmarks;
    private DefaultListModel<Bookmark> lm_bookmarks;

    private JPanel panel_bookmarks;
    private JPanel panel_add;
    private JTextField text_name;
    private JButton btn_add;
    private JButton btn_delete;

    public BookmarkManager(Main mainWindow){
        this.mainWindow = mainWindow;
        this.config = mainWindow.getConfigManager();
        this.config.addConfigChangeListener(new selectedPointHandler());

        this.loadBookmarks();

        this.panel_bookmarks = new JPanel(new BorderLayout());
        this.panel_bookmarks.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Bookmarks"),
                new EmptyBorder(6, 6, 6, 6)
        ));

        this.init();
    }

    /**
     * Creates bookmark panel
     */
    @SuppressWarnings("unchecked")
    private void init() {
        // Create list model and add elements to it
        this.lm_bookmarks = new DefaultListModel<>();

        // Add loaded bookmarks to list model
        this.bookmarks.values().forEach(this.lm_bookmarks::addElement);

        // Create list view
        this.list_bookmarks = new JList(this.lm_bookmarks);
        this.list_bookmarks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.list_bookmarks.setLayoutOrientation(JList.VERTICAL);
        this.list_bookmarks.addListSelectionListener(new bookmarkSelectionHandler());

        // Create scroller to scroll list view if there are too many bookmarks
        JScrollPane listScroller = new JScrollPane(this.list_bookmarks);
        this.panel_bookmarks.add(listScroller, BorderLayout.CENTER);

        // Add add panel at the bottom of the border layouts
        this.panel_add = new JPanel(new GridBagLayout());
        this.panel_add.setBorder(new EmptyBorder(6, 0, 6, 0));
        this.panel_bookmarks.add(this.panel_add, BorderLayout.PAGE_END);

        this.text_name = new JTextField();
        this.text_name.setUI(new HintTextFieldUI("Bookmark Name", true, Color.lightGray));
        this.text_name.getDocument().addDocumentListener(new textChangeHandler());

        this.btn_add = new JButton("Add");
        this.btn_add.setMnemonic(KeyEvent.VK_A);
        this.btn_add.setEnabled(false);
        this.btn_add.addActionListener(new addClickHandler());

        this.btn_delete = new JButton("Delete");
        this.btn_delete.setMnemonic(KeyEvent.VK_D);
        this.btn_delete.setEnabled(false);
        this.btn_delete.addActionListener(new deleteClickHandler());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 3);
        constraints.weightx = 0.8;

        this.panel_add.add(this.text_name, constraints);

        constraints.gridx = 1;
        constraints.weightx = 0.2;
        constraints.insets = new Insets(0, 3, 0, 3);
        this.panel_add.add(this.btn_add, constraints);
        constraints.gridx = 2;
        constraints.insets = new Insets(0, 3, 0, 0);
        this.panel_add.add(this.btn_delete, constraints);
    }

    //region Saving/Loading

    /**
     * Loads the bookmarks from file to memory
     */
    private void loadBookmarks() {
        Log.Information("Loading bookmarks...");
        this.bookmarks = new HashMap<>();

        // Get bookmarks.json from resources
        InputStream inputStream = this.getClass().getResourceAsStream("/mandelbrot/bookmarks.json");
        if (inputStream == null) {
            Log.Warning("Failed to find bookmarks.json!");
            return;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        JSONParser parser = new JSONParser();

        try {
            // Parse JSON file
            JSONObject root = (JSONObject) parser.parse(reader);
            JSONArray bookmarkArray = (JSONArray) root.get("bookmarks");

            Iterator jsonIterator = bookmarkArray.iterator();
            JSONObject bookmark;

            // Loop over bookmarks and create their Bookmark object
            // Then add then to the HashMap
            while (jsonIterator.hasNext()) {
                bookmark = (JSONObject) jsonIterator.next();
                String name = (String) bookmark.get("name");
                double real = Double.parseDouble(bookmark.get("real").toString());
                double imaginary = Double.parseDouble(bookmark.get("imaginary").toString());

                this.bookmarks.put(name, new Bookmark(name, real, imaginary));
            }

        } catch (IOException e) {
            Log.Error(e.getMessage());
        } catch (ParseException e) {
            Log.Error("Failed to parse JSON." + e.getMessage());
        }
    }

    /**
     * Saves the bookmarks from memory to file
     */
    @SuppressWarnings("unchecked")              // Suppress warning because of the 3rd party library
    public void saveBookmarks() {
        Log.Information("Saving bookmarks...");
        try {
            PrintWriter writer = new PrintWriter(new File(this.getClass().getResource("/mandelbrot/bookmarks.json").getPath()));

            // Create root object
            JSONObject bookmarks = new JSONObject();
            JSONObject bookmark;
            JSONArray bookmarksArray = new JSONArray();

            // Create bookmark JSON Object and add them to the array
            Bookmark bookmarkObj;
            for(Map.Entry<String, Bookmark> b: this.bookmarks.entrySet()){
                bookmarkObj = b.getValue();
                bookmark = new JSONObject();
                bookmark.put("name", bookmarkObj.getName());
                bookmark.put("real", bookmarkObj.getReal());
                bookmark.put("imaginary", bookmarkObj.getImaginary());
                bookmarksArray.add(bookmark);
            }

            // Add the array to the root object
            bookmarks.put("bookmarks", bookmarksArray);

            // Write out the new file
            StringWriter out = new StringWriter();
            bookmarks.writeJSONString(out);
            writer.write(out.toString());
            writer.close();

        } catch (FileNotFoundException e) {
            Log.Warning("Could not find file to write to!");
        } catch (IOException e) {
            Log.Error("Failed to write output!");
        }
    }

    //endregion

    public JPanel getBookmarkPanel(){
        return this.panel_bookmarks;
    }

    /**
     * Adds a bookmark object to the HashMap
     * @param bookmark bookmark to add
     */
    public void addBookmark(Bookmark bookmark) {
        this.bookmarks.put(bookmark.getName(), bookmark);
    }

    //region Event Handling
    private void setBtnAddState() {
        // If no text is entered, can't save
        boolean btn_enabled = !this.text_name.getText().equals("");

        // If name already exists, can't save
        btn_enabled &= !this.bookmarks.containsKey(this.text_name.getText());

        // If selected point is null, can't save
        btn_enabled &= this.config.getSelectedPoint() != null;

        this.btn_add.setEnabled(btn_enabled);
    }

    /**
     * Updates the btn_add state when the text field content changes
     */
    private class textChangeHandler extends DocumentAdapter {

        /**
         * Gives notification that an attribute or set of attributes changed.
         *
         * @param e the document event
         */
        @Override
        public void allUpdate(DocumentEvent e) {
            BookmarkManager.this.setBtnAddState();
        }
    }

    /**
     * Updates the btn_add state when the selected point changes
     */
    private class selectedPointHandler extends ConfigChangeAdapter {
        @Override
        public void selectedPointChange(Complex complex) {
            BookmarkManager.this.setBtnAddState();
        }
    }

    /**
     * Updates the selected point when the selected bookmark changes
     */
    private class bookmarkSelectionHandler implements ListSelectionListener {

        /**
         * Called whenever the value of the selection changes.
         *
         * @param e the event that characterizes the change.
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {
            List selectedObj = BookmarkManager.this.list_bookmarks.getSelectedValuesList();
            BookmarkManager.this.btn_delete.setEnabled(selectedObj.size() == 1);
            if (selectedObj.size() != 1) return;

            Bookmark bookmark = (Bookmark) selectedObj.get(0);
            BookmarkManager.this.config.setSelectedPoint(bookmark.getComplex());
        }
    }

    /**
     * Invoked when btn_add is clicked
     */
    private class addClickHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Create new bookmark
            Bookmark bookmark = new Bookmark(BookmarkManager.this.text_name.getText(), BookmarkManager.this.config.getSelectedPoint());

            // Add it to HashMap
            BookmarkManager.this.addBookmark(bookmark);

            // Add it to ListView
            BookmarkManager.this.lm_bookmarks.addElement(bookmark);

            // Clear text field
            BookmarkManager.this.text_name.setText("");

            BookmarkManager.this.saveBookmarks();
        }
    }

    /**
     * Invoked when btn_delete is clicked
     */
    private class deleteClickHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Check if a bookmark is selected, otherwise return
            List selectedObj = BookmarkManager.this.list_bookmarks.getSelectedValuesList();
            BookmarkManager.this.btn_delete.setEnabled(selectedObj.size() == 1);
            if (selectedObj.size() != 1) return;

            // Get selected bookmark
            Bookmark bookmark = (Bookmark) selectedObj.get(0);

            // Prompt user if they want to delete it
            int option = JOptionPane.showConfirmDialog(
                    BookmarkManager.this.mainWindow,
                    "Are you sure want to delete the bookmark\n" +
                            "\"" + bookmark.getName() + "\"?",
                    "Delete Bookmark",
                    JOptionPane.YES_NO_OPTION
            );

            // If they do, then remove it
            if(option == JOptionPane.YES_OPTION) {
                BookmarkManager.this.lm_bookmarks.removeElement(bookmark);
                BookmarkManager.this.bookmarks.remove(bookmark.getName());
            }
        }
    }
    //endregion
}
