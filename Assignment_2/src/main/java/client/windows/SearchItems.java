package client.windows;

import client.Client;
import client.components.ItemPanel;
import client.components.WindowPanel;
import org.jdatepicker.JDateComponentFactory;
import org.jdatepicker.JDatePicker;
import shared.*;
import shared.components.HintTextFieldUI;
import shared.components.ItemList;
import shared.events.PacketListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.*;
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
    private JProgressBar progress_search;
    private JButton btn_update;
    private JLabel label_sort;
    private JComboBox<String> combo_sort;
    private JLabel label_search;
    private JTextField text_search;
    private JLabel label_keyword;
    private JComboBox<Keyword> combo_keyword;
    private JLabel label_from;
    private JDatePicker date_from;
    private JLabel label_to;
    private JDatePicker date_to;
    private JLabel label_reserve;
    private JSlider slider_reserve;
    private JTextField text_reserve;
    private JLabel label_noBids;
    private JCheckBox check_noBids;
    private JCheckBox check_closedAuctions;
    //endregion
    private JSplitPane panel_split;
    private JPanel panel_results;
    private ItemList<Item> list_results;

    private ActionHandler searchHandler = new ActionHandler();

    public SearchItems() {
        super("Search Items");
        this.initSearchOptions();
        this.initComponents();
        this.initEventListeners();
        this.setMainPanel(this.panel_split);
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

        this.list_results = new ItemList<Item>() {
            @Override
            public Component drawItem(Item item) {
                return new ItemPanel(item);
            }
        };


        JScrollPane scroller = new JScrollPane(this.list_results);
        scroller.setBorder(new EmptyBorder(0, 0, 0, 0));
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.getVerticalScrollBar().setUnitIncrement(8);

        this.panel_results.add(scroller, BorderLayout.CENTER);

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

        this.progress_search = new JProgressBar(JProgressBar.HORIZONTAL);
        this.progress_search.setMaximum(100);
        this.progress_search.setMinimum(0);
        this.progress_search.setIndeterminate(false);
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.panel_search.add(this.progress_search, c);
        row++;

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
        this.combo_sort.setSelectedIndex(0);
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

        this.label_keyword = new JLabel("Keyword", JLabel.LEADING);
        this.label_keyword.setLabelFor(this.combo_keyword);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.panel_search.add(this.label_keyword, c);
        row++;

        this.combo_keyword = new JComboBox<>(new Keyword[]{new Keyword(-1, "All Keywords")});
        this.combo_keyword.setSelectedIndex(0);
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.panel_search.add(this.combo_keyword, c);
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

        this.check_closedAuctions = new JCheckBox("Include closed auctions");
        this.check_closedAuctions.setBackground(Color.WHITE);
        this.check_closedAuctions.setSelected(false);
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.panel_search.add(this.check_closedAuctions, c);
        row++;

        JPanel padder = new JPanel();
        padder.setBackground(Color.WHITE);
        c.gridy = row;
        c.weighty = 0.5;
        this.panel_search.add(padder, c);
    }

    private void initEventListeners() {
        this.addComponentListener(new ComponentHandler());
        Client.addPacketListener(new PacketHandler());
        Client.sendPacket(new Packet<>(PacketType.FETCH_RESERVE_RANGE));
        Client.sendPacket(new Packet<>(PacketType.FETCH_KEYWORDS));
        this.btn_update.addActionListener((e) -> this.search());

        this.combo_sort.addActionListener(this.searchHandler);
        this.combo_keyword.addActionListener(this.searchHandler);
        this.text_search.addFocusListener(this.searchHandler);
        this.date_to.addActionListener(this.searchHandler);
        this.date_from.addActionListener(this.searchHandler);
        this.slider_reserve.addChangeListener(this.searchHandler);
        this.check_noBids.addActionListener(this.searchHandler);
        this.check_closedAuctions.addActionListener(this.searchHandler);
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
            (Keyword)this.combo_keyword.getSelectedItem(),
            new Timestamp(fromDate.getTime().getTime()),
            new Timestamp(toDate.getTime().getTime()),
            new BigDecimal(this.slider_reserve.getValue()).divide(BigDecimal.valueOf(100), BigDecimal.ROUND_HALF_UP),
            this.check_noBids.isSelected(),
            this.check_closedAuctions.isSelected()
        );

        Client.sendPacket(new Packet<>(PacketType.SEARCH, options));
        this.progress_search.setIndeterminate(true);
    }

    private class ComponentHandler extends ComponentAdapter {
        @Override
        public void componentShown(ComponentEvent e) {
            Client.sendPacket(new Packet<>(PacketType.FETCH_KEYWORDS));
            Client.sendPacket(new Packet<>(PacketType.FETCH_RESERVE_RANGE));
        }
    }

    private class ActionHandler extends FocusAdapter implements ActionListener, ChangeListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            SearchItems.this.search();
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            SearchItems.this.search();
        }

        @Override
        public void focusLost(FocusEvent e) {
            SearchItems.this.search();
        }
    }

    private class PacketHandler implements PacketListener {
        @Override
        public void packetReceived(Packet packet) {
            switch (packet.getType()) {
                case RESERVE_RANGE:
                    SwingUtilities.invokeLater(() -> {
                        SearchItems.this.slider_reserve.removeChangeListener(SearchItems.this.searchHandler);
                        SearchItems.this.slider_reserve.setMaximum((int) packet.getPayload());
                        SearchItems.this.slider_reserve.addChangeListener(SearchItems.this.searchHandler);
                    });
                    break;

                case KEYWORDS:
                    SwingUtilities.invokeLater(() -> {
                        SearchItems.this.combo_keyword.removeActionListener(SearchItems.this.searchHandler);
                        SearchItems.this.combo_keyword.removeAllItems();
                        SearchItems.this.combo_keyword.addItem(new Keyword(-1, "All Keywords"));
                        ArrayList<Keyword> keywords = new ArrayList<>(Arrays.asList((Keyword[])packet.getPayload()));
                        keywords.forEach(keyword -> SearchItems.this.combo_keyword.addItem(keyword));
                        SearchItems.this.combo_keyword.addActionListener(SearchItems.this.searchHandler);
                        SearchItems.this.combo_keyword.setSelectedIndex(0);
                    });
                    break;

                case AUCTION_END:
                case AUCTION_START:
                    SearchItems.this.search();
                    break;

                case SEARCH_RESULTS:
                    List<Item> results = Arrays.asList((Item[]) packet.getPayload());
                    SwingUtilities.invokeLater(() -> {
                        SearchItems.this.list_results.removeAllElements();
                        int i = 0;
                        SearchItems.this.progress_search.setIndeterminate(false);
                        SearchItems.this.progress_search.setMaximum(results.size());
                        for(Item item: results){
                            SearchItems.this.list_results.addElement(item);
                            SearchItems.this.progress_search.setValue(i);
                            i++;
                        }
                        SearchItems.this.progress_search.setValue(0);
                    });
                    break;
            }
        }
    }
}
