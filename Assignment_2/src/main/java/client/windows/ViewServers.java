package client.windows;

import client.Client;
import client.components.ServerTableModel;
import client.components.WindowPanel;
import client.utils.Server;
import shared.components.JLinkLabel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;

/**
 * Panel for viewing list of servers
 *
 * @author Huw Jones
 * @since 26/04/2016
 */
public class ViewServers extends WindowPanel {

    private JTable table_servers;
    private ServerTableModel model_servers;
    public JButton btn_edit;
    public JButton btn_new;
    public JLinkLabel btn_back;
    private Server selectedServer = null;

    public ViewServers() {
        super("View Servers");
        this.initComponents();
        this.table_servers.getSelectionModel().addListSelectionListener(new SelectionChangedHandler());
    }

    private void initComponents(){
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        int row = 0;

        this.model_servers = new ServerTableModel();
        this.table_servers = new JTable(this.model_servers);
        this.table_servers.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        this.table_servers.setShowGrid(false);
        this.table_servers.setShowHorizontalLines(false);
        this.table_servers.setShowVerticalLines(false);
        this.table_servers.setRowMargin(0);
        this.table_servers.setIntercellSpacing(new Dimension(1, 1));
        this.table_servers.setFillsViewportHeight(true);
        this.table_servers.setRowSorter(new TableRowSorter<>(this.model_servers));
        this.table_servers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.model_servers.add(Client.getConfig().getServers());
        c.insets = new Insets(0, 0, 6, 0);
        c.gridy = row;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 0.5;
        this.add(new JScrollPane(this.table_servers), c);
        row++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0;

        this.btn_edit = new JButton("Edit Selected Server");
        this.btn_edit.setMnemonic('e');
        this.btn_edit.setEnabled(false);
        c.insets = new Insets(6, 0, 3, 0);
        c.gridy = row;
        this.add(this.btn_edit, c);
        row++;

        this.btn_new = new JButton("Add New Server");
        this.btn_new.setMnemonic('a');
        c.insets = new Insets(3, 0, 6, 0);
        c.gridy = row;
        this.add(this.btn_new, c);
        row++;

        this.btn_back = new JLinkLabel("Back to Login", JLabel.LEADING);
        this.btn_back.setFont(this.btn_back.getFont().deriveFont(this.btn_back.getFont().getStyle() | Font.BOLD));
        c.insets = new Insets(6, 0, 6, 0);
        c.gridy = row;
        c.fill = GridBagConstraints.NONE;
        this.add(this.btn_back, c);
        row++;
    }

    /**
     * Gets the default button for the panel
     *
     * @return Default button
     */
    @Override
    public JButton getDefaultButton() {
        return this.btn_new;
    }

    private class SelectionChangedHandler implements ListSelectionListener {

        /**
         * Called whenever the value of the selection changes.
         *
         * @param e the event that characterizes the change.
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {
            ViewServers.this.btn_edit.setEnabled(!ViewServers.this.table_servers.getSelectionModel().isSelectionEmpty());
            if(ViewServers.this.table_servers.getSelectionModel().isSelectionEmpty()){
                ViewServers.this.selectedServer = null;
                return;
            }
            int selectedRowIndex =  ViewServers.this.table_servers.getSelectedRow();
            ViewServers.this.selectedServer = ViewServers.this.model_servers.getServerAt(selectedRowIndex);
        }
    }

    public Server getSelectedServer(){
        return this.selectedServer;
    }
}
