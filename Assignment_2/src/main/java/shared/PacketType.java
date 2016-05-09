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
     * Used to say OK to a message
     */
    OK,

    /**
     * Used to say Not OK to a message
     */
    NOK,

    /**
     * Used to disconnect clients
     * On receiving this packet, a client should disconnect as the server has stopped listening
     */
    DISCONNECT,

    /**
     * Sends the secure port the server is listening on
     */
    SECURE,

    LOGIN,
    LOGIN_FAIL,
    LOGIN_SUCCESS,

    LOGOUT,
    REGISTER,
    REGISTER_FAIL,
    REGISTER_SUCCESS,

    AUCTION_START,
    AUCTION_END,
    NEW_BID,

    CREATE_ITEM,
    CREATE_ITEM_FAIL,
    CREATE_ITEM_SUCCESS,
    EDIT_ITEM,
    EDIT_ITEM_FAIL,
    EDIT_ITEM_SUCCESS,
    SEARCH_ITEM,

    KEYWORDS,
    FETCH_KEYWORDS,

    BID,
    FETCH_BIDS,

    /**
     * Gets the range of reserves for a search criteria
     */
    FETCH_RESERVE_RANGE,

    /**
     * Returns the range of reserves from the server
     */
    RESERVE_RANGE,


    /**
     * Sent to the client that wins the auction
     * Payload: UUID -> ItemID
     */
    AUCTION_WIN,

    /**
     * Sent to a client if an auction completes with no winner
     * Payload: UUID -> ItemID
     */
    AUCTION_NO_WINNER,


    // Let's create a game of ping pong to maintain connection
    /**
     * Server sends a ping to client to check client is alive, if client is alive, a ping is sent back
     */
    PING
}
