package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.socket.client.SocketClient;

public class ComponentRemovedMessage implements ClientMessage {
    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final Position position;
    private final boolean success;
    private final String errorMessage; // Null if success is true

    public ComponentRemovedMessage(int playerId, Position position, boolean success, String errorMessage) {
        this.playerId = playerId;
        this.position = position;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    // Getters...
    public int getPlayerId() { return playerId; }
    public Position getPosition() { return position; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }

    @Override
    public void handleClient(SocketClient client) {
        if (success) {
            // Update local model if necessary
            // client.getLocalGameModel().getPlayer(playerId).getShip().removeComponentAt(position);
            client.showUpdate("Component at " + position + " removed successfully for player " + playerId);
        } else {
            client.showUpdate("Failed to remove component at " + position + " for player " + playerId + ": " + errorMessage);
        }
        // Trigger TUI refresh?
    }
} 