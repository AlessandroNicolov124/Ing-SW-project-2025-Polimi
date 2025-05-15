package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.network.socket.client.SocketClient;
import java.util.Map;

/**
 * Message broadcast to all clients when all players have selected their positions.
 */
public class AllPositionsSelectedMessage implements ClientMessage {
    private Map<Integer, Integer> playerPositions; // Map of player IDs to positions

    public AllPositionsSelectedMessage(Map<Integer, Integer> playerPositions) {
        this.playerPositions = playerPositions;
    }

    @Override
    public void handleClient(SocketClient client) {
        System.out.println("[Client] All players have selected positions: " + playerPositions);
        
        // Notify the client to proceed to the game phase
        client.proceedToGamePhase(playerPositions);
    }

    public Map<Integer, Integer> getPlayerPositions() {
        return playerPositions;
    }
} 