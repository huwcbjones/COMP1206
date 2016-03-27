package client.utils;

/**
 * Represents a server a client can connect to
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
public class Server {

    private final String address;
    private final int port;
    private final boolean useSecurePort;
    private final int securePort;


    public Server (String address, int port, boolean useSecurePort, int securePort) {
        this.address = address;
        this.port = port;
        this.useSecurePort = useSecurePort;
        this.securePort = securePort;
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
     * Returns whether or not we should try to use the server's secure connection
     *
     * @return true if we should use the secure connection
     */
    public boolean useSecurePort () {
        return useSecurePort;
    }

    /**
     * Returns the secure port the server is listening on
     *
     * @return int, secure port
     */
    public int getSecurePort () {
        return securePort;
    }
}
