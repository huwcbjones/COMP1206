package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sqlite.SQLiteConfig;
import org.sqlite.javax.SQLiteConnectionPoolDataSource;
import org.sqlite.javax.SQLitePooledConnection;
import server.exceptions.OperationFailureException;
import server.objects.User;
import shared.utils.UUIDUtils;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
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

    private SQLitePooledConnection dataSource;

    //region Loading

    /**
     * Loads data into DataPersistence
     */
    public void loadData(boolean createDatabase) throws OperationFailureException {
        log.info("Loading data...");
        boolean shouldCreateInitialDB = false;
        File dataStore = Server.getConfig().getDataStore();
        if (!dataStore.exists()) {
            if (createDatabase) {
                try {
                    if (!dataStore.createNewFile()) {
                        throw new OperationFailureException("Failed to create datastore. An unspecified error occurred.");
                    } else {
                        shouldCreateInitialDB = true;
                        log.info("Created datastore: {}", dataStore.getAbsolutePath());
                    }
                } catch (IOException e) {
                    log.debug(e);
                    throw new OperationFailureException("Failed to create datastore, " + e.getMessage());
                }
            } else {
                throw new OperationFailureException("Failed to open datastore, file does not exist. To automatically create the database, start the server using the -m or --make-store argument.");
            }
        }

        if (!dataStore.isFile()) {
            throw new OperationFailureException("Failed to open datastore, datastore provided is not a file. " + dataStore.getAbsolutePath());
        }

        if (!dataStore.canWrite()) {
            throw new OperationFailureException("Failed to open datastore, cannot write to datastore. " + dataStore.getAbsolutePath());
        }
        try {
            SQLiteConnectionPoolDataSource ds = new SQLiteConnectionPoolDataSource();
            ds.setUrl("jdbc:sqlite:" + dataStore.getAbsolutePath());

            SQLiteConfig config = new SQLiteConfig();
            config.enableLoadExtension(true);
            config.enforceForeignKeys(true);
            ds.setConfig(config);
            this.dataSource = (SQLitePooledConnection)ds.getPooledConnection();

        } catch (SQLException e) {
            log.trace(e);
            throw new OperationFailureException("Failed to load database driver.");
        }

        if (shouldCreateInitialDB) {
            this.createDatabaseStructure();
        }

        log.info("Loaded data!");
    }

    /**
     * Creates the initial database structure
     *
     * @throws OperationFailureException If the operation faield
     */
    private void createDatabaseStructure() throws OperationFailureException {
        Connection c = null;
        Statement statement = null;
        try {
            c = this.getConnection();
            statement = c.createStatement();

            String usersTable = "CREATE TABLE users (" +
                "  userID BLOB PRIMARY KEY," +
                "  username VARCHAR(255) NOT NULL UNIQUE," +
                "  firstName VARCHAR(255) NOT NULL," +
                "  lastName VARCHAR(255) NOT NULL," +
                "  password BLOB NOT NULL" +
                ")";
            statement.execute(usersTable);
            log.info("Created users table!");

            String keywordsTable = "CREATE TABLE keywords (" +
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

            String bidsTable = "CREATE TABLE bids (" +
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

        } catch (SQLException e) {
            log.debug(e);
            throw new OperationFailureException("Failed to create initial database: " + e.getMessage());
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (c != null) {
                    c.close();
                }
            } catch (SQLException suppress) {
                log.trace(suppress);
            }
        }

    }

    /**
     * Loads users with userID, or username into memory
     *
     * @param query_userID   UserID of user(s)
     * @param query_username Username of user(s)
     * @throws OperationFailureException If the operation failed
     */
    private void loadUser(UUID query_userID, String query_username) throws OperationFailureException {
        log.debug("Querying database for User({})({})", query_userID, query_username);
        String selectQuerySql = "SELECT userID, username, firstName, lastName, password FROM users WHERE userID=? OR username=?";
        Connection c = null;
        PreparedStatement selectQuery = null;
        try {
            c = this.getConnection();

            byte[] query_uuid = UUIDUtils.UUIDToBytes(query_userID);
            selectQuery = c.prepareStatement(selectQuerySql);
            selectQuery.setBytes(1, query_uuid);
            selectQuery.setString(2, query_username);
            ResultSet results = selectQuery.executeQuery();

            UUID userID;
            String username, firstName, lastName;
            byte[] passwordBytes, password, salt;
            User newUser;


            while (results.next()) {
                userID = UUIDUtils.BytesToUUID(results.getBytes("userID"));
                username = results.getString("username");
                firstName = results.getString("firstName");
                lastName = results.getString("lastName");
                passwordBytes = results.getBytes("password");
                password = Arrays.copyOfRange(passwordBytes, 0, 32);
                salt = Arrays.copyOfRange(passwordBytes, 32, 64);
                newUser = new User(
                    userID,
                    username,
                    firstName,
                    lastName,
                    password,
                    salt
                );
                this.users.put(newUser.getUniqueID(), newUser);
                this.usernameToUUID.put(newUser.getUsername(), newUser.getUniqueID());
                log.info("Loaded user to memory.");
            }
            results.close();
        } catch (SQLException e) {
            log.debug("SQLState: {}", e.getSQLState());
            log.debug("Error Code: {}", e.getErrorCode());
            log.debug("Message: {}", e.getMessage());
            log.debug("Cause: {}", e.getCause());
            log.error("Failed to load user.", e);
        } finally {
            try {
                if (selectQuery != null) {
                    selectQuery.close();
                }
                if (c != null) {
                    c.close();
                }
            } catch (SQLException suppress) {
                log.trace(suppress);
            }
        }
    }
    //endregion

    //region Saving

    /**
     * Saves user data to disk
     */
    private void saveData() {
        log.info("Saving data...");
        try {
            this.dataSource.close();
        } catch (SQLException e) {
            log.trace(e);
            log.warn("Error whilst closing data source. {}", e.getMessage());
        }
        log.info("Data saved!");
    }
    //endregion

    public Connection getConnection() throws SQLException {
        Connection c = this.dataSource.getConnection();
        log.trace("Created new connection.");
        return c;
    }

    /**
     * Retrieves a user with the given username
     *
     * @param username Username of user
     * @return The user
     * @throws NoSuchElementException If the user does not exist
     */
    public User getUser(String username) throws NoSuchElementException {
        if (this.usernameToUUID.containsKey(username)) {
            return this.users.get(this.usernameToUUID.get(username));
        }
        try {
            this.loadUser(null, username);
            return this.users.get(this.usernameToUUID.get(username));
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
        throw new NoSuchElementException();
    }

    /**
     * Retrieves a user with the given uniqueID
     *
     * @param uniqueID UniqueID of the user
     * @return User
     * @throws NoSuchElementException If the user does not exist
     */
    public User getUser(UUID uniqueID) throws NoSuchElementException {
        if (this.users.containsKey(uniqueID)) {
            return this.users.get(uniqueID);
        } else {
            try {
                this.loadUser(uniqueID, null);
                return this.users.get(uniqueID);
            } catch (OperationFailureException e) {
                throw new NoSuchElementException();
            }
        }
    }

    /**
     * Returns whether a user exists or not
     *
     * @param userID UserID of user
     * @return true if user exists
     */
    public boolean userExists(UUID userID) {
        if(this.users.containsKey(userID)){
            return true;
        }
        try {
            this.loadUser(userID, null);
        } catch (OperationFailureException suppress) {
            return false;
        }
        return this.users.containsKey(userID);
    }

    /**
     * Returns whether a user exists or not
     *
     * @param username Username of user
     * @return true if user exists
     */
    public boolean userExists(String username) {
        if(this.usernameToUUID.containsKey(username)){
            return true;
        }
        try {
            this.loadUser(null, username);
        } catch (OperationFailureException suppress) {
            return false;
        }
        return this.usernameToUUID.containsKey(username);
    }
}
