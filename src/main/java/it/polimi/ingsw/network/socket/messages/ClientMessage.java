package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.network.socket.client.SocketClient;

public interface ClientMessage extends GenericMessage {

    void handleClient(SocketClient client);
    
}