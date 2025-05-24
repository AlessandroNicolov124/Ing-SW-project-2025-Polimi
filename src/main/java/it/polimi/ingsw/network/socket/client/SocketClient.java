package it.polimi.ingsw.network.socket.client;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import it.polimi.ingsw.network.client.GeneralClientIf;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.socket.messages.ClientMessage;
import it.polimi.ingsw.network.socket.messages.GenericMessage;
import it.polimi.ingsw.network.socket.messages.GetAvailablePositionsMessage;
import it.polimi.ingsw.network.socket.messages.SelectPositionMessage;
import it.polimi.ingsw.network.socket.messages.TextUpdateMessage;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.socket.messages.RotationType;
import it.polimi.ingsw.Player;


public class SocketClient implements Runnable, GeneralClientIf {
    
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream  in;
    private final VirtualSocketServer proxy;
    private final BlockingDeque<GenericMessage> messages = new LinkedBlockingDeque<>();
    private int playerId = -1;
    private Player localPlayerReference;
    private GameListener listener;

    public SocketClient(String host, int port, GameListener gameListener) throws IOException {
        socket = new Socket(host,port);
        out = new ObjectOutputStream(socket.getOutputStream()); 
        out.flush();
        in  = new ObjectInputStream (socket.getInputStream());
        proxy = new VirtualSocketServer(out, messages);
        this.listener = gameListener;
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                Object obj = in.readObject();
                if (obj instanceof ClientMessage cm) {
                    cm.handleClient(this);
                }
                if (obj instanceof GenericMessage gm) {
                    if (!(gm instanceof TextUpdateMessage)){
                        messages.offerLast(gm);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void showUpdate(String text) {
        System.out.println("[Server] " + text);
    }

    @Override 
    public void close() throws Exception { 
        socket.close(); 
    }


    public int addPlayer(String username) throws IOException {
        return proxy.sendSrvAddPlayerReqAndWaitResp(username);
    }

    public Component pickComponentCard() throws IOException
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
    }

    // New method to request available positions
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
    }
    
    // Called by PositionResponseMessage
    public void notifyPositionResponse(int playerId, int position, boolean success, String message) {
        /*if (tui != null) {
            if (success) {
                tui.showMessage("Position " + position + " selected successfully");
            } else {
                tui.showMessage("Failed to select position " + position + ": " + message);
            }
            tui.triggerPositionSelectionRefresh();
        }*/
        if (success) {
            listener.printTextMsg("Position " + position + " selected successfully");
        } else {
            listener.printTextMsg("Failed to select position " + position + ": " + message);
        }

    }
    
    // Called by AvailablePositionsResponseMessage
    public void showAvailablePositions(List<Integer> positions) {
            listener.showAvailablePositions(positions);
    }
    
    // Called by PositionUpdateMessage
    public void updatePlayerPosition(int playerId, int position) {
            listener.notifyNewPlayerPosition(playerId, position);
    }

    /**
     * Called by AllPositionsSelectedMessage when all players have selected their positions
     * 
     * @param playerPositions Map of player IDs to their selected positions
     */
    public void proceedToGamePhase(Map<Integer, Integer> playerPositions) {
        System.out.println("[Client] All players have selected positions. Proceeding to game phase");

        // Display all player positions
        StringBuilder positionInfo = new StringBuilder("All players have selected positions:");
        for (Map.Entry<Integer, Integer> entry : playerPositions.entrySet()) {
            positionInfo.append("\nPlayer ").append(entry.getKey()).append(" at position ").append(entry.getValue());
        }
        listener.printTextMsg(positionInfo.toString());

        // Transition to game phase
        //TODO implement next state update on view
        //listener.proceedToRunningState();

    }

    public void setPlayerId(int id) {
        this.playerId = id;
    }

    public void setLocalPlayer(Player player) {
        this.localPlayerReference = player;
    }

    public void handleLocalComponentPlacement(Component component, Position position, boolean success) {
        if (success && localPlayerReference != null) {
            localPlayerReference.getShip().placeComponent(component, position);
            if (listener != null) 
                listener.displayRefresh();
        }
    }

    public void handleLocalComponentRemoval(Position position, boolean success) {
        if (success && localPlayerReference != null) {
            Component compToRemove = localPlayerReference.getShip().getComponentAt(position);
            if (compToRemove != null) {
                localPlayerReference.getShip().removeComponent(compToRemove);
            }
            if (listener != null) 
                listener.displayRefresh();
        }
    }

    public void handleLocalComponentRotation(Position position, Component updatedComponentState, boolean success) {
        if (success && localPlayerReference != null && updatedComponentState != null) {
            Component oldComp = localPlayerReference.getShip().getComponentAt(position);
            if (oldComp != null) localPlayerReference.getShip().removeComponent(oldComp);
            localPlayerReference.getShip().placeComponent(updatedComponentState, position);
            if (listener != null) 
                listener.displayRefresh();
        }
    }
}