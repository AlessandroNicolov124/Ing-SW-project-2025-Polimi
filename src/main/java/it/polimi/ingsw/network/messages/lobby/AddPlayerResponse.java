package it.polimi.ingsw.network.messages.lobby;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class AddPlayerResponse implements ResponseMessage {
    
    private final static long serialVersionUID = 1L;
    private final int playerId;
    private final String playerName;

    public AddPlayerResponse(int playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getUsername() {
        return playerName;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.playerCreated(playerId, playerName);
        listener.printTextMsg("Welcome " + playerName + " (id = " + playerId + ")");
    }    

}
