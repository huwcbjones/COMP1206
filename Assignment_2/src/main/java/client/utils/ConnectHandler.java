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

    private static Logger log = LogManager.getLogger(ConnectHandler.class);

    private NotificationWaiter waiter = new NotificationWaiter();

    public void connect() throws ConnectionFailedException, ConnectionUpgradeException {
        this.doConnect();

        //region Secure Connection
        SecureHandler secureHandler = new SecureHandler(this.waiter);
        Client.addPacketListener(secureHandler);

        // Tell server everything up to now is OK
        Client.sendPacket(Packet.wasOK(true));

        waiter.waitForReply();
        if (waiter.isReplyTimedOut()) {
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
        PacketListener okListener = packet -> {
            if (packet.getType() == PacketType.OK) waiter.replyReceived();
        };
        Client.addPacketListener(okListener);

        log.debug("Waiting for server to get ready...");
        this.waiter.waitForReply();
        if (this.waiter.isReplyTimedOut()) {
            throw new ConnectionFailedException("Server never connected.");
        }
        Client.removePacketListener(okListener);
        //endregion

        //region Say HELLO to server
        log.debug("Saying HELLO to the server...");
        ReplyWaiter replyHandler = new ReplyWaiter(this.waiter) {
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
        Client.sendPacket(new Packet<>(PacketType.HELLO, "hello"));

        // Wait for reply
        waiter.waitForReply();
        Client.removePacketListener(replyHandler);
        if (waiter.isReplyTimedOut()) {
            throw new ConnectionFailedException("Handshake was unsuccessful.");
        }
        //endregion

        //region Handle Versions
        VersionOKHandler versionOKHandler = new VersionOKHandler(this.waiter);
        PacketListener serverVersionHandler = packet -> {
            if (packet.getType() == PacketType.VERSION) log.info("Server version is: {}", (int) packet.getPayload());
        };

        Client.addPacketListener(versionOKHandler);
        Client.addPacketListener(serverVersionHandler);

        log.debug("Sending server our version number...");
        Client.sendPacket(new Packet<>(PacketType.VERSION, Config.VERSION));
        waiter.waitForReply();

        if (waiter.isReplyTimedOut()) {
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

        // Tell server everything is now OK
        Client.sendPacket(Packet.wasOK(true));
    }
    private class VersionOKHandler extends ReplyWaiter {
        boolean versionIsOK = false;

        public VersionOKHandler(NotificationWaiter waiter) {
            super(waiter);
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

    private class SecureHandler extends ReplyWaiter {
        int securePort = -1;

        public SecureHandler(NotificationWaiter waiter) {
            super(waiter);
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
