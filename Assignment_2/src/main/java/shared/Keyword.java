package shared;

import java.io.Serializable;

/**
 * Keyword Object
 *
 * @author Huw Jones
 * @since 04/05/2016
 */
public class Keyword implements Serializable {
    private static final long serialUID = 14L;

    private final int keywordID;
    private final String keyword;

    public Keyword(int keywordID, String keyword) {
        this.keywordID = keywordID;
        this.keyword = keyword;
    }

    public int getKeywordID() {
        return keywordID;
    }

    public String getKeyword() {
        return keyword;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Keyword)) return false;
        Keyword keyObj = (Keyword)obj;

        return keyObj.getKeywordID() == this.getKeywordID();
    }

    @Override
    public String toString() {
        return this.keyword;
    }
}
