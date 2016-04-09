package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.exceptions.OperationFailureException;
import shared.RegisterUser;
import shared.exceptions.UnsupportedSecurityException;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data Persistence service class
 *
 * @author Huw Jones
 * @since 08/04/2016
 */
public final class DataPersistence {

    private static final Logger log = LogManager.getLogger(DataPersistence.class);

    private final ConcurrentHashMap<UUID, User> users = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UUID> usernameToUUID = new ConcurrentHashMap<>();

    private Connection connection;

    //region Loading

    /**
     * Loads data into DataPersistence
     */
    public void loadData(boolean createDatabase) throws OperationFailureException {
        log.info("Loading data...");
        boolean shouldCreateInitialDB = false;
        File dataStore = Server.getConfig().getDataStore();
        if(!dataStore.exists()){
            if(createDatabase) {
                try {
                    if(!dataStore.createNewFile()){
                        throw new OperationFailureException("Failed to create datastore. An unspecified error occurred.");
                    } else {
                        shouldCreateInitialDB = true;
                        log.info("Created datastore: {}", dataStore.getAbsolutePath());
                    }
                } catch (IOException e) {
                    log.debug(e);
                    throw new OperationFailureException("Failed to craete datastore, " + e.getMessage());
                }
            } else {
                throw new OperationFailureException("Failed to open datastore, file does not exist. To automatically create the database, start the server using the -m or --make-store argument.");
            }
        }

        if(!dataStore.isFile()){
            throw new OperationFailureException("Failed to open datastore, datastore provided is not a file. "+ dataStore.getAbsolutePath());
        }

        if(!dataStore.canWrite()){
            throw new OperationFailureException("Failed to open datastore, cannot write to datastore. "+ dataStore.getAbsolutePath());
        }

        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dataStore.getAbsolutePath());

        } catch (SQLException | ClassNotFoundException e) {
            log.debug(e);
            throw new OperationFailureException("Failed to open data file.");
        }

        if(shouldCreateInitialDB){
            this.createDatabaseStructure();
        }

        log.info("Loaded data!");
    }

    private Statement createStatement() throws SQLException {
        return this.connection.createStatement();
    }

    private void createDatabaseStructure() throws OperationFailureException {
        try {
            Statement statement = this.createStatement();
            String usersTable = "CREATE TABLE users (" +
                                "  userID BLOB PRIMARY KEY," +
                                "  username VARCHAR(255) NOT NULL UNIQUE," +
                                "  firstName VARCHAR(255) NOT NULL," +
                                "  lastName VARCHAR(255) NOT NULL," +
                                "  password BLOB NOT NULL" +
                                ")";
            statement.execute(usersTable);
            log.info("Created users table!");

            String keywordsTable =  "CREATE TABLE keywords (" +
                                    "  keywordID INT PRIMARY KEY," +
                                    "  keyword VARCHAR(140) NOT NULL UNIQUE" +
                                    ")";
            statement.execute(keywordsTable);
            log.info("Created keywords table!");

            String itemsTable = "CREATE TABLE items (" +
                                "  itemID BLOB PRIMARY KEY," +
                                "  userID BLOB NOT NULL," +
                                "  title VARCHAR(140) NOT NULL," +
                                "  description TEXT NOT NULL," +
                                "  startTime DATETIME NOT NULL," +
                                "  endTime DATETIME NOT NULL," +
                                "  reservePrice INT," +
                                "  FOREIGN KEY (userID) REFERENCES users(userID)" +
                                ")";
            statement.execute(itemsTable);
            log.info("Created items table!");

            String bidsTable =  "CREATE TABLE bids (" +
                                "  bidID BLOB PRIMARY KEY, " +
                                "  itemID BLOB NOT NULL," +
                                "  userID BLOB NOT NULL," +
                                "  price INT NOT NULL," +
                                "  time DATETIME NOT NULL," +
                                "  FOREIGN KEY (itemID) REFERENCES items(itemID)," +
                                "  FOREIGN KEY (userID) REFERENCES users(userID)" +
                                ")";
            statement.execute(bidsTable);
            log.info("Created bids table!");

            String itemKeywordMap = "CREATE TABLE item_keywords (" +
                                    "  itemID BLOB NOT NULL," +
                                    "  keywordID INT NOT NULL," +
                                    "  FOREIGN KEY (itemID) REFERENCES items(itemID)," +
                                    "  FOREIGN KEY (keywordID) REFERENCES keywords(keywordID)" +
                                    ")";
            statement.execute(itemKeywordMap);
            log.info("Created item-keyword map table!");

            statement.execute("CREATE UNIQUE INDEX username ON users (username)");
            statement.execute("CREATE UNIQUE INDEX userID ON items (userID)");
            log.info("Created indexes!");

            statement.close();
        } catch (SQLException e) {
            log.debug(e);
            throw new OperationFailureException("Failed to create initial database: " + e.getMessage());
        }

    }

    //endregion

    //region Saving
    /**
     * Saves user data to disk
     */
    private void saveData(){
        log.info("Saving data...");
        try {
            this.connection.close();
        } catch (SQLException e) {
            log.debug(e);
            log.warn("Failed to close connection. {}", e.getMessage());
        }
        log.info("Data saved!");
    }
    //endregion

    public UUID addUser(RegisterUser user) throws OperationFailureException {
        if(this.usernameToUUID.containsKey(user.getUsername())){
            throw new OperationFailureException("Username already exists.");
        }

        UUID uniqueID;
        do {
            uniqueID = UUID.randomUUID();
        }
        while(this.users.keySet().contains(uniqueID));
        char[] password = user.getPassword();
        try {
            byte[] salt = User.getRandomSalt();
            byte[] passwordHash = User.generatePasswordHash(password, salt);
            User newUser = new User(uniqueID, user.getUsername(), user.getFirstName(), user.getLastName(),passwordHash, salt);
            this.users.put(uniqueID, newUser);
            this.usernameToUUID.put(newUser.getUsername(), newUser.getUniqueID());
            return uniqueID;
        } catch (UnsupportedSecurityException shouldNeverEverHappen) {
            log.debug(shouldNeverEverHappen);
            Server.getServer().shutdownServer();
            throw new OperationFailureException("Failed to add new user, server does meet the security requirements.");
        }
    }

    public User getUser(UUID uniqueID) throws  NoSuchElementException {
        if(this.users.containsKey(uniqueID)) {
            return this.users.get(uniqueID);
        } else {
            throw new NoSuchElementException();
        }
    }

    public User getUser(String username) throws  NoSuchElementException {
        if(this.usernameToUUID.containsKey(username)) {
            return this.users.get(this.usernameToUUID.get(username));
        } else {
            throw new NoSuchElementException();
        }
    }
}
