package it.polimi.ingsw.network.socket.server;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.GameMatch;
import it.polimi.ingsw.network.LobbyManager;
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
import it.polimi.ingsw.network.socket.messages.RotationType;

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
    public void createPlayer(String username) {
        int id = lobbyManager.generatePlayerId(this);
        try {
            showUpdate(new AddPlayerResponse(id, username));
        } catch (Exception e) {
            e.printStackTrace();
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

    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
            
        }
        server.removeClient(this);
    }

    @Override
    public void placeComponent(Component component, Position position) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'placeComponent'");
    }

    @Override
    public void validateShip() throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateShip'");
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'pickComponentCard'");
    }


}