package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.Difficulty;
import it.polimi.ingsw.network.GameMatch;
import it.polimi.ingsw.network.LobbyManager;
import it.polimi.ingsw.network.MatchStates;
import it.polimi.ingsw.network.client.RmiClientIf;
import it.polimi.ingsw.network.messages.lobby.AddPlayerResponse;
import it.polimi.ingsw.network.messages.lobby.CreateMatchResponse;
import it.polimi.ingsw.network.messages.lobby.GetMatchesResponse;
import it.polimi.ingsw.network.messages.lobby.JoinMatchResponse;
import it.polimi.ingsw.network.messages.lobby.LeaveMatchResponse;
import it.polimi.ingsw.network.messages.lobby.PlayersInMatchResponse;
import it.polimi.ingsw.network.messages.lobby.StartMatchResponse;
import it.polimi.ingsw.network.messages.lobby.UpdateHostResponse;
import it.polimi.ingsw.network.messages.lobby.UsernameRejectedResponse;
import it.polimi.ingsw.network.messages.shipbuilding.CabinColorSelectionResponse;
import it.polimi.ingsw.network.messages.shipbuilding.PickComponentResponse;
import it.polimi.ingsw.network.messages.shipbuilding.PlaceComponentResponse;
import it.polimi.ingsw.network.messages.shipbuilding.RemoveComponentResponse;
import it.polimi.ingsw.network.messages.shipbuilding.RotateComponentResponse;
import it.polimi.ingsw.network.messages.shipbuilding.ShipValidationResponse;
import it.polimi.ingsw.network.messages.RotationType;
import it.polimi.ingsw.network.messages.flight.FlightPreparationStartResponse;
import it.polimi.ingsw.network.messages.flight.FlightStartResponse;
import it.polimi.ingsw.network.messages.flight.PlayerReadyForFlightResponse;
import it.polimi.ingsw.network.messages.loading.LoadingStatusResponse;
import it.polimi.ingsw.network.messages.loading.PlayerReadyResponse;
import it.polimi.ingsw.network.messages.loading.ShipBuildingStartResponse;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class RmiServer extends UnicastRemoteObject
        implements VirtualServer, Unreferenced, AutoCloseable {

    private final LobbyManager lobbyManager;
    private final List<RmiClientIf> clients = new CopyOnWriteArrayList<>();
    private final Registry registry;

    public RmiServer(LobbyManager lobbyManager, Registry registry) throws RemoteException {
        super();
        this.lobbyManager = lobbyManager;
        this.registry = registry;
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
        client.showUpdate(new AddPlayerResponse(playerId, username));
        
        return playerId;
    }


    @Override
    public UUID createMatch(String matchName, int playerId, String playerName, Difficulty difficulty, RmiClientIf client) throws RemoteException {
        GameMatch match = lobbyManager.createMatch(matchName, playerId, playerName, difficulty);
        System.out.println("[ServerRMI] Match created: " + matchName + " with host " + playerName + " (Difficulty: " + difficulty + ")");

        lobbyManager.addClientToMatch(match.getMatchId(), client);
        
        client.showUpdate(new CreateMatchResponse(matchName, match.getMatchId(), playerName, playerId, difficulty));
        
        GetMatchesResponse matchListUpdate = new GetMatchesResponse(lobbyManager.getAvailableMatches());
        lobbyManager.broadcastToAllExceptOne(matchListUpdate, client);

        return match.getMatchId();
    }

    @Override
    public void getAvailableMatches(RmiClientIf client) throws RemoteException {
        List<String> matches = lobbyManager.getAvailableMatches();
        client.showUpdate(new GetMatchesResponse(matches));
    }

    @Override
    public boolean joinMatch(UUID matchId, int playerId, String playerName, RmiClientIf client) throws RemoteException {
        boolean success = lobbyManager.joinMatch(matchId, playerId, playerName);
        if (success) {
            lobbyManager.addClientToMatch(matchId, client);
            GameMatch match = lobbyManager.findMatchById(matchId);

            System.out.println("[ServerRMI] Player " + playerName + " joined match: " + matchId);

            lobbyManager.multicastToMatchExceptOne(matchId, new JoinMatchResponse(matchId, playerName), client);
            List<String> names = match.getPlayers().stream().map(Player::getNickName).toList();
            Player host = match.getHostPlayer();
            client.showUpdate(new PlayersInMatchResponse(matchId, names, host.getId()));
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
            try {
                client.showUpdate(new LeaveMatchResponse(matchId, playerId, playerName));
            } catch (RemoteException e) {
                System.err.println("[ServerRMI] Failed to notify leaving client: " + e.getMessage());
            }

            lobbyManager.removeClientFromMatch(matchId, client);

            GameMatch updatedMatch = lobbyManager.findMatchById(matchId);
            if (updatedMatch != null && !updatedMatch.isEmpty()) {
                try {
                    lobbyManager.multicastToMatch(matchId, new LeaveMatchResponse(matchId, playerId, playerName));

                    Player newHost = updatedMatch.getHostPlayer();
                    if (newHost != null) {
                        lobbyManager.multicastToMatch(matchId, new UpdateHostResponse(matchId, newHost.getId(), newHost.getNickName()));
                    }
                } catch (Exception e) {
                    System.err.println("[ServerRMI] Error during post-leave notifications: " + e.getMessage());
                }
            } else {
                System.out.println("[ServerRMI] Match " + matchId + " was removed or is empty after player left");
            }
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
    public boolean startMatch(UUID matchId, int playerId, String playerName, RmiClientIf client) throws RemoteException {

        GameMatch match = lobbyManager.findMatchById(matchId);

        if (match == null || !match.isPlayerInMatchById(playerId)){
            return false;
        }

        boolean success = lobbyManager.startMatch(matchId, playerId);
        if (success) {
            lobbyManager.multicastToMatch(matchId, new StartMatchResponse(matchId, playerName));
        }
        return success;
    }

    @Override
    public boolean loadingAck(UUID matchId, RmiClientIf client) throws RemoteException {

        GameMatch match = lobbyManager.findMatchById(matchId);
        if (match == null) {
            return false;
        } 

        lobbyManager.loadingAckResponse(matchId);
        
        int readyPlayersMap = match.getReadyPlayersMap().size();
        int totalPlayers = match.getPlayers().size();

        lobbyManager.multicastToMatch(matchId, new LoadingStatusResponse(matchId, readyPlayersMap, totalPlayers));
        return true;
    }

    @Override
    public boolean playerReady(UUID matchId, int playerId, RmiClientIf client) throws RemoteException {

        GameMatch match = lobbyManager.findMatchById(matchId);

        if (match == null || !match.isPlayerInMatchById(playerId)) {
            return false;
        }

        boolean ok = match.setPlayerReady(playerId);
        if (!ok) {
            return false;
        }

        int playersReady = (int) match.getReadyPlayersMap().values().stream().filter(Boolean::booleanValue).count();
        int totalPlayers = match.getPlayers().size();

        lobbyManager.multicastToMatch(matchId, new PlayerReadyResponse(matchId, playersReady, totalPlayers));

        if (playersReady == totalPlayers) {
            lobbyManager.multicastToMatch(matchId, new ShipBuildingStartResponse(matchId, playersReady));
        }
        return true;
    }

    @Override
    public Component pickComponent(int playerId, RmiClientIf client) throws RemoteException {

        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        
        if (match == null || match.getMatchState() != MatchStates.IN_PROGRESS)
            throw new RemoteException("Match not in progress");

        GameController controller = match.getController();
        Player player = controller.getPlayerById(playerId);

        if (player == null) {
            throw new RemoteException("Player not found");
        }

        Component comp = controller.getCompFromDeck(player);
        if (comp == null) {
            throw new RemoteException("No more components");
        }

        client.showUpdate(new PickComponentResponse(playerId, comp, true, "Component picked successfully"));
        return comp;
    }

    @Override
    public void placeComponent(int playerId, Component component, Position position, RmiClientIf client) throws RemoteException {

        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        if (match == null) throw new RemoteException("Match not found");

        GameController controller = match.getController();
        Player player = controller.getPlayerById(playerId);

        if (player == null) {
            throw new RemoteException("Player not found");
        }

        boolean ok = controller.addComponentForPlayer(player, component, position);

        client.showUpdate(new PlaceComponentResponse(playerId, component, position, ok, ok ? "Component placed" : "Invalid position"));
        if (!ok) {
            throw new RemoteException("Cannot place component at " + position);
        }
    }

    @Override
    public void removeComponent(int playerId, Position position, RmiClientIf client) throws RemoteException {
        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        if (match == null) throw new RemoteException("Match not found");

        GameController gc = match.getController();
        Player p = gc.getPlayerById(playerId);
        if (p == null) { 
            throw new RemoteException("Player not found");
        }

        Component toRemove = p.getShip().getComponentAt(position);
        if (toRemove == null) {
            throw new RemoteException("No component at " + position);
        }

        boolean ok = gc.removeComponentAt(p, position);

        client.showUpdate(new RemoveComponentResponse(playerId, position, ok, ok ? "Component removed" : "Cannot remove component"));
        if (!ok) {
            throw new RemoteException("Removal refused by game rules");
        }
    }

    @Override
    public void rotateComponent(int playerId, Position position, RotationType rotationType, RmiClientIf client) throws RemoteException {
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
                client.showUpdate(new RotateComponentResponse(playerId, position, rotationType, isValidRotation));
            }
        }
    }

    @Override
    public void selectCabinColor(String color, int playerId, RmiClientIf client) throws RemoteException {
        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        if (match == null) {
            System.err.println("[ServerRMI] Match not found for player ID: " + playerId);
            throw new RemoteException("Match not found for player ID: " + playerId);
        }

        String existingColor = match.getPlayerCabinColor(playerId);
        if (existingColor != null) {
            System.err.println("[ServerRMI] Player " + playerId + " already selected color: " + existingColor);
            throw new RemoteException("You already selected cabin color: " + existingColor);
        }

        if (match.isColorTaken(color)) {
            System.err.println("[ServerRMI] Color " + color + " is already taken in match " + match.getMatchId());
            throw new RemoteException("Color " + color + " is already taken by another player");
        }

        match.setPlayerCabinColor(playerId, color);
        System.out.println("[ServerRMI] Player " + playerId + " selected color: " + color);

        client.showUpdate(new CabinColorSelectionResponse(color));
    }


    @Override
    public void validateShip(int playerId, RmiClientIf client) throws RemoteException {
        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        GameController controller = match.getController();
        Player player = controller.getPlayerById(playerId);

        boolean isValid = controller.validateShipForPlayer(player);
        String msg = isValid ? "Ship is valid!" : "Ship validation failed:\n" + controller.getShipValidationErrors(player);

        client.showUpdate(new ShipValidationResponse(playerId, isValid, msg));

        match.getPlayersValidated().put(playerId, isValid);

        if (match.allPlayersValidated()) {
            match.setMatchState(MatchStates.FLIGHT_PREPARATION);
            lobbyManager.multicastToMatch(match.getMatchId(), new FlightPreparationStartResponse(match.getMatchId()));
        }
    }

    @Override
    public void readyForFlight(UUID matchId, int playerId, RmiClientIf client) throws RemoteException {
        GameMatch match = lobbyManager.findMatchById(matchId);
        if (match == null) {
            throw new RemoteException("Match not found");
        }

        GameController controller = match.getController();
        Player player = controller.getPlayerById(playerId);
        if (player == null) {
            throw new RemoteException("Player not found");
        }

        boolean isShipValid = controller.validateShipForPlayer(player);
        
        if (!isShipValid) {
            throw new RemoteException("Ship has connection issues. Please fix them before proceeding.");
        }
        
        match.setPlayerReadyForFlight(playerId, true);
        client.showUpdate(new ShipValidationResponse(playerId, true, "Ship is valid! Ready for flight."));

        
        // lobbyManager.multicastToMatchExceptOne(matchId, 
                        // new PlayerReadyForFlightResponse(matchId, playerId, player.getNickName(), true), client);
        
        if (match.allPlayersReadyForFlight()) {
            match.setMatchState(MatchStates.FLIGHT_IN_PROGRESS);
            lobbyManager.multicastToMatch(matchId, new FlightStartResponse(matchId));
        }
        
    }

}