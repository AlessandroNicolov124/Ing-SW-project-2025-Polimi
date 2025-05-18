package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.socket.messages.RotationType;
import it.polimi.ingsw.network.client.RmiClientIf;
import it.polimi.ingsw.network.client.listeners.ShipBuildingListener;
import it.polimi.ingsw.network.rmi.messages.ComponentPlacedMessage;
import it.polimi.ingsw.network.rmi.messages.UpdateMessage;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


// Asynchronous client stub for the RMI server.
public class RmiClient extends UnicastRemoteObject implements RmiClientIf {
    private final VirtualServer server;
    private final BlockingQueue<UpdateMessage> incoming = new LinkedBlockingQueue<>();
    private int playerId = -1;
    private Player localPlayerReference;
    private ShipBuildingListener listener;

    private RmiClient(VirtualServer server, ShipBuildingListener sbl) throws RemoteException {
        super();
        this.server = server;
        this.listener = sbl;
        server.connect(this); // register this client on the server
        startConsumer(); // thread which prints incoming messages on the TUI
    }

    public static RmiClient connectTo(String host, ShipBuildingListener sbl) throws Exception {
        VirtualServer srv = (VirtualServer) Naming.lookup("//" + host + ":1099/GalaxyTruckerServer");
        return new RmiClient(srv, sbl);
    }

    @Override
    public void showUpdate(String message) throws RemoteException {
        //incoming.offer(message);    // put the message in the queue without verifying if it is full
                                    // if the queue is full, the message will be discarded 
        //System.out.println("[Server] " + message);
    }

    @Override
    public void showUpdate(UpdateMessage msg) throws RemoteException {
        incoming.offer(msg);
    }

    // Consumer thread which takes messages from the queue and prints them on the TUI
    private void startConsumer() {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    UpdateMessage msg = incoming.take();

                    if (msg instanceof ComponentPlacedMessage m
                            && m.playerID() == playerId && m.success()) {
                        localPlayerReference.getShip().placeComponent(m.component(), m.position());
                    }

                    msg.update(listener, playerId);
                }
            } catch (InterruptedException ignored) { }
        }, "TUI-Update");
        t.setDaemon(true);
        t.start();
    }
  
    public Player getPlayer() throws RemoteException {
        return localPlayerReference;
    }

    @Override
    public int addPlayer(String username) throws RemoteException {
        playerId = server.sendSrvAddPlayerReqAndWaitResp(username);
        return playerId;
    }

    @Override
    public Component pickComponentCard() throws RemoteException {
        return server.pickComponent(playerId);
    }

    @Override
    public void placeComponent(Component component, Position position) throws RemoteException {
        server.placeComponent(playerId, component, position);
    }

    @Override
    public void validateShip() throws RemoteException {

    }

    @Override
    public void rotateComponent(Position position, RotationType rotationType) throws RemoteException {

    }

    @Override
    public void setLocalPlayer(Player player) {
        this.localPlayerReference = player;
    }

    @Override
    public void close() throws Exception {
        UnicastRemoteObject.unexportObject(this, true);
    }

}