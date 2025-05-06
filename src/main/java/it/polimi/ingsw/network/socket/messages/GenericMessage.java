package it.polimi.ingsw.network.socket.messages;

import java.io.Serializable;

import it.polimi.ingsw.network.socket.client.SocketClient;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;


public interface GenericMessage extends Serializable {

    default void handleClient(SocketClient c) throws Exception { 

    }
    
    default void handleServer(SocketClientHandler h) throws Exception { 

    }  
}