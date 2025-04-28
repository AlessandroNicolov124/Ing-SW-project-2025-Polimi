package it.polimi.ingsw.network.socket.server;

import java.io.IOException;
import it.polimi.ingsw.network.socket.client.VirtualView;

public interface VirtualServer {

    void connect(VirtualView client) throws IOException;

    void x() throws IOException;

    void y() throws IOException;

    void stopServer() throws IOException;

}