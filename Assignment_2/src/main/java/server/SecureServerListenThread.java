package server;

import shared.exceptions.ConnectionFailedException;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

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
            log.debug(e);
            throw new ConnectionFailedException(e.getMessage());
        }
        SSLSession session = (( SSLSocket) socket).getSession();
        log.trace("Peer host is {}", session.getPeerHost());
        log.trace("Cipher is {}", session.getCipherSuite());
        log.trace("Protocol is {}", session.getProtocol());
        log.trace("ID is {}", new BigInteger(session.getId()));

        return new ClientConnection(clientID, socket, true);
    }
}
