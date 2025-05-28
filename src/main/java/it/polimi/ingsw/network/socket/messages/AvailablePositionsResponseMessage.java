package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.network.socket.client.SocketClient;
import java.util.List;

/**
 * Response message containing available positions on the flight board.
 */
public class AvailablePositionsResponseMessage implements ClientMessage {
    private List<Integer> availablePositions;

    public AvailablePositionsResponseMessage(List<Integer> availablePositions) {
        this.availablePositions = availablePositions;
    }

    @Override
    public void handleClient(SocketClient client) {
        // Log the available positions
        System.out.println("[Client] Available positions: " + availablePositions);
        
        // Update the client's view with available positions
        client.showAvailablePositions(availablePositions);
    }

    public List<Integer> getAvailablePositions() {
        return availablePositions;
    }
} 