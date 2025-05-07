package it.polimi.ingsw.network.client;

public interface GameClient extends AutoCloseable {

    int addPlayer(String username) throws Exception;
    
}
