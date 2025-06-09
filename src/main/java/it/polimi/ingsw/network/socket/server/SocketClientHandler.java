package it.polimi.ingsw.network.socket.server;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
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
import it.polimi.ingsw.network.messages.lobby.UsernameRejectedResponse;
import it.polimi.ingsw.network.messages.RotationType;
import it.polimi.ingsw.network.messages.loading.LoadingStatusResponse;
import it.polimi.ingsw.network.messages.loading.PlayerReadyResponse;
import it.polimi.ingsw.network.messages.loading.ShipBuildingStartResponse;
import it.polimi.ingsw.network.messages.shipbuilding.PlaceComponentResponse;
import it.polimi.ingsw.network.messages.shipbuilding.PickComponentResponse;
import it.polimi.ingsw.network.messages.shipbuilding.RemoveComponentResponse;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.UUID;

public class SocketClientHandler implements Runnable, GeneralClientIf {
    private final Socket socket;
    private final LobbyManager lobbyManager;
    private final SocketServer server;
    private final ObjectInputStream  in;
    private final ObjectOutputStream out;
    private int currentPlayerId = -1;

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


    @Override
    public void createPlayer(String username) throws Exception{
        int id = lobbyManager.generatePlayerId(this);

        if (id == -1) {
            showUpdate(new UsernameRejectedResponse("Failed to generate player ID"));
            return;
        }

        if (!lobbyManager.registerUsername(id, username)) {
            showUpdate(new UsernameRejectedResponse("Username already taken: " + username));
            return;
        }

        try {
            this.currentPlayerId = id;
            showUpdate(new AddPlayerResponse(id, username));
        } catch (Exception e) {
            showUpdate(new UsernameRejectedResponse("Error creating player: " + e.getMessage()));
        }
    }

    public void createMatch(String matchName, int playerId, String playerName) {
        GameMatch match = lobbyManager.createMatch(matchName, playerId, playerName);
        lobbyManager.addClientToMatch(match.getMatchId(), this);
        try {
            // showUpdate(new CreateMatchResponse(matchName, match.getMatchId(), playerName));
            lobbyManager.broadcastToAll(new CreateMatchResponse(matchName, match.getMatchId(), playerName, playerId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void joinMatch(UUID matchId, int playerId, String username) throws Exception {
        boolean success = lobbyManager.joinMatch(matchId, playerId, username);
        if (success) {
            lobbyManager.addClientToMatch(matchId, this);
            

            GameMatch match = lobbyManager.findMatchById(matchId);
            List<String> playerNames = match.getPlayers().stream().map(Player::getNickName).toList();
            int hostId = match.getHostPlayer().getId();
            showUpdate(new PlayersInMatchResponse(matchId, playerNames, hostId));

            System.out.println("[ServerSocket: " + Thread.currentThread().getName() + "] Player " + username + " joined match " + matchId);
            lobbyManager.multicastToMatchExceptOne(matchId, new JoinMatchResponse(matchId, username), this);
        }

    }

    @Override
    public void leaveMatch(UUID matchId, int playerId, String username) throws Exception {
        GameMatch match = lobbyManager.findMatchById(matchId);
        if (match == null) {
            System.out.println("[ServerSocket] Match not found: " + matchId);
            return;
        }

        if (!match.isPlayerInMatchById(playerId)) {
            System.out.println("[ServerSocket] Player " + username + " not in match: " + matchId);
            return;
        }

        boolean success = lobbyManager.leaveMatch(matchId, playerId);
        if (success) {
            System.out.println("[ServerSocket] Player " + username + " left match: " + matchId);
            lobbyManager.multicastToMatch(matchId, new LeaveMatchResponse(matchId, playerId, username));

            // Notify the client to update the host
            Player newHost = match.getHostPlayer();
            if (newHost != null) {
                lobbyManager.multicastToMatchExceptOne(matchId, new UpdateHostResponse(matchId, newHost.getId(), newHost.getNickName()), this);
            }
            else {
                System.out.println("[ServerSocket] No new host found for match: " + matchId);
            }
            lobbyManager.removeClientFromMatch(matchId, this);
            }
        return;
    }

    @Override
    public void getAvailableMatches() throws Exception {
        List<String> matches = lobbyManager.getAvailableMatches();
        try {
            showUpdate(new GetMatchesResponse(matches));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startMatch(UUID matchId, int playerId, String username) throws Exception {
        GameMatch match = lobbyManager.findMatchById(matchId);
        if (match == null) {
            System.out.println("[ServerSocket] Match not found: " + matchId);
            return;
        }

        if (!match.isPlayerInMatchById(playerId)) {
            System.out.println("[ServerSocket] Player ID " + playerId + " not in match: " + matchId);
            return;
        }

        boolean success = lobbyManager.startMatch(matchId, playerId);
        if (success) {
            System.out.println("[ServerSocket] Match started by player: " + username);
            lobbyManager.multicastToMatch(matchId, new StartMatchResponse(matchId, username));
        }
        return;
    }


    // remainder for // loadingAck and playerReady
    // things are already loaded in startMatch
    // so when the host sends us a loadingAck, we need to send back a message like loadingStatus (maybe changing names will help)
    // so that each of them in the match knows that the match is being started by the host.
    // in order to start all together we send first the loadingStatus message (server -> clients)
    // the clients will be in another state now. After That they (clients) send another message to say that they are ready
    // the server will track how many players are ready and if all of them are ready the server will 
    // answer back and the clients will start the match together.

    @Override
    public void loadingAck(UUID matchId) throws Exception {
        GameMatch match = lobbyManager.findMatchById(matchId);

        if (match == null) {
            System.out.println("[ServerSocket] Match not found: " + matchId);
            return;
        }

        lobbyManager.loadingAckResponse(matchId);
        System.out.println("[ServerSocket] Loading acknowledged for match: " + matchId);
        int readyPlayersMap = match.getReadyPlayersMap().size();
        int totalPlayers = match.getPlayers().size();
        lobbyManager.multicastToMatch(matchId, new LoadingStatusResponse(matchId, readyPlayersMap, totalPlayers));
        return;
    }

    @Override
    public void playerReady(UUID matchId, int playerId) throws Exception {
        GameMatch match = lobbyManager.findMatchById(matchId);
        if (match == null) {
            System.out.println("[ServerSocket] Match not found: " + matchId);
            return;
        }

        if (!match.isPlayerInMatchById(playerId)) {
            System.out.println("[ServerSocket] Player ID " + playerId + " not in match: " + matchId);
            return;
        }

        boolean isReady = match.setPlayerReady(playerId);
        if (!isReady) {
            System.out.println("[ServerSocket] Duplicate ready state for player ID " + playerId + " in match: " + matchId);
            return;
        }
        int playersReady = (int) match.getReadyPlayersMap().values().stream().filter(Boolean::booleanValue).count();
        int totalPlayers = match.getPlayers().size();
        lobbyManager.multicastToMatch(matchId, new PlayerReadyResponse(matchId, playersReady, totalPlayers));
        System.out.println("[ServerSocket] Player ID " + playerId + " is ready for match: " + matchId);
        if (playersReady == totalPlayers) {
            System.out.println("[ServerSocket] All players are ready for match: " + matchId);
            lobbyManager.multicastToMatch(matchId, new ShipBuildingStartResponse(matchId, playersReady));
        }
        return;
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
            
        }
        server.removeClient(this);
    }

    @Override
    public void placeComponent(Component component, Position position) throws Exception {
        if (currentPlayerId == -1) {
            System.out.println("[ServerSocket] No player ID set for client handler");
            showUpdate(new PlaceComponentResponse(
                -1, component, position, false, "No player ID set"));
            return;
        }

        GameMatch match = lobbyManager.findMatchByPlayerId(currentPlayerId);
        if (match == null) {
            System.out.println("[ServerSocket] Match not found for player ID: " + currentPlayerId);
            showUpdate(new PlaceComponentResponse(
                currentPlayerId, component, position, false, "Match not found"));
            return;
        }

        if (match.getMatchState() != MatchStates.IN_PROGRESS) {
            System.out.println("[ServerSocket] Match not in progress for player ID: " + currentPlayerId);
            showUpdate(new PlaceComponentResponse(
                currentPlayerId, component, position, false, "Match not in progress"));
            return;
        }

        GameController controller = match.getController();
        Player player = controller.getPlayerById(currentPlayerId);
        if (player == null) {
            System.out.println("[ServerSocket] Player not found with ID: " + currentPlayerId);
            showUpdate(new PlaceComponentResponse(
                currentPlayerId, component, position, false, "Player not found"));
            return;
        }

        boolean success = controller.addComponentForPlayer(player, component, position);
        String message = success ? "Component placed successfully" : "Failed to place component";
        
        System.out.println("[ServerSocket] Component placement for player " + currentPlayerId + ": " + success);
        
        // Send response back to the requesting client
        showUpdate(new PlaceComponentResponse(
            currentPlayerId, component, position, success, message));
        
        // Optionally broadcast to other players in the match
        // lobbyManager.multicastToMatchExceptOne(match.getMatchId(), 
        //     new PlaceComponentResponse(currentPlayerId, component, position, success, message), this);
    }

    @Override
    public void validateShip() throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateShip'");
    }

    @Override
    public void removeComponent(Position position) throws Exception {
        if (currentPlayerId == -1) {
            System.out.println("[ServerSocket] No player ID set for client handler");
            showUpdate(new RemoveComponentResponse(
                -1, position, false, "No player ID set"));
            return;
        }

        GameMatch match = lobbyManager.findMatchByPlayerId(currentPlayerId);
        if (match == null) {
            System.out.println("[ServerSocket] Match not found for player ID: " + currentPlayerId);
            showUpdate(new RemoveComponentResponse(
                currentPlayerId, position, false, "Match not found"));
            return;
        }

        if (match.getMatchState() != MatchStates.IN_PROGRESS) {
            System.out.println("[ServerSocket] Match not in progress for player ID: " + currentPlayerId);
            showUpdate(new RemoveComponentResponse(
                currentPlayerId, position, false, "Match not in progress"));
            return;
        }

        GameController controller = match.getController();
        Player player = controller.getPlayerById(currentPlayerId);
        if (player == null) {
            System.out.println("[ServerSocket] Player not found with ID: " + currentPlayerId);
            showUpdate(new RemoveComponentResponse(
                currentPlayerId, position, false, "Player not found"));
            return;
        }

        // Get the component at the specified position
        Component componentToRemove = player.getShip().getComponentAt(position);
        if (componentToRemove == null) {
            System.out.println("[ServerSocket] No component found at position " + position + " for player " + currentPlayerId);
            showUpdate(new RemoveComponentResponse(
                currentPlayerId, position, false, "No component found at position"));
            return;
        }

        boolean success = controller.removeComponentForPlayer(player, componentToRemove);
        String message;
        if (success) {
            message = "Component removed successfully";
        } else {
            // Provide more specific error messages
            if (componentToRemove instanceof it.polimi.ingsw.Components.Cabin cabin && cabin.isStartingCabin()) {
                message = "Cannot remove starting cabin - it's the ship's core";
            } else if (player.getShip().getComponents().size() == 1) {
                message = "Cannot remove the only component on the ship";
            } else {
                message = "Cannot remove component - would disconnect the ship";
            }
        }
        
        System.out.println("[ServerSocket] Component removal for player " + currentPlayerId + " at " + position + ": " + success);
        
        // Send response back to the requesting client
        showUpdate(new RemoveComponentResponse(
            currentPlayerId, position, success, message));
    }

    @Override
    public void rotateComponent(Position position, RotationType rotationType) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rotateComponent'");
    }

    @Override
    public void setLocalPlayer(Player player) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setLocalPlayer'");
    }

    @Override
    public Component pickComponentCard() throws Exception {
        if (currentPlayerId == -1) {
            System.out.println("[ServerSocket] No player ID set for client handler");
            showUpdate(new PickComponentResponse(
                -1, null, false, "No player ID set"));
            return null;
        }

        GameMatch match = lobbyManager.findMatchByPlayerId(currentPlayerId);
        if (match == null) {
            System.out.println("[ServerSocket] Match not found for player ID: " + currentPlayerId);
            showUpdate(new PickComponentResponse(
                currentPlayerId, null, false, "Match not found"));
            return null;
        }

        if (match.getMatchState() != MatchStates.IN_PROGRESS) {
            System.out.println("[ServerSocket] Match not in progress for player ID: " + currentPlayerId);
            showUpdate(new PickComponentResponse(
                currentPlayerId, null, false, "Match not in progress"));
            return null;
        }

        GameController controller = match.getController();
        Player player = controller.getPlayerById(currentPlayerId);
        if (player == null) {
            System.out.println("[ServerSocket] Player not found with ID: " + currentPlayerId);
            showUpdate(new PickComponentResponse(
                currentPlayerId, null, false, "Player not found"));
            return null;
        }

        Component component = controller.getCompFromDeck(player);
        String message = component != null ? "Component picked successfully" : "No more components available";
        boolean success = component != null;
        
        System.out.println("[ServerSocket] Component pick for player " + currentPlayerId + ": " + 
            (component != null ? component.getClass().getSimpleName() : "null"));
        
        // Send response back to the requesting client
        showUpdate(new PickComponentResponse(
            currentPlayerId, component, success, message));
        
        return component;
    }




}