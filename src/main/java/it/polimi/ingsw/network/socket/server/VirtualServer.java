package it.polimi.ingsw.network.socket.server;

import java.io.IOException;

import it.polimi.ingsw.network.client.ClientCommunicateToSrvIf;

public interface VirtualServer extends ClientCommunicateToSrvIf {

    @Override
    int addPlayer(String username) throws IOException;

}