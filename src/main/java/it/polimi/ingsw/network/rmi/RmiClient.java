package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.socket.messages.RotationType;
import it.polimi.ingsw.network.client.RmiClientIf;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;



// This class is a RMI client implementation.
// It takes care of user interactions and showing the current state of the 
// application to the user.
// It needs a reference to the server stub to send user input.
// Also there will be the need to synchronize the state of the client with the server
// because for example while the run methos is executing, an update may come from the server
// causing a data race which interferes with the user input.

// Asynchronous client stub for the RMI server.
public class RmiClient extends UnicastRemoteObject implements RmiClientIf {
    private final VirtualServer server;
    private final BlockingQueue<String> incoming = new LinkedBlockingQueue<>();
    private int playerId = -1;

    private RmiClient(VirtualServer server) throws RemoteException {
        super();
        this.server = server;
        server.connect(this); // register this client on the server
        startConsumer(); // thread which prints incoming messages on the TUI
    }

    public static RmiClient connectTo(String host) throws Exception {
        VirtualServer srv = (VirtualServer) Naming.lookup("//" + host + ":1099/GalaxyTruckerServer");
        return new RmiClient(srv);
    }

    @Override
    public void showUpdate(String message) throws RemoteException {
        incoming.offer(message);    // put the message in the queue without verifying if it is full
                                    // if the queue is full, the message will be discarded 
        System.out.println("[Server] " + message);
    }

    @Override
    public void validateShip() throws RemoteException {

    }

    @Override
    public void rotateComponent(Position position, RotationType rotationType) throws RemoteException {

    }

    // Consumer thread which takes messages from the queue and prints them on the TUI
    private void startConsumer() {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    incoming.take();
                }
            } catch (InterruptedException ignored) { }
        }, "TUI-Update");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public int addPlayer(String username) throws RemoteException {
        playerId = server.sendSrvAddPlayerReqAndWaitResp(username);
        return playerId;
    }

    @Override
    public void placeComponent(Component component, Position position) throws RemoteException {
        if (playerId == -1) {
            throw new IllegalStateException("Cannot place component before player ID is assigned.");
        }
        server.placeComponent(playerId, component, position);

    }

    @Override
    public void setLocalPlayer(Player player) {

    }

    @Override
    public Component pickComponentCard() throws Exception {
        return null;
    }

    @Override
    public void close() throws Exception {
        UnicastRemoteObject.unexportObject(this, true);
    }

}