package server.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.ClientConnection;
import server.Config;
import server.Server;
import shared.Packet;
import shared.PacketType;
import shared.events.PacketListener;
import shared.exceptions.ConnectionFailedException;
import shared.utils.ReplyWaiter;

/**
 * Handles Client connection sequence
 *
 * @author Huw Jones
 * @since 05/04/2016
 */
public class ConnectHandler {
    private static final Logger log = LogManager.getLogger(ConnectHandler.class);

    private NotificationWaiter waiter;
    private final ClientConnection client;

    public ConnectHandler(ClientConnection client) {
        this.client = client;
    }

    public void connect() throws ConnectionFailedException {
        this.waiter = new NotificationWaiter();
        PacketListener helloListener;

        //region Tell client we are ready
        helloListener = packet -> {
            if(packet.getType() == PacketType.HELLO) waiter.replyReceived();
        };
        this.client.addPacketListener(helloListener);

        log.debug("Telling client we are now ready.");

        this.client.sendPacket(Packet.wasOK(true));
        //endregion

        //region Say HELLO
        log.debug("Waiting for HELLO...");
        waiter.waitForReply();

        if (waiter.isReplyTimedOut()) {
            throw new ConnectionFailedException("Client failed to send hello.");
        }
        log.debug("HELLO Client #{}", this.client.getClientID());
        this.client.removePacketListener(helloListener);

        // Add version listener before we send a reply as we should get a version back as soon as we say hello
        VersionListener versionListener = new VersionListener(this.waiter);
        this.client.addPacketListener(versionListener);
        this.client.sendPacket(Packet.Hello(Server.getConfig().getRandomHello() + " (hello)"));
        //endregion

        //region Handle Versions
        log.debug("Waiting for client to send VERSION...");
        this.waiter.waitForReply();
        if (this.waiter.isReplyTimedOut()) {
            throw new ConnectionFailedException("Client failed to respond to hello and send version.");
        }
        int clientVersion = versionListener.getClientVersion();
        log.debug("Client VERSION is: {}", clientVersion);
        if (Config.VERSION != clientVersion) {
            this.client.sendPacket(Packet.wasOK(false));
            throw new ConnectionFailedException("Client version (" + clientVersion + ") is incompatible with server version (" + Config.VERSION + ").");
        }
        this.client.sendPacket(Packet.wasOK(true));
        this.client.sendPacket(new Packet<>(PacketType.VERSION, Config.VERSION));
        //endregion

        //region
        PacketListener okListener = packet -> {
            if (packet.getType() == PacketType.OK) waiter.replyReceived();
        };
        this.client.addPacketListener(okListener);
        log.debug("Waiting for client to ready...");
        this.waiter.waitForReply();
        if (this.waiter.isReplyTimedOut()) {
            throw new ConnectionFailedException("Client never got ready.");
        }
        this.client.removePacketListener(okListener);

        if(!Server.getConfig().isSecureConnectionEnabled() || this.client.isSecureConnection()){
            this.client.sendPacket(Packet.wasOK(true));
            return;
        }

        this.client.sendPacket(new Packet<>(PacketType.SECURE, Server.getConfig().getSecurePort()));
        //endregion
    }

    private class VersionListener extends ReplyWaiter {

        private int clientVersion;

        public VersionListener(NotificationWaiter waiter) {
            super(waiter);
        }

        public int getClientVersion() {
            return this.clientVersion;
        }

        @Override
        public void packetReceived(Packet packet) {
            if(packet.getType() == PacketType.VERSION){
                this.clientVersion = (int) packet.getPayload();
                this.waiter.replyReceived();
            }
        }
    }
}
