package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sqlite.SQLiteConfig;
import org.sqlite.javax.SQLiteConnectionPoolDataSource;
import org.sqlite.javax.SQLitePooledConnection;
import server.exceptions.OperationFailureException;
import server.objects.Item;
import server.objects.User;
import server.tasks.AuctionEndTask;
import server.tasks.AuctionStartTask;
import shared.Bid;
import shared.ItemBuilder;
import shared.Keyword;
import shared.utils.UUIDUtils;

import java.io.*;
import java.sql.*;
import java.util.*;
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

    private final ConcurrentHashMap<UUID, Item> items = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Keyword> keywords = new ConcurrentHashMap<>();

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
            this.dataSource = (SQLitePooledConnection) ds.getPooledConnection();

        } catch (SQLException e) {
            log.trace(e);
            throw new OperationFailureException("Failed to load database driver.");
        }

        if (shouldCreateInitialDB) {
            this.createDatabaseStructure();
        }
        this.loadServer();
        log.info("Loaded data!");
    }

    /**
     * Creates the initial database structure
     *
     * @throws OperationFailureException If the operation failed
     */
    private void createDatabaseStructure() throws OperationFailureException {
        Connection c = null;
        Statement statement = null;
        try {
            c = this.getConnection();
            statement = c.createStatement();

            String usersTable = "CREATE TABLE users (" +
                "  userID BLOB PRIMARY KEY NOT NULL," +
                "  username VARCHAR(64) NOT NULL UNIQUE," +
                "  firstName VARCHAR(64) NOT NULL," +
                "  lastName VARCHAR(64) NOT NULL," +
                "  password BLOB NOT NULL" +
                ")";
            statement.execute(usersTable);
            log.info("Created users table!");

            String keywordsTable = "CREATE TABLE keywords (" +
                "  keywordID INTEGER PRIMARY KEY NOT NULL," +
                "  keyword VARCHAR(140) NOT NULL UNIQUE" +
                ")";
            statement.execute(keywordsTable);
            log.info("Created keywords table!");

            String itemsTable = "CREATE TABLE items (" +
                "  itemID BLOB PRIMARY KEY NOT NULL," +
                "  userID BLOB NOT NULL," +
                "  title VARCHAR(140) NOT NULL," +
                "  description TEXT NOT NULL," +
                "  startTime DATETIME NOT NULL," +
                "  endTime DATETIME NOT NULL," +
                "  reservePrice DECIMAL(9, 2)," +
                "  FOREIGN KEY (userID) REFERENCES users(userID)" +
                ")";
            statement.execute(itemsTable);
            log.info("Created items table!");

            String bidsTable = "CREATE TABLE bids (" +
                "  bidID BLOB PRIMARY KEY NOT NULL, " +
                "  itemID BLOB NOT NULL," +
                "  userID BLOB NOT NULL," +
                "  price INTEGER NOT NULL," +
                "  time DATETIME NOT NULL," +
                "  FOREIGN KEY (itemID) REFERENCES items(itemID)," +
                "  FOREIGN KEY (userID) REFERENCES users(userID)" +
                ")";
            statement.execute(bidsTable);
            log.info("Created bids table!");

            String itemKeywordMap = "CREATE TABLE item_keywords (" +
                "  itemID BLOB NOT NULL," +
                "  keywordID INTEGER NOT NULL," +
                "  FOREIGN KEY (itemID) REFERENCES items(itemID)," +
                "  FOREIGN KEY (keywordID) REFERENCES keywords(keywordID)" +
                ")";
            statement.execute(itemKeywordMap);
            log.info("Created item-keyword map table!");

            statement.execute("CREATE UNIQUE INDEX user_username ON users (username)");
            statement.execute("CREATE INDEX item_userID ON items (userID)");
            statement.execute("CREATE UNIQUE INDEX itemKeywordIndex ON item_keywords (itemID, keywordID)");
            log.info("Created indexes!");

            try {
                log.info("Adding initial keywords...");
                Set<String> keywords = this.getKeywordsFromFile();
                c.setAutoCommit(false);
                PreparedStatement insert = c.prepareStatement("INSERT INTO keywords (keyword) VALUES (?)");
                for (String keyword : keywords) {
                    insert.setString(1, keyword);
                    insert.addBatch();
                }
                ArrayList<Integer> result = new ArrayList<>();
                for (int r : insert.executeBatch()) {
                    result.add(r);
                }
                c.commit();
                long numInserts = result.size();
                long numSuccess = numInserts - result.parallelStream().filter(i -> i == PreparedStatement.EXECUTE_FAILED).count();
                log.info("Added {}/{} keyword(s) to the database.", numSuccess, numInserts);
            } catch (OperationFailureException e) {
                log.warn("Failed to load keywords: {}", e.getMessage());
            }
        } catch (SQLException e) {
            log.catching(e);
            throw new OperationFailureException("Failed to create initial database: " + e.getMessage());
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (c != null) {
                    c.setAutoCommit(true);
                    c.close();
                }
            } catch (SQLException suppress) {
                log.trace(suppress);
            }
        }
    }

    /**
     * Loads the list of keywords into the database
     *
     * @return List of Keywords
     */
    private Set<String> getKeywordsFromFile() throws OperationFailureException {
        InputStream inputStream = this.getClass().getResourceAsStream("/keywords.txt");
        if (inputStream == null) {
            throw new OperationFailureException("File not could not be opened.");
        }
        HashSet<String> keywords = new HashSet<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                // Ignore comments
                if (line.substring(0, 1).equals("#")) {
                    continue;
                }
                // Truncate keywords longer than 140
                if (line.length() > 140) {
                    keywords.add(line.substring(0, 140));
                } else {
                    keywords.add(line);
                }
            }
        } catch (IOException e) {
            log.catching(e);
            throw new OperationFailureException(e.getMessage());
        }
        return keywords;
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
                log.info("Loaded User({}) to memory.", newUser.getUniqueID());
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

    /**
     * Loads items with itemID into memory
     *
     * @param query_itemID ItemID of item
     * @throws OperationFailureException If the operation failed
     */
    public void loadItem(UUID query_itemID) throws OperationFailureException {
        if(this.items.containsKey(query_itemID)){
            log.debug("Item({}) already in memory.", query_itemID);
            return;
        }
        log.debug("Querying database for Item({})", query_itemID);
        String selectItemSql = "SELECT itemID, userID, title, description, startTime, endTime, reservePrice FROM items WHERE itemID=?";
        String selectBidSql = "SELECT bidID, userID, price, time FROM bids WHERE itemID=?";
        Connection c = null;
        PreparedStatement selectItemQuery = null;
        PreparedStatement selectBidQuery = null;
        try {
            c = this.getConnection();

            selectItemQuery = c.prepareStatement(selectItemSql);
            selectBidQuery = c.prepareStatement(selectBidSql);

            selectItemQuery.setBytes(1, UUIDUtils.UUIDToBytes(query_itemID));
            ResultSet itemResults = selectItemQuery.executeQuery();

            ItemBuilder ib;
            Item newItem;
            Bid newBid;
            ResultSet bidResults;

            while (itemResults.next()) {
                ib = Item.createBuilder();
                ib
                    .setID(query_itemID)
                    .setUserID(UUIDUtils.BytesToUUID(itemResults.getBytes("userID")))
                    .setTitle(itemResults.getString("title"))
                    .setDescription(itemResults.getString("description"))
                    .setStartTime(new Timestamp(itemResults.getLong("startTime") * 1000L))
                    .setEndTime(new Timestamp(itemResults.getLong("endTime") * 1000L))
                    .setReservePrice(itemResults.getBigDecimal("reservePrice"));

                selectBidQuery.setBytes(1, UUIDUtils.UUIDToBytes(query_itemID));
                bidResults = selectBidQuery.executeQuery();

                while (bidResults.next()) {
                    newBid = new Bid(
                        query_itemID,
                        UUIDUtils.BytesToUUID(bidResults.getBytes("itemID")),
                        UUIDUtils.BytesToUUID(bidResults.getBytes("userID")),
                        bidResults.getBigDecimal("bidPrice"),
                        bidResults.getTimestamp("time")
                    );
                    ib.addBid(newBid);
                }

                newItem = Item.createServerItem(ib.getItem());
                this.items.put(newItem.getID(), newItem);

                // If auction hasn't ended, queue start/end tasks
                if(!newItem.isAuctionEnded()) {
                    Server.getWorkerPool().scheduleTask(new AuctionStartTask(null, newItem.getID()), newItem.getTimeUntilStart());
                    Server.getWorkerPool().scheduleTask(new AuctionEndTask(null, newItem.getID()), newItem.getTimeUntilEnd());
                }
                log.info("Loaded Item({}) to memory.", newItem.getID());
            }
            itemResults.close();
        } catch (SQLException e) {
            log.debug("SQLState: {}", e.getSQLState());
            log.debug("Error Code: {}", e.getErrorCode());
            log.debug("Message: {}", e.getMessage());
            log.debug("Cause: {}", e.getCause());
            log.error("Failed to load user.", e);
        } finally {
            try {
                if (selectItemQuery != null) {
                    selectItemQuery.close();
                }
                if (selectBidQuery != null) {
                    selectBidQuery.close();
                }
                if (c != null) {
                    c.close();
                }
            } catch (SQLException suppress) {
                log.trace(suppress);
            }
        }
    }

    private void loadKeywords() {
        log.debug("Loading keywords...");
        int initialSize = this.keywords.size();
        String selectKeywordSql = "SELECT keywordID, keyword FROM keywords";
        Connection c = null;
        PreparedStatement selectKeyword = null;
        try {
            c = this.getConnection();

            selectKeyword = c.prepareStatement(selectKeywordSql);

            ResultSet keywordResults = selectKeyword.executeQuery();
            Keyword newKeyword;
            while (keywordResults.next()) {
                newKeyword = new Keyword(keywordResults.getInt("keywordID"), keywordResults.getString("keyword"));
                this.keywords.put(newKeyword.getKeywordID(), newKeyword);
            }
            keywordResults.close();
        } catch (SQLException e) {
            log.debug("SQLState: {}", e.getSQLState());
            log.debug("Error Code: {}", e.getErrorCode());
            log.debug("Message: {}", e.getMessage());
            log.debug("Cause: {}", e.getCause());
            log.error("Failed to load user.", e);
        } finally {
            try {
                if (selectKeyword != null) {
                    selectKeyword.close();
                }
                if (c != null) {
                    c.close();
                }
            } catch (SQLException suppress) {
                log.trace(suppress);
            }
        }

        log.info("Loaded {} keyword(s).", (this.keywords.size() - initialSize));
    }

    /**
     * Loads all the data into the server and starts tasks
     *
     * @throws OperationFailureException
     */
    private void loadServer() throws OperationFailureException {
        ArrayList<UUID> itemIDs = getCurrentItems(true);
        log.info("Processing {} items...", itemIDs.size());
        for (UUID itemID : itemIDs) {
            this.loadItem(itemID);
        }
        this.loadKeywords();
    }

    /**
     * Gets an ArrayList of item UUIDs where the Item is currently in Auction
     *
     * @param includePreAuction If true, the function will also return items that have yet to start their auction.
     * @return UUIDs of Items in Auction
     * @throws OperationFailureException If the operation failed
     */
    private ArrayList<UUID> getCurrentItems(boolean includePreAuction) throws OperationFailureException {
        log.debug("Querying database for all 'in-auction' items");
        String selectItemSql;
        if (includePreAuction) {
            selectItemSql = "SELECT itemID, endTime FROM items WHERE CAST(strftime('%s', 'now') AS INT) < endTime";
        } else {
            selectItemSql = "SELECT itemID FROM items WHERE CAST(strftime('%s', 'now') AS INT) BETWEEN startTime AND endTime";
        }
        Connection c = null;
        PreparedStatement selectItemQuery = null;
        ArrayList<UUID> itemIDs = new ArrayList<>();

        try {
            c = this.getConnection();
            selectItemQuery = c.prepareStatement(selectItemSql);
            ResultSet itemResults = selectItemQuery.executeQuery();

            while (itemResults.next()) {
                itemIDs.add(UUIDUtils.BytesToUUID(itemResults.getBytes("itemID")));
            }
            itemResults.close();
        } catch (SQLException e) {
            log.debug("SQLState: {}", e.getSQLState());
            log.debug("Error Code: {}", e.getErrorCode());
            log.debug("Message: {}", e.getMessage());
            log.debug("Cause: {}", e.getCause());
            log.error("Failed to load user.", e);
        } finally {
            try {
                if (selectItemQuery != null) {
                    selectItemQuery.close();
                }
                if (c != null) {
                    c.close();
                }
            } catch (SQLException suppress) {
                log.trace(suppress);
            }
        }
        return itemIDs;
    }
    //endregion

    //region Saving

    /**
     * Saves user data to disk
     */
    public void saveData() {
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
     * Retrieves a set of keywords
     * @return HashSet of keywords
     */
    public HashSet<Keyword> getKeywords() {
        return new HashSet<>(this.keywords.values());
    }

    /**
     * Retrieves a user with the given username
     *
     * @param username Username of user
     * @return The user
     * @throws NoSuchElementException If the user does not exist
     */
    public User getUser(String username) throws NoSuchElementException {
        username = username.toLowerCase();
        if (this.usernameToUUID.containsKey(username)) {
            return this.users.get(this.usernameToUUID.get(username));
        }
        try {
            this.loadUser(null, username);
            if (!this.usernameToUUID.containsKey(username)) {
                throw new NoSuchElementException();
            }

            User user = this.users.get(this.usernameToUUID.get(username));
            if (user == null) {
                throw new NoSuchElementException();
            }
            return user;
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
                User user = this.users.get(uniqueID);
                if (user == null) {
                    throw new NoSuchElementException();
                }
                return user;
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
        if (this.users.containsKey(userID)) {
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
        username = username.toLowerCase();
        if (this.usernameToUUID.containsKey(username)) {
            return true;
        }
        try {
            this.loadUser(null, username);
        } catch (OperationFailureException suppress) {
            return false;
        }
        return this.usernameToUUID.containsKey(username);
    }

    /**
     * Returns whether an item exists or not
     *
     * @param itemID UUID of item
     * @return true if item exists
     */
    public boolean itemExists(UUID itemID) {
        if (this.items.containsKey(itemID)) {
            return true;
        }
        try {
            this.loadItem(itemID);
        } catch (OperationFailureException e) {
            return false;
        }
        return this.items.containsKey(itemID);
    }

    /**
     * Retrieves an item with the given itemID
     *
     * @param itemID ID of the Item
     * @return Item
     * @throws NoSuchElementException If the item does not exist
     */
    public Item getItem(UUID itemID) throws NoSuchElementException {
        if (this.items.containsKey(itemID)) {
            return this.items.get(itemID);
        } else {
            try {
                this.loadItem(itemID);
                Item item = this.items.get(itemID);
                if (item == null) {
                    throw new NoSuchElementException();
                }
                return item;
            } catch (OperationFailureException e) {
                throw new NoSuchElementException();
            }
        }
    }
}
