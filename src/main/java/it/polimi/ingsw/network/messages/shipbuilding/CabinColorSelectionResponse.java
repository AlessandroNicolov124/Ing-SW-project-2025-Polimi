package it.polimi.ingsw.network.messages.shipbuilding;


import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class CabinColorSelectionResponse implements ResponseMessage {
    private static final long serialVersionUID = 1L;
    private final String color;

    public CabinColorSelectionResponse(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.cabinColorSelected(color);
    }
}
