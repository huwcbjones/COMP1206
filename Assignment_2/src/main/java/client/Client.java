package client;

import client.utils.Server;
import client.windows.Login;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.exceptions.ConfigLoadException;
import shared.Comms;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Auction Client
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public final class Client {

    private static Logger log = LogManager.getLogger(Client.class);
    private static final Config config = new Config();
    private static boolean isConnected = false;

    private static Comms plainComms;
    private static Comms secureComms;
    private Socket plainSocket;
    private Socket secureSocket;

    public Client () {

        try {
            Client.config.loadConfig();
        } catch (ConfigLoadException e) {

        }
        Login loginWindow = new Login();

        loginWindow.setVisible(true);
    }

    public static Config getConfig () {
        return Client.config;
    }

    public boolean connect () {
        Server server = Client.config.getSelectedServer();
        try {
            this.plainSocket = new Socket(server.getAddress(), server.getPort());
            ObjectInputStream inputStream = new ObjectInputStream(this.plainSocket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(this.plainSocket.getOutputStream());

            Client.plainComms = new Comms(inputStream, outputStream);
        } catch (IOException e) {
            log.debug(e);
            return false;
        }
        return true;
    }
}
