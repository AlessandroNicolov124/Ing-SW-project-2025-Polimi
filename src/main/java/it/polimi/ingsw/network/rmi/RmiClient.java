package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.client.GeneralClientIf;
import it.polimi.ingsw.network.socket.messages.RotationType;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.rmi.ConnectException;




// This class is a RMI client implementation.
// It takes care of user interactions and showing the current state of the 
// application to the user.
// It needs a reference to the server stub to send user input.
// Also there will be the need to synchronize the state of the client with the server
// because for example while the run methos is executing, an update may come from the server
// causing a data race which interferes with the user input.

// Asynchronous client stub for the RMI server.
public class RmiClient extends UnicastRemoteObject implements GeneralClientIf {
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
        System.out.println("[Server] " + message);
    }

    @Override
    public void validateShip() throws Exception {

    }

    @Override
    public void rotateComponent(Position position, RotationType rotationType) throws Exception {

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

    public void placeComponent(Component component, Position position) throws IOException {

    }

    @Override
    public void close() throws Exception {
        UnicastRemoteObject.unexportObject(this, true);
    }

}