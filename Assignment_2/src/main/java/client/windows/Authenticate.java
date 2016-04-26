package client.windows;

import client.Client;
import client.components.WindowPanel;
import client.events.LoginAdapter;
import client.events.RegisterListener;
import shared.User;
import shared.components.ImagePanel;
import shared.components.RolloverImagePanel;
import shared.utils.WindowTemplate;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

/**
 * JFrame for Logging in/Registering
 *
 * @author Huw Jones
 * @since 25/04/2016
 */
public class Authenticate extends WindowTemplate {

    private final static String PANEL_LOGIN = "Login";
    private final static String PANEL_REGISTER = "Register";
    private final static String PANEL_VIEWSERVERS = "Servers";
    private final static String PANEL_SERVER = "ServerPanel";
    private static String username;
    private JPanel panel_GUI;
    private JPanel panel_header;
    private JPanel panel_footer;
    private RolloverImagePanel btn_config;
    private ImagePanel panel_banner;
    private JPanel panel_cards;
    private HashMap<String, WindowPanel> panels = new HashMap<>();
    private Login panel_login;
    private Register panel_register;
    private ViewServers panel_servers;
    private ServerPanel panel_serverpanel;
    private LoginHandler loginListener;
    private RegisterHandler registerListener;

    public Authenticate() {
        super("Login");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(600, 550));
        this.setMaximumSize(new Dimension(600, 550));
        this.setLocationRelativeTo(null);
        this.setResizable(false);

        this.panels.put(PANEL_LOGIN, this.panel_login);
        this.panels.put(PANEL_REGISTER, this.panel_register);
        this.panels.put(PANEL_VIEWSERVERS, this.panel_servers);
        this.panels.put(PANEL_SERVER, this.panel_serverpanel);
        this.changePanel(PANEL_LOGIN);
    }

    /**
     * Initialises the GUI components
     */
    @Override
    protected void initComponents() {
        this.initPanels();
        initEventListeners();
    }

    private void initPanels() {
        this.panel_GUI = new JPanel(new BorderLayout());
        this.panel_GUI.setBackground(Color.WHITE);

        this.panel_header = new JPanel(new BorderLayout());
        this.panel_banner = new ImagePanel();
        try {
            BufferedImage banner = ImageIO.read(Login.class.getResource("/img/biddr_banner_login.png"));
            this.panel_banner.setImage(banner, true);
            this.panel_banner.setHorizontalAlignment(SwingConstants.CENTER);
        } catch (IOException e) {
            log.error("Could not load banner image.");
        }
        this.panel_header.add(this.panel_banner, BorderLayout.CENTER);

        this.panel_GUI.add(this.panel_header, BorderLayout.PAGE_START);

        this.panel_footer = new JPanel(new BorderLayout());
        this.panel_footer.setBackground(Color.WHITE);
        this.panel_footer.setBorder(new EmptyBorder(8, 8, 8, 8));
        try {
            BufferedImage gear_default = ImageIO.read(Login.class.getResource("/img/gear_default.png"));
            BufferedImage gear_hover = ImageIO.read(Login.class.getResource("/img/gear_hover.png"));
            this.btn_config = new RolloverImagePanel(gear_default, gear_hover);
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.BELOW_BASELINE_TRAILING;
            this.panel_footer.add(btn_config, BorderLayout.LINE_END);
        } catch (IOException e) {
            log.error("Could not load config images.");
        }

        this.panel_GUI.add(this.panel_footer, BorderLayout.PAGE_END);

        this.panel_cards = new JPanel(new CardLayout());
        this.panel_cards.setBackground(Color.WHITE);
        this.panel_cards.setBorder(new EmptyBorder(32, 96, 0, 96));

        this.panel_login = new Login();
        this.panel_cards.add(panel_login, PANEL_LOGIN);

        this.panel_register = new Register();
        this.panel_cards.add(panel_register, PANEL_REGISTER);

        this.panel_servers = new ViewServers();
        this.panel_cards.add(panel_servers, PANEL_VIEWSERVERS);

        this.panel_serverpanel = new ServerPanel();
        this.panel_cards.add(panel_serverpanel, PANEL_SERVER);

        this.panel_GUI.add(this.panel_cards, BorderLayout.CENTER);

        this.setContentPane(this.panel_GUI);
    }

    private void initEventListeners() {
        this.panel_login.btn_register.addActionListener(e -> this.changePanel(PANEL_REGISTER));
        this.panel_register.btn_back.addActionListener(e -> this.changePanel(PANEL_LOGIN));
        this.panel_serverpanel.btn_do.addActionListener(e -> this.changePanel(PANEL_VIEWSERVERS));
        this.panel_servers.btn_back.addActionListener(e -> this.changePanel(PANEL_LOGIN));
        this.panel_servers.btn_new.addActionListener(e ->{
            this.panel_serverpanel.setServer(null);
            this.changePanel(PANEL_SERVER);
        });
        this.panel_servers.btn_edit.addActionListener(e -> {
            this.panel_serverpanel.setServer(this.panel_servers.getSelectedServer());
            this.changePanel(PANEL_SERVER);
        });
        this.panel_serverpanel.btn_back.addActionListener(e -> this.changePanel(PANEL_VIEWSERVERS));

        this.btn_config.addActionListener(e -> this.changePanel(PANEL_VIEWSERVERS));
        this.loginListener = new LoginHandler();
        Client.addLoginListener(this.loginListener);

        this.registerListener = new RegisterHandler();
        Client.addRegisterListener(this.registerListener);
    }

    /**
     * Sets the displayed panel
     *
     * @param panelID The panel that should be changed to
     */
    private void changePanel(String panelID) {
        CardLayout layout = (CardLayout) this.panel_cards.getLayout();
        layout.show(this.panel_cards, panelID);
        WindowPanel panel = this.panels.get(panelID);
        this.setTitle(panel.getTitle());
        // Set default button on enter press
        this.getRootPane().setDefaultButton(panel.getDefaultButton());
    }

    public static String getUsername() {
        return Authenticate.username;
    }

    public static void setUsername(String username) {
        Authenticate.username = username;
    }

    public static void main(String[] args) {
        Authenticate main = new Authenticate();
        main.setVisible(true);
    }

    /**
     * Handles LoginEvents
     */
    private class LoginHandler extends LoginAdapter {
        /**
         * Fired when a successful login occurs
         *
         * @param user User object for current user
         */
        @Override
        public void loginSuccess(User user) {
            Client.removeLoginListener(Authenticate.this.loginListener);
            Authenticate.this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            Authenticate.this.dispatchEvent(new WindowEvent(Authenticate.this, WindowEvent.WINDOW_CLOSING));
        }
    }

    /**
     * Handles RegisterEvents
     */
    private class RegisterHandler implements RegisterListener {
        /**
         * Fired when a user successfully registers
         *
         * @param user Registered user
         */
        @Override
        public void registerSuccess(User user) {
            Authenticate.this.changePanel(PANEL_LOGIN);
        }

        /**
         * Fired when a user fails to register
         *
         * @param reason Reason why registration failed
         */
        @Override
        public void registerFail(String reason) {
        }
    }
}
