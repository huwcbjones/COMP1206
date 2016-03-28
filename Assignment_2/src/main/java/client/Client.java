package client;

import client.events.LoginEventListener;
import client.utils.Server;
import client.windows.Login;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.exceptions.ConfigLoadException;
import shared.Comms;
import shared.Packet;
import shared.PacketType;
import shared.User;
import shared.exceptions.ConnectionFailedException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Auction Client
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public final class Client {

    private static Logger log = LogManager.getLogger(Client.class);
    private static final Config config = new Config();
    private static User user;
    private static boolean isConnected = false;

    private static ArrayList<LoginEventListener> loginEventListeners = new ArrayList<>();

    private static Comms plainComms;
    private static Comms secureComms;

    public Client () {

        try {
            Client.config.loadConfig();
        } catch (ConfigLoadException e) {
            log.warn(e.getMessage());
        }
        Login loginWindow = new Login();

        loginWindow.setVisible(true);
    }

    public static Config getConfig () {
        return Client.config;
    }

    public static void shutdown () {
        log.info("Shutting down client...");
        if(Client.isConnected){
            Client.plainComms.shutdown();

            if(Client.secureComms != null && Client.config.getSelectedServer().useSecurePort()){
                Client.secureComms.shutdown();
            }
        }
    }

    public static void connect () throws ConnectionFailedException {
        Server server = Client.config.getSelectedServer();
        log.info("Connecting to {} ({} on {})...", server.getName(), server.getAddress(), server.getPort());
        try {
            Socket plainSocket = new Socket(server.getAddress(), server.getPort());
            ObjectOutputStream outputStream = new ObjectOutputStream(plainSocket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(plainSocket.getInputStream());

            Client.plainComms = new Comms(inputStream, outputStream);
            Client.plainComms.start();
            Client.plainComms.sendMessage(new Packet<>(PacketType.HELLO, "hello"));
            
            Client.isConnected = true;
        } catch (IOException e) {
            log.debug(e);
            throw new ConnectionFailedException("Failed to connect to server.");
        }
    }

    public static void login () {
        try {
            Client.connect();
        } catch (ConnectionFailedException e) {
            log.error(e);
        }
    }

    /**
     * Adds a login listener to the Client
     *
     * @param listener Listener to add
     */
    public static void addLoginListener (LoginEventListener listener) {
        Client.loginEventListeners.add(listener);
    }

    /**
     * Removes a login listener to the Client
     *
     * @param listener Listener to remove
     */
    public static void removeLoginListener (LoginEventListener listener) {
        Client.loginEventListeners.remove(listener);
    }

    private static void fireLoginFailHandler (String message) {
        for (LoginEventListener l : Client.loginEventListeners) {
            l.loginError(message);
        }
    }

    private static void fireLoginSuccessHandler (User user) {
        for (LoginEventListener l : Client.loginEventListeners) {
            l.loginSuccess(user);
        }
    }
}
