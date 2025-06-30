package it.polimi.ingsw.network.messages.lobby;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class PlayersInMatchResponse implements ResponseMessage {
    
    private static final long serialVersionUID = 1L;
    private final UUID matchId;
    private final List<String> playerNames;
    private final int hostId;
    private final Map<String, Integer> takenColors;
    private final boolean success;
    private final String errorMessage;

    public PlayersInMatchResponse(UUID matchId, List<String> playerNames, int hostId, Map<String, Integer> takenColors, boolean success, String errorMessage) {
        this.matchId = matchId;
        this.playerNames = playerNames;
        this.hostId = hostId;
        this.takenColors = takenColors;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    @Override
    public void handleClient(GameListener listener) {
        if (success) {
            listener.playersInMatch(matchId, playerNames, hostId);
            for (Map.Entry<String, Integer> entry : takenColors.entrySet()) {
                listener.cabinColorSelected(entry.getValue(), entry.getKey());
            }
        } else {
            listener.playersInMatchFailed(errorMessage);
        }
    }
    
}