package it.polimi.ingsw;

import it.polimi.ingsw.network.GeneralServer;

import java.io.IOException;


public class App {
    public static void main(String[] args) throws IOException {

        GeneralServer srv = new GeneralServer();
        srv.serverRun();

    }

}
