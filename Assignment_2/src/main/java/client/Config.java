package client;

import client.utils.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import server.exceptions.ConfigLoadException;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Auction Client Configuration Manager
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
public final class Config {

    private static final Logger log = LogManager.getLogger(Config.class);

    File configFileLocation = new File("auction_config.json");

    private ArrayList<Server> servers;
    private Server selectedServer;

    public Config () {
        this.servers = new ArrayList<>();
    }

    public void loadConfig () throws ConfigLoadException {
        log.info("Loading config file...");

        if (!configFileLocation.exists()) {
            log.warn("Config file was not found.");
            return;
        }

        if (!configFileLocation.canRead()) {
            log.warn("Could not read config file!");
            return;
        }

        try {
            InputStream inputStream = new FileInputStream(this.configFileLocation);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            JSONParser parser = new JSONParser();

            try {
                // Parse JSON file
                JSONObject root = (JSONObject) parser.parse(reader);
                JSONObject clientRoot = (JSONObject) root.get("client");

                if (clientRoot == null) {
                    throw new ConfigLoadException("Failed to find root element.");
                }


                JSONArray servers = (JSONArray) clientRoot.get("servers");
                if (servers != null) {
                    log.info("Found servers root.");
                    JSONObject serverObject;
                    Server server;
                    Iterator serverIterator = servers.iterator();
                    while (serverIterator.hasNext()) {
                        try {
                            serverObject = (JSONObject) serverIterator.next();
                            String name = (String) serverObject.get("name");
                            String serverAddress = (String) serverObject.get("address");
                            Number plainPort = (Number) serverObject.get("port");
                            boolean useSecurePort = (boolean) serverObject.get("useSecurePort");
                            Number securePort = (Number) serverObject.get("securePort");

                            if(name == null) {
                                server = new Server(serverAddress, plainPort.intValue(), useSecurePort, securePort.intValue());
                            } else {
                                server = new Server(name, serverAddress, plainPort.intValue(), useSecurePort, securePort.intValue());
                            }
                            this.addServer(server);
                        } catch (ClassCastException | NullPointerException e) {
                            log.debug(e);
                            log.warn("Failed to parse a server definition.");
                        }
                    }
                }
            } catch (IOException | ParseException e) {
                log.debug(e);
                throw new ConfigLoadException("Failed to load config file! " + e.getMessage());
            }
        } catch (FileNotFoundException e) {
            log.debug(e);
            throw new ConfigLoadException("Failed to load config file!" + e.getMessage());
        }
    }

    /**
     * Adds a server to the list of servers
     *
     * @param server Server to add
     */
    public void addServer (Server server) {
        log.info("Adding server '{}'.", server.getName());
        this.servers.add(server);
    }

    /**
     * Removes a server from the list of servers
     *
     * @param server Server to remove
     */
    public void removeServer (Server server) {
        log.info("Removing server '{}'.", server.getName());
        this.servers.remove(server);
    }

    /**
     * Returns an ArrayList of servers the client has saved
     *
     * @return list of servers
     */
    public ArrayList<Server> getServers () {
        return this.servers;
    }

    /**
     * Returns the currently selected server
     *
     * @return Server
     */
    public Server getSelectedServer () {
        return this.selectedServer;
    }

    /**
     * Sets the currently selected server
     *
     * @param server
     */
    public void setSelectedServer (Server server) {
        log.info("Selected server is '{}'.", server.getName());
        this.selectedServer = server;
    }
}
