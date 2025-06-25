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

    public CreateMatchResponse(String matchName, UUID matchId, String playerName, int playerId, Difficulty difficulty) {
        this.matchName = matchName;
        this.matchId = matchId;
        this.playerName = playerName;
        this.playerId = playerId;
        this.difficulty = difficulty;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.matchCreated(matchId, matchName, playerName, playerId, difficulty);
    }
}