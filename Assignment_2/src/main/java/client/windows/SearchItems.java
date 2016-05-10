package client.windows;

import client.Client;
import client.components.WindowPanel;
import javafx.util.Pair;
import org.jdatepicker.JDateComponentFactory;
import org.jdatepicker.JDatePicker;
import shared.Item;
import shared.Packet;
import shared.PacketType;
import shared.SearchOptions;
import shared.components.HintTextFieldUI;
import shared.events.PacketListener;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

import static shared.SearchOptions.Direction.ASC;
import static shared.SearchOptions.Direction.DESC;
import static shared.SearchOptions.Sort.*;

/**
 * Search Panel
 *
 * @author Huw Jones
 * @since 12/04/2016
 */
public class SearchItems extends WindowPanel {

    private final TreeMap<String, Pair<SearchOptions.Sort, SearchOptions.Direction>> sort_options = new TreeMap<>();
    //region Search Pane
    private JPanel panel_search;
    private JButton btn_update;
    private JLabel label_sort;
    private JComboBox<String> combo_sort;
    private JLabel label_search;
    private JTextField text_search;
    private JLabel label_from;
    private JDatePicker date_from;
    private JLabel label_to;
    private JDatePicker date_to;
    private JLabel label_reserve;
    private JSlider slider_reserve;
    private JTextField text_reserve;
    private JLabel label_noBids;
    //endregion
    private JCheckBox check_noBids;
    private JSplitPane panel_split;
    private JPanel panel_results;
    private JList<Item> list_results;
    private DefaultListModel<Item> lm_items;

    public SearchItems() {
        super("Search Items");
        this.initSearchOptions();
        this.initComponents();
        this.setMainPanel(this.panel_split);
        this.addComponentListener(new ComponentHandler());
        Client.addPacketListener(new PacketHandler());
        Client.sendPacket(new Packet<>(PacketType.FETCH_RESERVE_RANGE));
        this.btn_update.addActionListener((e) -> this.search());
    }

    private void initComponents() {
        this.panel_split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        //region Search
        this.panel_search = new JPanel(new GridBagLayout());
        this.panel_search.setBackground(Color.WHITE);
        this.panel_search.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Search"));
        this.initSearchPane();
        this.panel_split.add(this.panel_search);
        this.panel_split.setBackground(Color.WHITE);
        //endregion

        //region Results
        this.panel_results = new JPanel(new BorderLayout());
        this.panel_results.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Results"));
        this.panel_results.setBackground(Color.WHITE);

        this.lm_items = new DefaultListModel<>();
        this.list_results = new JList<>(this.lm_items);
        this.list_results.setDragEnabled(false);
        this.list_results.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.list_results.setLayoutOrientation(JList.VERTICAL);

        this.panel_results.add(this.list_results, BorderLayout.CENTER);

        this.panel_split.add(this.panel_results);
        this.panel_split.setDividerSize(10);
        this.panel_split.setContinuousLayout(true);
        this.panel_split.setResizeWeight(0);
        this.panel_split.setDividerLocation(0.1d);
        this.panel_split.setOneTouchExpandable(false);
        BasicSplitPaneUI ui = (BasicSplitPaneUI) this.panel_split.getUI();
        BasicSplitPaneDivider divider = ui.getDivider();
        divider.setBackground(Color.WHITE);
        //endregion
    }

    private void initSearchPane() {
        this.panel_search.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        int row = 0;

        this.btn_update = new JButton("Update");
        this.btn_update.setMnemonic('u');
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.panel_search.add(this.btn_update, c);
        row++;

        this.label_sort = new JLabel("Sort Order", JLabel.LEADING);
        this.label_sort.setLabelFor(this.combo_sort);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.panel_search.add(this.label_sort, c);
        row++;

        this.combo_sort = new JComboBox<>(this.sort_options.keySet().toArray(new String[0]));
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.panel_search.add(this.combo_sort, c);
        row++;

        this.label_search = new JLabel("Search", JLabel.LEADING);
        this.label_search.setLabelFor(this.text_search);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.panel_search.add(this.label_search, c);
        row++;

        this.text_search = new JTextField();
        this.text_search.setUI(new HintTextFieldUI("Search Term", true));
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.panel_search.add(this.text_search, c);
        row++;

        this.label_from = new JLabel("From", JLabel.LEADING);
        this.label_from.setLabelFor((Component) this.date_from);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.panel_search.add(this.label_from, c);
        row++;

        this.date_from = new JDateComponentFactory().createJDatePicker();
        this.date_from.setShowYearButtons(true);
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.panel_search.add((JComponent) this.date_from, c);
        row++;

        this.label_to = new JLabel("To", JLabel.LEADING);
        this.label_to.setLabelFor((Component) this.date_to);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.panel_search.add(this.label_to, c);
        row++;

        this.date_to = new JDateComponentFactory().createJDatePicker();
        this.date_to.setShowYearButtons(true);
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.panel_search.add((JComponent) this.date_to, c);
        row++;

        this.label_reserve = new JLabel("Reserve", JLabel.LEADING);
        this.label_reserve.setLabelFor(this.slider_reserve);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.panel_search.add(this.label_reserve, c);
        row++;

        this.slider_reserve = new JSlider();
        this.slider_reserve.setBackground(Color.WHITE);
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
        reservePanel.setBackground(Color.WHITE);
        reservePanel.add(this.slider_reserve, BorderLayout.CENTER);
        reservePanel.add(this.text_reserve, BorderLayout.PAGE_END);

        this.slider_reserve.setValue(0);
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.panel_search.add(reservePanel, c);
        row++;

        this.label_noBids = new JLabel();
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.panel_search.add(this.label_noBids, c);
        row++;

        this.check_noBids = new JCheckBox("Show items with no bids only");
        this.check_noBids.setBackground(Color.WHITE);
        this.check_noBids.setSelected(false);
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.panel_search.add(this.check_noBids, c);
        row++;

        JPanel padder = new JPanel();
        padder.setBackground(Color.WHITE);
        c.gridy = row;
        c.weighty = 0.5;
        this.panel_search.add(padder, c);
    }

    private void initSearchOptions() {
        this.sort_options.put("Bid Price: High to Low", new Pair<>(BID, DESC));
        this.sort_options.put("Bid Price: Low to High", new Pair<>(BID, ASC));

        this.sort_options.put("Time Left: High to Low", new Pair<>(TIME, DESC));
        this.sort_options.put("Time Left: Low to High", new Pair<>(TIME, ASC));

        this.sort_options.put("Reserve Price: High to Low", new Pair<>(RESERVE, DESC));
        this.sort_options.put("Reserve Price: Low to High", new Pair<>(RESERVE, ASC));

        this.sort_options.put("Number of Bids: High to Low", new Pair<>(NUM_BIDS, DESC));
        this.sort_options.put("Number of Bids: Low to High", new Pair<>(NUM_BIDS, ASC));
    }

    /**
     * Gets the default button for the panel
     *
     * @return Default button
     */
    @Override
    public JButton getDefaultButton() {
        return this.btn_update;
    }

    private void search() {
        Calendar fromDate = (Calendar) this.date_from.getModel().getValue();
        fromDate.set(Calendar.HOUR_OF_DAY, 0);
        fromDate.set(Calendar.MINUTE, 0);
        fromDate.set(Calendar.SECOND, 0);
        fromDate.set(Calendar.MILLISECOND, 0);

        Calendar toDate = (Calendar) this.date_to.getModel().getValue();
        toDate.set(Calendar.HOUR_OF_DAY, 23);
        toDate.set(Calendar.MINUTE, 59);
        toDate.set(Calendar.SECOND, 59);
        toDate.set(Calendar.MILLISECOND, 99);

        String sortString = (String) this.combo_sort.getSelectedItem();
        Pair<SearchOptions.Sort, SearchOptions.Direction> sort = this.sort_options.get(sortString);

        SearchOptions options = new SearchOptions(
            sort.getKey(),
            sort.getValue(),
            this.text_search.getText(),
            new Timestamp(fromDate.getTime().getTime()),
            new Timestamp(toDate.getTime().getTime()),
            new BigDecimal(this.slider_reserve.getValue()).divide(BigDecimal.valueOf(100), BigDecimal.ROUND_HALF_UP),
            this.check_noBids.isSelected()
        );

        Client.sendPacket(new Packet<>(PacketType.SEARCH, options));
    }

    private class ComponentHandler extends ComponentAdapter {
        @Override
        public void componentShown(ComponentEvent e) {
            Client.sendPacket(new Packet<>(PacketType.FETCH_RESERVE_RANGE));
        }
    }

    private class PacketHandler implements PacketListener {
        @Override
        public void packetReceived(Packet packet) {
            switch (packet.getType()) {
                     case RESERVE_RANGE:
                    SwingUtilities.invokeLater(() -> SearchItems.this.slider_reserve.setMaximum((int) packet.getPayload()));
                    break;

                case AUCTION_END:
                case AUCTION_START:
                    SearchItems.this.search();
                    break;

                case SEARCH_RESULTS:
                    List<Item> results = Arrays.asList((Item[]) packet.getPayload());
                    break;
            }
        }
    }
}
