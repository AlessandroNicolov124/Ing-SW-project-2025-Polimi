package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.AdventureCards.AdventureCard;
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
import it.polimi.ingsw.network.messages.shipbuilding.CabinColorSelectionResponse;
import it.polimi.ingsw.network.messages.shipbuilding.PickComponentResponse;
import it.polimi.ingsw.network.messages.shipbuilding.PlaceComponentResponse;
import it.polimi.ingsw.network.messages.shipbuilding.RemoveComponentResponse;
import it.polimi.ingsw.network.messages.shipbuilding.RotateComponentResponse;
import it.polimi.ingsw.network.messages.shipbuilding.ShipValidationResponse;
import it.polimi.ingsw.network.messages.RotationType;
import it.polimi.ingsw.network.messages.flight.FlightPreparationStartResponse;
import it.polimi.ingsw.network.messages.flight.PlayerReadyForFlightResponse;
import it.polimi.ingsw.network.messages.loading.LoadingAckResponse;
import it.polimi.ingsw.network.messages.loading.LoadingStatusResponse;
import it.polimi.ingsw.network.messages.loading.PlayerReadyResponse;
import it.polimi.ingsw.network.messages.loading.ShipBuildingStartResponse;
import it.polimi.ingsw.network.messages.adventure.PickAdventureCardResponse;
import it.polimi.ingsw.network.messages.adventure.AdventurePhaseStartResponse;
import it.polimi.ingsw.network.messages.adventure.AdventurePhaseCompleteResponse;
import it.polimi.ingsw.network.messages.shipbuilding.PositionAssignmentResponse;

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
            client.showUpdate(new AddPlayerResponse(playerId, username, false, username));
            return -1; 
        }

        if (!lobbyManager.registerUsername(playerId, username)) {
            client.showUpdate(new AddPlayerResponse(playerId, username, false, "Username already taken"));
            return -1;
        }

        System.out.println("[ServerRMI] Player " + username + " added with ID " + playerId);
        client.showUpdate(new AddPlayerResponse(playerId, username, true, "Player added successfully"));
        
        return playerId;
    }


    @Override
    public UUID createMatch(String matchName, int playerId, String playerName, Difficulty difficulty, RmiClientIf client) throws RemoteException {
        GameMatch match = lobbyManager.createMatch(matchName, playerId, playerName, difficulty);
        System.out.println("[ServerRMI] Match created: " + matchName + " with host " + playerName + " (Difficulty: " + difficulty + ")");

        lobbyManager.addClientToMatch(match.getMatchId(), client);
        
        client.showUpdate(new CreateMatchResponse(
                matchName,
                match.getMatchId(),
                playerName,
                playerId,
                difficulty,
                true,
                "Match created successfully"
        ));
        
        List<String> matchList = lobbyManager.getAvailableMatches();

        // if the match list is not empty we can succesfully broadcast the update
        if (!matchList.isEmpty()) {
            lobbyManager.broadcastToAllExceptOne(new GetMatchesResponse(matchList, true, ""), client);
        } else {
            client.showUpdate(new GetMatchesResponse(matchList, false, "No matches available"));
        }

        return match.getMatchId();
    }

    @Override
    public void getAvailableMatches(RmiClientIf client) throws RemoteException {
        List<String> matches = lobbyManager.getAvailableMatches();
        if (matches.isEmpty()) {
            client.showUpdate(new GetMatchesResponse(matches, false, "No matches available"));
        } else {
            client.showUpdate(new GetMatchesResponse(matches, true, ""));
        }
    }

    @Override
    public boolean joinMatch(UUID matchId, int playerId, String playerName, RmiClientIf client) throws RemoteException {
        boolean success = lobbyManager.joinMatch(matchId, playerId, playerName);
        if (success) {
            lobbyManager.addClientToMatch(matchId, client);
            GameMatch match = lobbyManager.findMatchById(matchId);

            System.out.println("[ServerRMI] Player " + playerName + " joined match: " + matchId);
            
            lobbyManager.multicastToMatchExceptOne(matchId, new JoinMatchResponse(matchId, playerName, true, "Player joined successfully"), client);
            List<String> names = match.getPlayers().stream().map(Player::getNickName).toList();
            Player host = match.getHostPlayer();
            client.showUpdate(new PlayersInMatchResponse(matchId, names, host.getId(), true, "Players in match retrieved successfully"));
        } else {
            System.out.println("[ServerRMI] Player " + playerName + " failed to join match: " + matchId);
            client.showUpdate(new JoinMatchResponse(matchId, playerName, false, "Failed to join match"));
        }
        return success;
    }

    @Override
    public boolean leaveMatch(UUID matchId, int playerId, String playerName, RmiClientIf client) throws RemoteException {
        GameMatch match = lobbyManager.findMatchById(matchId);
        if (match == null) {
            System.out.println("[ServerRMI] Match not found: " + matchId);
            client.showUpdate(new LeaveMatchResponse(matchId, playerId, playerName, false, "Match not found"));
            return false;
        }

        if (!match.isPlayerInMatchById(playerId)) {
            System.out.println("[ServerRMI] Player " + playerName + " not in match: " + matchId);
            client.showUpdate(new LeaveMatchResponse(matchId, playerId, playerName, false, "Player not in match"));
            return false;
        }

        boolean success = lobbyManager.leaveMatch(matchId, playerId);
        
        if (success) {
            client.showUpdate(new LeaveMatchResponse(matchId, playerId, playerName, true, "Player left match successfully"));

            lobbyManager.removeClientFromMatch(matchId, client);

            GameMatch updatedMatch = lobbyManager.findMatchById(matchId);
            if (updatedMatch != null && !updatedMatch.isEmpty()) {
                try {
                    lobbyManager.multicastToMatch(matchId, new LeaveMatchResponse(matchId, playerId, playerName, true, "Player" + playerName + " left match successfully"));

                    Player newHost = updatedMatch.getHostPlayer();
                    if (newHost != null) {
                        lobbyManager.multicastToMatch(matchId, new UpdateHostResponse(matchId, newHost.getId(), newHost.getNickName(), true, "New host updated successfully"));
                    }
                } catch (Exception e) {
                    client.showUpdate(new LeaveMatchResponse(matchId, playerId, playerName, false, "Error updating match after player left: " + e.getMessage()));
                }
            } else {
                System.out.println("[ServerRMI] Match " + matchId + " was removed or is empty after player left");
                client.showUpdate(new LeaveMatchResponse(matchId, playerId, playerName, true, "Match was removed or is empty after player left"));
            }
        }
        
        return success;
    }

    @Override
    public boolean startMatch(UUID matchId, int playerId, String playerName, RmiClientIf client) throws RemoteException {

        GameMatch match = lobbyManager.findMatchById(matchId);

        if (match == null || !match.isPlayerInMatchById(playerId)){
            client.showUpdate(new StartMatchResponse(matchId, playerName, false, "Match not found or player not in match"));
            return false;
        }

        boolean success = lobbyManager.startMatch(matchId, playerId);
        if (success) {
            lobbyManager.multicastToMatch(matchId, new StartMatchResponse(matchId, playerName, true, "Match started successfully"));
        } else {
            client.showUpdate(new StartMatchResponse(matchId, playerName, false, "Failed to start match"));
        }

        return success;
    }

    @Override
    public boolean loadingAck(UUID matchId, RmiClientIf client) throws RemoteException {

        GameMatch match = lobbyManager.findMatchById(matchId);
        if (match == null) {
            client.showUpdate(new LoadingAckResponse(false, "Match not found"));
        } 

        lobbyManager.loadingAckResponse(matchId);
        
        int readyPlayersMap = match.getReadyPlayersMap().size();
        int totalPlayers = match.getPlayers().size();

        lobbyManager.multicastToMatch(matchId, new LoadingStatusResponse(matchId, readyPlayersMap, totalPlayers, true, "Loading acknowledged"));
        return true;
    }

    @Override
    public boolean playerReady(UUID matchId, int playerId, RmiClientIf client) throws RemoteException {

        GameMatch match = lobbyManager.findMatchById(matchId);

        if (match == null || !match.isPlayerInMatchById(playerId)) {
            client.showUpdate(new PlayerReadyResponse(matchId, 0, 0, false, "Match not found or player not in match"));
            return false;
        }

        boolean ok = match.setPlayerReady(playerId);

        if (!ok) {
            client.showUpdate(new PlayerReadyResponse(matchId, 0, 0, false, "Player already ready or match not in loading state"));
            return false;
        }

        int playersReady = (int) match.getReadyPlayersMap().values().stream().filter(Boolean::booleanValue).count();
        int totalPlayers = match.getPlayers().size();

        lobbyManager.multicastToMatch(matchId, new PlayerReadyResponse(matchId, playersReady, totalPlayers, true, "Player ready status updated"));

        if (playersReady == totalPlayers) {
            lobbyManager.multicastToMatch(matchId, new ShipBuildingStartResponse(matchId, playersReady, true, "All players ready, starting ship building phase"));
        }
        return true;
    }

    @Override
    public Component pickComponent(int playerId, RmiClientIf client) throws RemoteException {

        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        
        if (match == null || match.getMatchState() != MatchStates.IN_PROGRESS) {
            client.showUpdate(new PickComponentResponse(playerId, null, false, "Match not found or not in progress"));
            return null;
        }   

        GameController controller = match.getController();
        Player player = controller.getPlayerById(playerId);

        if (player == null) {
            client.showUpdate(new PickComponentResponse(playerId, null, false, "Player not found"));
            return null;
        }

        Component comp = controller.getCompFromDeck(player);
        if (comp == null) {
            client.showUpdate(new PickComponentResponse(playerId, null, false, "No components available to pick"));
            return null;
        }

        client.showUpdate(new PickComponentResponse(playerId, comp, true, "Component picked successfully"));
        return comp;
    }

    @Override
    public void placeComponent(int playerId, Component component, Position position, RmiClientIf client) throws RemoteException {

        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        if (match == null) {
            client.showUpdate(new PlaceComponentResponse(playerId, component, position, false, "Match not found"));
            return;
        }

        GameController controller = match.getController();
        Player player = controller.getPlayerById(playerId);

        if (player == null) {
            client.showUpdate(new PlaceComponentResponse(playerId, component, position, false, "Player not found"));
            return;
        }

        boolean ok = controller.addComponentForPlayer(player, component, position);

        if (!ok) {
            client.showUpdate(new PlaceComponentResponse(playerId, component, position, false, "Placement refused by game rules"));
            return;
        }

        client.showUpdate(new PlaceComponentResponse(playerId, component, position, ok, ok ? "Component placed" : "Invalid position"));

    }

    @Override
    public void removeComponent(int playerId, Position position, RmiClientIf client) throws RemoteException {
        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        if (match == null) {
            client.showUpdate(new RemoveComponentResponse(playerId, position, false, "Match not found"));
            return;
        }

        GameController gameController = match.getController();
        Player player = gameController.getPlayerById(playerId);
        if (player == null) { 
            client.showUpdate(new RemoveComponentResponse(playerId, position, false, "Player not found"));
            return;
        }

        Component toRemove = player.getShip().getComponentAt(position);
        if (toRemove == null) {
            client.showUpdate(new RemoveComponentResponse(playerId, position, false, "No component at the specified position"));
            return;
        }

        boolean ok = gameController.removeComponentAt(player, position);

        if (!ok) {
            client.showUpdate(new RemoveComponentResponse(playerId, position, false, "Cannot remove component at this position"));
            return;
        }

        client.showUpdate(new RemoveComponentResponse(playerId, position, ok, ok ? "Component removed" : "Cannot remove component"));

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
                client.showUpdate(new RotateComponentResponse(playerId, position, rotationType, isValidRotation, true, "Component rotated successfully"));
            }
        } else {
            System.out.println("[ServerRMI] Match not found or not in progress for player " + playerId);
            client.showUpdate(new RotateComponentResponse(playerId, position, rotationType, false ,false, "Match not found or not in progress"));
        }
    }

    @Override
    public void selectCabinColor(String color, int playerId, RmiClientIf client) throws RemoteException {
        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        if (match == null) {
            client.showUpdate(new CabinColorSelectionResponse(false, "Match not found"));
            return;
        }
        String existing = match.getPlayerCabinColor(playerId);
        if (existing != null) {
            client.showUpdate(new CabinColorSelectionResponse(false, "Color already selected " + existing));
            return;
        }
        if (match.isColorTaken(color)) {
            client.showUpdate(new CabinColorSelectionResponse(false, "Color already taken by another player"));
            return;
        }
        match.setPlayerCabinColor(playerId, color);
        client.showUpdate(new CabinColorSelectionResponse(true, color));
    }

    @Override
    public void pickAdventureCard(int playerId, RmiClientIf client) throws RemoteException {
        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        if (match == null) {
            System.out.println("[ServerRMI] Match not found for player ID: " + playerId);
            client.showUpdate(new PickAdventureCardResponse(
                    playerId, null, false, "Match not found", -1));
            return;
        }
        if (match.getMatchState() != MatchStates.IN_PROGRESS) {
            System.out.println("[ServerRMI] Match not in progress for player ID: " + playerId);
            client.showUpdate(new PickAdventureCardResponse(
                    playerId, null, false, "Match not in progress", -1));
            return;
        }

        // Only the leading player (position 1) can pick adventure cards
        int leadingPlayerId = match.getFirstPlayerToAct();
        if (playerId != leadingPlayerId) {
            System.out.println("[ServerRMI] Only the leading player can pick adventure cards. Player ID: " + playerId + " is not the leader (ID: " + leadingPlayerId + ")");
            client.showUpdate(new PickAdventureCardResponse(
                    playerId, null, false, "Only the leading player can pick adventure cards", -1));
            return;
        }

        GameController controller = match.getController();
        Player player = controller.getPlayerById(playerId);
        if (player == null) {
            System.out.println("[ServerRMI] Player not found with ID: " + playerId);
            client.showUpdate(new PickAdventureCardResponse(
                    playerId, null, false, "Player not found", -1));
            return;
        }
        // Pick adventure card using the controller - only for the leading player
        controller.pickCardForPlayer(player);
        AdventureCard adventureCard = player.getPickedCard();
        String message = adventureCard != null ? adventureCard.getName() : "No more adventure cards available";
        boolean success = adventureCard != null;

        // Apply the adventure card effect to the game state
        if (success && adventureCard != null) {
            System.out.println("[ServerRMI] Applying adventure card effect: " + adventureCard.getClass().getSimpleName());
            controller.applyAdventureCardEffect(adventureCard);
        }

        System.out.println("[ServerRMI] Adventure card pick by leading player " + playerId + ": " +
                (adventureCard != null ? adventureCard.getClass().getSimpleName() : "null"));

        // Check if there are more adventure cards or if the game is complete
        int nextPlayerTurn = -1; // Default: adventure phase continues
        
        if (!controller.hasMoreAdventureCards() || controller.isFlightComplete()) {
            // Adventure deck is empty or game is complete
            nextPlayerTurn = -2; // Special value indicating deck is empty
            
            // Send the pick response first
            lobbyManager.multicastToMatch(match.getMatchId(),
                    new PickAdventureCardResponse(
                            playerId, adventureCard, success, message, nextPlayerTurn));
            
            // Then send completion notification
            String completionReason;
            boolean gameComplete = controller.isFlightComplete();
            
            if (!controller.hasMoreAdventureCards()) {
                completionReason = "Adventure deck is empty - all cards have been resolved";
            } else {
                completionReason = "Only one active player remains";
            }
            
            lobbyManager.multicastToMatch(match.getMatchId(),
                    new AdventurePhaseCompleteResponse(
                            match.getMatchId(), gameComplete, completionReason));
            return;
        } else {
            // More cards available - find new leader after effects have been applied
            int newLeaderId = match.getFirstPlayerToAct();
            System.out.println("[ServerRMI] New leading player for next adventure card: " + newLeaderId);
        }

        // Send response to all clients in the match
        lobbyManager.multicastToMatch(match.getMatchId(),
                new PickAdventureCardResponse(
                        playerId, adventureCard, success, message, nextPlayerTurn));
    }

    @Override
    public void validateShip(int playerId, RmiClientIf client) throws RemoteException {
        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        if (match == null) {
            client.showUpdate(new ShipValidationResponse(playerId, false, "Match not found", false, "Ship validation failed"));
            return;
        }

        GameController controller = match.getController();
        Player player = controller.getPlayerById(playerId);
        if (player == null) {
            client.showUpdate(new ShipValidationResponse(playerId, false, "Player not found", false, "Ship validation failed"));
            return;
        }

        boolean isValid = controller.validateShipForPlayer(player);
        String detailedMessage;
        
        if (isValid) {
            detailedMessage = "Ship is valid and ready for flight!";
            match.getPlayersValidated().put(playerId, true);
            match.recordPlayerValidation(playerId);
            
            // Assign position based on completion order
            int assignedPosition = match.assignPlayerPosition(playerId);
            if (assignedPosition > 0) {
                // Notify all players about the position assignment
                String positionMessage = player.getNickName() + " finished building and got position " + assignedPosition;
                lobbyManager.multicastToMatch(match.getMatchId(), 
                    new PositionAssignmentResponse(
                        playerId, player.getNickName(), assignedPosition, true, positionMessage));
                
                // Check if all players have been positioned and start adventure phase
                if (match.allPlayersPositioned()) {
                    int firstPlayerToAct = match.getFirstPlayerToAct();
                    lobbyManager.multicastToMatch(match.getMatchId(),
                        new AdventurePhaseStartResponse(
                            match.getMatchId(), match.getPlayerPositions(), firstPlayerToAct));
                }
            }
            
        } else {
            String validationErrors = controller.getShipValidationErrors(player);
            detailedMessage = "Ship validation failed with the following issues:\n\n" + validationErrors;
            match.getPlayersValidated().put(playerId, false);
        }

        client.showUpdate(new ShipValidationResponse(playerId, isValid, detailedMessage, true, detailedMessage));
    }

    @Override
    public void readyForFlight(UUID matchId, int playerId, RmiClientIf client) throws RemoteException {
        GameMatch match = lobbyManager.findMatchById(matchId);
        if (match == null) {
            client.showUpdate(new PlayerReadyForFlightResponse(matchId, playerId, "", false, false, "Match not found"));
            return;
        }

        Player player = match.getPlayerById(playerId);
        if (player == null) {
            client.showUpdate(new PlayerReadyForFlightResponse(matchId, playerId, "", false, false, "Player not found"));
            return;
        }

        GameController controller = match.getController();

        boolean isShipValid = controller.validateShipForPlayer(player);
        
        if (!isShipValid) {
            String validationErrors = controller.getShipValidationErrors(player);
            client.showUpdate(new PlayerReadyForFlightResponse(matchId, playerId, player.getNickName(), false, false, "Ship validation failed:\n" + validationErrors));
            return;
        }

        match.setPlayerReadyForFlight(playerId, true);
        
        lobbyManager.multicastToMatch(matchId, new PlayerReadyForFlightResponse(matchId, playerId, player.getNickName(), true, true, "Player " + player.getNickName() + " is ready for flight"));
        
        if (match.allPlayersReadyForFlight()) {
            match.setMatchState(MatchStates.FLIGHT_PREPARATION);
            lobbyManager.multicastToMatch(matchId, new FlightPreparationStartResponse(matchId, true, "All players ready – flight preparation begins"));
        }
    }

}