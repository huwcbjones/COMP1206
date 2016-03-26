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
    private SSLSocket sslSocket;
    private Config config;

    public Server(){
        config = new Config();
    }

    public void setConfigFile(String file){
        config.setConfigFile(file);
    }
    public void setDataDirectory(String dir){
        config.setDataDirectory(dir);
    }

    public void run(){
        Log.Information("Starting server...");
        this.loadConfig();
    }

    public void testConfig(){
        this.loadConfig();
    }

    private void loadConfig(){
        Log.Information("Loading config...");

        try {
            config.loadConfig();
        } catch (ConfigLoadException e) {
            Log.Fatal("Failed to load config. Quitting!");
        }
    }
}
