COMP1206 Assigment 2 - A Lightweight Auction System
Huw C B Jones (hcbj1g15)
====================================================================================================================================================
=             _      _       _     _                _       _     _                        _   _                _____           _                  =
=     /\     | |    (_)     | |   | |              (_)     | |   | |       /\             | | (_)              / ____|         | |                 =
=    /  \    | |     _  __ _| |__ | |___      _____ _  __ _| |__ | |_     /  \  _   _  ___| |_ _  ___  _ __   | (___  _   _ ___| |_ ___ _ __ ___   =
=   / /\ \   | |    | |/ _` | '_ \| __\ \ /\ / / _ \ |/ _` | '_ \| __|   / /\ \| | | |/ __| __| |/ _ \| '_ \   \___ \| | | / __| __/ _ \ '_ ` _ \  =
=  / ____ \  | |____| | (_| | | | | |_ \ V  V /  __/ | (_| | | | | |_   / ____ \ |_| | (__| |_| | (_) | | | |  ____) | |_| \__ \ ||  __/ | | | | | =
= /_/    \_\ |______|_|\__, |_| |_|\__| \_/\_/ \___|_|\__, |_| |_|\__| /_/    \_\__,_|\___|\__|_|\___/|_| |_| |_____/ \__, |___/\__\___|_| |_| |_| =
=                       __/ |                          __/ |                                                           __/ |                       =
=                      |___/                          |___/                                                           |___/                        =
====================================================================================================================================================

COMPILATION INSTRUCTIONS:
	My application uses 3 third party libraries.
		- json-simple
			For saving and loading config files using the JSON format.
		- log4j2
			For providing custom logging features.
			This allows me to provide more sophisticated logging and helps to debug as debugging multithreaded applications is very tricky
		- log4j-staticshutdown
		    Allows log4j to be shutdown after my application has finished. This was added to prevent a bug where the full shutdown sequence
		    would not be logged because Log4j2 shutdown before my application had finished shutting down.
		- jdatepicker
		    Swing Date Picker implementation. I thought it was better to not reinvent the wheel here.
		- sqlite-jdbc
		    SQLite Java DataBase Connection library.
		    This provides the JDBC libraries to use SQLite databases.
		    As SQLite is file based, this ensures my application can save data to files whilst also maintaining SQL features.
		- hamcrest-core
		- junit-4
		    Maven added these dependencies as I have written some (not enough) unit tests for my code.

    To compile and run, execute the following commands in the base directory of my submission. (Note: change {$OUTPUT_DIR} to where you wish the class files to be generated).
	The base directory of my submission is the directory that contains pom.xml.
	
		COMPILE:
		    Windows:
		        javac -cp "src/main/java;lib/*" -d {$OUTPUT_DIR} src/main/java/*.java
		    OS X/Unix:
		        javac -cp "src/main/java:lib/*" -d {$OUTPUT_DIR} src/main/java/*.java
		
		COPY DEPENDENCIES:
			The contents of "src/main/resources/" needs to be copied to {$OUTPUT_DIR}/. This copies the config files, image resources, log4j2 config file (log4j2.xml) and keystore to the output path.
			
		TO RUN:
		    Where {$CLASSNAME} is one of the following:
		        - BiddrClient       The Auction Server Client
		        - BiddrServer       The Auction Server GUI
		        - biddrd            Headless Auction Server (Unix type daemon service)
		    Windows:
		        java -cp "{$OUTPUT_DIR};lib/*" {$CLASSNAME}
		    OS X/Unix:
            	java -cp "{$OUTPUT_DIR}:lib/*" {$CLASSNAME}
		
	Alternatively, if you have maven, run "mvn package" in the base directory of my submission. This will create 3 executable jar's and copy the required dependencies to lib/*.
	This allows you to start the:
	- Auction Server Client with    "java -jar BiddrClient.jar";
	- Auction Server GUI with       "java -jar BiddrServer.jar";
	- Headless Auction Server with  "java -jar biddrd.jar";
	It also means you don't have to copy the keystore, log4j2 config file (log4j2.xml) or image resources to the output directory.

  ______      _                 _
 |  ____|    | |               (_)
 | |__  __  _| |_ ___ _ __  ___ _  ___  _ __  ___
 |  __| \ \/ / __/ _ \ '_ \/ __| |/ _ \| '_ \/ __|
 | |____ >  <| ||  __/ | | \__ \ | (_) | | | \__ \
 |______/_/\_\\__\___|_| |_|___/_|\___/|_| |_|___/

 1) Use of sockets. This (in my opinion) is far superior to a "file based" system and allows for point (4).
 2) Use of SSL (well, it's deprecated, so TLS) sockets if server has secure sockets enabled in its config.
    Connections to a server that supports secure connections will automatically be upgraded.
    After consulting advice about SSL/TLS and securing connections (namely: https://www.ssllabs.com/downloads/SSL_TLS_Deployment_Best_Practices.pdf), the server only uses:
    TLS 1/1.1/1.2, and secure cipher suites.
    The client is set to use all available protocols/cipher suites, so has no problem connecting to the server on a secure connection.
 3) Extensive use of multithreading.
    Shared:
    - Comms class has a read thread, and a write thread. Prevents locking up and means code can queue a message to be sent carry on, then wait for the reply when/if it comes back and handle appropriately.
    Server Side:
    - Multiple clients can connect at once (using a ServerSocket, instead of a Socket).
    - Each client runs in its own thread. Prevents clients locking each other up.
    - ServerSockets listen on two different thread. Allows simultaneous secure/insecure connections.
    - Worker pool for running tasks. Allows tasks queued to run immediately, or scheduled to run at a later date (through the use of a ScheduledExecutorService).
      Also allows for tasks such as auction closed to be handled on a separate thread, rather than blocking the main server thread and preventing clients connecting.
 4) Extensive use of event based programming. This means instead of manually having to call receiveMessage every time I want to fetch a message from the Comms class,
    an event listener can be attached on the Comms class and the relevant code will be executed when a message comes in.
    Also implemented is a ConnectionListener which listens to the server/client connection and allows for listening to ConnectionSucceeded, ConnectionFailed, and ConnectionClosed.
    Auction listener for AuctionStart, AuctionEnd, AuctionBid events.
    LoginListener for handling user login and logout events.
 5) Server/client ping-pong keep alive. A ping message is sent between server/client to ensure the connection is maintained. If a packet is not received in x time,
    the connection is declared to have been closed and the ConnectionClosed event is raised.
 6) Uses an SQLite database instead of a custom storage format. This means the data file is standardised and the DataPersistence class could be ported to run on any database implementation easily.
    All the developer has to do is change the JDBC driver (and tweak queries for the SQL dialect subset for the RDBMS driver).
    In addition, this allows the power of SQL to be used.
 7) Memory cache for Users/Items in front of the database.
    This allows changes to be made instantly to Objects in memory and then written back to the database (file system) at a later date.
    It also allows the server to be more responsive to client requests, then queue the write-back to the database later.

 
 
 THIRD PARTY LIBRARIES:
 json-simple: https://code.google.com/archive/p/json-simple/
 sqlite-jdbc: https://bitbucket.org/xerial/sqlite-jdbc
 jdatepicker: https://jdatepicker.org
