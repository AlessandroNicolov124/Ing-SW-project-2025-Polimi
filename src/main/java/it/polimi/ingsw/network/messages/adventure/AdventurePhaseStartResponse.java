package it.polimi.ingsw.network.messages.adventure;

import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

import java.util.Map;
import java.util.UUID;

public class AdventurePhaseStartResponse implements ResponseMessage {

    private static final long serialVersionUID = 1L;
    private final UUID matchId;
    private final Map<Integer, Integer> playerPositions; // Player ID to position mapping
    private final int firstPlayerToAct; // Player ID of the player in position 1

    public AdventurePhaseStartResponse(UUID matchId, Map<Integer, Integer> playerPositions, int firstPlayerToAct) {
        this.matchId = matchId;
        this.playerPositions = playerPositions;
        this.firstPlayerToAct = firstPlayerToAct;
    }

    public UUID getMatchId() {
        return matchId;
    }

    public Map<Integer, Integer> getPlayerPositions() {
        return playerPositions;
    }

    public int getFirstPlayerToAct() {
        return firstPlayerToAct;
    }

    @Override
    public void handleClient(GameListener listener) {
        listener.adventurePhaseStarted(matchId, playerPositions, firstPlayerToAct);
    }
} 