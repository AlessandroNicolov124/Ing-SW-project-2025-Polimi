package it.polimi.ingsw.network;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.network.rmi.RmiServer;
import it.polimi.ingsw.network.socket.server.SocketServer;

public class GeneralServer {
    private GameController controller;
    private RmiServer rmiServer;
    private SocketServer socketServer;

    public GeneralServer()
    {
        //controller = new GameController();
        //this.rmiServer = new RmiServer();
        this.socketServer = new SocketServer(7777);
    }

    public void serverRun()
    {
        new Thread(this.socketServer).start();
        //new Thread(this.rmiServer).start();
    }
}
