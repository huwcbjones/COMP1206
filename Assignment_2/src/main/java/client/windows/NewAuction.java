package client.windows;

import client.Client;
import client.components.WindowPanel;
import client.utils.ImageFilter;
import org.jdatepicker.JDateComponentFactory;
import org.jdatepicker.JDatePicker;
import shared.*;
import shared.components.HintTextFieldUI;
import shared.events.PacketListener;
import shared.exceptions.ValidationFailedException;
import shared.utils.NumberUtils;
import shared.utils.ReplyWaiter;
import shared.utils.TimeUtils;
import shared.utils.ValidationUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

import static shared.utils.ValidationUtils.*;

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
    private DefaultListModel<Keyword> lm_keywords;
    private JList<Keyword> list_keywords;
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
    private File file_image = null;
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

        this.text_description = new JTextArea(4, 20);
        this.text_description.setLineWrap(true);
        this.text_description.setBorder(UIManager.getBorder("TextField.border"));
        this.text_description.setFont(UIManager.getFont("TextField.font"));
        JScrollPane description_scroller = new JScrollPane(this.text_description);
        description_scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        description_scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        c.gridy = row;
        c.weighty = 0.5;
        c.insets = new Insets(0, 0, 6, 0);
        c.fill = GridBagConstraints.BOTH;
        this.add(description_scroller, c);
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
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
        JScrollPane scroller = new JScrollPane(this.list_keywords);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        c.gridy = row;
        c.weighty = 0.5;
        c.insets = new Insets(0, 0, 6, 0);
        c.fill = GridBagConstraints.BOTH;
        this.add(scroller, c);
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
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
        this.time_start.setEditor(new JSpinner.DateEditor(this.time_start, "HH:mm"));
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
        this.time_end.setEditor(new JSpinner.DateEditor(this.time_end, "HH:mm"));
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

        NumberFormatter currencyFormat = new NumberFormatter(NumberFormat.getCurrencyInstance(Locale.UK));
        currencyFormat.setAllowsInvalid(false);

        this.text_reserve = new JFormattedTextField(currencyFormat);
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
        this.text_image.setUI(new HintTextFieldUI("Path to Image", false));
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
    }

    private void initEventHandlers() {
        this.addComponentListener(new ComponentHandler());
        this.btn_image.addActionListener(new ImageBrowser());
        this.btn_add.addActionListener(new AddHandler());
        Client.addPacketListener(new KeywordHandler());

        this.text_title.addFocusListener(new FocusChangeHandler());
        this.text_description.addFocusListener(new FocusChangeHandler());
        this.list_keywords.addFocusListener(new FocusChangeHandler());
        ((JComponent)this.date_start).addFocusListener(new FocusChangeHandler());
        this.time_start.addFocusListener(new FocusChangeHandler());
        ((JComponent)this.date_end).addFocusListener(new FocusChangeHandler());
        this.time_end.addFocusListener(new FocusChangeHandler());
        this.text_reserve.addFocusListener(new FocusChangeHandler());
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

    private void clearForm(){
        this.text_title.setText("");
        ValidationUtils.setValidation(this.text_title, VALIDATION_CLEAR);

        this.text_description.setText("");
        ValidationUtils.setValidation(this.text_description, VALIDATION_CLEAR);

        this.list_keywords.setSelectedIndices(new int[0]);
        ValidationUtils.setValidation(this.list_keywords, VALIDATION_CLEAR);

        ValidationUtils.setValidation((JComponent) this.date_start, VALIDATION_CLEAR);
        ValidationUtils.setValidation(this.time_start, VALIDATION_CLEAR);
        ValidationUtils.setValidation((JComponent) this.date_end, VALIDATION_CLEAR);
        ValidationUtils.setValidation(this.time_end, VALIDATION_CLEAR);

        this.text_reserve.setValue(0);
        ValidationUtils.setValidation(this.text_reserve, VALIDATION_CLEAR);

        this.text_image.setText("");
        ValidationUtils.setValidation(this.text_image, VALIDATION_CLEAR);

        this.file_image = null;
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
        this.setFormState(true);
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
                .addAllKeywords(this.list_keywords.getSelectedValuesList())
                .setDescription(this.text_description.getText())
                .setReservePrice(NumberUtils.currencyToBigDecimal(this.text_reserve.getText(), NumberFormat.getCurrencyInstance(Locale.UK)))
                .setStartTime(this.getStartTime())
                .setEndTime(this.getEndTime());

            if(this.file_image != null) {
                BufferedImage image = null;
                image = ImageIO.read(this.file_image);
                ib.setImage(image);
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException("Failed to parse reserve price.");
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to load image.");
        }
        return ib.getItem();
    }

    private ArrayList<String> validateFields(boolean isFocusChangeValidation){
        ArrayList<String> errors = new ArrayList<>();
        Component firstWrongField = null;

        // Validate title
        try {
            ValidationUtils.validateTitle(this.text_title.getText());
            ValidationUtils.setValidation(this.text_title, VALIDATION_SUCCESS);
        } catch (ValidationFailedException e) {
            if (firstWrongField == null) firstWrongField = this.text_title;
            errors.add(e.getMessage());
            ValidationUtils.setValidation(this.text_title, VALIDATION_FAIL);
        }

        // Validate description
        try {
            ValidationUtils.validateDescription(this.text_description.getText());
            ValidationUtils.setValidation(this.text_description, VALIDATION_SUCCESS);
        } catch (ValidationFailedException e) {
            if (firstWrongField == null) firstWrongField = this.text_description;
            errors.add(e.getMessage());
            ValidationUtils.setValidation(this.text_description, VALIDATION_FAIL);
        }

        // Validate keywords
        if(this.list_keywords.getSelectedValuesList().size() == 0){
            if (firstWrongField == null) firstWrongField = this.list_keywords;
            errors.add("Please select some keywords.");
            ValidationUtils.setValidation(this.list_keywords, VALIDATION_FAIL);
        } else {
            ValidationUtils.setValidation(this.list_keywords, VALIDATION_SUCCESS);
        }

        // Start time is always fine
        ValidationUtils.setValidation((JComponent) this.date_start, VALIDATION_SUCCESS);
        ValidationUtils.setValidation(this.time_start, VALIDATION_SUCCESS);

        // Validate end date
        if(getEndTime().before(getStartTime()) || getEndTime().equals(getStartTime()) ){
            if (firstWrongField == null) firstWrongField = (Component) this.date_end;
            ValidationUtils.setValidation((JComponent) this.date_end, VALIDATION_FAIL);
            ValidationUtils.setValidation(this.time_end, VALIDATION_FAIL);
            if(getEndTime().before(getStartTime())) {
                errors.add("End date is before start date.");
            } else {
                errors.add("End date is the same as the start date.");
            }
        } else {
            ValidationUtils.setValidation((JComponent) this.date_end, VALIDATION_SUCCESS);
            ValidationUtils.setValidation(this.time_end, VALIDATION_SUCCESS);
        }

        // Validate reserve price
        if(new BigDecimal(this.text_reserve.getValue().toString()).compareTo(BigDecimal.ZERO) < 0) {
            ValidationUtils.setValidation(this.text_reserve, VALIDATION_FAIL);
            errors.add("Reserve price cannot be negative.");
        } else {
            ValidationUtils.setValidation(this.text_reserve, VALIDATION_SUCCESS);
        }

        if (firstWrongField != null && !isFocusChangeValidation) {
            firstWrongField.requestFocus();
        }

        return errors;
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
                NewAuction.this.file_image = imageChooser.getSelectedFile();
                NewAuction.this.text_image.setText(NewAuction.this.file_image.getAbsolutePath());
            }
        }
    }

    /**
     * Class to add Item
     */
    private class AddHandler implements ActionListener {
        /**
         * Invoked when an action occurs.
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            ArrayList<String> errors = NewAuction.this.validateFields(false);
            if (errors.size() != 0) {
                ValidationUtils.showValidationMessage(NewAuction.this, errors);
                return;
            }
            NewAuction.this.setFormState(false);
            new Thread(() -> {
                NewAuction.this.setFormState(false);

                ReplyWaiter handler = new ReplyWaiter(Client.getConfig().getTimeout()) {
                    @Override
                    public void packetReceived(Packet packet) {
                        switch (packet.getType()) {
                            case CREATE_ITEM_SUCCESS:
                                itemCreationSuccess((UUID) packet.getPayload());
                                NewAuction.this.clearForm();
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

    /**
     * Class to handle card changing
     */
    private class ComponentHandler extends ComponentAdapter {
        /**
         * Invoked when the component has been made visible.
         *
         * @param e
         */
        @Override
        public void componentShown(ComponentEvent e) {
            NewAuction.this.clearForm();
            NewAuction.this.text_title.requestFocus();
            Client.sendPacket(new Packet<>(PacketType.FETCH_KEYWORDS));
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

    /**
     * Card to handle keywords
     */
    private class KeywordHandler implements PacketListener {
        @Override
        public void packetReceived(Packet packet) {
            switch(packet.getType()){
                case KEYWORDS:
                    Keyword[] keywords = (Keyword[])packet.getPayload();
                    SwingUtilities.invokeLater(() -> {
                        NewAuction.this.lm_keywords.removeAllElements();
                        for(int i = 0; i < keywords.length; i++){
                            NewAuction.this.lm_keywords.add(i, keywords[i]);
                        }
                    });
                    break;
                case NOK:
                    break;
            }
        }
    }

    /**
     * Validate fields on focus change
     */
    private class FocusChangeHandler extends FocusAdapter {
        @Override
        public void focusLost(FocusEvent e) {
            NewAuction.this.validateFields(true);
        }
    }
}
