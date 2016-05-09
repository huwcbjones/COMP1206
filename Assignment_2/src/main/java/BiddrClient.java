import client.Client;
import client.Log4j2ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationFactory;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Auction Client Bootstrapper
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class BiddrClient {

    public static void main(String[] args) {
        ArrayList<String> argList = new ArrayList<>(Arrays.asList(args));

        String username = null;
        String password = null;
        if (argList.contains("-u") || argList.contains("--user")) {
            int index = ( argList.contains("-u") ) ? argList.indexOf("-u") : argList.indexOf("--user");
            if (index + 1 < argList.size()) {
                username = argList.get(index + 1);
            }
        }
        if (argList.contains("-p") || argList.contains("--password")) {
            int index = ( argList.contains("-p") ) ? argList.indexOf("-p") : argList.indexOf("--password");
            if (index + 1 < argList.size()) {
                password = argList.get(index + 1);
            }
        }
        ConfigurationFactory.setConfigurationFactory(new Log4j2ConfigurationFactory());
        Client c = new Client(username, password);
    }
}
