package shared;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 12/05/2016
 */
public class SearchResults implements Serializable {

    private static final long serialUID = -123;

    private final ArrayList<Item> items;
    private final long searchID;

    public SearchResults(ArrayList<Item> items, long searchID) {
        this.items = items;
        this.searchID = searchID;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public long getSearchID() {
        return searchID;
    }
}
