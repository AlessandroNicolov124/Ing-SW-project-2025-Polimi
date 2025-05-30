package it.polimi.ingsw.network;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.network.rmi.RmiServer;
import it.polimi.ingsw.network.socket.server.SocketServer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class GeneralServer implements LobbyManager {

    private RmiServer rmiServer;
    private SocketServer socketServer;
    private List<GameMatch> matches = new CopyOnWriteArrayList<>();
    private final AtomicInteger playerIdGenerator = new AtomicInteger(1);

    public GeneralServer() throws RemoteException, MalformedURLException {
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        Registry reg = LocateRegistry.createRegistry(1099);

        this.rmiServer = new RmiServer(this, reg);
        Naming.rebind("GalaxyTruckerServer", rmiServer);
        //this.socketServer = new SocketServer(7777, this);
    }

    public void serverRun() {
        new Thread(this.socketServer).start();
        new Thread(this.rmiServer).start();
    }

    @Override
    public int generatePlayerId() {
        return playerIdGenerator.getAndIncrement();
    }

    // Create and manage matches
    @Override
    public GameMatch createMatch(String matchName, int hostId, String hostName) {
        GameMatch newMatch = new GameMatch(matchName, hostId, hostName);
        matches.add(newMatch);
        System.out.println("[GeneralServer] Match created: " + matchName + " with host " + hostName);
        return newMatch;
    }

    @Override
    public boolean joinMatch(UUID matchId, int playerId, String playerName) {
        GameMatch match = findMatchById(matchId);
        if (match != null && !match.isFull() && match.getMatchState() == MatchStates.WAITING_FOR_PLAYERS) {
            match.addPlayer(playerId, playerName);
            System.out.println("[GeneralServer] Player " + playerName + " joined match " + match.getMatchName());
            return true;
        } else {
            System.out.println("[GeneralServer] Failed to join match " + matchId + ": Match is full or not found");
            return false;
        }
    }

    @Override
    public boolean leaveMatch(UUID matchId, int playerId) {
        GameMatch match = findMatchById(matchId);
        if (match == null) {
            System.out.println("[GeneralServer] Match " + matchId + " not found for player ID " + playerId);
            return false;
        }
        Player player = match.getPlayerById(playerId);
        boolean removed = match.removePlayer(playerId);
        if (removed) {
            System.out.println("[GeneralServer] Player " + player.getNickName() + " left match " + match.getMatchName());
            if (match.isEmpty()) {
                matches.remove(match);
                System.out.println("[GeneralServer] Match " + match.getMatchName() + " removed due to no players");
            }
        } else {
            System.out.println("[GeneralServer] Player " + player.getNickName() + " could not leave match " + match.getMatchName());
        }
        return removed;
    }

    @Override
    public boolean startMatch(UUID matchId, int requestingPlayerId) {
        GameMatch match = findMatchById(matchId);
        if (match != null && match.getMatchState() == MatchStates.WAITING_FOR_PLAYERS && match.getHostPlayer().getId() == requestingPlayerId) {
            match.startMatch();
            System.out.println("[GeneralServer] Match " + match.getMatchName() + " started");
            return true;
        }
        System.out.println("[GeneralServer] Failed to start match " + matchId + ": Match not found or already started");
        return false;
    }

    // Search and retrieve matches
    @Override
    public GameMatch findMatchById(UUID matchId) {
        return matches.stream()
                .filter(match -> match.getMatchId().equals(matchId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public GameMatch findMatchByPlayerId(int playerId) {
        return matches.stream()
                .filter(match -> match.isPlayerInMatchById(playerId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<String> getAvailableMatches() {
        if (matches.isEmpty()) {
            System.out.println("[GeneralServer] No available matches found");
            return List.of();
        }
        return matches.stream()
                .filter(match -> match.getMatchState() == MatchStates.WAITING_FOR_PLAYERS)
                .map(match -> match.getMatchId() + ": " + match.getMatchName())
                .toList();
    }

    // Remove matches based on conditions
    @Override
    public void removeEmptyMatches() {
        matches.removeIf(match -> match.isEmpty() || match.getMatchState() == MatchStates.FINISHED);
    }

    @Override
    public void removeMatch(UUID matchId, int hostPlayerId) {
        GameMatch match = findMatchById(matchId);
        if (match != null && match.getMatchState() == MatchStates.WAITING_FOR_PLAYERS && match.getHostPlayer().getId() == hostPlayerId) {
            matches.remove(match);
            System.out.println("[GeneralServer] Match " + match.getMatchName() + " removed");
        } else {
            System.out.println("[GeneralServer] Match " + matchId + " not found for removal");
        }
    }

    @Override
    public void removeInProgressMatch() {
        matches.removeIf(match -> match.getNumberOfPlayers() == 1 || match.getMatchState() == MatchStates.IN_PROGRESS);
        System.out.println("[GeneralServer] Removed all in-progress matches");
    }

}