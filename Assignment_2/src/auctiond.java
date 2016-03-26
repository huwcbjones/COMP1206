import server.Server;
/**
 * Auction Server Daemon Bootstrapper
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class auctiond {

    public static void main(String[] args) {
        Server server = new Server();
    }

    /**
     * Prints out programme header info
     */
    private static void header() {
        System.out.println("Usage: auctiond [OPTION]...");
        System.out.println("COMP1206 Auction System Server.\n");
    }

    /**
     * Prints out auctiond help
     */
    private static void help() {
        auctiond.header();
        System.out.println("Arguments:");
        System.out.println("  -c, --config-file\t\tSpecifies config file (defaults to execdir/config.json.");
        System.out.println("  -d, --data-dir\t\tSpecifies the data storage directory. If unspecified, loads from config.json");
        System.out.println("  -h, --help\t\tPrints this help message.");
        System.out.println("  -t, --test-config\tDon't run, just test config file. Will print out server config.");
        System.out.println("  -v, --version\t\tPrints version.");
    }

    private static void version() {
        System.out.println("auctiond 0.1");
        System.out.println("Written by Huw Jones for COMP1206 Coursework 2");
    }
}
