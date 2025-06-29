package it.polimi.ingsw.network.messages.adventure;

import it.polimi.ingsw.network.messages.RequestMessage;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

/**
 * Request message sent when a player wants to select a planet to land on
 */
public class PlanetSelectionRequest implements RequestMessage {
    
    private static final long serialVersionUID = 1L;
    private final int playerId;
    private final int planetIndex; // Index of the chosen planet (-1 if player chooses not to land)
    
    public PlanetSelectionRequest(int playerId, int planetIndex) {
        this.playerId = playerId;
        this.planetIndex = planetIndex;
    }
    
    public int getPlayerId() {
        return playerId;
    }
    
    public int getPlanetIndex() {
        return planetIndex;
    }
    
    @Override
    public void handleServer(SocketClientHandler handler) {
        try {
            handler.selectPlanet(playerId, planetIndex);
        } catch (Exception e) {
            System.err.println("Error handling planet selection request: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public String toString() {
        return "PlanetSelectionRequest{" +
                "playerId=" + playerId +
                ", planetIndex=" + planetIndex +
                '}';
    }
} 