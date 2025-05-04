package it.polimi.ingsw.network.rmi;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.rmi.ConnectException;

import it.polimi.ingsw.network.client.GameClient;;


// This class is a RMI client implementation.
// It takes care of user interactions and showing the current state of the 
// application to the user.
// It needs a reference to the server stub to send user input.
// Also there will be the need to synchronize the state of the client with the server
// because for example while the run methos is executing, an update may come from the server
// causing a data race which interferes with the user input.

// Asynchronous client stub for the RMI server.
public class RmiClient extends UnicastRemoteObject implements VirtualView, GameClient {
    private final VirtualServer server;
    private final BlockingQueue<String> incoming = new LinkedBlockingQueue<>();
    private int playerId = -1;

    private RmiClient(VirtualServer server) throws RemoteException {
        super();
        this.server = server;
        server.connect(this); // register this client on the server
        startConsumer(); // thread which prints incoming messages on the TUI
    }

    // Connect to the server using the given host.
    public static RmiClient connectTo(String host) {
        try {
            VirtualServer srv = (VirtualServer) Naming.lookup("//" + host + ":1099/GalaxyTruckerServer");
            return new RmiClient(srv);
        } catch (NotBoundException | ConnectException e) {
            System.err.println("Server unreachable: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error initializing client: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void showUpdate(String message) throws RemoteException {
        incoming.offer(message);    // put the message in the queue without verifying if it is full
                                    // if the queue is full, the message will be discarded 
    }

    // Consumer thread which takes messages from the queue and prints them on the TUI
    private void startConsumer() {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    String msg = incoming.take();
                    System.out.println("[Server] " + msg);
                }
            } catch (InterruptedException ignored) { }
        }, "TUI-Update");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public int addPlayer(String username) throws RemoteException {
        playerId = server.addPlayer(username);
        return playerId;
    }

    @Override
    public void close() throws Exception {
        UnicastRemoteObject.unexportObject(this, true);
    }

}