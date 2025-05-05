package it.polimi.ingsw.network.socket.messages;

import java.io.IOException;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public interface ServerMessage extends GenericMessage {
    void handleServer(SocketClientHandler h) throws IOException;
}