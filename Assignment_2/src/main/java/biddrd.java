import server.Server;
import server.exceptions.ArgumentNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Auction Server Daemon Bootstrapper
 *
 * @author Huw Jones
 * @since 24/03/2016
 */
public class biddrd {

    public static void main (String[] args) {
        ArrayList<String> argList = new ArrayList<>(Arrays.asList(args));

        if (argList.contains("-h") || argList.contains("--help")) {
            biddrd.help();
            return;
        } else if (argList.contains("-v") || argList.contains("--version")) {
            biddrd.version();
            return;
        }

        Server server = new Server();

        if (argList.contains("-c") || argList.contains("--config-file")) {
            try {
                server.setConfigFile(biddrd.getConfigFileOption(argList));
            } catch (ArgumentNotFoundException e) {
                System.err.println(e.getMessage());
            }
        }

        if (argList.contains("-d") || argList.contains("--data.dir")) {
            try {
                server.setDataDirectory(biddrd.getDataStoreOption(argList));
            } catch (ArgumentNotFoundException e) {
                System.err.println(e.getMessage());
            }
        }

        if (argList.contains("-t") || argList.contains("--test-config")) {
            biddrd.header();
            server.testConfig();
            return;
        }

        server.run(biddrd.getDataStoreCreate(argList));
    }

    /**
     * Prints out programme header info
     */
    private static void header () {
        System.out.println("Usage: biddrd [OPTION]...");
        System.out.println("COMP1206 Auction System Server.");
    }

    /**
     * Prints out biddrd help
     */
    private static void help () {
        biddrd.header();
        System.out.println("Arguments:");
        System.out.println("  -c, --config-file\tSpecifies config file (defaults to execdir/biddrd.json.");
        System.out.println("  -d, --data-store\tSpecifies the data store. If unspecified, loads from biddrd.json");
        System.out.println("  -h, --help\t\tPrints this help message.");
        System.out.println("  -m, --make-store\tMakes the data store file if it doesn't exist. (Use for first time run).");
        System.out.println("  -t, --test-config\tDon't run, just test config file. Will print out server config.");
        System.out.println("  -v, --version\t\tPrints version.");
    }

    private static void version () {
        System.out.println("biddrd 0.1s");
        System.out.println("Written by Huw Jones for COMP1206 Coursework 2");
    }

    private static String getConfigFileOption (ArrayList<String> argList) throws ArgumentNotFoundException {
        // Can use ternary operator here as we know either -c, or --config-file exist
        // therefore we know if it's not -c, then it must be --config-file
        int index = ( argList.contains("-c") ) ? argList.indexOf("-c") : argList.indexOf("--config-file");
        if(index +1 >= argList.size()){
            throw new ArgumentNotFoundException("No argument was specified for option " + argList.get(index) + ".");
        }
        return argList.get(index + 1);
    }

    private static String getDataStoreOption(ArrayList<String> argList) throws ArgumentNotFoundException {
        // Can use ternary operator here as we know either -d, or --data-dir exist
        // therefore we know if it's not -d, then it must be --data-dir
        int index = ( argList.contains("-d") ) ? argList.indexOf("-d") : argList.indexOf("--data-store");
        if (index + 1 >= argList.size()) {
            throw new ArgumentNotFoundException("No argument was specified for option " + argList.get(index) + ".");
        }
        return argList.get(index + 1);
    }

    private static boolean getDataStoreCreate (ArrayList<String> argList) {
        return argList.contains("-m") || argList.contains("--make-store");
    }
}
