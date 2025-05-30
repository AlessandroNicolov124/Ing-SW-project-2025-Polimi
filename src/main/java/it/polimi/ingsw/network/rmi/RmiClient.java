package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.socket.messages.RotationType;
import it.polimi.ingsw.network.GameMatch;
import it.polimi.ingsw.network.client.RmiClientIf;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.rmi.messages.UpdateMessage;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


// Asynchronous client stub for the RMI server.
public class RmiClient extends UnicastRemoteObject implements RmiClientIf {
    private final VirtualServer server;
    private final BlockingQueue<UpdateMessage> incoming = new LinkedBlockingQueue<>();
    private int playerId = -1;
    private Player localPlayerReference;
    private GameListener listener;

    private RmiClient(VirtualServer server, GameListener sbl) throws RemoteException {
        super();
        this.server = server;
        this.listener = sbl;
        server.connect(this); // register this client on the server
        startConsumer(); // thread which prints incoming messages on the TUI
    }

    public static RmiClient connectTo(String host, GameListener sbl) throws Exception {
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
                    msg.update(listener, playerId);
                }
            } catch (InterruptedException ignored) { }
        }, "TUI-Update");
        t.setDaemon(true);
        t.start();
    }
  
    @Override
    public int createPlayer(String username) throws RemoteException {
        playerId = server.createPlayer(username);
        localPlayerReference = new Player(playerId, username, true);
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
        server.validateShip(playerId);
    }

    @Override
    public void rotateComponent(Position position, RotationType rotationType) throws RemoteException {
        server.rotateComponent(playerId, position, rotationType);
    }

    @Override
    public void setLocalPlayer(Player player) {
        this.localPlayerReference = player;
    }

    @Override
    public List<String> getAvailableMatches() throws RemoteException {
        return server.getAvailableMatches();
    }

    @Override
    public UUID createMatch(String matchName) throws RemoteException {
        return server.createMatch(matchName, playerId, localPlayerReference.getNickName());
    }

    @Override
    public boolean joinMatch(UUID matchId) throws RemoteException {
        return server.joinMatch(matchId, playerId, localPlayerReference.getNickName());
    }

    @Override
    public boolean leaveMatch(UUID matchId) throws RemoteException {
        return server.leaveMatch(matchId, playerId, localPlayerReference.getNickName());
    }

    @Override
    public boolean startMatch(UUID matchId) throws RemoteException {
        return server.startMatch(matchId, playerId);
    }
     
    @Override
    public Player getLocalPlayer() throws RemoteException {
        return localPlayerReference;
    }

    @Override
    public boolean isPlayerHostById(UUID matchId) throws RemoteException {
        return server.isPlayerHostById(matchId, playerId);
    }

    @Override
    public List<String> getPlayersInMatch(UUID matchId) throws RemoteException {
        return server.getPlayersInMatch(matchId);
    }

    @Override
    public void close() throws Exception {
        UnicastRemoteObject.unexportObject(this, true);
    }

}