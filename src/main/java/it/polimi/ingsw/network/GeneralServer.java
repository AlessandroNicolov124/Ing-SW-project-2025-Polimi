package it.polimi.ingsw.network;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.network.client.GeneralClientIf;
import it.polimi.ingsw.network.messages.ResponseMessage;
import it.polimi.ingsw.network.rmi.RmiServer;
import it.polimi.ingsw.network.socket.server.SocketServer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class GeneralServer implements LobbyManager {

    private RmiServer rmiServer;
    private SocketServer socketServer;
    private List<GameMatch> matches = new CopyOnWriteArrayList<>();
    private final AtomicInteger playerIdGenerator = new AtomicInteger(1);
    
    private final Map<UUID, List<GeneralClientIf>> clientsInMatch = new ConcurrentHashMap<>();
    private final Map<UUID, BlockingQueue<ResponseMessage>> matchQueues = new ConcurrentHashMap<>();
    private final Map<UUID, Thread> matchDispatchers = new ConcurrentHashMap<>();
    private final BlockingQueue<ResponseMessage> generalBroadcastQueue = new LinkedBlockingQueue<>();
    private final Thread generalDispatcher;
    private final List<GeneralClientIf> allClients = new CopyOnWriteArrayList<>();
    // to make sure the usernames are unique
    private final Map<Integer, String> playerIdsAndNames = new ConcurrentHashMap<>();
    private final Set<String> usernames = ConcurrentHashMap.newKeySet();


    public GeneralServer() throws RemoteException, MalformedURLException {
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        Registry reg = LocateRegistry.createRegistry(1099);

        this.rmiServer = new RmiServer(this, reg);
        Naming.rebind("GalaxyTruckerServer", rmiServer);
        this.socketServer = new SocketServer(7777, this);
        
        this.generalDispatcher = startGlobalDispatcher();
    }

    public void serverRun() {
        new Thread(this.socketServer).start();
        new Thread(this.rmiServer).start();
    }

    @Override
    public boolean registerUsername(int playerId, String username) {
        boolean added = usernames.add(username);
        if (added) {
            playerIdsAndNames.put(playerId, username);
        }
        return added;
    }

    @Override
    public void unregisterUsername(int playerId) {
        String name = playerIdsAndNames.remove(playerId);
        if (name != null) {
            usernames.remove(name);
        }
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !usernames.contains(username);
    }

    @Override
    public int generatePlayerId(GeneralClientIf client) {
        try {
            int id = playerIdGenerator.getAndIncrement();
            allClients.add(client);
            return id;
        } catch (Exception e) {
            System.err.println("[GeneralServer] Error generating player ID: " + e.getMessage());
            return -1;
        }
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
            if (match.canStartMatch()){
                match.startMatch(); 
            } else {
                System.out.println("[GeneralServer] Match " + match.getMatchName() + " cannot be started: Not enough players or already started");
                return false;
            }

            System.out.println("[GeneralServer] Match " + match.getMatchName() + " started by " + match.getPlayerById(requestingPlayerId).getNickName());
            return true;
        }
        System.out.println("[GeneralServer] Failed to start match " + matchId + ": Match not found or already started");
        return false;
    }

    @Override
    public boolean loadingAckResponse(UUID matchId) {
        GameMatch match = findMatchById(matchId);
        if (match == null) {
            return false;
        } else {
            match.loadingAckResponse();
            System.out.println("[GeneralServer] Waiting for players to load in match: " + match.getMatchName());
            return true;
        } 

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
    public void removeInProgressMatch() {
        matches.removeIf(match -> match.getNumberOfPlayers() == 1 || match.getMatchState() == MatchStates.IN_PROGRESS);
        System.out.println("[GeneralServer] Removed all in-progress matches");
    }

    @Override
    public void addClientToMatch(UUID matchId, GeneralClientIf client) {
        clientsInMatch.computeIfAbsent(matchId, k -> new CopyOnWriteArrayList<>()).add(client);
        
        if (!matchQueues.containsKey(matchId)) {
            BlockingQueue<ResponseMessage> matchQueue = new LinkedBlockingQueue<>();
            matchQueues.put(matchId, matchQueue);
            Thread dispatcher = startMatchDispatcher(matchId);
            matchDispatchers.put(matchId, dispatcher);
        }
    }

    @Override
    public void removeClientFromMatch(UUID matchId, GeneralClientIf client) {
        List<GeneralClientIf> matchClients = clientsInMatch.get(matchId);
        if (matchClients != null) {
            matchClients.remove(client);
            allClients.remove(client);
            
            if (matchClients.isEmpty()) {
                Thread dispatcher = matchDispatchers.remove(matchId);
                if (dispatcher != null) {
                    dispatcher.interrupt();
                }
                matchQueues.remove(matchId);
                clientsInMatch.remove(matchId);
            }
        }
    }

    @Override
    public List<GeneralClientIf> getClientsInMatch(UUID matchId) {
        return clientsInMatch.getOrDefault(matchId, new CopyOnWriteArrayList<>());
    }

    @Override
    public void broadcastToAll(ResponseMessage responseMessage) {
        generalBroadcastQueue.offer(responseMessage);
    }

    @Override
    public void multicastToMatch(UUID matchId, ResponseMessage responseMessage) {
        BlockingQueue<ResponseMessage> matchQueue = matchQueues.get(matchId);
        if (matchQueue != null) {
            matchQueue.offer(responseMessage);
        }
    }

    @Override
    public void multicastToMatchExceptOne(UUID matchId, ResponseMessage responseMessage, GeneralClientIf client) {
        List<GeneralClientIf> clients = clientsInMatch.get(matchId);
        if (clients != null) {
            for (GeneralClientIf c : clients) {
                if (c != client) {
                    try {
                        sendMessageToClient(c, responseMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private Thread startGlobalDispatcher() {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    ResponseMessage msg = generalBroadcastQueue.take();
                    for (GeneralClientIf client : allClients) {
                        try {
                            sendMessageToClient(client, msg);
                        } catch (Exception e) {
                            allClients.remove(client);
                        }
                    }
                }
            } catch (InterruptedException ignored) {
                System.out.println("General dispatcher interrupted, exiting...");
            }
        }, "General-Dispatcher");
        t.setDaemon(true);
        t.start();
        return t;
    }

    private Thread startMatchDispatcher(UUID matchId) {
        Thread t = new Thread(() -> {
            BlockingQueue<ResponseMessage> matchQueue = matchQueues.get(matchId);
            try {
                while (true) {
                    ResponseMessage msg = matchQueue.take();
                    List<GeneralClientIf> matchClients = clientsInMatch.get(matchId);
                    if (matchClients != null) {
                        for (GeneralClientIf client : matchClients) {
                            try {
                                sendMessageToClient(client, msg);
                            } catch (Exception e) {
                                matchClients.remove(client);
                            }
                        }
                    }
                }
            } catch (InterruptedException ignored) {
                System.out.println("Match dispatcher for " + matchId + " interrupted, exiting...");
            }
        }, "Match-Dispatcher-" + matchId);
        t.setDaemon(true);
        t.start();
        return t;
    }

    private void sendMessageToClient(GeneralClientIf client, ResponseMessage message) throws Exception {
        client.showUpdate(message);
    }

}