package it.polimi.ingsw.network.socket.client;

import it.polimi.ingsw.network.client.GeneralVirtualViewIf;

import java.io.IOException;


public interface VirtualView extends GeneralVirtualViewIf {

    void showUpdate(String message) throws IOException;
}