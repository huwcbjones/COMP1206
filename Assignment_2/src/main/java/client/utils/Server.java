package client.utils;

/**
 * Represents a server a client can connect to
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
public final class Server {

    private final String name;
    private final String address;
    private final int port;
    private int securePort;


    public Server (String address, int port) {
        this(address + "(" + port + ")", address, port);
    }

    public Server (String name, String address, int port) {
        this(name, address, port, -1);
    }

    public Server(String name, String address, int port, int securePort) {
        this.name = name;
        this.address = address;
        this.port = port;
        this.securePort = securePort;
    }

    /**
     * Returns the name of the server as the user would like to see it
     *
     * @return User defined server name
     */
    public String getName () {
        return name;
    }

    /**
     * Returns the address of the server
     *
     * @return Address of the server
     */
    public String getAddress () {
        return address;
    }

    /**
     * Returns the port the server is listening on
     *
     * @return int, the port number
     */
    public int getPort () {
        return port;
    }

    /**
     * Returns the secure port the server is listening on
     *
     * @return int, the secure port number
     */
    public int getSecurePort() {
        return this.securePort;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Server)) return false;
        Server server = (Server) obj;

        return this.name.equals(server.getName()) && this.address.equals(server.getAddress()) && this.port == server.port;
    }

    @Override
    public String toString () {
        return this.name;
    }
}
