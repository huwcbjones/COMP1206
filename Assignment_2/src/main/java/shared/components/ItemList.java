package shared.components;


import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays a List of Items
 * Like a JList, but you have to provide your own cell renderer, and you add event listeners to them!
 *
 * @author Huw Jones
 * @since 10/05/2016
 */
public abstract class ItemList<E> extends JPanel {

    private List<E> data = new ArrayList<>();
    private Timer repaintTimer;

    public ItemList() {
        this(new ArrayList<>());
    }

    public ItemList(List<E> data) {
        this.setLayout(new GridBagLayout());
        this.addAllElements(data);
        this.setBackground(Color.WHITE);
        this.repaintTimer = new Timer(1000, e ->{
            if(this.data.size() != 0){
                this.repaint();
            }
        });
        this.repaintTimer.setRepeats(true);
        this.repaintTimer.start();
    }

    public void addElement(E... element) {
        for (E e : element) {
            this.data.add(e);
        }
        this.redraw();
    }

    public void addAllElements(List<E> element) {
        this.data.addAll(element);
        this.redraw();
    }

    public void removeElement(E... element) {
        for (E e : element) {
            this.data.remove(e);
        }
        this.redraw();
    }

    public void removeAllElements() {
        data = new ArrayList<>();
        this.redraw();
    }

    private void redraw() {
        this.removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.weighty = 0;
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        for (E e : this.data) {
            c.gridy = row;
            this.add(drawItem(e), c);
            row++;
            c.gridy = row;
            this.add(new JSeparator(JSeparator.HORIZONTAL), c);
            row++;
        }
        c.gridy = row;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;

        JPanel filler = new JPanel();
        filler.setBackground(Color.WHITE);
        this.add(filler, c);
        if (this.getParent() != null) {
            this.getParent().validate();
            this.getParent().repaint();
        }
    }

    public abstract Component drawItem(E item);
}
