package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.socket.client.SocketClient;

public class ComponentPlacedMessage implements ClientMessage {
    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final Component component;
    private final Position position;
    private final boolean success;
    private final String errorMessage; // Null if success is true

    public ComponentPlacedMessage(int playerId, Component component, Position position, boolean success, String errorMessage) {
        this.playerId = playerId;
        this.component = component;
        this.position = position;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    // Getters for all fields...
    public int getPlayerId() { return playerId; }
    public Component getComponent() { return component; }
    public Position getPosition() { return position; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }

    @Override
    public void handleClient(SocketClient client) {
        if (success) {
            client.handleLocalComponentPlacement(component, position, success);
            client.showUpdate("Component " + component.getClass().getSimpleName() + " placed successfully at " + position + " for player " + playerId);
        } else {
            client.showUpdate("Failed to place component for player " + playerId + ": " + errorMessage);
        }
        // Potentially trigger a TUI refresh here
        // client.getTui().refreshDisplay();
    }
} 