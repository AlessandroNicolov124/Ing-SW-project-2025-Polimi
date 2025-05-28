package it.polimi.ingsw.network.rmi.messages;

import it.polimi.ingsw.network.client.listeners.GameListener;
import java.util.List;

public class AvailablePositionsUpdateMessage implements UpdateMessage {
    private final List<Integer> availablePositions;

    public AvailablePositionsUpdateMessage(List<Integer> availablePositions) {
        this.availablePositions = availablePositions;
    }

    @Override
    public void update(GameListener listener, int localPlayerId) {
        // Update all clients with new available positions
        listener.showAvailablePositions(availablePositions);
    }

    @Override
    public String toString() {
        return "Available positions updated: " + availablePositions;
    }
} 