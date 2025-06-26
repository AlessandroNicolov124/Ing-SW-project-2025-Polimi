package it.polimi.ingsw.network.messages;

import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public interface RequestMessage extends CommonMessage {
    void handleServer(SocketClientHandler handler);

}
