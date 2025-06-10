package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class ValidateShipResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final boolean isValid;
    private final String message;
    private final int assignedPosition; // -1 if not assigned

    public ValidateShipResponse(int playerId, boolean isValid, String message, int assignedPosition) {
        this.playerId = playerId;
        this.isValid = isValid;
        this.message = message;
        this.assignedPosition = assignedPosition;
    }

    public int getPlayerId() {
        return playerId;
    }

    public boolean isValid() {
        return isValid;
    }

    public String getMessage() {
        return message;
    }

    public int getAssignedPosition() {
        return assignedPosition;
    }

    @Override
    public void handleClient(GameListener listener) {
        // Check if this response is for the current player (if possible)
        if (listener instanceof it.polimi.ingsw.view.tui.components.GalaxyTruckerTUI) {
            it.polimi.ingsw.view.tui.components.GalaxyTruckerTUI tui = 
                (it.polimi.ingsw.view.tui.components.GalaxyTruckerTUI) listener;
            
            // Check if this response is for the current player
            if (tui.getCurrentPlayer() != null && tui.getCurrentPlayer().getId() == playerId) {
                if (isValid && assignedPosition > 0) {
                    String positionMessage = message + " You have been assigned starting position: " + assignedPosition;
                    listener.shipValidated(isValid, positionMessage);
                } else {
                    listener.shipValidated(isValid, message);
                }
            }
            // If it's not for our player, ignore the response
        } else {
            // For non-TUI clients, handle as before
            listener.shipValidated(isValid, message);
        }
    }
} 