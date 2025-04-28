package it.polimi.ingsw.network.socket.messages;

import java.io.IOException;
import java.io.Serializable;

import it.polimi.ingsw.network.socket.server.SocketClientHandler;


public interface GenericMessage extends Serializable {

    void handle(SocketClientHandler handler) throws IOException;

}