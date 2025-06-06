package it.polimi.ingsw.network.messages.lobby;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class UsernameRejectedResponse implements ResponseMessage {
    
    private final static long serialVersionUID = 1L;
    private final String errorMessage;
    
    public UsernameRejectedResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.usernameRejected(errorMessage);
    }    
}