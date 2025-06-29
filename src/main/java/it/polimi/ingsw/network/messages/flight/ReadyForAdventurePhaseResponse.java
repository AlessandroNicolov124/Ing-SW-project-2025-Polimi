package it.polimi.ingsw.network.messages.flight;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;
import java.util.UUID;

public class ReadyForAdventurePhaseResponse implements ResponseMessage {
    private static final long serialVersionUID = 1L;
    private final UUID matchId;
    private final int playerId;
    private final boolean success;
    private final String message;

    public ReadyForAdventurePhaseResponse(UUID matchId, int playerId, boolean success, String message) {
        this.matchId = matchId;
        this.playerId = playerId;
        this.success = success;
        this.message = message;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.playerReadyForAdventurePhase(matchId, playerId, success, message);
    }

}