package it.polimi.ingsw.network.messages.lobby;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class AddPlayerResponse implements ResponseMessage {
    
    private final static long serialVersionUID = 1L;
    private final int playerId;
    private final String playerName;
    private final boolean success; 
    private final String errorMessage;
    

    public AddPlayerResponse(int playerId, String playerName, boolean success, String errorMessage) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    @Override
    public void handleClient(GameListener listener) {
        if (success) {
            listener.playerCreated(playerId, playerName);
        } else {
            listener.usernameRejected(errorMessage);
        }
    }    

}
