package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.socket.messages.RotationType;
import it.polimi.ingsw.network.client.RmiClientIf;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;
import it.polimi.ingsw.network.messages.lobby.AddPlayerResponse;
import it.polimi.ingsw.network.messages.lobby.CreateMatchResponse;
import it.polimi.ingsw.network.messages.lobby.GetMatchesResponse;
import it.polimi.ingsw.network.messages.lobby.JoinMatchResponse;
import it.polimi.ingsw.network.messages.lobby.LeaveMatchResponse;
import it.polimi.ingsw.network.messages.lobby.PlayersInMatchResponse;


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

    public static RmiClient connectTo(String host, GameListener sbl) throws Exception {
        VirtualServer srv = (VirtualServer) Naming.lookup("//" + host + ":1099/GalaxyTruckerServer");
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
            int prova = server.createPlayer(username, this);
            showUpdate(new AddPlayerResponse(prova, username));
        } catch (RemoteException e) {
            throw new RemoteException("Error creating player");
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
            showUpdate(new JoinMatchResponse(matchId, username));
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
    public void close() throws Exception {
        UnicastRemoteObject.unexportObject(this, true);
    }

    @Override
    public void placeComponent(Component component, Position position) throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'placeComponent'");
    }

    @Override
    public void validateShip() throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateShip'");
    }

    @Override
    public void rotateComponent(Position position, RotationType rotationType) throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rotateComponent'");
    }

    @Override
    public void setLocalPlayer(Player player) throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setLocalPlayer'");
    }

    @Override
    public Component pickComponentCard() throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'pickComponentCard'");
    }

}