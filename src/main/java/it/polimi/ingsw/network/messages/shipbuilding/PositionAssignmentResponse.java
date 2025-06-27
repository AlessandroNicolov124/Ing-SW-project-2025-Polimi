package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class PositionAssignmentResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final String playerName;
    private final int assignedPosition;
    private final boolean success;
    private final String message;

    public PositionAssignmentResponse(int playerId, String playerName, int assignedPosition, boolean success, String message) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.assignedPosition = assignedPosition;
        this.success = success;
        this.message = message;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getAssignedPosition() {
        return assignedPosition;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.positionAssigned(playerId, playerName, assignedPosition, success, message);
    }
} 