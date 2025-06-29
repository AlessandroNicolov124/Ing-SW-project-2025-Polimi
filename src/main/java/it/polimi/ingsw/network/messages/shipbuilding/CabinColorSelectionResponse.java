package it.polimi.ingsw.network.messages.shipbuilding;


import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class CabinColorSelectionResponse implements ResponseMessage {
    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final boolean success;
    private final String message;
    private final String color;

    public CabinColorSelectionResponse(int playerId, boolean success, String message, String color) {
        this.playerId = playerId;
        this.success = success;
        this.message = message;
        this.color = color;
    }

    @Override
    public void handleClient(GameListener listener) {
        if (success) {
            listener.cabinColorSelected(playerId, color);
        } else {
            listener.cabinColorSelectionFailed(message);
        }
    }
}
