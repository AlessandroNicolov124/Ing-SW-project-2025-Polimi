package it.polimi.ingsw.network.socket.client;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

import it.polimi.ingsw.TokenType;
import it.polimi.ingsw.network.Difficulty;
import it.polimi.ingsw.network.client.GeneralClientIf;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;
import it.polimi.ingsw.network.messages.RotationType;
import it.polimi.ingsw.network.messages.flight.ReadyForFlightRequest;
import it.polimi.ingsw.network.messages.loading.LoadingAckRequest;
import it.polimi.ingsw.network.messages.loading.PlayerReadyRequest;
import it.polimi.ingsw.network.messages.lobby.AddPlayerRequest;
import it.polimi.ingsw.network.messages.lobby.CreateMatchRequest;
import it.polimi.ingsw.network.messages.lobby.GetMatchesRequest;
import it.polimi.ingsw.network.messages.lobby.JoinMatchRequest;
import it.polimi.ingsw.network.messages.lobby.LeaveMatchRequest;
import it.polimi.ingsw.network.messages.lobby.StartMatchRequest;
import it.polimi.ingsw.network.messages.shipbuilding.PlaceComponentRequest;
import it.polimi.ingsw.network.messages.shipbuilding.AddTokenToComponentRequest;
import it.polimi.ingsw.network.messages.shipbuilding.CabinColorSelectionRequest;
import it.polimi.ingsw.network.messages.shipbuilding.PickComponentRequest;
import it.polimi.ingsw.network.messages.shipbuilding.RemoveComponentRequest;
import it.polimi.ingsw.network.messages.shipbuilding.RotateComponentRequest;
import it.polimi.ingsw.network.messages.shipbuilding.ValidateShipRequest;
import it.polimi.ingsw.network.messages.adventure.PickAdventureCardRequest;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Position;


public class SocketClient implements Runnable, GeneralClientIf {
    
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream  in;
    private GameListener listener;

    public SocketClient(String host, int port, GameListener gameListener) throws IOException {
        socket = new Socket(host,port);
        out = new ObjectOutputStream(socket.getOutputStream()); 
        out.flush();
        in  = new ObjectInputStream (socket.getInputStream());
        this.listener = gameListener;
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                Object obj = in.readObject();
                if (obj instanceof ResponseMessage responseMessage) {
                    showUpdate(responseMessage);
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void showUpdate(ResponseMessage responseMessage) {
        responseMessage.handleClient(listener);
    }

    @Override 
    public void close() throws Exception { 
        socket.close(); 
    }

    @Override
    public void createPlayer(String username) throws Exception {
        synchronized (out) {
            out.writeObject(new AddPlayerRequest(username));
            out.flush();
        }
    }

    @Override
    public void createMatch(String matchName, int playerId, String playerName, Difficulty difficulty) throws Exception {
        synchronized (out) {
            out.writeObject(new CreateMatchRequest(matchName, playerId, playerName, difficulty));
            out.flush();
        }
    }

    @Override
    public void getAvailableMatches() throws Exception {
        synchronized (out) {
            out.writeObject(new GetMatchesRequest());
            out.flush();

        }
    }

    @Override
    public void joinMatch(UUID matchId, int playerId, String username) throws Exception {
        synchronized (out) {
            out.writeObject(new JoinMatchRequest(matchId, playerId, username));
            out.flush();
        }
    }

    @Override
    public void leaveMatch(UUID matchId, int playerId, String username) throws Exception {
        synchronized (out) {
            out.writeObject(new LeaveMatchRequest(username, matchId, playerId));
            out.flush();
        }
    }

    @Override
    public void startMatch(UUID matchId, int playerId, String username) throws Exception {
        synchronized (out) {
            out.writeObject(new StartMatchRequest(matchId, playerId, username));
            out.flush();
        }
    }

    @Override
    public void loadingAck(UUID matchId) throws Exception {
        synchronized (out) {
            out.writeObject(new LoadingAckRequest(matchId));
            out.flush();
        }
    }

    @Override
    public void playerReady(UUID matchId, int playerId) throws Exception {
        synchronized (out) {
            out.writeObject(new PlayerReadyRequest(matchId, playerId));
            out.flush();
        }
    }

    @Override
    public void pickAdventureCard(int currentPlayer) throws Exception {
        synchronized (out) {
            out.writeObject(new PickAdventureCardRequest(currentPlayer));
            out.flush();
        }
    }

    @Override
    public void placeComponent(Component component, Position position, int playerId) throws Exception {
        if (playerId <= 0) {
            throw new IllegalArgumentException("Invalid player ID: " + playerId);
        }
        
        synchronized (out) {
            out.writeObject(new PlaceComponentRequest(playerId, component, position));
            out.flush();
        }
    }

    @Override
    public void validateShip(int playerId) throws Exception {
        if (playerId <= 0) {
            throw new IllegalArgumentException("Invalid player ID: " + playerId);
        }

        synchronized (out) {
            out.writeObject(new ValidateShipRequest(playerId));
            out.flush();
        }
    }

    @Override
    public void pickComponentCard(int playerId) throws Exception {
        if (playerId <= 0) {
            throw new IllegalArgumentException("Invalid player ID: " + playerId);
        }
        
        synchronized (out) {
            out.writeObject(new PickComponentRequest(playerId));
            out.flush();
        }
        
    }

    @Override
    public void removeComponent(Position position, int playerId) throws Exception {
        if (playerId <= 0) {
            throw new IllegalArgumentException("Invalid player ID: " + playerId);
        }
        
        synchronized (out) {
            out.writeObject(new RemoveComponentRequest(playerId, position));
            out.flush();
        }
    }

    public void rotateComponent(int playerId, Position position, RotationType rotationType) throws Exception {
        if (position == null || rotationType == null) {
            throw new IllegalArgumentException("Position and rotation type cannot be null");
        }
        
        synchronized (out) {
            out.writeObject(new RotateComponentRequest(playerId, position, rotationType));
            out.flush();
        }
    }

    @Override
    public void selectCabinColor(String color, int playerId) throws Exception {
        if (color == null || color.isEmpty()) {
            throw new IllegalArgumentException("Color cannot be null or empty");
        }
        
        synchronized (out) {
            out.writeObject(new CabinColorSelectionRequest(playerId, color));
            out.flush();
        }
    }

    @Override
    public void readyForFlight(UUID matchId, int playerId) throws Exception {
        if (matchId == null || playerId <= 0) {
            throw new IllegalArgumentException("Match ID cannot be null and player ID must be positive");
        }
        
        synchronized (out) {
            out.writeObject(new ReadyForFlightRequest(matchId, playerId));
            out.flush();
        }
    }

    @Override
    public void getPlayersOrderForFlightboard(UUID matchId, int playerId) throws Exception{
        synchronized (out) {
            out.writeObject(new ReadyForFlightRequest(matchId, playerId));
            out.flush();
        }
    }

    @Override
    public void addTokenToComponent(String playerName, Position position, TokenType tokenType) throws Exception {
        synchronized (out) {
            out.writeObject(new AddTokenToComponentRequest(playerName, position, tokenType));
            out.flush();
        }
    }

    @Override
    public void getPlayerTokenList(String playerName) throws Exception {
        synchronized (out) {
            out.writeObject(new GetPlayerTokenListRequest(playerName));
            out.flush();
        }
    }

    @Override
    public void removeTokenFromComponent(String playerName, Position position, TokenType tokenType) throws Exception {
        synchronized (out) {
            out.writeObject(new RemoveTokenFromComponentRequest(playerName, position, tokenType));
            out.flush();
        }
    }

    @Override
    public void getPlayerShip(String playerName) throws Exception {
        synchronized (out) {
            out.writeObject(new GetPlayerShipRequest(playerName));
            out.flush();
        }
    }

    @Override
    public void readyForAdventurePhase(UUID matchId, int playerId) throws Exception {
        synchronized (out) {
            out.writeObject(new ReadyForAdventurePhaseRequest(matchId, playerId));
            out.flush();
        }
    }

}