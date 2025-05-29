package it.polimi.ingsw.network;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.network.rmi.RmiServer;
import it.polimi.ingsw.network.socket.server.SocketServer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class GeneralServer {
    private GameController controller;
    private RmiServer rmiServer;
    private SocketServer socketServer;
    private List<GameMatch> matches; // maybe concurrent list is better?
    // private List<GameMatch> matches = new CopyOnWriteArrayList<>();


    public GeneralServer() throws RemoteException, MalformedURLException {
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        Registry reg = LocateRegistry.createRegistry(1099);
        this.controller = new GameController();
        this.rmiServer = new RmiServer(this.controller, reg);
        Naming.rebind("GalaxyTruckerServer", rmiServer);
        this.socketServer = new SocketServer(7777, this.controller);
    }

    public void serverRun(){
        new Thread(this.socketServer).start();
        new Thread(this.rmiServer).start();
    }


}
