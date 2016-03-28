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
import shared.events.PacketListener;
import shared.exceptions.ConnectionFailedException;

import javax.swing.*;
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
    private static boolean isLoggingIn = false;
    private static boolean cancelLogin = false;

    private static ArrayList<LoginEventListener> loginEventListeners = new ArrayList<>();

    private static Comms comms;

    private static final Object waitForReply = new Object();

    public Client () {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "ShutdownThread"));
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

    private void shutdown () {
        log.info("Shutting down client...");
        if (Client.isConnected) {
            Client.comms.shutdown();
        }
    }

    public static void connect () throws ConnectionFailedException {
        Server server = Client.config.getSelectedServer();
        log.info("Connecting to {} ({} on {})...", server.getName(), server.getAddress(), server.getPort());
        try {
            Socket plainSocket = new Socket(server.getAddress(), server.getPort());
            ObjectOutputStream outputStream = new ObjectOutputStream(plainSocket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(plainSocket.getInputStream());

            Client.comms = new Comms(plainSocket, inputStream, outputStream);
            Client.comms.start();
            helloReplyHandler helloHandler = new helloReplyHandler();
            Client.comms.addMessageListener(helloHandler);

            log.info("Saying HELLO to the server...");
            Client.comms.sendMessage(new Packet<>(PacketType.HELLO, "hello"));
            synchronized (Client.waitForReply) {

                try {
                    log.info("Waiting for server response...");
                    Client.waitForReply.wait(5 * 1000);
                } catch (InterruptedException e) {
                    log.debug(e);
                }
            }
            Client.comms.removeMessageListener(helloHandler);
            if(!Client.isConnected){
                Client.comms.shutdown();
                throw new ConnectionFailedException("Handshake was unsuccessful.");
            }
        } catch (IOException e) {
            log.debug(e);
            throw new ConnectionFailedException(e.getMessage());
        }
    }

    public static void login () {
        try {
            if(!Client.isConnected) {
                Client.connect();
            }
            if (Client.cancelLogin) {
                Client.cancelLogin = false;
                return;
            }
        } catch (ConnectionFailedException e) {
            log.error("Failed to connect to server! {}", e.getMessage());
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

    public static void cancelLogin () {
        if (Client.isLoggingIn) {
            Client.cancelLogin = true;
        }
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

    private static class helloReplyHandler implements PacketListener {
        @Override
        public void packetReceived (Packet packet) {
            if (packet.getType() == PacketType.HELLO) {
                synchronized (Client.waitForReply) {
                    log.info("Server says: {}", packet.getPayload());
                    Client.isConnected = true;
                    Client.waitForReply.notify();
                }
            }
        }
    }
}
