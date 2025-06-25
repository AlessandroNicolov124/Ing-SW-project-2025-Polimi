package it.polimi.ingsw.network.messages.shipbuilding;


import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class ShipValidationResponse implements ResponseMessage {
    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final boolean isValid;
    private final String validationMessage;

    public ShipValidationResponse(int playerId, boolean isValid, String validationMessage) {
        this.playerId = playerId;
        this.isValid = isValid;
        this.validationMessage = validationMessage;
    }

    public int getPlayerId() {
        return playerId;
    }

    public boolean isValid() {
        return isValid;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.shipValidated(isValid, validationMessage);
    }
}