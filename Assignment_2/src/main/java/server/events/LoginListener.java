package server.events;

import server.ServerComms.ClientConnection;
import server.objects.User;

import java.util.EventListener;

/**
 * Event Handler for Login Events
 *
 * @author Huw Jones
 * @since 22/04/2016
 */
public interface LoginListener extends EventListener {

    /**
     * Occurs when a user logs in
     *
     * @param user User that logged in
     */
    void userLoggedIn(User user);

    /**
     * Occurs when a user logs out
     *
     * @param user         User that logged in
     * @param clientNumber Client number of client where user was logged in
     */
    void userLoggedOut(User user, ClientConnection clientNumber);
}
