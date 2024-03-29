package client.events;

import shared.User;

import java.util.EventListener;

/**
 * Handles Login Events
 *
 * @author Huw Jones
 * @since 28/03/2016
 */
public interface LoginListener extends EventListener {

    /**
     * Fired when a successful login occurs
     *
     * @param user User object for current user
     */
    void loginSuccess (User user);

    /**
     * Fired when an unsuccessful login occurs
     *
     * @param message Reason why login failed
     */
    void loginFail(String message);

    /**
     * Fired when a logout occurs
     */
    void logout();
}
