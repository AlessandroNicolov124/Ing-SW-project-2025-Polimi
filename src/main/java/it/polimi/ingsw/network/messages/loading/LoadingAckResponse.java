package it.polimi.ingsw.network.messages.loading;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class LoadingAckResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String errorMessage;

    public LoadingAckResponse(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.loadingAckResponseFailed(errorMessage);
    }    
    
}
