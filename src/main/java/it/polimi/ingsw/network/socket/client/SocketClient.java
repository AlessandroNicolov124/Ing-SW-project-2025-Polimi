package it.polimi.ingsw.network.socket.client;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import it.polimi.ingsw.network.client.GeneralClientIf;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;
import it.polimi.ingsw.network.messages.RotationType;
import it.polimi.ingsw.network.messages.loading.LoadingAckRequest;
import it.polimi.ingsw.network.messages.loading.PlayerReadyRequest;
import it.polimi.ingsw.network.messages.lobby.AddPlayerRequest;
import it.polimi.ingsw.network.messages.lobby.CreateMatchRequest;
import it.polimi.ingsw.network.messages.lobby.GetMatchesRequest;
import it.polimi.ingsw.network.messages.lobby.JoinMatchRequest;
import it.polimi.ingsw.network.messages.lobby.LeaveMatchRequest;
import it.polimi.ingsw.network.messages.lobby.StartMatchRequest;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.Player;


public class SocketClient implements Runnable, GeneralClientIf {
    
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream  in;
    private GameListener listener;
    private Player localPlayer;

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
    public void createMatch(String matchName, int playerId, String playerName) throws Exception {
        synchronized (out) {
            out.writeObject(new CreateMatchRequest(matchName, playerId, playerName));
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
    

/*     public Component pickComponentCard() throws IOException
    {
        Component c = null;
        if (playerId != -1) {
            c = proxy.pickCompForShip(playerId);
        }
        return c;
    }

    public void placeComponent(Component component, Position position) throws IOException {
        if (playerId == -1) {
            throw new IllegalStateException("Cannot place component before player ID is assigned.");
        }
        proxy.placeComponent(playerId, component, position);
    }

    public void removeComponent(Position position) throws IOException {
        if (playerId == -1) {
            throw new IllegalStateException("Cannot remove component before player ID is assigned.");
        }
        proxy.removeComponent(playerId, position);
    }

    public void rotateComponent(Position position, RotationType rotationType) throws IOException {
        if (playerId == -1) {
            throw new IllegalStateException("Cannot rotate component before player ID is assigned.");
        }
        proxy.rotateComponent(playerId, position, rotationType);
    }

    public void validateShip() throws IOException {
        if (playerId == -1) {
            throw new IllegalStateException("Cannot validate ship before player ID is assigned.");
        }
        proxy.validateShip(playerId);
    } */

 /*    // New method to request available positions
    public void requestAvailablePositions() throws IOException {
        if (playerId == -1) {
            throw new IllegalStateException("Cannot request positions before player ID is assigned.");
        }
        System.out.println("[Client] Requesting available positions from server");
        synchronized (out) {
            out.writeObject(new GetAvailablePositionsMessage(playerId));
            out.flush();
        }
    }
    
    // New method to select a position on the flight board
    public void selectPosition(int position) throws IOException {
        if (playerId == -1) {
            throw new IllegalStateException("Cannot select position before player ID is assigned.");
        }
        if (position < 1 || position > 4) {
            throw new IllegalArgumentException("Position must be between 1 and 4");
        }
        System.out.println("[Client] Sending position selection request: " + position);
        synchronized (out) {
            out.writeObject(new SelectPositionMessage(playerId, position));
            out.flush();
        }
    } */
    
    // // Called by PositionResponseMessage
    // public void notifyPositionResponse(int playerId, int position, boolean success, String message) {
    //     /*if (tui != null) {
    //         if (success) {
    //             tui.showMessage("Position " + position + " selected successfully");
    //         } else {
    //             tui.showMessage("Failed to select position " + position + ": " + message);
    //         }
    //         tui.triggerPositionSelectionRefresh();
    //     }*/
    //     if (success) {
    //         listener.printTextMsg("Position " + position + " selected successfully");
    //     } else {
    //         listener.printTextMsg("Failed to select position " + position + ": " + message);
    //     }

    // }
 
    
    // // Called by AvailablePositionsResponseMessage
    // public void showAvailablePositions(List<Integer> positions) {
    //         listener.showAvailablePositions(positions);
    // }
    
    // // Called by PositionUpdateMessage
    // public void updatePlayerPosition(int playerId, int position) {
    //         listener.notifyNewPlayerPosition(playerId, position);
    // }

    // /**b
    //  * Called by AllPositionsSelectedMessage when all players have selected their positions
    //  * 
    //  * @param playerPositions Map of player IDs to their selected positions
    //  */
    // public void proceedToGamePhase(Map<Integer, Integer> playerPositions) {
    //     System.out.println("[Client] All players have selected positions. Proceeding to game phase");

    //     // Display all player positions
    //     StringBuilder positionInfo = new StringBuilder("All players have selected positions:");
    //     for (Map.Entry<Integer, Integer> entry : playerPositions.entrySet()) {
    //         positionInfo.append("\nPlayer ").append(entry.getKey()).append(" at position ").append(entry.getValue());
    //     }
    //     listener.printTextMsg(positionInfo.toString());

    //     // Transition to game phase
    //     //TODO implement next state update on view
    //     //listener.proceedToRunningState();

    // }

    // public void handleLocalComponentPlacement(Component component, Position position, boolean success) {
    //     if (success && localPlayerReference != null) {
    //         localPlayerReference.getShip().placeComponent(component, position);
    //         if (listener != null) 
    //             listener.displayRefresh();
    //     }
    // }

    // public void handleLocalComponentRemoval(Position position, boolean success) {
    //     if (success && localPlayerReference != null) {
    //         Component compToRemove = localPlayerReference.getShip().getComponentAt(position);
    //         if (compToRemove != null) {
    //             localPlayerReference.getShip().removeComponent(compToRemove);
    //         }
    //         if (listener != null) 
    //             listener.displayRefresh();
    //     }
    // }

    // public void handleLocalComponentRotation(Position position, Component updatedComponentState, boolean success) {
    //     if (success && localPlayerReference != null && updatedComponentState != null) {
    //         Component oldComp = localPlayerReference.getShip().getComponentAt(position);
    //         if (oldComp != null) localPlayerReference.getShip().removeComponent(oldComp);
    //         localPlayerReference.getShip().placeComponent(updatedComponentState, position);
    //         if (listener != null) 
    //             listener.displayRefresh();
    //     }
    // }

    @Override
    public void placeComponent(Component component, Position position) throws Exception {
        if (localPlayer == null) {
            throw new IllegalStateException("Cannot place component: local player not set");
        }
        
        synchronized (out) {
            out.writeObject(new it.polimi.ingsw.network.messages.shipbuilding.PlaceComponentRequest(
                localPlayer.getId(), component, position));
            out.flush();
        }
    }

    @Override
    public void validateShip() throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateShip'");
    }

    @Override
    public Component pickComponentCard() throws Exception {
        if (localPlayer == null) {
            throw new IllegalStateException("Cannot pick component: local player not set");
        }
        
        synchronized (out) {
            out.writeObject(new it.polimi.ingsw.network.messages.shipbuilding.PickComponentRequest(
                localPlayer.getId()));
            out.flush();
        }
        
        // Note: This is asynchronous - the actual component will be returned via PickComponentResponse
        return null;
    }

    @Override
    public void removeComponent(Position position) throws Exception {
        if (localPlayer == null) {
            throw new IllegalStateException("Cannot remove component: local player not set");
        }
        
        synchronized (out) {
            out.writeObject(new it.polimi.ingsw.network.messages.shipbuilding.RemoveComponentRequest(
                localPlayer.getId(), position));
            out.flush();
        }
    }

    public void rotateComponent(Position position, RotationType rotationType) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rotateComponent'");
    }

    @Override
    public void setLocalPlayer(Player player) throws Exception {
        this.localPlayer = player;
    }

}