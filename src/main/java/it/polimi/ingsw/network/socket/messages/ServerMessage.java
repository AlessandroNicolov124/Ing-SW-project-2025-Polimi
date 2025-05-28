package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.network.socket.server.SocketClientHandler;

/**
 * Represents a message sent from the client to the server.
 */
public interface ServerMessage extends GenericMessage {

    void handleServer(SocketClientHandler h) throws Exception;

}