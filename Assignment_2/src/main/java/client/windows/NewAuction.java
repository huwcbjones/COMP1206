package client.windows;

import client.Client;
import client.components.WindowPanel;
import client.utils.ImageFilter;
import org.jdatepicker.JDateComponentFactory;
import org.jdatepicker.JDatePicker;
import shared.Item;
import shared.ItemBuilder;
import shared.Packet;
import shared.PacketType;
import shared.components.HintTextAreaUI;
import shared.components.HintTextFieldUI;
import shared.utils.NumberUtils;
import shared.utils.ReplyWaiter;
import shared.utils.TimeUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.sql.Timestamp;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * New Item Panel
 *
 * @author Huw Jones
 * @since 20/04/2016
 */
public class NewAuction extends WindowPanel {

    public JButton btn_add;
    private JLabel label_title;
    private JTextField text_title;
    private JLabel label_description;
    private JTextArea text_description;
    private JLabel label_keywords;
    private DefaultListModel<String> lm_keywords;
    private JList<String> list_keywords;
    private JLabel label_start;
    private JDatePicker date_start;
    private JSpinner time_start;
    private JLabel label_end;
    private JDatePicker date_end;
    private JSpinner time_end;
    private JLabel label_reserve;
    private JFormattedTextField text_reserve;
    private JLabel label_image;
    private JTextField text_image;
    private JButton btn_image;

    public NewAuction() {
        super("New Auction");
        this.initComponents();
        this.initEventHandlers();
    }

    private void initComponents() {
        this.setLayout(new GridBagLayout());
        this.setBorder(new EmptyBorder(0, 128, 0, 128));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        int row = 0;

        this.label_title = new JLabel("Title", JLabel.LEADING);
        this.label_title.setLabelFor(this.text_title);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.add(this.label_title, c);
        row++;

        this.text_title = new JTextField();
        this.text_title.setUI(new HintTextFieldUI("Item Title", true));
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.add(this.text_title, c);
        row++;

        this.label_description = new JLabel("Description", JLabel.LEADING);
        this.label_description.setLabelFor(this.text_description);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.add(this.label_description, c);
        row++;

        this.text_description = new JTextArea(4, 2);
        this.text_description.setLineWrap(true);
        this.text_description.setUI(new HintTextAreaUI("Item Title", true));
        this.text_description.setBorder(UIManager.getBorder("TextField.border"));
        this.text_description.setFont(UIManager.getFont("TextField.font"));
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.add(this.text_description, c);
        row++;

        this.label_keywords = new JLabel("Keywords", JLabel.LEADING);
        this.label_keywords.setLabelFor(this.list_keywords);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.add(this.label_keywords, c);
        row++;

        this.lm_keywords = new DefaultListModel<>();
        this.list_keywords = new JList<>(this.lm_keywords);
        this.list_keywords.setLayoutOrientation(JList.VERTICAL);
        this.list_keywords.setBorder(UIManager.getBorder("TextField.border"));
        c.gridy = row;
        c.weighty = 0.5;
        c.insets = new Insets(0, 0, 6, 0);
        this.add(this.list_keywords, c);
        c.weighty = 0;
        row++;

        this.label_start = new JLabel("Auction Start Date", JLabel.LEADING);
        this.label_start.setLabelFor(this.label_start);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.add(this.label_start, c);
        row++;

        JPanel start_dateTimePanel = new JPanel(new GridLayout(1, 2));
        this.date_start = new JDateComponentFactory().createJDatePicker();
        this.date_start.setTextEditable(false);
        this.date_start.setShowYearButtons(true);
        start_dateTimePanel.add((JComponent) this.date_start);

        this.time_start = new JSpinner(new SpinnerDateModel());
        this.time_start.setEditor(new JSpinner.DateEditor(this.time_start, "hh:mm"));
        start_dateTimePanel.add(this.time_start);

        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.add(start_dateTimePanel, c);
        row++;

        this.label_end = new JLabel("Auction End Date", JLabel.LEADING);
        this.label_end.setLabelFor(this.label_end);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.add(this.label_end, c);
        row++;

        JPanel end_dateTimePanel = new JPanel(new GridLayout(1, 2));
        this.date_end = new JDateComponentFactory().createJDatePicker();
        this.date_end.setTextEditable(false);
        this.date_end.setShowYearButtons(true);
        end_dateTimePanel.add((JComponent) this.date_end);

        this.time_end = new JSpinner(new SpinnerDateModel());
        this.time_end.setEditor(new JSpinner.DateEditor(this.time_end, "hh:mm"));
        end_dateTimePanel.add(this.time_end);

        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.add(end_dateTimePanel, c);
        row++;

        this.label_reserve = new JLabel("Reserve Price", JLabel.LEADING);
        this.label_reserve.setLabelFor(this.text_reserve);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.add(this.label_reserve, c);
        row++;

        Format currency = NumberFormat.getCurrencyInstance(Locale.UK);
        this.text_reserve = new JFormattedTextField(currency);
        this.text_reserve.setValue(0.0);
        this.text_reserve.setUI(new HintTextFieldUI("Reserve Price", true));
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.add(this.text_reserve, c);
        row++;

        this.label_image = new JLabel("Item Image", JLabel.LEADING);
        this.label_image.setLabelFor(this.label_image);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.add(this.label_image, c);
        row++;

        JPanel image_panel = new JPanel(new BorderLayout());
        this.text_image = new JTextField();
        this.text_image.setUI(new HintTextFieldUI("Path to Image", true));
        this.text_image.setEditable(false);
        image_panel.add(this.text_image, BorderLayout.CENTER);

        this.btn_image = new JButton("Select Image");
        this.btn_image.setMnemonic('i');
        image_panel.add(this.btn_image, BorderLayout.LINE_END);

        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.add(image_panel, c);
        row++;

        this.btn_add = new JButton("Add New Item");
        this.btn_add.setMnemonic('r');
        c.insets = new Insets(6, 0, 6, 0);
        c.gridy = row;
        this.add(this.btn_add, c);
        row++;

        JPanel padder = new JPanel();
        padder.setBackground(Color.WHITE);
        c.gridy = row;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        this.add(padder, c);
    }

    private void initEventHandlers() {
        this.addComponentListener(new ComponentHandler());
        this.btn_image.addActionListener(new ImageBrowser());
        this.btn_add.addActionListener(new AddHandler());
    }

    /**
     * Gets the default button for the panel
     *
     * @return Default button
     */
    @Override
    public JButton getDefaultButton() {
        return this.btn_add;
    }

    private void setFormState(boolean enabled) {
        this.text_title.setEnabled(enabled);
        this.text_description.setEnabled(enabled);
        this.list_keywords.setEnabled(enabled);
        ((JComponent) this.date_start).setEnabled(enabled);
        this.time_start.setEnabled(enabled);
        ((JComponent) this.date_end).setEnabled(enabled);
        this.time_end.setEnabled(enabled);
        this.text_reserve.setEnabled(enabled);
        this.btn_image.setEnabled(enabled);
        this.btn_add.setEnabled(enabled);
    }

    private void itemCreationSuccess(UUID itemID) {
        this.setFormState(true);
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
            "Item successfully added!",
            "Item Added!",
            JOptionPane.INFORMATION_MESSAGE
        ));
    }

    private void itemCreationFail(String reason) {
        this.setFormState(false);
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
            "Item was not added for auction.\nReason: " + reason,
            "Failed to add Item!",
            JOptionPane.ERROR_MESSAGE
        ));
    }

    private Timestamp getStartTime() {
        Calendar date = (Calendar) this.date_start.getModel().getValue();
        Calendar timeDate = TimeUtils.DateToCalendar((Date) this.time_start.getValue());

        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        for (int type : new int[]{Calendar.HOUR_OF_DAY, Calendar.MINUTE}) {
            date.add(type, timeDate.get(type));
        }
        return new Timestamp(date.getTime().getTime());
    }

    private Timestamp getEndTime() {
        Calendar date = (Calendar) this.date_end.getModel().getValue();
        Calendar timeDate = TimeUtils.DateToCalendar((Date) this.time_end.getValue());

        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        for (int type : new int[]{Calendar.HOUR_OF_DAY, Calendar.MINUTE}) {
            date.add(type, timeDate.get(type));
        }
        return new Timestamp(date.getTime().getTime());
    }

    private Item getItem() throws IllegalArgumentException {
        ItemBuilder ib = Item.createBuilder();
        try {
            ib
                .setID(new UUID(0L, 0L))
                .setUserID(Client.getUser().getUniqueID())
                .setTitle(this.text_title.getText())
                .setDescription(this.text_description.getText())
                .setReservePrice(NumberUtils.currencyToBigDecimal(this.text_reserve.getText(), NumberFormat.getCurrencyInstance(Locale.UK)))
                .setStartTime(this.getStartTime())
                .setEndTime(this.getEndTime());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Failed to parse reserve price.");
        }
        return ib.getItem();
    }

    /**
     * Class to handle image selection
     */
    private class ImageBrowser implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser imageChooser = new JFileChooser();
            imageChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            imageChooser.setName("Select Item Image");
            imageChooser.setMultiSelectionEnabled(false);
            imageChooser.setFileFilter(new ImageFilter());
            if (imageChooser.showOpenDialog(NewAuction.this) == JFileChooser.APPROVE_OPTION) {
                File image = imageChooser.getSelectedFile();
                NewAuction.this.text_image.setText(image.getAbsolutePath());
            }
        }
    }

    private class AddHandler implements ActionListener {
        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(() -> {
                NewAuction.this.setFormState(false);

                ReplyWaiter handler = new ReplyWaiter(Client.getConfig().getTimeout()) {
                    @Override
                    public void packetReceived(Packet packet) {
                        switch (packet.getType()) {
                            case CREATE_ITEM_SUCCESS:
                                itemCreationSuccess((UUID) packet.getPayload());
                                break;
                            case CREATE_ITEM_FAIL:
                                itemCreationFail((String) packet.getPayload());
                                break;
                            case NOK:
                                itemCreationFail("Server failed to process request.");
                                break;
                            default:
                                return;
                        }
                        waiter.replyReceived();
                    }
                };
                Client.addPacketListener(handler);
                Client.sendPacket(new Packet<>(PacketType.CREATE_ITEM, NewAuction.this.getItem()));
                handler.getWaiter().waitForReply();
                if (handler.getWaiter().isReplyTimedOut()) {
                    itemCreationFail("Server timed out.");
                }
            }, "NewAuction").start();
        }
    }

    private class ComponentHandler extends ComponentAdapter {
        /**
         * Invoked when the component has been made visible.
         *
         * @param e
         */
        @Override
        public void componentShown(ComponentEvent e) {
            NewAuction.this.text_title.requestFocus();
        }

        /**
         * Invoked when the component has been made invisible.
         *
         * @param e
         */
        @Override
        public void componentHidden(ComponentEvent e) {
            super.componentHidden(e);
        }
    }
}
