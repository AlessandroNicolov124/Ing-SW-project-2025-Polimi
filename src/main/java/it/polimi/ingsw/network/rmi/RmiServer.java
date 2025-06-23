package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.GameMatch;
import it.polimi.ingsw.network.LobbyManager;
import it.polimi.ingsw.network.MatchStates;
import it.polimi.ingsw.network.client.RmiClientIf;
import it.polimi.ingsw.network.messages.lobby.CreateMatchResponse;
import it.polimi.ingsw.network.messages.lobby.JoinMatchResponse;
import it.polimi.ingsw.network.messages.lobby.LeaveMatchResponse;
import it.polimi.ingsw.network.messages.lobby.StartMatchResponse;
import it.polimi.ingsw.network.messages.lobby.UpdateHostResponse;
import it.polimi.ingsw.network.messages.lobby.UsernameRejectedResponse;
import it.polimi.ingsw.network.messages.RotationType;
import it.polimi.ingsw.network.messages.loading.LoadingStatusResponse;
import it.polimi.ingsw.network.messages.loading.PlayerReadyResponse;
import it.polimi.ingsw.network.messages.loading.ShipBuildingStartResponse;
import it.polimi.ingsw.network.messages.shipbuilding.PickComponentResponse;
import it.polimi.ingsw.network.messages.shipbuilding.PlaceComponentResponse;
import it.polimi.ingsw.network.messages.shipbuilding.RemoveComponentResponse;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class RmiServer extends UnicastRemoteObject
        implements VirtualServer, Unreferenced, AutoCloseable, Runnable {

    private final LobbyManager lobbyManager;
    private final List<RmiClientIf> clients = new CopyOnWriteArrayList<>();
    private final Registry registry;

    public RmiServer(LobbyManager lobbyManager, Registry registry) throws RemoteException {
        super();
        this.lobbyManager = lobbyManager;
        this.registry = registry;
    }

    public void run() {
        System.out.println("GalaxyTruckerServer RMI ready (port 1099)");
    }

    @Override
    public void connect(RmiClientIf client) throws RemoteException {
        clients.add(client);
        try {
            //client.showUpdate("Welcome!");
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
    public int createPlayer(String username, RmiClientIf client) throws RemoteException {
        int playerId = lobbyManager.generatePlayerId(client);
        if (playerId == -1) {
            try {
                client.showUpdate(new UsernameRejectedResponse("Failed to generate player ID"));
            } catch (RemoteException e) {
                System.err.println("Failed to send rejection message: " + e.getMessage());
            }
            return -1; 
        }

        if (!lobbyManager.registerUsername(playerId, username)) {
            try {
                client.showUpdate(new UsernameRejectedResponse("Username already taken: " + username + ". Please choose another one."));
            } catch (RemoteException e) {
                System.err.println("Failed to send rejection message: " + e.getMessage());
            }
            return -1;
        }

        System.out.println("[ServerRMI] Player " + username + " added with ID " + playerId);
        return playerId;
    }

    @Override
    public UUID createMatch(String matchName, int playerId, String playerName, RmiClientIf client) throws RemoteException {
        GameMatch match = lobbyManager.createMatch(matchName, playerId, playerName);
        System.out.println("[ServerRMI] Match created: " + matchName + " with host " + playerName);

        lobbyManager.addClientToMatch(match.getMatchId(), client);
        lobbyManager.broadcastToAll(new CreateMatchResponse(matchName, match.getMatchId(), playerName, playerId));

        return match.getMatchId();

    }

    @Override
    public List<String> getAvailableMatches() throws RemoteException {
        return lobbyManager.getAvailableMatches();
    }

    @Override
    public boolean joinMatch(UUID matchId, int playerId, String playerName, RmiClientIf client) throws RemoteException {
        boolean success = lobbyManager.joinMatch(matchId, playerId, playerName);
        if (success) {
            lobbyManager.addClientToMatch(matchId, client);

            System.out.println("[ServerRMI] Player " + playerName + " joined match: " + matchId);

            lobbyManager.multicastToMatchExceptOne(matchId, new JoinMatchResponse(matchId, playerName), client);
        }
        return success;
    }

    @Override
    public boolean leaveMatch(UUID matchId, int playerId, String playerName, RmiClientIf client) throws RemoteException {
        GameMatch match = lobbyManager.findMatchById(matchId);
        if (match == null) {
            System.out.println("[ServerRMI] Match not found: " + matchId);
            return false;
        }

        if (!match.isPlayerInMatchById(playerId)) {
            System.out.println("[ServerRMI] Player " + playerName + " not in match: " + matchId);
            return false;
        }

        boolean success = lobbyManager.leaveMatch(matchId, playerId);
        if (success) {
            System.out.println("[ServerRMI] Player " + playerName + " left match: " + matchId);
            lobbyManager.multicastToMatch(matchId, new LeaveMatchResponse(matchId, playerId, playerName));

            Player newHost = match.getHostPlayer();
            if (newHost != null) {
                lobbyManager.multicastToMatchExceptOne(matchId, new UpdateHostResponse(matchId, newHost.getId(), newHost.getNickName()), client);
            }
            else {
                System.out.println("[ServerRMI] No new host found for match: " + matchId);
            }
            lobbyManager.removeClientFromMatch(matchId, client);
            }
        return success;
    }

    @Override
    public List<String> getPlayersInMatch(UUID matchId) throws RemoteException {
        GameMatch match = lobbyManager.findMatchById(matchId);
        if (match != null) {
            List<String> playerNames = match.getPlayers().stream().map(Player::getNickName).toList();
            return playerNames;
        } else {
            throw new RemoteException("Match not found: " + matchId);
        }
    }

    @Override
    public int getHostPlayerId(UUID matchId, int playerId) throws RemoteException {
        GameMatch match = lobbyManager.findMatchById(matchId);
        if (match != null) {
            Player hostPlayer = match.getHostPlayer();
            if (hostPlayer != null) {
                //System.out.println("[ServerRMI] Host player ID: " + hostPlayer.getId());
                return hostPlayer.getId();
            } else {
                throw new RemoteException("Host player not found for match: " + matchId);
            }
        } else {
            throw new RemoteException("Match not found: " + matchId);
        }

    }

    @Override
    public boolean startMatch(UUID matchId, int playerId, String username) throws RemoteException {
        GameMatch match = lobbyManager.findMatchById(matchId);
        if (match == null) {
            System.out.println("[ServerRMI] Match not found: " + matchId);
            return false;
        }

        if (!match.isPlayerInMatchById(playerId)) {
            System.out.println("[ServerRMI] Player ID " + playerId + " not in match: " + matchId);
            return false;
        }

        boolean success = lobbyManager.startMatch(matchId, playerId);
        if (success) {
            System.out.println("[ServerRMI] Match started by player: " + username);
            lobbyManager.multicastToMatch(matchId, new StartMatchResponse(matchId, username));
        }
        return success;
    }

    // remainder for // loadingAck and playerReady
    // things are already loaded in startMatch
    // so when the host sends us a loadingAck, we need to send back a message like loadingStatus
    // so that each of them in the match knows that the match is being started by the host.
    // in order to start all together we send first the loadingStatus message
    // the clients will be in another state, they then send another message to say that they are ready
    // the server will track how many players are ready and if all of them are ready the server will 
    // answer back and the clients will start the match together.

    @Override
    public boolean loadingAck(UUID matchId) throws RemoteException {
        GameMatch match = lobbyManager.findMatchById(matchId);

        if (match == null) {
            System.out.println("[ServerRMI] Match not found: " + matchId);
            return false;
        }

        lobbyManager.loadingAckResponse(matchId);
        System.out.println("[ServerRMI] Loading acknowledged for match: " + matchId);
        int readyPlayersMap = match.getReadyPlayersMap().size();
        int totalPlayers = match.getPlayers().size();
        lobbyManager.multicastToMatch(matchId, new LoadingStatusResponse(matchId, readyPlayersMap, totalPlayers));
        return true;
    }

    @Override
    public boolean playerReady(UUID matchId, int playerId) throws RemoteException {
        GameMatch match = lobbyManager.findMatchById(matchId);
        if (match == null) {
            System.out.println("[ServerRMI] Match not found: " + matchId);
            return false;
        }

        if (!match.isPlayerInMatchById(playerId)) {
            System.out.println("[ServerRMI] Player ID " + playerId + " not in match: " + matchId);
            return false;
        }

        boolean isReady = match.setPlayerReady(playerId);
        if (!isReady) {
            System.out.println("[ServerRMI] Duplicate ready state for player ID " + playerId + " in match: " + matchId);
            return false;
        }
        int playersReady = (int) match.getReadyPlayersMap().values().stream().filter(Boolean::booleanValue).count();
        int totalPlayers = match.getPlayers().size();
        lobbyManager.multicastToMatch(matchId, new PlayerReadyResponse(matchId, playersReady, totalPlayers));
        System.out.println("[ServerRMI] Player ID " + playerId + " is ready for match: " + matchId);
        if (playersReady == totalPlayers) {
            System.out.println("[ServerRMI] All players are ready for match: " + matchId);
            lobbyManager.multicastToMatch(matchId, new ShipBuildingStartResponse(matchId, playersReady));
        }
        return true;
    }

    @Override
    public Component pickComponent(int playerId) throws RemoteException {
        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        if (match == null) {
            System.out.println("[ServerRMI] Match not found for player ID: " + playerId);
            throw new RemoteException("Match not found for player ID: " + playerId);
        }

        if (match.getMatchState() != MatchStates.IN_PROGRESS) {
            System.out.println("[ServerRMI] Match not in progress for player ID: " + playerId);
            throw new RemoteException("Match not in progress");
        }

        GameController controller = match.getController();
        Player player = controller.getPlayerById(playerId);
        if (player == null) {
            System.out.println("[ServerRMI] Player not found with ID: " + playerId);
            throw new RemoteException("Player not found");
        }

        Component component = controller.getCompFromDeck(player);
        if (component == null) {
            System.out.println("[ServerRMI] No more components available for player " + playerId);
            throw new RemoteException("No more components available");
        }
        
        System.out.println("[ServerRMI] Component picked for player " + playerId + ": " + component.getClass().getSimpleName());
        
        return component;
    }

    @Override
    public void placeComponent(int playerId, Component component, Position position) throws RemoteException {
        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        if (match == null) {
            System.out.println("[ServerRMI] Match not found for player ID: " + playerId);
            throw new RemoteException("Match not found");
        }

        if (match.getMatchState() != MatchStates.IN_PROGRESS) {
            System.out.println("[ServerRMI] Match not in progress for player ID: " + playerId);
            throw new RemoteException("Match not in progress");
        }

        GameController controller = match.getController();
        Player player = controller.getPlayerById(playerId);
        if (player == null) {
            System.out.println("[ServerRMI] Player not found with ID: " + playerId);
            throw new RemoteException("Player not found");
        }

        boolean success = controller.addComponentForPlayer(player, component, position);
        if (!success) {
            throw new RemoteException("Cannot place component at position " + position + " for player " + playerId +
                    ". Position may be occupied or invalid.");
        }
        
        System.out.println("[ServerRMI] Component placed successfully for player " + playerId);
    }

    @Override
    public void removeComponent(int playerId, Position position) throws RemoteException {
        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        if (match == null) {
            System.out.println("[ServerRMI] Match not found for player ID: " + playerId);
            throw new RemoteException("Match not found");
        }

        if (match.getMatchState() != MatchStates.IN_PROGRESS) {
            System.out.println("[ServerRMI] Match not in progress for player ID: " + playerId);
            throw new RemoteException("Match not in progress");
        }

        GameController controller = match.getController();
        Player player = controller.getPlayerById(playerId);
        if (player == null) {
            System.out.println("[ServerRMI] Player not found with ID: " + playerId);
            throw new RemoteException("Player not found");
        }

        Component componentToRemove = player.getShip().getComponentAt(position);
        if (componentToRemove == null) {
            System.out.println("[ServerRMI] No component found at position " + position + " for player " + playerId);
            throw new RemoteException("No component found at position");
        }

        boolean success = controller.removeComponentForPlayer(player, componentToRemove);
        if (!success) {
            String errorMsg;
            if (componentToRemove.isStartingCabin()) {
                errorMsg = "Cannot remove starting cabin - it's the ship's core";
            } else if (player.getShip().getComponents().size() == 1) {
                errorMsg = "Cannot remove the only component on the ship";
            } else {
                errorMsg = "Cannot remove component - would disconnect the ship";
            }
            throw new RemoteException(errorMsg);
        }
        
        System.out.println("[ServerRMI] Component removed successfully for player " + playerId + " at " + position);
    }

    @Override
    public void rotateComponent(int playerId, Position position, RotationType rotationType) throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rotateComponent'");
    }

    @Override
    public void validateShip(int playerId) throws RemoteException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateShip'");
    }

    // private void broadcastErrorToAllClients(int playerId, Component component, Position position, String errorMessage) {
    //     try {
    //         lobbyManager.broadcastToAll(new PlaceComponentResponse(
    //             playerId, component, position, false, errorMessage));
    //     } catch (Exception e) {
    //         System.err.println("[ServerRMI] Failed to broadcast error to all clients: " + e.getMessage());
    //     }
    // }

    // private void broadcastErrorToMatch(UUID matchId, int playerId, Component component, Position position, String errorMessage) {
    //     try {
    //         lobbyManager.multicastToMatch(matchId, new PlaceComponentResponse(
    //             playerId, component, position, false, errorMessage));
    //     } catch (Exception e) {
    //         System.err.println("[ServerRMI] Failed to broadcast error to match: " + e.getMessage());
    //     }
    // }

    // private void broadcastErrorToAllClients(int playerId, String errorMessage) {
    //     try {
    //         lobbyManager.broadcastToAll(new PickComponentResponse(
    //             playerId, null, false, errorMessage));
    //     } catch (Exception e) {
    //         System.err.println("[ServerRMI] Failed to broadcast error to all clients: " + e.getMessage());
    //     }
    // }

    // private void broadcastErrorToMatch(UUID matchId, int playerId, String errorMessage) {
    //     try {
    //         lobbyManager.multicastToMatch(matchId, new PickComponentResponse(
    //             playerId, null, false, errorMessage));
    //     } catch (Exception e) {
    //         System.err.println("[ServerRMI] Failed to broadcast error to match: " + e.getMessage());
    //     }
    // }

    // private void broadcastRemovalErrorToMatch(UUID matchId, int playerId, Position position, String errorMessage) {
    //     try {
    //         if (matchId != null) {
    //             lobbyManager.multicastToMatch(matchId, new RemoveComponentResponse(
    //                 playerId, position, false, errorMessage));
    //         } else {
    //             lobbyManager.broadcastToAll(new RemoveComponentResponse(
    //                 playerId, position, false, errorMessage));
    //         }
    //     } catch (Exception e) {
    //         System.err.println("[ServerRMI] Failed to broadcast removal error: " + e.getMessage());
    //     }
    // }

/*     public Component pickComponent(int playerId) throws RemoteException {
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

    } */


    // modificare col compoenente sulla nave
/*     @Override
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
    } */
/* 
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
        
*/

}