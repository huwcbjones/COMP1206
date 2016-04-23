package server;

import shared.exceptions.ConnectionFailedException;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Secure implementation of ServerListenThread
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
class SecureServerListenThread extends ServerListenThread {
    public SecureServerListenThread (Server server, ServerSocket socket) {
        super(server, socket, "SecureServer");
    }

    @Override
    protected ClientConnection connectClient (long clientID, Socket socket) throws ConnectionFailedException {
        SSLSocket sslSocket = (SSLSocket) socket;
        try {
            sslSocket.startHandshake();
        } catch (IOException e) {
            log.catching(e);
            throw new ConnectionFailedException(e.getMessage());
        }
        SSLSession session = (( SSLSocket) socket).getSession();
        log.trace("Secure client connecting from {}, using {} with {}", session.getPeerHost(), session.getProtocol(), session.getCipherSuite());

        return new ClientConnection(clientID, socket, true);
    }
}
