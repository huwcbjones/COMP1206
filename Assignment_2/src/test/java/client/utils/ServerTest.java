package client.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Server test
 *
 * @author Huw Jones
 * @since 06/04/2016
 */
public class ServerTest {
    @Test
    public void equals() throws Exception {
        String name = "asdfg";
        String address = "10.1.1.1";
        int port = 473;
        Server server1 = new Server(name, address, port);
        Server server2 = new Server(name, address, port);

        assertTrue(server1.equals(server2));
    }

}