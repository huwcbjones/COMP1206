package client;

import client.utils.Server;
import server.exceptions.ConfigLoadException;

import java.util.ArrayList;

/**
 * Auction Client Configuration Manager
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
public final class Config {

    private ArrayList<Server> servers;
    private Server selectedServer;

    public void loadConfig () throws ConfigLoadException {

    }

    /**
     * Adds a server to the list of servers
     *
     * @param server Server to add
     */
    public void addServer (Server server) {
        this.servers.add(server);
    }

    /**
     * Removes a server from the list of servers
     *
     * @param server Server to remove
     */
    public void removeServer (Server server) {
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
     * @return Server
     */
    public Server getSelectedServer(){
        return this.selectedServer;
    }

    /**
     * Sets the currently selected server
     * @param server
     */
    public void setSelectedServer(Server server){
        this.selectedServer = server;
    }
}
