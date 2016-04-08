package client.events;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Implements Adapter for DocumentListener
 * NOTE: FROM COMP1206 ASSIGNMENT 1
 *
 * @author Huw Jones
 * @since 02/03/2016
 */
public class DocumentAdapter implements DocumentListener {
    /**
     * Gives notification that there was an change in the document.
     *
     * @param e the document event
     */
    public void allUpdate(DocumentEvent e){

    }

    /**
     * Gives notification that there was an insert into the document.  The
     * range given by the DocumentEvent bounds the freshly inserted region.
     *
     * @param e the document event
     */
    @Override
    public void insertUpdate(DocumentEvent e) {
        this.allUpdate(e);
    }

    /**
     * Gives notification that a portion of the document has been
     * removed.  The range is given in terms of what the view last
     * saw (that is, before updating sticky positions).
     *
     * @param e the document event
     */
    @Override
    public void removeUpdate(DocumentEvent e) {
        this.allUpdate(e);
    }

    /**
     * Gives notification that an attribute or set of attributes changed.
     *
     * @param e the document event
     */
    @Override
    public void changedUpdate(DocumentEvent e) {
        this.allUpdate(e);
    }
}
