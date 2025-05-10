package it.polimi.ingsw.network.socket.server;

import java.io.IOException;

import it.polimi.ingsw.network.client.GameClient;

public interface VirtualServer extends GameClient {

    @Override
    int addPlayer(String username) throws IOException;

}