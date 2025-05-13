package it.polimi.ingsw.network.socket.client;

import java.io.IOException;


public interface VirtualView {

    void showUpdate(String message) throws IOException;
}