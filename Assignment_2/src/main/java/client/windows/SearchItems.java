package client.windows;

import client.components.WindowPanel;
import client.utils.SpringUtilities;
import org.jdatepicker.JDateComponentFactory;
import org.jdatepicker.JDatePicker;
import shared.Item;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Search Panel
 *
 * @author Huw Jones
 * @since 12/04/2016
 */
public class SearchItems extends WindowPanel {

    //region Search Pane
    private JPanel panel_search;

    private JLabel label_search;
    private JTextField text_search;

    private JLabel label_keywords;
    private JList<String> list_keywords;
    private DefaultListModel<String> lm_keywords;

    private JLabel label_from;
    private JDatePicker date_from;

    private JLabel label_to;
    private JDatePicker date_to;

    private JLabel label_reserve;
    private JSlider slider_reserve;
    private JTextField text_reserve;

    private JLabel label_noBids;
    private JCheckBox check_noBids;
    //endregion
    private JSplitPane panel_split;
    private JPanel panel_results;
    private JList<Item> list_results;
    private DefaultListModel<Item> lm_items;

    public SearchItems(){
        super("Search Items");
        this.initComponents();
        this.setMainPanel(this.panel_split);
    }

    private void initComponents(){
        this.panel_split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        //region Search
        this.panel_search = new JPanel(new GridBagLayout());
        this.panel_search.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Search"));
        this.initSearchPane();
        this.panel_split.add(this.panel_search);
        //endregion

        //region Results
        this.panel_results = new JPanel(new BorderLayout());
        this.panel_results.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Results"));

        this.lm_items = new DefaultListModel<>();
        this.list_results = new JList<>(this.lm_items);
        this.list_results.setDragEnabled(false);
        this.list_results.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.list_results.setLayoutOrientation(JList.VERTICAL);

        this.panel_results.add(this.list_results, BorderLayout.CENTER);

        this.panel_split.add(this.panel_results);
        this.panel_split.setDividerSize(20);
        this.panel_split.setContinuousLayout(true);
        this.panel_split.setResizeWeight(0);
        this.panel_split.setDividerLocation(0.1d);
        this.panel_split.setOneTouchExpandable(true);
        //endregion
    }

    private void initSearchPane(){
        JPanel panel_search = new JPanel(new SpringLayout());

        int row = 0;
        this.label_search = new JLabel("Search", JLabel.LEADING);
        panel_search.add(this.label_search);

        this.text_search = new JTextField();
        panel_search.add(this.text_search);
        row++;

        this.label_keywords = new JLabel("Keywords", JLabel.LEADING);
        panel_search.add(this.label_keywords);

        this.lm_keywords = new DefaultListModel<>();
        this.list_keywords = new JList<>(this.lm_keywords);
        this.list_keywords.setLayoutOrientation(JList.VERTICAL);
        panel_search.add(this.list_keywords);
        row++;

        this.label_from = new JLabel("From", JLabel.LEADING);
        panel_search.add(this.label_from);

        this.date_from = new JDateComponentFactory().createJDatePicker();
        this.date_from.setShowYearButtons(true);
        panel_search.add((JComponent)this.date_from);
        row++;

        this.label_to = new JLabel("To", JLabel.LEADING);
        panel_search.add(this.label_to);

        this.date_to = new JDateComponentFactory().createJDatePicker();
        this.date_to.setShowYearButtons(true);
        panel_search.add((JComponent)this.date_to);
        row++;

        this.label_reserve = new JLabel("Reserve", JLabel.LEADING);
        panel_search.add(this.label_reserve);

        this.slider_reserve = new JSlider();
        this.slider_reserve.setMajorTickSpacing(1000);
        this.slider_reserve.setMinorTickSpacing(100);
        this.slider_reserve.setSnapToTicks(false);
        this.slider_reserve.setMinimum(0);
        this.slider_reserve.setMaximum(100 * 100);
        this.slider_reserve.setPaintTicks(true);
        this.slider_reserve.addChangeListener(e -> {
            int value = this.slider_reserve.getValue();
            BigDecimal reserve = new BigDecimal(value);
            reserve = reserve.divide(BigDecimal.valueOf(100), BigDecimal.ROUND_HALF_UP);
            this.text_reserve.setText(
                NumberFormat.getCurrencyInstance(Locale.UK).format(reserve) +
                " to " +
                NumberFormat.getCurrencyInstance(Locale.UK).format(this.slider_reserve.getMaximum() / 100f)
            );
        });

        this.text_reserve = new JTextField();
        this.text_reserve.setHorizontalAlignment(JTextField.LEADING);
        this.text_reserve.setEditable(false);

        JPanel reservePanel = new JPanel(new BorderLayout());
        reservePanel.add(this.slider_reserve, BorderLayout.CENTER);
        reservePanel.add(this.text_reserve, BorderLayout.PAGE_END);

        this.slider_reserve.setValue(0);
        panel_search.add(reservePanel);
        row++;

        this.label_noBids = new JLabel();
        panel_search.add(this.label_noBids);

        this.check_noBids = new JCheckBox("Show items with no bids only");
        this.check_noBids.setSelected(false);
        panel_search.add(this.check_noBids);
        row++;

        SpringUtilities.makeCompactGrid(panel_search, row, 2, 3, 3, 3, 3);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        this.panel_search.add(panel_search, c);

        c = new GridBagConstraints();
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        this.panel_search.add(new JPanel(), c);
    }

    /**
     * Gets the default button for the panel
     *
     * @return Default button
     */
    @Override
    public JButton getDefaultButton() {
        return null;
    }
}
