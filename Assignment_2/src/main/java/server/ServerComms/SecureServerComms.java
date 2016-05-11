package server.ServerComms;

import nl.jteam.tls.StrongTls;
import server.Server;
import server.exceptions.OperationFailureException;
import shared.exceptions.ConnectionFailedException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * Secure implementation of ServerComms
 *
 * @author Huw Jones
 * @since 27/03/2016
 */
public class SecureServerComms extends ServerComms {
    public SecureServerComms(Server server, int port) throws OperationFailureException {
        super(server, "SecureServer");

        // Set the JVM SSL Keystore
        File keyStore = Server.getConfig().getKeyStore();

        if (keyStore == null) {
            throw new OperationFailureException("Could not find server keystore.");
        }
        log.debug("Using key-store: {}", keyStore.getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStore", keyStore.getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStorePassword", "fkZC17Az8f6Cuqd1bgnimMnAnhwiEm0GCly4T1sB8zmV2iCrxUyuCI1JcFznokQ98T4LS3e8ZoX6DUi7");

        try {
            // Create SLLServerSocket
            SSLServerSocketFactory sslSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket sslSocket = (SSLServerSocket) sslSocketFactory.createServerSocket(port);
            sslSocket.setUseClientMode(false);

            // Enable strong protocols
            sslSocket.setEnabledProtocols(StrongTls.intersection(sslSocket.getSupportedProtocols(), StrongTls.ENABLED_PROTOCOLS));
            log.debug("Enabled Protocols: ");
            for (String protocol : sslSocket.getEnabledProtocols()) {
                log.debug("\t- {}", protocol);
            }

            // Enable stong cipher suites
            sslSocket.setEnabledCipherSuites(StrongTls.intersection(sslSocket.getSupportedCipherSuites(), StrongTls.ENABLED_CIPHER_SUITES));
            log.debug("Enabled Cipher Suites: ");
            for (String cipherSuite : sslSocket.getEnabledCipherSuites()) {
                log.debug("\t- {}", cipherSuite);
            }

            this.socket = sslSocket;
        } catch (SocketException e) {
            log.catching(e);
            throw new OperationFailureException("Possible key-store location problem?");
        } catch (IOException e){
            log.catching(e);
            throw new OperationFailureException("Failed to start comms, " + e.getMessage());
        }
    }

    @Override
    protected ClientConnection connectClient(long clientID, Socket socket) throws ConnectionFailedException {
        SSLSocket sslSocket = (SSLSocket) socket;
        try {
            sslSocket.startHandshake();
        } catch (IOException e) {
            log.catching(e);
            throw new ConnectionFailedException(e.getMessage());
        }
        SSLSession session = ((SSLSocket) socket).getSession();
        log.trace("Secure client connecting from {}, using {} with {}", session.getPeerHost(), session.getProtocol(), session.getCipherSuite());

        return new ClientConnection(clientID, socket, true);
    }
}
