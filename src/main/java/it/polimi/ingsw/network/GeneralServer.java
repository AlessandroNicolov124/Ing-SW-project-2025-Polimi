package it.polimi.ingsw.network;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.network.rmi.RmiServer;
import it.polimi.ingsw.network.socket.server.SocketServer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class GeneralServer {
    private GameController controller;
    private RmiServer rmiServer;
    private SocketServer socketServer;

    public GeneralServer() throws RemoteException {
        Registry reg = LocateRegistry.createRegistry(1099);
        this.controller = new GameController();
        this.rmiServer = new RmiServer(this.controller, reg);
        this.socketServer = new SocketServer(7777, this.controller);
    }

    public void serverRun()
    {
        new Thread(this.socketServer).start();
        new Thread(this.rmiServer).start();
    }
}
