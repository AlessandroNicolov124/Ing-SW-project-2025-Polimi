package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.network.socket.client.SocketClient;

public class ShipValidationResultMessage implements ClientMessage {
    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final boolean isValid;
    private final String reason;

    public ShipValidationResultMessage(int playerId, boolean isValid, String reason) {
        this.playerId = playerId;
        this.isValid = isValid;
        this.reason = reason;
    }

    // Getters...
    public int getPlayerId() { return playerId; }
    public boolean isValid() { return isValid; }
    public String getReason() { return reason; }

    @Override
    public void handleClient(SocketClient client) {
        // The client needs to react to this. For now, just print.
        // The ShipBuildingHandler might need a way to get this result to proceed.
        if (isValid) {
            client.showUpdate("Server: Ship for player " + playerId + " is VALID. " + reason);
        } else {
            client.showUpdate("Server: Ship for player " + playerId + " is INVALID. Reason: " + reason);
        }
        // This message should probably update a flag in the TUI or allow it to proceed.
        // client.getShipBuildingHandler().processValidationResult(isValid, reason);
    }
} 