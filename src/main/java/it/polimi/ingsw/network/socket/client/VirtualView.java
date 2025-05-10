package it.polimi.ingsw.network.socket.client;

import java.io.IOException;

import it.polimi.ingsw.network.client.GameClient;

public interface VirtualView extends GameClient{

    void showUpdate(String message) throws IOException;

    @Override
    int addPlayer(String username) throws IOException;

}