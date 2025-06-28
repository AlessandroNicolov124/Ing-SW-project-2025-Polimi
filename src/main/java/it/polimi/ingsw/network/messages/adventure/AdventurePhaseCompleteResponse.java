package it.polimi.ingsw.network.messages.adventure;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;
import java.util.UUID;

/**
 * Response sent when the adventure phase is complete (deck empty or game ends)
 */
public class AdventurePhaseCompleteResponse implements ResponseMessage {
    private static final long serialVersionUID = 1L;
    private final UUID matchId;
    private final boolean gameComplete;
    private final String completionReason;

    public AdventurePhaseCompleteResponse(UUID matchId, boolean gameComplete, String completionReason) {
        this.matchId = matchId;
        this.gameComplete = gameComplete;
        this.completionReason = completionReason;
    }

    public UUID getMatchId() {
        return matchId;
    }

    public boolean isGameComplete() {
        return gameComplete;
    }

    public String getCompletionReason() {
        return completionReason;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.adventurePhaseComplete(matchId, gameComplete, completionReason);
    }

    @Override
    public String toString() {
        return "AdventurePhaseCompleteResponse{" +
                "matchId=" + matchId +
                ", gameComplete=" + gameComplete +
                ", completionReason='" + completionReason + '\'' +
                '}';
    }
} 