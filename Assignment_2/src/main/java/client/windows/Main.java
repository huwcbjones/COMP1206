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

    private JMenuBar menuBar;
    private JMenu menu_file;
    private JMenu menu_items;
    private JMenu menu_options;
    private JMenu menu_help;


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
    }

    private void initMainMenu(){
        this.menuBar = new JMenuBar();

        this.menu_file = new JMenu("File");
        this.menu_file.setMnemonic('f');

        this.menuBar.add(this.menu_file);

        this.menu_items = new JMenu("Items");
        this.menu_items.setMnemonic('i');

        this.menuBar.add(this.menu_items);

        this.menu_options = new JMenu("Options");
        this.menu_options.setMnemonic('o');

        this.menuBar.add(this.menu_options);

        this.menu_help = new JMenu("Help");
        this.menu_help.setMnemonic('h');

        this.menuBar.add(this.menu_help);

        this.setJMenuBar(this.menuBar);
    }
}
