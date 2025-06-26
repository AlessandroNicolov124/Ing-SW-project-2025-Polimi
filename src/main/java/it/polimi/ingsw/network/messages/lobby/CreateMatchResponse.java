package it.polimi.ingsw.network.messages.lobby;

import java.util.UUID;

import it.polimi.ingsw.network.Difficulty;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class CreateMatchResponse implements ResponseMessage {
    private static final long serialVersionUID = 1L;
    private final String matchName;
    private final UUID matchId;
    private final String playerName;
    private final int playerId;
    private final Difficulty difficulty;
    private final boolean success;
    private final String errorMessage;

    public CreateMatchResponse(String matchName, UUID matchId, String playerName, int playerId, Difficulty difficulty, boolean success, String errorMessage) {
        this.matchName = matchName;
        this.matchId = matchId;
        this.playerName = playerName;
        this.playerId = playerId;
        this.difficulty = difficulty;
        this.success = success;
        this.errorMessage = errorMessage;

    }

    @Override
    public void handleClient(GameListener listener) {
        if (success) {
            listener.matchCreated(matchId, matchName, playerName, playerId, difficulty);
        } else {
            listener.matchCreatedFailed(errorMessage);
        }
    }
}