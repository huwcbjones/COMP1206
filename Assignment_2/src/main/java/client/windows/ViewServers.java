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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Panel for viewing list of servers
 *
 * @author Huw Jones
 * @since 26/04/2016
 */
public class ViewServers extends WindowPanel {

    public JButton btn_delete;
    public JButton btn_edit;
    public JButton btn_new;
    public JLinkLabel btn_back;
    private JLabel label_servers;
    private JTable table_servers;
    private ServerTableModel model_servers;
    private Server selectedServer = null;

    public ViewServers() {
        super("View Servers");
        this.initComponents();
        this.table_servers.getSelectionModel().addListSelectionListener(new SelectionChangedHandler());
        this.btn_delete.addActionListener(new DeleteHandler());
        this.addComponentListener(new ComponentHandler());
    }

    private void initComponents() {
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        int row = 0;

        this.label_servers = new JLabel("Servers", JLabel.LEADING);
        this.label_servers.setLabelFor(this.label_servers);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.add(this.label_servers, c);
        row++;

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

        this.btn_delete = new JButton("Delete Selected Server");
        this.btn_delete.setMnemonic('e');
        this.btn_delete.setEnabled(false);
        c.insets = new Insets(6, 0, 3, 0);
        c.gridy = row;
        this.add(this.btn_delete, c);
        row++;

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

    public Server getSelectedServer() {
        return this.selectedServer;
    }

    private void updateServerTable() {
        // Refresh list of servers
        ViewServers.this.selectedServer = null;
        this.model_servers.removeAll();
        this.model_servers.add(Client.getConfig().getServers());
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
            ViewServers.this.btn_delete.setEnabled(!ViewServers.this.table_servers.getSelectionModel().isSelectionEmpty());
            if (ViewServers.this.table_servers.getSelectionModel().isSelectionEmpty()) {
                ViewServers.this.selectedServer = null;
                return;
            }
            int selectedRowIndex = ViewServers.this.table_servers.getSelectedRow();
            ViewServers.this.selectedServer = ViewServers.this.model_servers.getServerAt(selectedRowIndex);
        }
    }

    private class ComponentHandler extends ComponentAdapter {

        /**
         * Invoked when the component has been made visible.
         *
         * @param e
         */
        @Override
        public void componentShown(ComponentEvent e) {
            updateServerTable();
        }
    }

    private class DeleteHandler implements ActionListener {
        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            int result = JOptionPane.showConfirmDialog(ViewServers.this,
                "Are you sure you wish to remove \"" + ViewServers.this.selectedServer.getName() + "\"?",
                "Delete Server?",
                JOptionPane.YES_NO_OPTION
            );
            if (result != JOptionPane.YES_OPTION) return;

            Client.getConfig().removeServer(ViewServers.this.selectedServer);
            Client.getConfig().saveConfig();
            updateServerTable();
        }
    }
}
