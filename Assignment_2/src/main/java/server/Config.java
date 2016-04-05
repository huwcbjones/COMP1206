package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import server.exceptions.ConfigLoadException;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Server COnfig Manager
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public final class Config {

    public static final int VERSION = 1;

    private static Logger log = LogManager.getLogger(Config.class);

    private boolean configFileLocationWasSet = false;
    private File configFileLocation;
    private File dataDirectory;

    private ArrayList<String> hellos = new ArrayList<>();
    private int plainPort = 473;
    private boolean secureConnectionEnabled = false;
    private int securePort = 474;
    private int timeoutTime = 5 * 1000;

    private int workers = Runtime.getRuntime().availableProcessors();

    public Config () {
        this.configFileLocation = new File("auctiond_config.json").getAbsoluteFile();
        this.dataDirectory = configFileLocation.getParentFile();
    }

    public void setConfigFile (String fileLocation) {
        this.configFileLocationWasSet = true;
        this.configFileLocation = new File(fileLocation).getAbsoluteFile();
        this.dataDirectory = configFileLocation.getParentFile();
    }

    public void setDataDirectory (String directory) {
        dataDirectory = new File(directory);
    }

    /**
     * Loads the config file
     *
     * @throws ConfigLoadException
     */
    public void loadConfig () throws ConfigLoadException {
        if (!configFileLocation.exists()) {
            throw new ConfigLoadException("Config file was not found!");
        }

        if (!configFileLocation.canRead()) {
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

                if (serverRoot == null) {
                    throw new ConfigLoadException("Failed to find root element.");
                }

                this.loadDataDir(serverRoot);
                this.loadServerListening(serverRoot);
                this.loadWorkerPoolSize(serverRoot);
                this.loadHellos(serverRoot);
            } catch (IOException | ParseException e) {
                throw new ConfigLoadException("Failed to load config file! " + e.getMessage());
            }
        } catch (FileNotFoundException e) {
            throw new ConfigLoadException("Failed to load config file!" + e.getMessage());
        }
    }

    private void loadDataDir (JSONObject serverRoot) {
        String dataDir = (String) serverRoot.get("data-dir");
        if (dataDir != null && !dataDir.equals("")) {
            if (dataDir.substring(0, 1).equals(File.separator)) {
                this.dataDirectory = new File(dataDir);
            } else {
                File parentDir = this.configFileLocation.getParentFile();
                this.dataDirectory = new File(parentDir.getAbsolutePath() + File.separator + dataDir);
            }
        }
    }

    private void loadServerListening (JSONObject serverRoot) throws ConfigLoadException {
        try {
            Number plainPort = (Number) serverRoot.get("port");
            this.plainPort = plainPort.intValue();

            JSONObject secure = (JSONObject) serverRoot.get("secure");
            if (secure != null) {
                boolean enableSecure = (boolean) secure.get("enable");
                Number securePort = (Number) secure.get("port");

                this.secureConnectionEnabled = enableSecure;
                this.securePort = securePort.intValue();
            }
        } catch (ClassCastException e) {
            throw new ConfigLoadException("Failed to parse server config!");
        }
    }

    private void loadWorkerPoolSize (JSONObject serverRoot) throws ConfigLoadException {
        try {
            Number workerPoolSize = (Number) serverRoot.get("workers");
            if (workerPoolSize == null) {
                return;
            }
            this.workers = workerPoolSize.intValue();
            if (this.workers < 1) {
                log.warn("workers directive was < 1. Setting to 1.");
                this.workers = 1;
            }
        } catch (ClassCastException e) {
            throw new ConfigLoadException("Failed to parse server config! workers directive was invalid.");
        }
    }

    private void loadHellos(JSONObject serverRoot) {
        File hellosFile = new File((String) serverRoot.get("hellos"));
        if (hellosFile == null) {
            return;
        }
        if (!hellosFile.canRead()) {
            this.hellos.add("Hello");
            return;
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(hellosFile);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                this.hellos.add(line);
            }
            bufferedReader.close();

        } catch (FileNotFoundException e) {
            log.warn("Failed to open " + hellosFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Error whilst reading file. {}", e.getMessage());
        }
    }

    /**
     * Gets the port the server listens to (unencrypted)
     *
     * @return Port number to listen on
     */
    public int getPlainPort () {
        return this.plainPort;
    }

    /**
     * Returns true if a secure is going to be used to protect communication between server and client
     *
     * @return True if the server should listen on a secure socket
     */
    public boolean isSecureConnectionEnabled () {
        return this.secureConnectionEnabled;
    }

    /**
     * Returns the secure port the server should listen on
     *
     * @return Secure port
     */
    public int getSecurePort () {
        return this.securePort;
    }

    /**
     * Returns the number of workers the server should use
     *
     * @return Number of workers
     */
    public int getWorkers () { return this.workers; }

    public String getRandomHello() {
        return this.hellos.get(new Random().nextInt(this.hellos.size()));
    }

    /**
     * Returns the number of milliseconds the server should wait for a reply from the client
     *
     * @return timeout
     */
    public int getTimeout() { return this.timeoutTime; }

    public String getConfig () {
        String config = "";

        config += "config-file: " + this.configFileLocation.getAbsolutePath() + "\n";
        config += "data-dir: " + this.dataDirectory.getAbsolutePath() + "\n";
        config += "workers: " + this.workers + "\n";
        config += "port: " + this.plainPort + "\n";
        if (this.isSecureConnectionEnabled()) {
            config += "SecureListeningEnabled: true" + "\n";
            config += "secure-port: " + this.securePort + "\n";
        }
        return config;
    }
}
