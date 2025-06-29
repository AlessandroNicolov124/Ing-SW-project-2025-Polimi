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
import java.util.ArrayList;
import java.util.HashMap;
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
    private Map<UUID, GameMatch> matches = new HashMap<>();
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


    public GeneralServer(String host, int rmiPort, int socketPort) throws RemoteException, MalformedURLException {
        System.setProperty("java.rmi.server.hostname", host);
        Registry reg = LocateRegistry.createRegistry(rmiPort);

        this.rmiServer = new RmiServer(this, reg);
        Naming.rebind("//" + host + ":" + rmiPort + "/GalaxyTruckerServer", rmiServer);
        System.out.println("RmiServer listening at port " + rmiPort);
        this.socketServer = new SocketServer(socketPort, this);
        serverRun();


        this.generalDispatcher = startGlobalDispatcher();
    }

    public void serverRun() {
        new Thread(this.socketServer).start();
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
    public GameMatch createMatch(String matchName, int playerId, String playerName, Difficulty difficulty) {
        UUID matchId = UUID.randomUUID();
        GameMatch match = new GameMatch(matchId, matchName, playerId, playerName, difficulty);
        matches.put(matchId, match);
        
        System.out.println("[GeneralServer] Match created: " + matchName + " by " + playerName + " (ID: " + playerId + ")");
        
        return match;
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
        if (player == null) {
            System.out.println("[GeneralServer] Player " + playerId + " not found in match " + matchId);
            return false;
        }
        
        String playerName = player.getNickName();
        boolean removed = match.removePlayer(playerId);
        
        if (removed) {
            System.out.println("[GeneralServer] Player " + playerName + " left match " + match.getMatchName());
            
            if (match.isEmpty()) {
                matches.remove(matchId);
                cleanupMatchResources(matchId);
                System.out.println("[GeneralServer] Match " + match.getMatchName() + " removed due to no players");
            } else {

            }
        } else {
            System.out.println("[GeneralServer] Player " + playerName + " could not leave match " + match.getMatchName());
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
    public GameMatch findMatchById(UUID matchId) {
        return matches.get(matchId);
    }

    @Override
    public GameMatch findMatchByPlayerId(int playerId) {
        for (GameMatch match : matches.values()) {
            if (match.getPlayerById(playerId) != null) {
                return match;
            }
        }
        return null;
    }
    
    @Override
    public GameMatch findMatchByPlayerName(String playername) {
        for (GameMatch match : matches.values()) {
            if (match.getPlayerByName(playername) != null) {
                return match;
            }
        }
        return null;
    }  


    @Override
    public List<String> getAvailableMatches() {
        List<String> matchList = new ArrayList<>();
        for (GameMatch match : matches.values()) {
            if (match.getMatchState() == MatchStates.WAITING_FOR_PLAYERS) {
                String matchInfo = match.getMatchId() + ": " + match.getMatchName() + 
                                 " (Host: " + match.getHostPlayer().getNickName() + 
                                 ", Difficulty: " + match.getDifficulty() + 
                                 ", Players: " + match.getPlayers().size() + "/4)";
                matchList.add(matchInfo);
            }
        }
        return matchList;
    }

    // Remove matches based on conditions
    @Override
    public void removeEmptyMatches() {
        matches.values().removeIf(match -> match.getPlayers().isEmpty() || match.getMatchState() == MatchStates.FINISHED);
        System.out.println("[GeneralServer] Removed all empty matches");
    }

    @Override
    public void removeInProgressMatch() {
        matches.values().removeIf(match -> match.getMatchState() == MatchStates.IN_PROGRESS);
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
            boolean wasInMatch = matchClients.remove(client);
            
            if (wasInMatch) {
                System.out.println("[GeneralServer] Client removed from match " + matchId);

                if (matchClients.isEmpty()) {
                    cleanupMatchResources(matchId);
                }
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
    public void broadcastToAllExceptOne(ResponseMessage responseMessage, GeneralClientIf excludeClient) {
        List<GeneralClientIf> clientsCopy = new ArrayList<>(allClients);
        
        for (GeneralClientIf client : clientsCopy) {
            if (client != excludeClient) {
                try {
                    sendMessageToClient(client, responseMessage);
                } catch (Exception e) {
                    System.err.println("[GeneralServer] Error in broadcast, removing client: " + e.getMessage());
                    allClients.remove(client);
                    for (List<GeneralClientIf> matchClients : clientsInMatch.values()) {
                        matchClients.remove(client);
                    }
                }
            }
        }
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
            List<GeneralClientIf> clientsCopy = new ArrayList<>(clients);
            for (GeneralClientIf c : clientsCopy) {
                if (c != client) {
                    try {
                        sendMessageToClient(c, responseMessage);
                    } catch (Exception e) {
                        System.err.println("[GeneralServer] Error sending message to client, removing: " + e.getMessage());
                        clients.remove(c);
                        allClients.remove(c);
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
                    
                    List<GeneralClientIf> clientsCopy = new ArrayList<>(allClients);
                    
                    for (GeneralClientIf client : clientsCopy) {
                        try {
                            sendMessageToClient(client, msg);
                        } catch (Exception e) {
                            System.err.println("[GeneralServer] Error in global broadcast, removing client: " + e.getMessage());
                            allClients.remove(client);
                            for (List<GeneralClientIf> matchClients : clientsInMatch.values()) {
                                matchClients.remove(client);
                            }
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
    
    private void cleanupMatchResources(UUID matchId) {
        Thread dispatcher = matchDispatchers.remove(matchId);
        if (dispatcher != null && dispatcher.isAlive()) {
            dispatcher.interrupt();
            System.out.println("[GeneralServer] Dispatcher thread stopped for match " + matchId);
        }
        
        BlockingQueue<ResponseMessage> queue = matchQueues.remove(matchId);
        if (queue != null) {
            queue.clear();
            System.out.println("[GeneralServer] Message queue cleared for match " + matchId);
        }
        
        clientsInMatch.remove(matchId);
        System.out.println("[GeneralServer] Client list cleared for match " + matchId);
    }

    private void sendMessageToClient(GeneralClientIf client, ResponseMessage message) throws Exception {
        try {
            client.showUpdate(message);
        } catch (Exception e) {
            System.err.println("[GeneralServer] Failed to send message to client: " + e.getMessage());
            allClients.remove(client);
            
            for (List<GeneralClientIf> matchClients : clientsInMatch.values()) {
                matchClients.remove(client);
            }
            
            throw e;
        }
    }

}