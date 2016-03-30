package shared;

/**
 * Defines a type of message
 *
 * @author Huw Jones
 * @since 26/03/2016
 */
public enum PacketType {

    /**
     * Used to say hello between client and server to ensure the two can communicate
     */
    HELLO,

    /**
     * Used to get client/server versions
     */
    VERSION,

    /**
     * Used to disconnect clients
     * On receiving this packet, a client should disconnect as the server has stopped listening
     */
    DISCONNECT,

    /**
     * Connection should use the secure connection.
     * Returns the secure port.
     */
    USE_SECURE,

    //region Login Types
    LOGIN,
    LOGIN_FAIL,
    LOGIN_SUCCESS,
    //endregion
}
