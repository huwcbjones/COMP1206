package client.components;

import client.Client;
import shared.utils.UUIDUtils;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Bid Table Model
 *
 * @author Huw Jones
 * @since 26/04/2016
 */
public class BidTableModel extends AbstractTableModel {
    protected static final String[] COLUMN_NAMES = {
        "User",
        "Price",
        "Time"
    };

    private List<Bid> rowData;

    public BidTableModel() {
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

    public void add(Bid... bid) {
        add(Arrays.asList(bid));
    }

    public void add(List<Bid> bid) {
        rowData.addAll(bid);
        fireTableDataChanged();
    }

    public void removeAll(){
        rowData = new ArrayList<>();
        fireTableDataChanged();
    }

    public void remove(Bid... bid) {
        remove(Arrays.asList(bid));
    }

    public void remove(List<Bid> bid) {
        rowData.removeAll(bid);
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

    public Bid getBidAt(int row) {
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
        Bid bid = getBidAt(rowIndex);
        Object value = null;
        switch (columnIndex) {
            case 0:
                if(bid.getUser() == null){
                    value = UUIDUtils.UUIDToBase64String(bid.getUserID());
                } else if(bid.getUserID().equals(Client.getUser().getUniqueID())) {
                    value = "You";
                } else {
                    value = bid.getUser().getUsername();
                }
                break;
            case 1:
                value = bid.getPriceString();
                break;
            case 2:
                value = bid.getTimeString("dd/MM/yyyy HH:mm:ss z");
                break;
        }
        return value;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }
}