package server;

import server.exceptions.ConfigLoadException;
import shared.utils.Log;

import java.io.File;

/**
 * Server COnfig Manager
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
final class Config {

    private boolean configFileLocationWasSet = false;
    private File configFileLocation;
    private File dataDirectory;

    private int plainPort = 473;
    private boolean secureConnectionEnabled = false;
    private int securePort = 474;

    public Config () {
        configFileLocation = new File("config.json");
    }

    public void setConfigFile(String fileLocation){
        this.configFileLocationWasSet = true;
        this.configFileLocation = new File(fileLocation);
    }

    public void setDataDirectory(String directory){
        dataDirectory = new File(directory);
    }

    /**
     * Loads the config file
     * @throws ConfigLoadException
     */
    public void loadConfig () throws ConfigLoadException {
        if(!configFileLocation.exists()){
            throw new ConfigLoadException("Config file was not found!");
        }

        if(!configFileLocation.canRead()){
            throw new ConfigLoadException("Could not read config file!");
        }
    }

    /**
     * Gets the port the server listens to (unencrypted)
     * @return Port number to listen on
     */
    public int getPlainPort () {
        return plainPort;
    }

    /**
     * Returns true if a secure is going to be used to protect communication between server and client
     * @return True if the server should listen on a secure socket
     */
    public boolean isSecureConnectionEnabled () {
        return secureConnectionEnabled;
    }

    /**
     * Returns the secure port the server should listen on
     * @return Secure port
     */
    public int getSecurePort () {
        return securePort;
    }
}
