package it.polimi.ingsw.network.messages.adventure;

import it.polimi.ingsw.AdventureCards.Planets;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

import java.util.UUID;
import java.util.List;

/**
 * Response message sent when a Planets card is drawn and players need to choose planets
 */
public class PlanetChoiceRequest implements ResponseMessage {
    
    private static final long serialVersionUID = 1L;
    private final UUID matchId;
    private final List<Planets.Planet> availablePlanets;
    private final int flightDaysCost;
    private final int currentPlayerId; // Player who should choose next
    private final String currentPlayerName;
    
    public PlanetChoiceRequest(UUID matchId, List<Planets.Planet> availablePlanets, int flightDaysCost, int currentPlayerId, String currentPlayerName) {
        this.matchId = matchId;
        this.availablePlanets = availablePlanets;
        this.flightDaysCost = flightDaysCost;
        this.currentPlayerId = currentPlayerId;
        this.currentPlayerName = currentPlayerName;
    }
    
    public UUID getMatchId() {
        return matchId;
    }
    
    public List<Planets.Planet> getAvailablePlanets() {
        return availablePlanets;
    }
    
    public int getFlightDaysCost() {
        return flightDaysCost;
    }
    
    public int getCurrentPlayerId() {
        return currentPlayerId;
    }
    
    public String getCurrentPlayerName() {
        return currentPlayerName;
    }
    
    @Override
    public void handleClient(GameListener listener) {
        listener.planetChoiceRequested(matchId, availablePlanets, flightDaysCost, currentPlayerId, currentPlayerName);
    }
    
    @Override
    public String toString() {
        return "PlanetChoiceRequest{" +
                "matchId=" + matchId +
                ", availablePlanets=" + availablePlanets.size() +
                ", flightDaysCost=" + flightDaysCost +
                ", currentPlayerId=" + currentPlayerId +
                ", currentPlayerName='" + currentPlayerName + '\'' +
                '}';
    }
} 