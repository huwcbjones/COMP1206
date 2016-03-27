package server;

import server.exceptions.ConfigLoadException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.Iterator;

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

    private int workers = 8;

    public Config () {
        this.configFileLocation = new File("config.json");
        this.dataDirectory = configFileLocation.getParentFile();
    }

    public void setConfigFile(String fileLocation){
        this.configFileLocationWasSet = true;
        this.configFileLocation = new File(fileLocation);
        this.dataDirectory = configFileLocation.getParentFile();
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

        try {
            InputStream inputStream = new FileInputStream(this.configFileLocation);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            JSONParser parser = new JSONParser();

            try {
                // Parse JSON file
                JSONObject root = (JSONObject) parser.parse(reader);
                JSONObject serverRoot = (JSONObject) root.get("server");

                if(serverRoot == null){
                    throw new ConfigLoadException("Failed to find root element.");
                }

                String dataDir = (String)serverRoot.get("data-dir");
                if(dataDir != null && !dataDir.equals("")){
                    if(dataDir.substring(0, 1).equals(File.separator)) {
                        this.dataDirectory = new File(dataDir);
                    } else {
                        this.dataDirectory = new File(this.configFileLocation.getParentFile().getAbsolutePath() + File.separator + dataDir);
                    }
                }

                int plainPort = ((Long)serverRoot.get("port")).intValue();
                this.plainPort = plainPort;

                JSONObject secure = (JSONObject)serverRoot.get("secure");
                if(secure != null){
                    boolean enableSecure = (boolean)secure.get("enable");
                    int securePort = ((Long)secure.get("port")).intValue();

                    this.secureConnectionEnabled = enableSecure;
                    this.securePort = securePort;
                }
            } catch (IOException | ParseException e) {
                throw new ConfigLoadException("Failed to load config file! " + e.getMessage());
            }

        } catch (FileNotFoundException e) {
            throw new ConfigLoadException("Failed to load config file!" + e.getMessage());
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

    public String getConfig(){
        String config = "";

        config += "config-file: " + this.configFileLocation.getAbsolutePath() + "\n";
        config += "data-dir: " + this.dataDirectory.getAbsolutePath() + "\n";
        config += "port: " + this.plainPort + "\n";
        if(this.isSecureConnectionEnabled()) {
            config += "SecureListeningEnabled: true" + "\n";
            config += "secure-port: " + this.securePort + "\n";
        }
        return config;
    }
}
