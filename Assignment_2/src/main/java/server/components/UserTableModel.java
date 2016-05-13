package server.components;

import server.objects.User;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Table model for User
 *
 * @author Huw Jones
 * @since 23/04/2016
 */
public class UserTableModel extends AbstractTableModel {

    protected static final String[] COLUMN_NAMES = {
        "Username",
        "First Name",
        "Last Name",
        "Client",
        "UUID"
    };

    private List<User> rowData;

    public UserTableModel() {
        rowData = new ArrayList<>();
    }

    public void add(User... user) {
        add(Arrays.asList(user));
    }

    public void add(List<User> users) {
        rowData.addAll(users);
        fireTableDataChanged();
    }

    public void remove(User... user) {
        remove(Arrays.asList(user));
    }

    public void remove(List<User> users) {
        rowData.removeAll(users);
        fireTableDataChanged();
    }

    public void removeAll() {
        this.rowData = new ArrayList<>();
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
        User user = getUserAt(rowIndex);
        Object value = null;
        switch (columnIndex) {
            case 0:
                value = user.getUsername();
                break;
            case 1:
                value = user.getFirstName();
                break;
            case 2:
                value = user.getLastName();
                break;
            case 3:
                if(user.getClient() != null) {
                    value = user.getClient().toString();
                }
                break;
            case 4:
                value = user.getUniqueID();
                break;
        }
        return value;
    }

    public User getUserAt(int row) {
        return rowData.get(row);
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
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
}
