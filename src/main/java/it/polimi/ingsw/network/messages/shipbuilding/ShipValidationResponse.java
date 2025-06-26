package it.polimi.ingsw.network.messages.shipbuilding;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class ShipValidationResponse implements ResponseMessage {
    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final boolean isValid;
    private final String validationMessage;
    private final boolean success;
    private final String errorMessage;

    public ShipValidationResponse(int playerId, boolean isValid, String validationMessage, boolean success, String errorMessage) {
        this.playerId = playerId;
        this.isValid = isValid;
        this.validationMessage = validationMessage;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    @Override
    public void handleClient(GameListener listener) {
        if (success) {
            String messageToShow = isValid ? validationMessage : validationMessage;
            listener.shipValidated(isValid, messageToShow);
        } else {
            listener.shipValidationFailed(errorMessage);
        }
    }
}