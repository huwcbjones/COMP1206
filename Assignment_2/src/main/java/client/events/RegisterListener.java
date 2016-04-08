package client.events;

import shared.User;

import java.util.EventListener;

/**
 * Handles Register Events
 *
 * @author Huw Jones
 * @since 08/04/2016
 */
public interface RegisterListener extends EventListener {

    /**
     * Fired when a user successfully registers
     *
     * @param user Registered user
     */
    void registerSuccess(User user);

    /**
     * Fired when a user fails to register
     * @param reason Reason why registration failed
     */
    void registerFail(String reason);
}
