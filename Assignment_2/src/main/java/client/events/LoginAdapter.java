package client.events;

import shared.User;

/**
 * Implements Adapter for LoginListener
 *
 * @author Huw Jones
 * @since 12/04/2016
 */
public class LoginAdapter implements LoginListener {
    /**
     * Fired when a successful login occurs
     *
     * @param user User object for current user
     */
    @Override
    public void loginSuccess(User user) {

    }

    /**
     * Fire when an unsuccessful login occurs
     *
     * @param message Reason why login failed
     */
    @Override
    public void loginFail(String message) {

    }
}
