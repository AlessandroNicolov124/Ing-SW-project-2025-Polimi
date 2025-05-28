package it.polimi.ingsw.network.rmi.messages;

import it.polimi.ingsw.network.client.listeners.GameListener;

public class PositionUpdateMessage implements UpdateMessage {
    private final int playerId;
    private final String playerName;
    private final int position;

    public PositionUpdateMessage(int playerId, String playerName, int position) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.position = position;
    }

    @Override
    public void update(GameListener listener, int localPlayerId) {
        // Only update other players, not the local player who made the change
        if (playerId != localPlayerId) {
            listener.notifyNewPlayerPosition(playerId, position);
        }
    }

    @Override
    public String toString() {
        return "Player " + playerName + " (ID: " + playerId + ") selected position " + position;
    }
} 