package client.utils;

import client.Client;
import client.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shared.Packet;
import shared.PacketType;
import shared.events.PacketListener;
import shared.exceptions.ConnectionFailedException;
import shared.exceptions.ConnectionUpgradeException;
import shared.utils.ReplyWaiter;

/**
 * Handles client connecting after client/server connection has been established
 *
 * @author Huw Jones
 * @since 04/04/2016
 */
public class ConnectHandler {

    private static final Logger log = LogManager.getLogger(ConnectHandler.class);

    private NotificationWaiter waiter = new NotificationWaiter();
    private PacketListener okListener;

    public ConnectHandler() {
        this.initialise();
    }

    /**
     * Initialises event handlers before first message is sent
     * (Prevents misfire)
     */
    public void initialise(){
        this.okListener = packet -> {
            if (packet.getType() == PacketType.OK) waiter.replyReceived();
        };
        Client.addPacketListener(this.okListener);
    }

    /**
     * Connects a client to the server
     * @throws ConnectionFailedException If the connection failed
     * @throws ConnectionUpgradeException If the connection should be upgraded to a secure line
     */
    public void connect() throws ConnectionFailedException, ConnectionUpgradeException {
        this.doConnect();


        //region Secure Connection
        SecureHandler secureHandler = new SecureHandler(Client.getConfig().getTimeout());
        Client.addPacketListener(secureHandler);

        // Tell server everything up to now is OK
        Client.sendPacket(Packet.wasOK(true));

        secureHandler.getWaiter().waitForReply();
        if (secureHandler.getWaiter().isReplyTimedOut()) {
            throw new ConnectionFailedException("Handshake was unsuccessful.");
        }
        if (secureHandler.getSecurePort() == -1) {
            return;
        }
        int securePort = secureHandler.getSecurePort();
        Config config = Client.getConfig();
        Server selectedServer = config.getSelectedServer();
        Server secureServer = new Server(selectedServer.getName(), selectedServer.getAddress(), selectedServer.getPort(), securePort);
        config.setSelectedServer(secureServer);
        throw new ConnectionUpgradeException();
        //endregion
    }

    private void doConnect() throws ConnectionFailedException {
        //region Wait for Server OK
        log.debug("Waiting for server to get ready...");
        this.waiter.waitForReply();
        if (this.waiter.isReplyTimedOut()) {
            throw new ConnectionFailedException("Server never connected.");
        }
        Client.removePacketListener(this.okListener);
        //endregion

        //region Say HELLO to server
        log.debug("Saying HELLO to the server...");
        ReplyWaiter replyHandler = new ReplyWaiter(Client.getConfig().getTimeout()) {
            @Override
            public void packetReceived(Packet packet) {
                switch (packet.getType()) {
                    case HELLO:
                        log.debug("Server says: {}", packet.getPayload());
                        waiter.replyReceived();
                        break;
                }
            }
        };
        Client.addPacketListener(replyHandler);

        VersionOKHandler versionOKHandler = new VersionOKHandler(Client.getConfig().getTimeout());
        PacketListener serverVersionHandler = packet -> {
            if (packet.getType() == PacketType.VERSION) log.info("Server version is: {}", (int) packet.getPayload());
            waiter.replyReceived();
        };

        Client.addPacketListener(versionOKHandler);
        Client.addPacketListener(serverVersionHandler);

        Client.sendPacket(Packet.Hello());

        // Wait for reply
        replyHandler.getWaiter().waitForReply();
        Client.removePacketListener(replyHandler);
        if (replyHandler.getWaiter().isReplyTimedOut()) {
            throw new ConnectionFailedException("Handshake was unsuccessful.");
        }
        //endregion

        //region Handle Versions

        log.debug("Sending server our version number...");
        Client.sendPacket(new Packet<>(PacketType.VERSION, Config.VERSION));
        versionOKHandler.getWaiter().waitForReply();

        if (versionOKHandler.getWaiter().isReplyTimedOut()) {
            throw new ConnectionFailedException("Handshake was unsuccessful.");
        }
        if (!versionOKHandler.versionIsOK()) {
            throw new ConnectionFailedException("Server/Client versions are not compatible.");
        }

        // Wait for server version
        waiter.waitForReply();

        if (waiter.isReplyTimedOut()) {
            throw new ConnectionFailedException("Handshake was unsuccessful.");
        }
        Client.removePacketListener(versionOKHandler);
        Client.removePacketListener(serverVersionHandler);
        //endregion
    }

    public void secureConnect() throws ConnectionFailedException {
        this.doConnect();

        //region Wait for Server OK
        PacketListener okListener = packet -> {
            if (packet.getType() == PacketType.OK) waiter.replyReceived();
        };
        Client.addPacketListener(okListener);

        // Tell server everything is now OK
        Client.sendPacket(Packet.wasOK(true));

        log.debug("Waiting for server to get ready...");
        this.waiter.waitForReply();
        if (this.waiter.isReplyTimedOut()) {
            throw new ConnectionFailedException("Server never connected.");
        }
        Client.removePacketListener(okListener);
        //endregion
    }

    /**
     * Class for checking versions
     */
    private class VersionOKHandler extends ReplyWaiter {
        boolean versionIsOK = false;

        public VersionOKHandler(int timeout) {
            super(timeout);
        }

        public boolean versionIsOK() {
            return this.versionIsOK;
        }

        @Override
        public void packetReceived(Packet packet) {
            switch (packet.getType()) {
                case OK:
                    log.info("Client/Server version is OK");
                    this.versionIsOK = true;
                    break;
                case NOK:
                    log.info("Client/Server versions are not OK");
                    this.versionIsOK = false;
                    break;
            }
            waiter.replyReceived();
        }
    }

    /**
     * Class for handling secure connection upgrading
     */
    private class SecureHandler extends ReplyWaiter {
        int securePort = -1;

        public SecureHandler(int timeout) {
            super(timeout);
        }

        public int getSecurePort() {
            return this.securePort;
        }

        @Override
        public void packetReceived(Packet packet) {
            // Either way, we want to know if we got a reply
            // securePort is set to -1 as default, so if we get a SECURE, update the value,
            // then trigger the replyReceived
            switch(packet.getType()){
                case SECURE:
                    this.securePort = (int)packet.getPayload();
                case OK:
                    waiter.replyReceived();
                    break;
            }
        }
    }

}
