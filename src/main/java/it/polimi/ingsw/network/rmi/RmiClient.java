package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.messages.RotationType;
import it.polimi.ingsw.network.Difficulty;
import it.polimi.ingsw.network.client.RmiClientIf;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


// Asynchronous client stub for the RMI server.
public class RmiClient extends UnicastRemoteObject implements RmiClientIf {
    private final VirtualServer server;
    private final BlockingQueue<ResponseMessage> incoming = new LinkedBlockingQueue<>();
    private GameListener listener;

    private RmiClient(VirtualServer server, GameListener listener) throws RemoteException {
        super();
        this.server = server;
        this.listener = listener;
        server.connect(this); // register this client on the server
        startConsumer(); // thread which prints incoming messages on the TUI
    }

    public static RmiClient connectTo(String host, int registryPort, GameListener sbl) throws Exception {
        VirtualServer srv = (VirtualServer) Naming.lookup("//" + host + ":" + registryPort + "/GalaxyTruckerServer");
        return new RmiClient(srv, sbl);
    }

    @Override
    public void showUpdate(ResponseMessage msg) throws RemoteException {
        incoming.offer(msg);
    }

    // Consumer thread which takes messages from the queue and prints them on the TUI
    private void startConsumer() {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    ResponseMessage msg = incoming.take();
                    msg.handleClient(listener);
                }
            } catch (InterruptedException ignored) { }
        }, "Rmi-Message-Consumer");
        t.setDaemon(true);
        t.start();
    }
  
    @Override
    public void createPlayer(String username) throws RemoteException {
        server.createPlayer(username, this);
    }

    @Override
    public void createMatch(String matchName, int playerId, String username, Difficulty difficulty) throws RemoteException {
        server.createMatch(matchName, playerId, username, difficulty, this);
    }

    @Override
    public void getAvailableMatches() throws RemoteException {
        server.getAvailableMatches(this);
    }

    @Override
    public void joinMatch(UUID matchId, int playerId, String username) throws RemoteException {
        server.joinMatch(matchId, playerId, username, this); 
    }

    @Override
    public void selectCabinColor(String color, int playerId) throws RemoteException {
        server.selectCabinColor(color, playerId, this);
    }

    @Override
    public void leaveMatch(UUID matchId, int playerId, String username) throws RemoteException {
        server.leaveMatch(matchId, playerId, username, this);
    }

    @Override
    public void startMatch(UUID matchId, int playerId, String username) throws RemoteException {
        server.startMatch(matchId, playerId, username, this);
    }

    @Override
    public void loadingAck(UUID matchId) throws RemoteException {
        server.loadingAck(matchId, this);
    }

    @Override
    public void playerReady(UUID matchId, int playerId) throws RemoteException {
        server.playerReady(matchId, playerId, this);
    }

    @Override
    public void close() throws Exception {
        UnicastRemoteObject.unexportObject(this, true);
    }


    @Override
    public void pickComponentCard(int playerId) throws RemoteException {
        server.pickComponent(playerId, this);
    }

    @Override
    public void placeComponent(Component component, Position position, int playerId) throws RemoteException {
        server.placeComponent(playerId, component, position, this);
    }

    @Override
    public void removeComponent(Position position, int playerId) throws RemoteException {
        server.removeComponent(playerId, position, this);
    }

    @Override
    public void rotateComponent(int playerId, Position position, RotationType rotationType) throws RemoteException {
        server.rotateComponent(playerId, position, rotationType, this);
    }


    @Override
    public void validateShip(int playerId) throws RemoteException {
        server.validateShip(playerId, this);
    }

    @Override
    public void readyForFlight(UUID matchId, int playerId) throws RemoteException {
        server.readyForFlight(matchId, playerId, this);
    }

    @Override
    public void getPlayersOrderForFlightboard(UUID matchId, int playerId) throws RemoteException {
    }

    @Override
    public void pickAdventureCard(int currentPlayer) throws RemoteException {
//        if (localPlayer == null) {
//            throw new IllegalStateException("Cannot pick adventure card: local player not set");
//        }
        
        try {
            server.pickAdventureCard(currentPlayer, this);
        } catch (RemoteException e) {
            throw new RemoteException("Error picking adventure card", e);
        }
    }

    @Override
    public void selectPlanet(int playerId, int planetIndex) throws RemoteException {
        if (playerId <= 0) {
            throw new IllegalArgumentException("Invalid player ID: " + playerId);
        }
        
        try {
            server.selectPlanet(playerId, planetIndex, this);
        } catch (RemoteException e) {
            throw new RemoteException("Error selecting planet", e);
        }
    }
}