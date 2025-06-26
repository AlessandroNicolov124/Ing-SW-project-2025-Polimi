package it.polimi.ingsw.network.messages.shipbuilding;


import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class CabinColorSelectionResponse implements ResponseMessage {
    private static final long serialVersionUID = 1L;
    private final boolean success;
    private final String payload;

    public CabinColorSelectionResponse(boolean success, String payload) {
        this.success = success;
        this.payload = payload;
    }

    @Override
    public void handleClient(GameListener listener) {
        if (success) {
            listener.cabinColorSelected(payload);
        } else {
            listener.cabinColorSelectionFailed(payload);
        }
    }
}
