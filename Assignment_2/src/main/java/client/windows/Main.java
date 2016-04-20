package client.windows;

import client.utils.WindowTemplate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

/**
 * Main Application Window
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
public final class Main extends WindowTemplate {

    private final static String PANEL_SEARCH = "SearchItems";
    private final static String PANEL_NEWITEM = "NewItem";
    private final static String PANEL_VIEWITEM = "ViewItem";
    private final static String PANEL_VIEWUSER = "ViewUser";

    private JPanel panel_cards;
    private SearchItems panel_search;
    private NewItem panel_newItem;
    private ViewItem panel_viewItem;
    private ViewUser panel_viewUser;

    private JMenuBar menuBar;
    private JMenu menu_file;
    private JMenuItem menu_file_logout;
    private JMenuItem menu_file_exit;
    private JMenu menu_items;
    private JMenuItem menu_items_search;
    private JMenuItem menu_items_new;
    private JMenuItem menu_items_fromID;
    private JMenu menu_options;
    private JMenu menu_help;
    private JMenuItem menu_help_about;

    public static void main(String[] args){
        Main main = new Main();
        main.setVisible(true);
    }

    public Main() {
        super("Home");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(800, 600));

    }

    /**
     * Initialises the GUI components
     */
    @Override
    protected void initComponents() {
        this.initMainMenu();
        this.initPanels();
        initEventListeners();
    }

    private void initPanels(){
        this.panel_cards = new JPanel(new CardLayout());

        this.panel_search = new SearchItems();
        this.panel_cards.add(panel_search, PANEL_SEARCH);

        this.panel_newItem = new NewItem();
        this.panel_cards.add(panel_newItem, PANEL_NEWITEM);

        this.panel_viewItem = new ViewItem();
        this.panel_cards.add(panel_viewItem, PANEL_VIEWITEM);

        this.panel_viewUser = new ViewUser();
        this.panel_cards.add(panel_viewUser, PANEL_VIEWUSER);
    }

    private void initMainMenu(){
        this.menuBar = new JMenuBar();

        //region File
        this.menu_file = new JMenu("File");
        this.menu_file.setMnemonic('f');

        this.menu_file_logout = new JMenuItem("Logout");
        this.menu_file_logout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
        this.menu_file.add(this.menu_file_logout);

        this.menu_file.addSeparator();

        this.menu_file_exit = new JMenuItem("Exit");
        this.menu_file_exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
        this.menu_file.add(this.menu_file_exit);

        this.menuBar.add(this.menu_file);
        //endregion

        //region Items
        this.menu_items = new JMenu("Items");
        this.menu_items.setMnemonic('i');

        this.menu_items_new = new JMenuItem("New Item");
        this.menu_items_new.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        this.menu_items_new.setName(PANEL_NEWITEM);
        this.menu_items.add(this.menu_items_new);

        this.menu_items.addSeparator();

        this.menu_items_search = new JMenuItem("Search");
        this.menu_items_search.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        this.menu_items_new.setName(PANEL_SEARCH);
        this.menu_items.add(this.menu_items_search);

        this.menu_items_fromID = new JMenuItem("From ID");
        this.menu_items_fromID.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
        this.menu_items.add(this.menu_items_fromID);

        this.menuBar.add(this.menu_items);
        //endregion

        //region Options
        this.menu_options = new JMenu("Options");
        this.menu_options.setMnemonic('o');

        this.menuBar.add(this.menu_options);
        //endregion

        //region Help
        this.menu_help = new JMenu("Help");
        this.menu_help.setMnemonic('h');

        this.menu_help_about = new JMenuItem("About");
        this.menu_help_about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        this.menu_help.add(this.menu_help_about);

        this.menuBar.add(this.menu_help);
        //endregion

        this.setJMenuBar(this.menuBar);
    }

    private void initEventListeners() {
        this.menu_file_exit.addActionListener(e -> this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        this.menu_items_new.addActionListener(e -> this.changePanel(PANEL_NEWITEM));
        this.menu_items_search.addActionListener(e -> this.changePanel(PANEL_SEARCH));
    }

    /**
     * Sets the displayed panel
     * @param panelID The panel that should be changed to
     * @beaninfo
     *   preferred: true
     *       bound: true
     *        enum: PANEL_SEARCH        Main.PANEL_SEARCH
     *              PANEL_NEWITEM       Main.PANEL_NEWITEM
     *              PANEL_VIEWITEM      Main.PANEL_VIEWITEM
     *              PANEL_VIEWUSER      Main.PANEL_VIEWUSER
     * description: The panel to switch to
     */
    private void changePanel(String panelID){
        CardLayout layout = (CardLayout)this.panel_cards.getLayout();
        layout.show(this.panel_cards, panelID);
    }
}
