package it.polimi.ingsw;

import it.polimi.ingsw.network.GeneralServer;

import java.io.IOException;


public class App {
    public static void main(String[] args) throws IOException {
        String host = "127.0.0.1";
        int rmiPort = 1099;
        int socketPort = 7777;

        if (args.length >= 1) 
            host = args[0];
        if (args.length >= 2)
            rmiPort = Integer.parseInt(args[1]); 
        if (args.length >= 3)
            socketPort = Integer.parseInt(args[2]);

        GeneralServer srv = new GeneralServer(host, rmiPort, socketPort);

    }

}