package shared;

import java.io.Serializable;

/**
 * Represents a packet of object type, T extends Serializable, sent between client and server
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public final class Packet<T extends Serializable> implements Serializable {

    public static final long serialUID = 1L;

    private final PacketType type;
    private final T payload;

    public Packet (PacketType type, T payload) {
        this.type = type;
        this.payload = payload;
    }

    public T getPayload () {
        return this.payload;
    }

    public PacketType getType () {
        return this.type;
    }
}
