import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Panel Position GUI
 *
 * @author Huw Jones
 * @since 05/02/2016
 */
public class Panels extends JFrame {

    private ArrayList<JPanel> panels;
    private JPanel panel_lower;
    private JPanel panel_upper;

    private JLabel label_cursorPosition;
    private JLabel label_cursorPositionRelative;

    private Component currentEnteredComponent = null;

    private boolean redrawInProgress = false;

    public Panels() {
        // Create JFrame and add listeners/set properties
        super("Panels");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setSize(new Dimension(1600, 900));
        this.setMinimumSize(new Dimension(640, 480));
        this.addComponentListener(new ResizeEventListener());
        this.addMouseMotionListener(new CursorPositionListener());
        this.panels = new ArrayList<>();

        // Create Panes
        panel_upper = new JPanel(null);
        panel_upper.setBorder(BorderFactory.createLineBorder(Color.black));
        this.add(panel_upper, BorderLayout.CENTER);

        panel_lower = new JPanel(new FlowLayout());
        this.add(panel_lower, BorderLayout.PAGE_END);

        // Add labels
        label_cursorPosition = new JLabel("Cursor Position: (,)");
        panel_lower.add(label_cursorPosition);

        label_cursorPositionRelative = new JLabel("Relative Cursor Position: (,)");
        panel_lower.add(label_cursorPositionRelative);

        this.setVisible(true);
        this.addPanels();
    }

    /**
     * Adds a (fairly) random amount of panels to the upper pane
     */
    private void addPanels() {
        redrawInProgress = true;

        if (this.panels.size() != 0) this.panels.forEach(panel_upper::remove);

        this.panels = new ArrayList<>();

        JPanel randomPanel;

        for (int i = 0; i < (10 + new Random().nextInt(15)); i++) {
            randomPanel = this.addPanel();
            panel_upper.add(randomPanel);
        }
        redrawInProgress = false;
    }

    /**
     * Creates a random panel and adds it to the panels arraylist
     *
     * @return Newly created JPanel
     */
    private JPanel addPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new RandomColour());
        Dimension size = new Dimension(10 + new Random().nextInt(310), 10 + new Random().nextInt(170));
        panel.setBounds(getRandomX(size.width), getRandomY(size.height), size.width, size.height);
        panel.addMouseListener(new RelativeCursorPositionListener());
        panel.addMouseMotionListener(new CursorPositionListener());

        this.panels.add(panel);
        return panel;
    }

    /**
     * Gets a valid x coordinate on the JFrame that ensure the object won't disappear offscreen
     *
     * @param width Width of the object
     * @return Random X Coordinate
     */
    private int getRandomX(int width) {
        // Prevent negative bounds
        if (this.getWidth() - width > 0) {
            return new Random().nextInt(this.getWidth() - width);
        }
        return 0;
    }

    /**
     * Gets a valid y coordinate on the JFrame that ensure the object won't disappear offscreen
     *
     * @param height Height of the object
     * @return Random Y Coordinate
     */
    private int getRandomY(int height) {
        // Prevent negative bounds
        if (this.getHeight() - height > 0) {
            return new Random().nextInt(this.getHeight() - height);
        }
        return 0;
    }

    public static void main(String[] args) {
        Panels panels = new Panels();
    }

    /**
     * Creates a random colour
     */
    private class RandomColour extends Color {
        public RandomColour() {
            super(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255));
        }
    }

    /**
     * Listens to Window Resize
     */
    private class ResizeEventListener extends ComponentAdapter {

        /**
         * Invoked when the component's size changes.
         *
         * @param e
         */
        @Override
        public void componentResized(ComponentEvent e) {
            super.componentResized(e);
            if (!redrawInProgress) addPanels();
        }
    }

    /**
     * Listens to mouse motion
     */
    private class CursorPositionListener extends MouseMotionAdapter {

        /**
         * Invoked when the mouse button has been moved on a component
         * (with no buttons no down).
         *
         * @param e
         */
        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
            if (currentEnteredComponent == null) {
                label_cursorPosition.setText("Cursor Position: (" + e.getX() + ", " + e.getY() + ")");
                label_cursorPositionRelative.setText("Relative Cursor Position: (-, -)");
                return;
            }

            int positionX = e.getX() + currentEnteredComponent.getX();
            int positionY = e.getY() + currentEnteredComponent.getY();
            label_cursorPosition.setText("Cursor Position: (" + positionX + ", " + positionY + ")");
            label_cursorPositionRelative.setText("Relative Cursor Position: (" + e.getX() + ", " + e.getY() + ")");


        }
    }

    /**
     * Listens to mouse entering/leaving components
     */
    private class RelativeCursorPositionListener extends MouseAdapter {

        /**
         * {@inheritDoc}
         *
         * @param e
         */
        @Override
        public void mouseEntered(MouseEvent e) {
            super.mouseEntered(e);
            currentEnteredComponent = e.getComponent();
        }

        /**
         * {@inheritDoc}
         *
         * @param e
         */
        @Override
        public void mouseExited(MouseEvent e) {
            super.mouseExited(e);
            currentEnteredComponent = null;
        }
    }
}
