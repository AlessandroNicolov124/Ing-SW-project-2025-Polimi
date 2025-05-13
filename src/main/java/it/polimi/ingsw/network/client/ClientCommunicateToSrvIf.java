package it.polimi.ingsw.network.client;

public interface ClientCommunicateToSrvIf extends AutoCloseable {

    int addPlayer(String username) throws Exception;
    
}
