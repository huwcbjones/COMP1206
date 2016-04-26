package client.components;

import client.utils.Server;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Server Table Model
 *
 * @author Huw Jones
 * @since 26/04/2016
 */
public class ServerTableModel extends AbstractTableModel {
    protected static final String[] COLUMN_NAMES = {
        "Name",
        "Address",
        "Port"
    };

    private List<Server> rowData;

    public ServerTableModel() {
        rowData = new ArrayList<>();
    }

    /**
     * Returns false.  This is the default implementation for all cells.
     *
     * @param rowIndex    the row being queried
     * @param columnIndex the column being queried
     * @return false
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void add(Server... server) {
        add(Arrays.asList(server));
    }

    public void add(List<Server> servers) {
        rowData.addAll(servers);
        fireTableDataChanged();
    }

    public void remove(Server... user) {
        remove(Arrays.asList(user));
    }

    public void remove(List<Server> servers) {
        rowData.removeAll(servers);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return rowData.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    public Server getServerAt(int row) {
        return rowData.get(row);
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     *
     * @param rowIndex    the row whose value is to be queried
     * @param columnIndex the column whose value is to be queried
     * @return the value Object at the specified cell
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Server server = getServerAt(rowIndex);
        Object value = null;
        switch (columnIndex) {
            case 0:
                value = server.getName();
                break;
            case 1:
                value = server.getAddress();
                break;
            case 2:
                value = server.getPort();
                break;
        }
        return value;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }
}