package it.polimi.ingsw;

import it.polimi.ingsw.AdventureCards.*;
import it.polimi.ingsw.Components.*;
import it.polimi.ingsw.network.GeneralServer;
import it.polimi.ingsw.network.socket.client.SocketClient;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws IOException {

        GeneralServer srv = new GeneralServer();
        srv.serverRun();

    }

}
