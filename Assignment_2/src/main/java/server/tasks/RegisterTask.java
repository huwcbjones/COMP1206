package server.tasks;

import server.ClientConnection;
import server.Server;
import server.exceptions.OperationFailureException;
import shared.Packet;
import shared.PacketType;
import shared.RegisterUser;
import shared.exceptions.ValidationFailedException;

import java.util.UUID;

/**
 * Processes user registration
 *
 * @author Huw Jones
 * @since 08/04/2016
 */
public class RegisterTask extends Task {

    private RegisterUser user;

    public RegisterTask(ClientConnection client, RegisterUser user) {
        super("RegisterUser", client);
        this.user = user;
    }

    @Override
    public void run() {
        try{
            validateFields();
            UUID newUser = Server.getData().addUser(this.user);
            this.client.sendPacket(new Packet<>(PacketType.REGISTER_SUCCESS, Server.getData().getUser(newUser)));
        } catch (ValidationFailedException | OperationFailureException e) {
            this.client.sendPacket(new Packet<>(PacketType.REGISTER_FAIL, e.getMessage()));
        }
    }

    private void validateFields() throws ValidationFailedException {

    }
}
