package client;

import client.utils.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import shared.exceptions.ConfigLoadException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Auction Client Configuration Manager
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
public final class Config {

    public static final int VERSION = 1;

    private static final Logger log = LogManager.getLogger(Config.class);

    File configFileLocation = new File("config/biddr.json");

    private HashMap<String, Server> servers;
    private Server selectedServer;
    private int timeoutTime = 5 * 1000;

    public Config() {
        this.servers = new HashMap<>();
    }

    /**
     * Attempts to load the config file
     * @throws ConfigLoadException Thrown if loading failed
     */
    public void loadConfig() throws ConfigLoadException {
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

                            if (name == null) {
                                server = new Server(serverAddress, plainPort.intValue());
                            } else {
                                server = new Server(name, serverAddress, plainPort.intValue());
                            }
                            this.addServer(server);
                        } catch (ClassCastException | NullPointerException e) {
                            log.catching(e);
                            log.warn("Failed to parse a server definition.");
                        }
                    }
                    // If there are servers, set selected server to 0.
                    if (this.servers.size() != 0) {
                        this.selectedServer = this.servers.get(0);
                    }
                }
            } catch (IOException | ParseException e) {
                log.catching(e);
                throw new ConfigLoadException("Failed to load config file! " + e.getMessage());
            }
        } catch (FileNotFoundException e) {
            log.catching(e);
            throw new ConfigLoadException("Failed to load config file!" + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")              // Suppress warning because of the 3rd party library
    public void saveConfig() {
        log.info("Saving config...");
        try {
            PrintWriter writer = new PrintWriter(configFileLocation);

            JSONObject client = new JSONObject();
            JSONObject servers = new JSONObject();
            JSONObject server;
            JSONArray serversArray = new JSONArray();

            for (Server s : this.servers.values()) {
                server = new JSONObject();
                server.put("name", s.getName());
                server.put("address", s.getAddress());
                server.put("port", s.getPort());
                serversArray.add(server);
            }
            servers.put("servers", serversArray);

            client.put("client", servers);

            StringWriter output = new StringWriter();
            client.writeJSONString(output);
            writer.write(output.toString());
            writer.close();
            log.info("Config saved!");
        } catch (FileNotFoundException e) {
            log.error("Failed to find file to write to.");
        } catch (IOException e) {
            log.catching(e);
            log.fatal("Failed to write config file. {}", e.getMessage());
        }
    }

    /**
     * Adds a server to the list of servers
     *
     * @param server Server to add
     */
    public void addServer(Server server) {
        log.info("Adding server '{}'.", server.getName());
        this.servers.put(server.getName(), server);
    }

    /**
     * Removes a server from the list of servers
     *
     * @param server Server to remove
     */
    public void removeServer(Server server) {
        log.info("Removing server '{}'.", server.getName());
        this.servers.remove(server.getName());
    }

    /**
     * Returns an ArrayList of servers the client has saved
     *
     * @return list of servers
     */
    public ArrayList<Server> getServers() {
        return new ArrayList<>(this.servers.values());
    }

    /**
     * Returns the currently selected server
     *
     * @return Server
     */
    public Server getSelectedServer() {
        return this.selectedServer;
    }

    /**
     * Sets the currently selected server
     *
     * @param server
     */
    public void setSelectedServer(Server server) {
        log.info("Selected server is '{}'.", server.getName());
        this.selectedServer = server;
    }

    /**
     * Gets the amount of time in milliseconds the client should wait for replies from the server
     *
     * @return int, milliseconds, timeout time
     */
    public int getTimeout() {
        return this.timeoutTime;
    }
}
