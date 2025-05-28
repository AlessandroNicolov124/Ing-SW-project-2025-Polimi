package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component; // Server will send back the (potentially) new state of the component
import it.polimi.ingsw.network.socket.client.SocketClient;

public class ComponentRotatedMessage implements ClientMessage {
    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final Position position; // Original position
    private final Component rotatedComponentData; // The component in its new state, or null if rotation failed badly
    private final boolean success;
    private final String errorMessage;

    public ComponentRotatedMessage(int playerId, Position position, Component rotatedComponentData, boolean success, String errorMessage) {
        this.playerId = playerId;
        this.position = position;
        this.rotatedComponentData = rotatedComponentData;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    // Getters...
    public int getPlayerId() { return playerId; }
    public Position getPosition() { return position; }
    public Component getRotatedComponentData() { return rotatedComponentData; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }

    @Override
    public void handleClient(SocketClient client) {
        if (success && rotatedComponentData != null) {
            client.handleLocalComponentRotation(position, rotatedComponentData, success);
            client.showUpdate("Component at " + position + " rotated successfully for player " + playerId);
        } else {
            client.showUpdate("Failed to rotate component at " + position + " for player " + playerId + ": " + (errorMessage != null ? errorMessage : "Unknown error"));
        }
        // Trigger TUI refresh?
    }
} 