package client.windows;

import client.components.WindowPanel;
import client.utils.Server;
import shared.components.HintTextFieldUI;
import shared.components.JLinkLabel;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for Adding/Editing Servers
 *
 * @author Huw Jones
 * @since 25/04/2016
 */
public class ServerPanel extends WindowPanel {

    private JLabel label_name;
    private JTextField text_name;

    private JLabel label_address;
    private JTextField text_address;

    private JLabel label_port;
    private JTextField text_port;

    public JButton btn_do;
    public JLinkLabel btn_back;

    public ServerPanel(){
        super("New Server");
        this.initComponents();
    }

    public void setServer(Server server){
        if(server != null){
            this.setTitle("Edit Server");
            this.text_name.setText(server.getName());
            this.text_address.setText(server.getAddress());
            this.text_port.setText(server.getPort() + "");
            this.btn_do.setText("Save Server");
            this.btn_do.setMnemonic('s');
        } else {
            this.setTitle("New Server");
            this.text_name.setText("");
            this.text_address.setText("");
            this.text_port.setText("473");
            this.btn_do.setText("Add Server");
            this.btn_do.setMnemonic('a');
        }
    }

    // TODO: 26/04/2016 Make this work and save servers

    private void initComponents() {
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        int row = 0;

        //region Fields
        this.label_name = new JLabel("Name", JLabel.LEADING);
        this.label_name.setLabelFor(this.label_name);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.add(this.label_name, c);
        row++;

        this.text_name = new JTextField();
        this.text_name.setUI(new HintTextFieldUI("Name", true));
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.add(this.text_name, c);
        row++;

        this.label_address = new JLabel("Server Address", JLabel.LEADING);
        this.label_address.setLabelFor(this.label_address);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.add(this.label_address, c);
        row++;

        this.text_address = new JTextField();
        this.text_address.setUI(new HintTextFieldUI("Address", true));
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.add(this.text_address, c);
        row++;

        this.label_port = new JLabel("Port", JLabel.LEADING);
        this.label_port.setLabelFor(this.label_port);
        c.insets = new Insets(3, 0, 3, 0);
        c.gridy = row;
        this.add(this.label_port, c);
        row++;

        this.text_port = new JTextField();
        this.text_port.setUI(new HintTextFieldUI("Port", true));
        c.gridy = row;
        c.insets = new Insets(0, 0, 6, 0);
        this.add(this.text_port, c);
        row++;
        //endregion

        //region Buttons

        this.btn_do = new JButton("Add Server");
        this.btn_do.setMnemonic('a');
        c.insets = new Insets(6, 0, 6, 0);
        c.gridy = row;
        this.add(this.btn_do, c);
        row++;

        this.btn_back = new JLinkLabel("Back to Servers", JLabel.LEADING);
        this.btn_back.setFont(this.btn_back.getFont().deriveFont(this.btn_back.getFont().getStyle() | Font.BOLD));
        c.insets = new Insets(6, 0, 6, 0);
        c.gridy = row;
        c.fill = GridBagConstraints.NONE;
        this.add(this.btn_back, c);
        row++;

        JPanel padder = new JPanel();
        padder.setBackground(Color.WHITE);
        c.gridy = row;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        this.add(padder, c);
        //endregion
    }

    /**
     * Gets the default button for the panel
     *
     * @return Default button
     */
    @Override
    public JButton getDefaultButton() {
        return this.btn_do;
    }
}
