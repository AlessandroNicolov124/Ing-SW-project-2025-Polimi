package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.network.socket.client.SocketClient;

/**
 * Represents a message sent from the server to the client.
 */
public interface ClientMessage extends GenericMessage {

    void handleClient(SocketClient client);
    
}