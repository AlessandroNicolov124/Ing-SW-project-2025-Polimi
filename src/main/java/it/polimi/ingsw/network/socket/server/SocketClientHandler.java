package it.polimi.ingsw.network.socket.server;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.Difficulty;
import it.polimi.ingsw.network.GameMatch;
import it.polimi.ingsw.network.LobbyManager;
import it.polimi.ingsw.network.MatchStates;
import it.polimi.ingsw.network.client.GeneralClientIf;
import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.messages.ResponseMessage;
import it.polimi.ingsw.network.messages.lobby.AddPlayerResponse;
import it.polimi.ingsw.network.messages.lobby.CreateMatchResponse;
import it.polimi.ingsw.network.messages.lobby.GetMatchesResponse;
import it.polimi.ingsw.network.messages.lobby.JoinMatchResponse;
import it.polimi.ingsw.network.messages.lobby.LeaveMatchResponse;
import it.polimi.ingsw.network.messages.lobby.PlayersInMatchResponse;
import it.polimi.ingsw.network.messages.lobby.StartMatchResponse;
import it.polimi.ingsw.network.messages.lobby.UpdateHostResponse;
import it.polimi.ingsw.network.messages.RotationType;
import it.polimi.ingsw.network.messages.flight.FlightPreparationStartResponse;
import it.polimi.ingsw.network.messages.flight.PlayerReadyForFlightResponse;
import it.polimi.ingsw.network.messages.loading.LoadingAckResponse;
import it.polimi.ingsw.network.messages.loading.LoadingStatusResponse;
import it.polimi.ingsw.network.messages.loading.PlayerReadyResponse;
import it.polimi.ingsw.network.messages.loading.ShipBuildingStartResponse;
import it.polimi.ingsw.network.messages.shipbuilding.PlaceComponentResponse;
import it.polimi.ingsw.network.messages.shipbuilding.CabinColorSelectionResponse;
import it.polimi.ingsw.network.messages.shipbuilding.PickComponentResponse;
import it.polimi.ingsw.network.messages.shipbuilding.RemoveComponentResponse;
import it.polimi.ingsw.network.messages.shipbuilding.RotateComponentResponse;
import it.polimi.ingsw.network.messages.shipbuilding.ShipValidationResponse;

import java.io.*;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

public class SocketClientHandler implements Runnable, GeneralClientIf {
    private final Socket socket;
    private final LobbyManager lobbyManager;
    private final SocketServer server;
    private final ObjectInputStream  in;
    private final ObjectOutputStream out;

    public SocketClientHandler(Socket s, LobbyManager lobbyManager, SocketServer srv) throws IOException {
        socket = s; 
        this.lobbyManager = lobbyManager; 
        server = srv;
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in  = new ObjectInputStream (socket.getInputStream());
    }

    public LobbyManager getLobbyManager() {
        return this.lobbyManager;
    }

    public void showUpdate(ResponseMessage responseMessage) throws Exception {
        synchronized (out) {
            out.writeObject(responseMessage);
            out.flush();
        }
    }

    // client -> server
    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                Object obj = in.readObject();
                if (obj instanceof RequestMessage requestMessage) {
                    System.out.println("[ServerSocket: " + Thread.currentThread().getName() + "] Received from " + socket.getRemoteSocketAddress() + ": " + requestMessage.getClass().getSimpleName());
                    requestMessage.handleServer(this); 
                }
            }
        } catch (Exception ignored) {
            System.out.println("[ServerSocket: " + Thread.currentThread().getName() + "] Client disconnected or error: " + socket.getRemoteSocketAddress() + " " + ignored.getMessage());
        } finally {
            close();
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
            
        }
        server.removeClient(this);
    }

    @Override
    public void createPlayer(String username) throws Exception{
        int id = lobbyManager.generatePlayerId(this);

        if (id == -1) {
            showUpdate(new AddPlayerResponse(id, username, false, "Failed to create player"));
            return;
        }

        if (!lobbyManager.registerUsername(id, username)) {
            showUpdate(new AddPlayerResponse(id, username, false, "Username already taken"));
            return;
        }

        try {
            showUpdate(new AddPlayerResponse(id, username, true, ""));
        } catch (Exception e) {
            showUpdate(new AddPlayerResponse(id, username, false, "Failed to send player creation response"));
        }
    }

    @Override
    public void createMatch(String matchName, int playerId, String username, Difficulty difficulty) {
        try {
            GameMatch match = lobbyManager.createMatch(matchName, playerId, username, difficulty);
            System.out.println("[ServerSocket] Match created: " + matchName + " with host " + username + " (Difficulty: " + difficulty + ")");

            lobbyManager.addClientToMatch(match.getMatchId(), this);
            
            showUpdate(new CreateMatchResponse(
                    matchName,
                    match.getMatchId(),
                    username,
                    playerId,
                    difficulty,
                    true,
                    "Match created successfully"
            ));
            
            List<String> matchList = lobbyManager.getAvailableMatches();
            if (!matchList.isEmpty()) {
                lobbyManager.broadcastToAllExceptOne(new GetMatchesResponse(matchList, true, ""), this);
            } else {
                showUpdate(new GetMatchesResponse(matchList, false, "No matches available"));
            }
        } catch (Exception e) {
            System.err.println("[ServerSocket] Error creating match: " + e.getMessage());
            try {
                showUpdate(new CreateMatchResponse(matchName, null, username, playerId, difficulty, false, "Failed to create match: " + e.getMessage()));
            } catch (Exception ignored) {}
        }

    }

    @Override
    public void joinMatch(UUID matchId, int playerId, String username) throws Exception {
        boolean success = lobbyManager.joinMatch(matchId, playerId, username);
        if (success) {
            lobbyManager.addClientToMatch(matchId, this);
            GameMatch match = lobbyManager.findMatchById(matchId);

            System.out.println("[ServerSocket] Player " + username + " joined match: " + matchId);
            
            lobbyManager.multicastToMatchExceptOne(matchId, new JoinMatchResponse(matchId, username, true, "Player joined successfully"), this);
            List<String> names = match.getPlayers().stream().map(Player::getNickName).toList();
            Player host = match.getHostPlayer();
            showUpdate(new PlayersInMatchResponse(matchId, names, host.getId(), true, "Players in match retrieved successfully"));
        } else {
            System.out.println("[ServerSocket] Player " + username + " failed to join match: " + matchId);
            showUpdate(new JoinMatchResponse(matchId, username, false, "Failed to join match"));
        }

    }

    @Override
    public void leaveMatch(UUID matchId, int playerId, String username) throws Exception {
        GameMatch match = lobbyManager.findMatchById(matchId);
        if (match == null) {
            System.out.println("[ServerSocket] Match not found: " + matchId);
            showUpdate(new LeaveMatchResponse(matchId, playerId, username, false, "Match not found"));
            return;
        }

        if (!match.isPlayerInMatchById(playerId)) {
            System.out.println("[ServerSocket] Player " + username + " not in match: " + matchId);
            showUpdate(new LeaveMatchResponse(matchId, playerId, username, false, "Player not in match"));
            return;
        }

        boolean success = lobbyManager.leaveMatch(matchId, playerId);
        
        if (success) {
            showUpdate(new LeaveMatchResponse(matchId, playerId, username, true, "Player left match successfully"));

            lobbyManager.removeClientFromMatch(matchId, this);

            GameMatch updatedMatch = lobbyManager.findMatchById(matchId);
            if (updatedMatch != null && !updatedMatch.isEmpty()) {
                try {
                    lobbyManager.multicastToMatch(matchId, new LeaveMatchResponse(matchId, playerId, username, true, "Player" + username + " left match successfully"));

                    Player newHost = updatedMatch.getHostPlayer();
                    if (newHost != null) {
                        lobbyManager.multicastToMatch(matchId, new UpdateHostResponse(matchId, newHost.getId(), newHost.getNickName(), true, "New host updated successfully"));
                    }
                } catch (Exception e) {
                    showUpdate(new LeaveMatchResponse(matchId, playerId, username, false, "Error updating match after player left: " + e.getMessage()));
                }
            } else {
                System.out.println("[ServerSocket] Match " + matchId + " was removed or is empty after player left");
                showUpdate(new LeaveMatchResponse(matchId, playerId, username, true, "Match was removed or is empty after player left"));
            }
        }
    }

    @Override
    public void getAvailableMatches() throws Exception {
        List<String> matches = lobbyManager.getAvailableMatches();
        if (matches.isEmpty()) {
            showUpdate(new GetMatchesResponse(matches, false, "No matches available"));
        } else {
            showUpdate(new GetMatchesResponse(matches, true, ""));
        }
    }

    @Override
    public void startMatch(UUID matchId, int playerId, String username) throws Exception {

        GameMatch match = lobbyManager.findMatchById(matchId);

        if (match == null || !match.isPlayerInMatchById(playerId)){
            showUpdate(new StartMatchResponse(matchId, username, false, "Match not found or player not in match"));
            return;
        }

        boolean success = lobbyManager.startMatch(matchId, playerId);
        if (success) {
            lobbyManager.multicastToMatch(matchId, new StartMatchResponse(matchId, username, true, "Match started successfully"));
        } else {
            showUpdate(new StartMatchResponse(matchId, username, false, "Failed to start match"));
        }

    }

    @Override
    public void loadingAck(UUID matchId) throws Exception {
        GameMatch match = lobbyManager.findMatchById(matchId);
        if (match == null) {
            showUpdate(new LoadingAckResponse(false, "Match not found"));
        } 

        lobbyManager.loadingAckResponse(matchId);
        
        int readyPlayersMap = match.getReadyPlayersMap().size();
        int totalPlayers = match.getPlayers().size();

        lobbyManager.multicastToMatch(matchId, new LoadingStatusResponse(matchId, readyPlayersMap, totalPlayers, true, "Loading acknowledged"));
        return;
    }

    @Override
    public void playerReady(UUID matchId, int playerId) throws Exception {
        GameMatch match = lobbyManager.findMatchById(matchId);

        if (match == null || !match.isPlayerInMatchById(playerId)) {
            showUpdate(new PlayerReadyResponse(matchId, 0, 0, false, "Match not found or player not in match"));
            return;
        }

        boolean ok = match.setPlayerReady(playerId);

        if (!ok) {
            showUpdate(new PlayerReadyResponse(matchId, 0, 0, false, "Player already ready or match not in loading state"));
            return;
        }

        int playersReady = (int) match.getReadyPlayersMap().values().stream().filter(Boolean::booleanValue).count();
        int totalPlayers = match.getPlayers().size();

        lobbyManager.multicastToMatch(matchId, new PlayerReadyResponse(matchId, playersReady, totalPlayers, true, "Player ready status updated"));

        if (playersReady == totalPlayers) {
            lobbyManager.multicastToMatch(matchId, new ShipBuildingStartResponse(matchId, playersReady, true, "All players ready, starting ship building phase"));
        }
        return;
    }

    @Override
    public void placeComponent(Component component, Position position, int playerId) throws Exception {
        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        if (match == null) {
            showUpdate(new PlaceComponentResponse(playerId, component, position, false, "Match not found"));
            return;
        }

        GameController controller = match.getController();
        Player player = controller.getPlayerById(playerId);

        if (player == null) {
            showUpdate(new PlaceComponentResponse(playerId, component, position, false, "Player not found"));
            return;
        }

        boolean ok = controller.addComponentForPlayer(player, component, position);

        if (!ok) {
            showUpdate(new PlaceComponentResponse(playerId, component, position, false, "Placement refused by game rules"));
            return;
        }

        showUpdate(new PlaceComponentResponse(playerId, component, position, ok, ok ? "Component placed" : "Invalid position"));
    }

    @Override
    public void validateShip(int playerId) throws Exception {
        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        if (match == null) {
            showUpdate(new ShipValidationResponse(playerId, false, "Match not found", false, "Ship validation failed"));
            return;
        }

        GameController controller = match.getController();
        Player player = controller.getPlayerById(playerId);
        if (player == null) {
            showUpdate(new ShipValidationResponse(playerId, false, "Player not found", false, "Ship validation failed"));
            return;
        }

        boolean isValid = controller.validateShipForPlayer(player);
        String detailedMessage;
        
        if (isValid) {
            detailedMessage = "Ship is valid and ready for flight!";
            match.getPlayersValidated().put(playerId, true);
            
        } else {
            String validationErrors = controller.getShipValidationErrors(player);
            detailedMessage = "Ship validation failed with the following issues:\n\n" + validationErrors;
            match.getPlayersValidated().put(playerId, false);
        }

        showUpdate(new ShipValidationResponse(playerId, isValid, detailedMessage, true, detailedMessage));
    }

    @Override
    public void removeComponent(Position position, int playerId) throws Exception {
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

        showUpdate(new RemoveComponentResponse(playerId, position, ok, ok ? "Component removed" : "Cannot remove component"));
        if (!ok) {
            showUpdate(new RemoveComponentResponse(playerId, position, false, "Cannot remove component at " + position));
        }
    }

    @Override
    public void rotateComponent(int playerId, Position position, RotationType rotationType) throws Exception {
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
                
                System.out.println("[ServerSocket] Rotated component at " + position + " " + rotationType + ": " + isValidRotation);
                showUpdate(new RotateComponentResponse(playerId, position, rotationType, isValidRotation, true, "Component rotated successfully"));
            }
        } else {
            System.out.println("[ServerSocket] Match not found or not in progress for player " + playerId);
            showUpdate(new RotateComponentResponse(playerId, position, rotationType, false ,false, "Match not found or not in progress"));
        }
    }

    @Override
    public void pickComponentCard(int playerId) throws Exception {
        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        
        if (match == null || match.getMatchState() != MatchStates.IN_PROGRESS) {
            showUpdate(new PickComponentResponse(playerId, null, false, "Match not found or not in progress"));
            return;
        }   

        GameController controller = match.getController();
        Player player = controller.getPlayerById(playerId);

        if (player == null) {
            showUpdate(new PickComponentResponse(playerId, null, false, "Player not found"));
            return;
        }

        Component comp = controller.getCompFromDeck(player);
        if (comp == null) {
            showUpdate(new PickComponentResponse(playerId, null, false, "No components available to pick"));
            return;
        }

        showUpdate(new PickComponentResponse(playerId, comp, true, "Component picked successfully"));
        
    }

    @Override
    public void selectCabinColor(String color, int playerId) throws Exception {
        GameMatch match = lobbyManager.findMatchByPlayerId(playerId);
        if (match == null) {
            showUpdate(new CabinColorSelectionResponse(false, "Match not found"));
            return;
        }
        String existing = match.getPlayerCabinColor(playerId);
        if (existing != null) {
            showUpdate(new CabinColorSelectionResponse(false, "Color already selected " + existing));
            return;
        }
        if (match.isColorTaken(color)) {
            showUpdate(new CabinColorSelectionResponse(false, "Color already taken by another player"));
            return;
        }
        match.setPlayerCabinColor(playerId, color);
        showUpdate(new CabinColorSelectionResponse(true, color));
    }

    @Override
    public void readyForFlight(UUID matchId, int playerId) throws Exception {
        GameMatch match = lobbyManager.findMatchById(matchId);
        if (match == null) {
            showUpdate(new PlayerReadyForFlightResponse(matchId, playerId, "", false, false, "Match not found"));
            return;
        }

        Player player = match.getPlayerById(playerId);
        if (player == null) {
            showUpdate(new PlayerReadyForFlightResponse(matchId, playerId, "", false, false, "Player not found"));
            return;
        }

        GameController controller = match.getController();

        boolean isShipValid = controller.validateShipForPlayer(player);
        
        if (!isShipValid) {
            String validationErrors = controller.getShipValidationErrors(player);
            showUpdate(new PlayerReadyForFlightResponse(matchId, playerId, player.getNickName(), false, false, "Ship validation failed:\n" + validationErrors));
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