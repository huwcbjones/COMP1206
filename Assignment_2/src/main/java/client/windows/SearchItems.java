package client.windows;

import shared.Item;

import javax.swing.*;
import java.awt.*;

/**
 * Search Panel
 *
 * @author Huw Jones
 * @since 12/04/2016
 */
public class SearchItems extends JPanel {

    private JPanel panel_search;
    private JScrollPane panel_results;

    public SearchItems(){
        super();
        this.initComponents();
    }

    private void initComponents(){
        JPanel panel_results = new JPanel(new GridBagLayout());

    }

    public void addItem(Item item){

    }
}
