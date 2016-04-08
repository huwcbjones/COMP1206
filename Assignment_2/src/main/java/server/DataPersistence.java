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

    //region Loading

    /**
     * Loads data into DataPersistence
     */
    public void loadData(boolean createDatabase) throws OperationFailureException {
        log.info("Loading data...");
        Connection c = null;
        File dataStore = Server.getConfig().getDataStore();
        if(!dataStore.exists()){
            if(createDatabase) {
                try {
                    if(!dataStore.createNewFile()){
                        throw new OperationFailureException("Failed to create datastore. An unspecified error occurred.");
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
            c = DriverManager.getConnection("jdbc:sqlite:" + dataStore.getAbsolutePath());
        } catch (SQLException | ClassNotFoundException e) {
            log.debug(e);
            throw new OperationFailureException("Failed to open data file.");
        }


        this.loadUsers();
        // TODO: 08/04/2016 Load more types of data.
    }

    private void loadUsers() {
        log.info("Loading users...");
        // Load users from file

        // Populate HashMap

        log.info("Loaded users!");
    }
    //endregion

    //region Saving
    /**
     * Saves user data to disk
     */
    private void saveUsers(){
        log.info("Saving users...");
        // Iterate over users

        // Save to file

        log.info("Saved users!");
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
