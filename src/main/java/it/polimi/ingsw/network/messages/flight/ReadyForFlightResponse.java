package it.polimi.ingsw.network.messages.flight;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class ReadyForFlightResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final boolean success;
    private final String message;

    public ReadyForFlightResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void handleClient(GameListener listener) {
        if (success) {
            listener.printTextMsg("pronti " + message);
        } else {
            listener.playerReadyForFlightFailed(message);
        }
    }
}