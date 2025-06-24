package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.messages.RotationType;
import it.polimi.ingsw.network.client.RmiClientIf;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;
import it.polimi.ingsw.network.messages.lobby.AddPlayerResponse;
import it.polimi.ingsw.network.messages.lobby.CreateMatchResponse;
import it.polimi.ingsw.network.messages.lobby.GetMatchesResponse;
import it.polimi.ingsw.network.messages.lobby.LeaveMatchResponse;
import it.polimi.ingsw.network.messages.lobby.PlayersInMatchResponse;
import it.polimi.ingsw.network.messages.lobby.UsernameRejectedResponse;
import it.polimi.ingsw.network.messages.shipbuilding.PickComponentResponse;
import it.polimi.ingsw.network.messages.shipbuilding.PlaceComponentResponse;
import it.polimi.ingsw.network.messages.shipbuilding.RemoveComponentResponse;

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
        try{
            int playerId = server.createPlayer(username, this);
            if (playerId != -1) {
                showUpdate(new AddPlayerResponse(playerId, username));
            }
        } catch (RemoteException e) {
            showUpdate(new UsernameRejectedResponse("Connection error: " + e.getMessage()));
        }
    }

    @Override
    public void createMatch(String matchName, int playerId, String username) throws RemoteException {
        try {
            UUID matchId = server.createMatch(matchName, playerId, username, this);
            showUpdate(new CreateMatchResponse(matchName, matchId, username, playerId));
        } catch (RemoteException e) {
            throw new RemoteException("Error creating match", e);
        }
    }

    @Override
    public void getAvailableMatches() throws RemoteException {
        try {
            List<String> matches = server.getAvailableMatches();
            showUpdate(new GetMatchesResponse(matches));
        } catch (RemoteException e) {
            throw new RemoteException("Error retrieving available matches", e);
        }
    }

    @Override
    public void joinMatch(UUID matchId, int playerId, String username) throws RemoteException {
        try {
            boolean isJoined = server.joinMatch(matchId, playerId, username, this);
            List<String> playerNames = server.getPlayersInMatch(matchId);
            int hostId = server.getHostPlayerId(matchId, playerId);
            if (!isJoined) {
                throw new RemoteException("Failed to join match: " + matchId);
            }
            showUpdate(new PlayersInMatchResponse(matchId, playerNames, hostId));

        } catch (RemoteException e) {
            throw new RemoteException("Error joining match", e);
        }
    }

    @Override
    public void leaveMatch(UUID matchId, int playerId, String username) throws RemoteException {
        try {
            boolean isLeft = server.leaveMatch(matchId, playerId, username, this);
            if (!isLeft) {
                throw new RemoteException("Failed to leave match: " + matchId);
            }
            showUpdate(new LeaveMatchResponse(matchId, playerId, username));
        } catch (RemoteException e) {
            throw new RemoteException("Error leaving match", e);
        }
    }

    @Override
    public void startMatch(UUID matchId, int playerId, String username) throws RemoteException {
        try {
            boolean isStarted = server.startMatch(matchId, playerId, username);
            if (!isStarted) {
                throw new RemoteException("Failed to start match: " + matchId);
            }
        } catch (RemoteException e) {
            throw new RemoteException("Error starting match", e);
        }
    }

    @Override
    public void loadingAck(UUID matchId) throws RemoteException {
        try {
            boolean isAcknowledged = server.loadingAck(matchId);
            if (!isAcknowledged) {
                throw new RemoteException("Failed to acknowledge loading for match: " + matchId);
            }
        } catch (RemoteException e) {
            throw new RemoteException("Error acknowledging loading", e);
        }
    }

    @Override
    public void playerReady(UUID matchId, int playerId) throws RemoteException {
        try {
            boolean isReady = server.playerReady(matchId, playerId);
            if (!isReady) {
                throw new RemoteException("Failed to set player ready for match: " + matchId);
            }
        } catch (RemoteException e) {
            throw new RemoteException("Error setting player ready", e);
        }
    }

    @Override
    public void close() throws Exception {
        UnicastRemoteObject.unexportObject(this, true);
    }


    @Override
    public Component pickComponentCard(int playerId) throws RemoteException {
        if (playerId <= 0) {
            throw new IllegalArgumentException("Invalid player ID: " + playerId);
        }
        
        try {
            Component component = server.pickComponent(playerId);
            showUpdate(new PickComponentResponse(playerId, component, true, "Component picked successfully"));
            return component;
        } catch (RemoteException e) {
            showUpdate(new PickComponentResponse(playerId, null, false, e.getMessage()));
            throw new RemoteException("Error picking component", e);
        }
    }

    @Override
    public void placeComponent(Component component, Position position, int playerId) throws RemoteException {
        if (playerId <= 0) {
            throw new IllegalArgumentException("Invalid player ID: " + playerId);
        }
        
        try {
            server.placeComponent(playerId, component, position);
            showUpdate(new PlaceComponentResponse(playerId, component, position, true, "Component placed successfully"));
        } catch (RemoteException e) {
            showUpdate(new PlaceComponentResponse(playerId, component, position, false, e.getMessage()));
            throw new RemoteException("Error placing component: " + e.getMessage());
        }
    }

    @Override
    public void removeComponent(Position position, int playerId) throws RemoteException {
        if (playerId <= 0) {
            throw new IllegalArgumentException("Invalid player ID: " + playerId);
        }
        
        try {
            server.removeComponent(playerId, position);
            showUpdate(new RemoveComponentResponse(playerId, position, true, "Component removed successfully"));
        } catch (RemoteException e) {
            showUpdate(new RemoveComponentResponse(playerId, position, false, e.getMessage()));
            throw new RemoteException("Error removing component: " + e.getMessage(), e);
        }
    }

    @Override
    public void rotateComponent(Position position, RotationType rotationType) throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rotateComponent'");
    }



    @Override
    public void validateShip() throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateShip'");
    }

}