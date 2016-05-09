package server.tasks;

import server.ClientConnection;
import server.Server;
import shared.Keyword;
import shared.Packet;
import shared.PacketType;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Sends a list of keywords to the client
 *
 * @author Huw Jones
 * @since 04/05/2016
 */
public class KeywordTask extends Task {

    public KeywordTask(ClientConnection client) {
        super("KeywordTask", client);
    }

    /**
     * Executed on task failure
     */
    @Override
    protected void failureAction() {
        this.client.sendPacket(new Packet<>(PacketType.KEYWORDS, new ArrayList<>()));
    }

    @Override
    public void runSafe() throws Exception {
        HashSet<Keyword> keywords = Server.getData().getKeywords();
        this.client.sendPacket(new Packet<>(PacketType.KEYWORDS, keywords.toArray(new Keyword[keywords.size()])));
    }
}
