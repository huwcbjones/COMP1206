package server;

import server.exceptions.ConfigLoadException;

/**
 * Server COnfig Manager
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
final class Config {

    private int port;
    private boolean tlsEnabled;
    private int tlsPort;

    public Config () {

    }

    /**
     * Loads the config file
     * @throws ConfigLoadException
     */
    public void loadConfig () throws ConfigLoadException {


        this.port = 473;
        this.tlsEnabled = false;
        this.tlsPort = 474;
    }

    /**
     * Gets the port the server listens to (unencrypted)
     * @return Port number to listen on
     */
    public int getPort () {
        return port;
    }

    /**
     * Returns true if TLS is going to be used to protect communication between server and client
     * @return True if TLS is to be enabled
     */
    public boolean isTlsEnabled () {
        return tlsEnabled;
    }

    /**
     * Returns the TLS port the server should listen on
     * @return TLS port
     */
    public int getTlsPort () {
        return tlsPort;
    }
}
