package it.polimi.ingsw.network.messages.adventure;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

/**
 * Response message sent when a planet selection is processed
 */
public class PlanetSelectionResponse implements ResponseMessage {
    
    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final String playerName;
    private final int planetIndex; // Index of the chosen planet (-1 if player chose not to land)
    private final String planetName; // Name of the chosen planet (null if not landing)
    private final boolean success;
    private final String message;
    
    public PlanetSelectionResponse(int playerId, String playerName, int planetIndex, String planetName, boolean success, String message) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.planetIndex = planetIndex;
        this.planetName = planetName;
        this.success = success;
        this.message = message;
    }
    
    public int getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public int getPlanetIndex() {
        return planetIndex;
    }
    
    public String getPlanetName() {
        return planetName;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public void handleClient(GameListener listener) {
        listener.planetSelected(playerId, playerName, planetIndex, planetName, success, message);
    }
    
    @Override
    public String toString() {
        return "PlanetSelectionResponse{" +
                "playerId=" + playerId +
                ", playerName='" + playerName + '\'' +
                ", planetIndex=" + planetIndex +
                ", planetName='" + planetName + '\'' +
                ", success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
} 