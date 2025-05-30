package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.GameMatch;
import it.polimi.ingsw.network.LobbyManager;
import it.polimi.ingsw.network.MatchStates;
import it.polimi.ingsw.network.client.GeneralClientIf;
import it.polimi.ingsw.network.client.RmiClientIf;
import it.polimi.ingsw.network.rmi.messages.ComponentPlacedMessage;
import it.polimi.ingsw.network.rmi.messages.HostChangedMessage;
import it.polimi.ingsw.network.rmi.messages.MatchCreatedMessage;
import it.polimi.ingsw.network.rmi.messages.MatchStartedMessage;
import it.polimi.ingsw.network.rmi.messages.PickComponentMessage;
import it.polimi.ingsw.network.rmi.messages.PlayerAddedMessage;
import it.polimi.ingsw.network.rmi.messages.PlayerJoinedMessage;
import it.polimi.ingsw.network.rmi.messages.RemoveComponentMessage;
import it.polimi.ingsw.network.rmi.messages.RotateComponentMessage;
import it.polimi.ingsw.network.rmi.messages.UpdateMessage;
import it.polimi.ingsw.network.rmi.messages.ValidateShipMessage;
import it.polimi.ingsw.network.rmi.messages.PlayerLeavedMessage;
import it.polimi.ingsw.network.socket.messages.RotationType;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class RmiServer extends UnicastRemoteObject
        implements VirtualServer, Unreferenced, AutoCloseable, Runnable {

    private final LobbyManager lobbyManager;
    private final List<RmiClientIf> clients = new CopyOnWriteArrayList<>();
    private final Map<UUID, List<GeneralClientIf>> clientsInMatch = new ConcurrentHashMap<>();
    private final BlockingQueue<UpdateMessage> updates = new LinkedBlockingQueue<>();
    private final Thread dispatcher;
    private final Registry registry;

    public RmiServer(LobbyManager lobbyManager, Registry registry) throws RemoteException {
        super();
        this.lobbyManager = lobbyManager;
        this.registry = registry;
        this.dispatcher = startDispatcher();
    }

    // Thread which dispatches general updates to all clients
    private Thread startDispatcher() {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    UpdateMessage msg = updates.take();
                    for (RmiClientIf c : clients) {
                        try {
                            c.showUpdate(msg);
                        }
                        catch (RemoteException e) {
                            clients.remove(c);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (InterruptedException ignored) {
                System.out.println("Dispatcher thread if iterrupted, exiting...");
            }
        }, "Update-Dispatcher");
        t.setDaemon(true);
        t.start();

        return t;
    }

    public void run() {
        System.out.println("GalaxyTruckerServer RMI ready (port 1099)");
    }

    @Override
    public void connect(RmiClientIf client) throws RemoteException {
        clients.add(client);
        try {
            client.showUpdate("Welcome!");
            System.out.println("Client connected: " + client.toString());
        }
        catch (Exception e) {
            System.out.println("Error connecting to RMI server: " + e.getMessage());
        }

    }

    @Override
    public void stopServer() throws RemoteException {
        new Thread(() -> {
            try {
                close();
            } catch (Exception ignored) {
                System.out.println("Error while closing server: " + ignored.getMessage());
            }
        }).start();
    }

    @Override
    public void close() throws Exception {
        dispatcher.interrupt();
        UnicastRemoteObject.unexportObject(this, true);
        if (registry != null) 
            UnicastRemoteObject.unexportObject(registry, true);
        System.out.println("Shutting down GalaxyTruckerServer...");
    }

    // Unreferenced method, called when the server is no longer referenced by any client
    // This is a fallback mechanism to ensure the server is closed when all clients disconnect
    @Override public void unreferenced() {
        System.out.println("Unreferenced: " + this);
        try {
            close();
        }
        catch (Exception ignored) {
            System.out.println("Error while closing server: " + ignored.getMessage());
        }
    }

    @Override
    public int createPlayer(String username) throws RemoteException {
        int playerId = lobbyManager.generatePlayerId();
        System.out.println("[ServerRMI] Player: " + username + " added with ID: " + playerId);
        updates.offer(new PlayerAddedMessage(playerId, username));
        return playerId;
    }

    @Override
    public List<String> getAvailableMatches() throws RemoteException {
        return lobbyManager.getAvailableMatches();
    }

    @Override
    public UUID createMatch(String matchName, int playerId, String playerName, RmiClientIf client) throws RemoteException {
        GameMatch match = lobbyManager.createMatch(matchName, playerId, playerName);
        System.out.println("[ServerRMI] Match created: " + matchName + " with host " + playerName);

        this.clientsInMatch.computeIfAbsent(match.getMatchId(), k -> new CopyOnWriteArrayList<>()).add(client);

        // Notify all clients about the new match
        updates.offer(new MatchCreatedMessage(match.getMatchId(), matchName, playerName));

        return match.getMatchId();
    }


    @Override
    public boolean joinMatch(UUID matchId, int playerId, String playerName, RmiClientIf client) throws RemoteException {
        boolean success = lobbyManager.joinMatch(matchId, playerId, playerName);
        if (success) {
            System.out.println("[ServerRMI] Player " + playerName + " joined match: " + matchId);

            this.clientsInMatch.computeIfAbsent(matchId, k -> new CopyOnWriteArrayList<>()).add(client);

            // Notify all the clients IN THE MATCH about the new player
            List<GeneralClientIf> matchClients = clientsInMatch.get(matchId);
            if (matchClients != null) {
                for (GeneralClientIf c : matchClients) {
                    try {
                        c.showUpdate(new PlayerJoinedMessage(matchId, playerName));
                    } catch (RemoteException e) {
                        matchClients.remove(c);
                        System.out.println("[ServerRMI] Client disconnected: " + c);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return success;
    }

    @Override
    public boolean leaveMatch(UUID matchId, int playerId, String playerName, RmiClientIf client) throws RemoteException {
        boolean success = lobbyManager.leaveMatch(matchId, playerId);
        if (success) {
            System.out.println("[ServerRMI] Player " + playerName + " left match: " + matchId);

            List<GeneralClientIf> matchClients = clientsInMatch.get(matchId);
            if (matchClients != null) {
                matchClients.remove(client);

                for (GeneralClientIf c : matchClients) {
                    try {
                        c.showUpdate(new PlayerLeavedMessage(playerName));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                     }
                }

                GameMatch match = lobbyManager.findMatchById(matchId);
                if (match != null && !match.isEmpty()) {
                    String newHostName = match.getHostPlayer().getNickName();
                    for (GeneralClientIf c : matchClients) {
                        try {
                            c.showUpdate(new HostChangedMessage(newHostName));
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return success;
    }

    @Override
    public boolean startMatch(UUID matchId, int playerId) throws RemoteException {
        boolean success = lobbyManager.startMatch(matchId, playerId);
        if (success) {
            System.out.println("[ServerRMI] Match started by player " + playerId);

            List<GeneralClientIf> matchClients = clientsInMatch.get(matchId);
            if (matchClients != null) {
                for (GeneralClientIf c : matchClients) {
                    try {
                        c.showUpdate(new MatchStartedMessage(matchId, "Match has started! Assemble your ships."));
                    } catch (RemoteException e) {
                        matchClients.remove(c);
                        System.out.println("[ServerRMI] Client disconnected: " + c);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return success;
    }

    //@Override
    //public int sendSrvAddPlayerReqAndWaitResp(String username) throws RemoteException {
    //    int min = 1;
    //    int max = 1000;
    //    int newId = ThreadLocalRandom.current().nextInt(min, max + 1);
    //    Player p = new Player(newId, username, true);
    //    controller.addPlayer(p);
    //    //updates.offer("Player " + username + " joined (id = " + newId + ")");
    //    System.out.println("[ServerRMI] Player " + username + " joined (id = " + newId + ")");
    //    updates.offer(new PlayerAddedMessage(newId));
    //    return newId;
    //}

    public Component pickComponent(int playerId) throws RemoteException {
        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        if (match != null && match.getMatchState() == MatchStates.IN_PROGRESS){
            GameController controller = match.getController();
            Player p = controller.getPlayerById(playerId);
            if (p != null) {
                Component c = controller.getCompFromDeck(p);
                System.out.println("[ServerRMI] Picked component: " + c.getClass().getSimpleName());
                updates.offer(new PickComponentMessage(playerId, c));
                return c;
            }
        }
        return null;
    }

    @Override
    public void placeComponent(int playerId, Component component, Position position) {
        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        if (match != null && match.getMatchState() == MatchStates.IN_PROGRESS) {
            GameController controller = match.getController();
            Player player = controller.getPlayerById(playerId);
            if (player != null) {
                boolean isValidPosition = controller.addComponentForPlayer(player, component, position);
                System.out.println("[ServerRMI] Placed component: " + component.getClass().getSimpleName() + " at " + position);
                updates.offer(new ComponentPlacedMessage(playerId, component, position, isValidPosition));
            }
        }
    }

    @Override
    public void removeComponent(int playerId, Position position) throws RemoteException {
        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        if (match != null && match.getMatchState() == MatchStates.IN_PROGRESS) {
            GameController controller = match.getController();
            Player player = controller.getPlayerById(playerId);
            if (player != null) {
                Component componentToRemove = player.getShip().getComponentAt(position);
                boolean isComponentRemoved = false;
                if (componentToRemove != null) {
                    isComponentRemoved = controller.removeComponentForPlayer(player, componentToRemove);
                
                }
                System.out.println("[ServerRMI] Removed component at " + position + ": " + isComponentRemoved);
                updates.offer(new RemoveComponentMessage(playerId, position, isComponentRemoved));
            }
        }

    }


    // modificare col compoenente sulla nave
    @Override
    public void rotateComponent(int playerId, Position position, RotationType rotationType) throws RemoteException {
        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        if (match != null && match.getMatchState() == MatchStates.IN_PROGRESS) {
            GameController controller = match.getController();
            Player player= controller.getPlayerById(playerId);
            if (player != null) {
                Component componentToRotate = player.getShip().getComponentAt(position);
                boolean isValidRotation = false;

                if (componentToRotate != null) {
                    switch (rotationType) {
                        case CLOCKWISE:
                            controller.rotateComponentClockwiseForPlayer(player, componentToRotate);
                            isValidRotation = true;
                            break;
                        case ANTI_CLOCKWISE:
                            controller.rotateComponentCounterClockwiseForPlayer(player, componentToRotate);
                            isValidRotation = true;
                            break;
                        case OPPOSITE:
                            controller.rotateComponentOppositeForPlayer(player, componentToRotate);
                            isValidRotation = true;
                            break;
                    }
                }
                System.out.println("[ServerRMI] Rotated component at " + position + " " + rotationType + ": " + isValidRotation);
                updates.offer(new RotateComponentMessage(playerId, position, rotationType, isValidRotation));
            }
        }
    }

    @Override
    public void validateShip(int playerId) throws RemoteException {
        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        if (match != null && match.getMatchState() == MatchStates.IN_PROGRESS) {
            GameController controller = match.getController();
            Player p = controller.getPlayerById(playerId);
            if (p != null) {
                boolean isValid = controller.validateShipForPlayer(p);
                String message = isValid ? "Ship is valid!" : "Ship validation failed";
                
                System.out.println("[RMI Server] Ship validation for player " + playerId + ": " + isValid);
                updates.offer(new ValidateShipMessage(playerId, isValid, message));
            }
        }
    }

    @Override
    public boolean isPlayerHostById(UUID matchId, int playerId) throws RemoteException {
        GameMatch match = lobbyManager.findMatchById(matchId);
        if (match != null) {
            Player player = match.getPlayerById(playerId);
            if (player != null) {
                boolean isHost = match.getHostPlayer().getId() == playerId;
                System.out.println("[ServerRMI] Player " + player.getNickName() + " is host: " + isHost);
                return isHost;
            }
        }
        return false;
    }

    @Override
    public List<String> getPlayersInMatch(UUID matchId) throws RemoteException {
        GameMatch match = lobbyManager.findMatchById(matchId);
        if (match != null && match.getMatchState() == MatchStates.WAITING_FOR_PLAYERS) {
            List<String> playerNames = match.getPlayers().stream()
                    .map(Player::getNickName)
                    .toList();
            System.out.println("[ServerRMI] Players in match " + matchId + ": " + playerNames);
            return playerNames;
        }
        return null;
    }

}