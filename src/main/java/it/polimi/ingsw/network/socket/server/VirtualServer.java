package it.polimi.ingsw.network.socket.server;

import java.io.IOException;

public interface VirtualServer {

    int addPlayer(String username) throws IOException;

    void stopServer() throws IOException;

}