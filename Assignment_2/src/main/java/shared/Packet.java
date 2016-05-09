package shared;

import java.io.Serializable;

/**
 * Represents a packet of object type, T extends Serializable, sent between client and server
 * Forcing packet payloads to implement Serializable has saved many hours of debugging and wondering why some
 * objects failed to send.
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public final class Packet<T extends Serializable> implements Serializable {

    public static final long serialUID = 1L;

    private final PacketType type;
    private final T payload;

    public Packet(PacketType type){
        this(type, null);
    }

    public Packet(PacketType type, T payload) {
        this.type = type;
        this.payload = payload;
    }

    public T getPayload() {
        return this.payload;
    }

    public PacketType getType() {
        return this.type;
    }

    /**
     * Returns a HELLO packet
     *
     * @return HELLO packet
     */
    public static Packet<String> Hello() {
        return Packet.Hello("Hello");
    }

    /**
     * Returns a HELLO packet with a given greeting
     *
     * @param message Message to send
     * @return HELLO packet
     */
    public static Packet<String> Hello(String message) {
        return new Packet<>(PacketType.HELLO, message);
    }

    /**
     * Returns a DISCONNECT packet with a given reason
     *
     * @param reason Reason for disconnect
     * @return DISCONNECT packet
     */
    public static Packet<String> Disconnect(String reason) {
        return new Packet<>(PacketType.DISCONNECT, reason);
    }

    /**
     * Returns an OK/NOK packet
     *
     * @param wasOK If operation was OK, will return OK, otherwise will return NOK
     * @return OK/NOK packet
     */
    public static Packet<Serializable> wasOK(boolean wasOK) {
        return new Packet<>(wasOK ? PacketType.OK : PacketType.NOK);
    }

    /**
     * Returns a PING packet
     *
     * @return PING packet
     */
    public static Packet<Serializable> Ping() {
        return new Packet<>(PacketType.PING);
    }

    /**
     * Returns a LOGOUT packet
     *
     * @return LOGOUT packet
     */
    public static Packet<Serializable> Logout() {
        return new Packet<>(PacketType.LOGOUT);
    }
}
