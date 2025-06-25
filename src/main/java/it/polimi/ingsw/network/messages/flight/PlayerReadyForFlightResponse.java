package it.polimi.ingsw.network.messages.flight;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;
import java.util.UUID;

public class PlayerReadyForFlightResponse implements ResponseMessage {
    private static final long serialVersionUID = 1L;
    private final UUID matchId;
    private final int playerId;
    private final String playerName;
    private final boolean shipValid;

    public PlayerReadyForFlightResponse(UUID matchId, int playerId, String playerName, boolean shipValid) {
        this.matchId = matchId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.shipValid = shipValid;
    }

    // Getters
    public UUID getMatchId() { return matchId; }
    public int getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public boolean isShipValid() { return shipValid; }

    @Override
    public void handleClient(GameListener listener) {
        listener.playerReadyForFlight(matchId, playerId, playerName, shipValid);
    }
}