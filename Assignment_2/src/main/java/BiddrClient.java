import client.Client;
import client.Log4j2ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationFactory;

/**
 * Auction Client Bootstrapper
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class BiddrClient {

    public static void main(String[] args) {
        ConfigurationFactory.setConfigurationFactory(new Log4j2ConfigurationFactory());
        Client c = new Client();
    }
}
