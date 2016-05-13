package server.components;

import server.objects.Item;
import shared.utils.UUIDUtils;

import javax.swing.table.AbstractTableModel;
import java.util.*;

/**
 * Table model for Items
 *
 * @author Huw Jones
 * @since 23/04/2016
 */
public class ItemTableModel extends AbstractTableModel {

    protected static final String[] COLUMN_NAMES = {
        "Item ID",
        "Title",
        "Seller",
        "Start Time",
        "End Time",
        "Reserve",
        "Top Bidder",
        "Top Bid",
    };

    private HashMap<UUID, Item> rowData;
    private ArrayList<UUID> indexData;

    public ItemTableModel(){
        rowData = new HashMap<>();
        indexData = new ArrayList<>();
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
        items.stream().forEach(item -> {
            this.rowData.put(item.getID(), item);
            this.indexData.add(item.getID());
        });
        fireTableDataChanged();
    }

    public void remove(Item...item) {
        remove(Arrays.asList(item));
    }

    public void remove(List<Item> items) {
        items.stream().forEach(item -> {
            this.rowData.remove(item.getID());
            this.indexData.remove(item.getID());
        });
        fireTableDataChanged();
    }

    public void removeAll() {
        this.rowData = new HashMap<>();
        this.indexData = new ArrayList<>();
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
        return this.rowData.get(this.indexData.get(row));
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
                value = UUIDUtils.UUIDToBase64String(item.getID());
                break;
            case 1:
                value = item.getTitle();
                break;
            case 2:
                value = item.getUser().getUsername();
                break;
            case 3:
                value = item.getStartTimeString();
                break;
            case 4:
                value = item.getEndTimeString();
                break;
            case 5:
                value = item.getReserveString();
                break;
            case 6:
                if(item.getNumberOfBids() != 0) {
                    value = item.getTopBid().getUser().getUsername();
                } else {
                    value = "-";
                }
                break;
            case 7:
                if(item.getNumberOfBids() != 0) {
                    value = item.getTopBid().getPriceString();
                } else {
                    value = "-";
                }
                break;
        }
        return value;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }
}
