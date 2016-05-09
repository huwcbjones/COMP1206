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

    //region Login Types
    LOGIN,
    LOGIN_FAIL,
    LOGIN_SUCCESS,

    LOGOUT,
    //endregion

    //region Register Types
    REGISTER,
    REGISTER_FAIL,
    REGISTER_SUCCESS,
    //endregion

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
    GET_KEYWORDS,

    BID,
    FETCH_BIDS,

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


    //region User

    //endregion

    //region Connection Maintenance
    // Let's create a game of ping pong to maintain connection
    /**
     * Server sends a ping to client to check client is alive, if client is alive, a ping is sent back
     */
    PING
    //endregion
}
