package shared.components;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTextAreaUI;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * TextFieldUI, with input hint (like HTML text inputs)
 * Taken from: http://stackoverflow.com/a/4962829/5909019
 *
 * @author Huw Jones
 * @since 01/03/2016
 */
public class HintTextAreaUI extends BasicTextAreaUI implements FocusListener {

    private String hint;
    private boolean hideOnFocus;
    private Color color;

    public HintTextAreaUI(){
        this("");
    }
    public HintTextAreaUI(String hint) {
        this(hint, false);
    }

    public HintTextAreaUI(String hint, boolean hideOnFocus) {
        this(hint, hideOnFocus, Color.lightGray);
    }

    public HintTextAreaUI(String hint, boolean hideOnFocus, Color color) {
        this.hint = hint;
        this.hideOnFocus = hideOnFocus;
        this.color = color;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
        this.repaint();
    }

    private void repaint() {
        if (this.getComponent() != null) {
            this.getComponent().repaint();
        }
    }

    public boolean isHideOnFocus() {
        return this.hideOnFocus;
    }

    public void setHideOnFocus(boolean hideOnFocus) {
        this.hideOnFocus = hideOnFocus;
        this.repaint();
    }

    public String getHint() {
        return this.hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
        this.repaint();
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (this.hideOnFocus) this.repaint();

    }

    @Override
    public void focusLost(FocusEvent e) {
        if (this.hideOnFocus) this.repaint();
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        this.getComponent().addFocusListener(this);
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        this.getComponent().removeFocusListener(this);
    }

    @Override
    protected void paintSafely(Graphics g) {
        super.paintSafely(g);
        JTextArea comp = (JTextArea)this.getComponent();
        if (this.hint != null && comp.getText().length() == 0 && !(this.hideOnFocus && comp.hasFocus())) {
            if (this.color != null) {
                g.setColor(this.color);
            } else {
                g.setColor(comp.getForeground().brighter().brighter().brighter());
            }
            int position = comp.getHeight() - comp.getFont().getSize();
            if(comp.getRows() != 0) {
                int padding = ((comp.getHeight() / comp.getRows()) - comp.getFont().getSize());
                position = comp.getHeight() - (comp.getRows() * comp.getFont().getSize()) + padding;
            }
            g.drawString(this.hint, 3, position);
        }
    }
}
