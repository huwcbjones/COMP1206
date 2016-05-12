package server.components;

import server.objects.Item;

import javax.swing.table.AbstractTableModel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 23/04/2016
 */
public class ItemTableModel extends AbstractTableModel {

    protected static final String[] COLUMN_NAMES = {
        "Title",
        "User",
        "Start Time",
        "End Time",
        "Reserve",
        "Top Bid",
    };

    private HashMap<UUID, Item> rowData;

    public ItemTableModel(){
        rowData = new HashMap<>();
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

    public void add(Item... item) {
        add(Arrays.asList(item));
    }

    public void add(List<Item> items) {
        items.stream().forEach(item -> this.rowData.put(item.getID(), item));
        fireTableDataChanged();
    }

    public void remove(Item...item) {
        remove(Arrays.asList(item));
    }

    public void remove(List<Item> items) {
        items.stream().forEach(item -> this.rowData.remove(item.getID()));
        fireTableDataChanged();
    }

    public void removeAll() {
        this.rowData = new HashMap<>();
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

    public Item getItemAt(int row){
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
        Item item = getItemAt(rowIndex);
        Object value = null;
        switch (columnIndex) {
            case 0:
                value = item.getTitle();
                break;
            case 1:
                value = item.getUser().getUsername();
                break;
            case 2:
                value = item.getStartTimeString();
                break;
            case 3:
                value = item.getEndTimeString();
                break;
            case 4:
                value = item.getReserveString();
                break;
            case 5:
                if(item.getNumberOfBids() != 0) {
                    value = item.getTopBid().getUser().getUsername() + ": " + item.getTopBid().getPriceString();
                } else {
                    value = "-";
                }
        }
        return value;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }
}
