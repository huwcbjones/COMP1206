package server;

import server.exceptions.ConfigLoadException;
import shared.utils.Log;

import javax.net.ssl.SSLSocket;
import java.net.ServerSocket;

/**
 * Auction Server Daemon
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public final class Server {

    private ServerSocket serverSocket;
    private SSLSocket
    private Config config;

    public Server(){

    }

    public void run(){
        loadConfig();
    }

    private void loadConfig(){
        config = new Config();
        try {
            config.loadConfig();
        } catch (ConfigLoadException e) {
            Log.Fatal("Failed to load config. Quitting!");
        }
    }
}
