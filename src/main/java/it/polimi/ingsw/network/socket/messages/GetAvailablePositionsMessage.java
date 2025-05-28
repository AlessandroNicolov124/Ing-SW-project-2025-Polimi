package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.network.socket.server.SocketClientHandler;
import java.util.List;

/**
 * Message sent from client to server to request available positions on the flight board.
 */
public class GetAvailablePositionsMessage implements ServerMessage {
    private int playerId;

    public GetAvailablePositionsMessage(int playerId) {
        this.playerId = playerId;
    }

    @Override
    public void handleServer(SocketClientHandler handler) {
        System.out.println("[Server] Player " + playerId + " requested available positions");
        
        // Get available positions from the controller
        List<Integer> availablePositions = handler.getController().getAvailableStartingPositions();
        
        // Log available positions
        System.out.println("[Server] Available positions: " + availablePositions);
        
        // Send response to the client
        handler.sendUpdate(new AvailablePositionsResponseMessage(availablePositions));
    }

    public int getPlayerId() {
        return playerId;
    }
} 