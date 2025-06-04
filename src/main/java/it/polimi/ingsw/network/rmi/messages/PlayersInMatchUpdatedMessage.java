package it.polimi.ingsw.network.rmi.messages;

import java.util.List;
import java.util.UUID;
import it.polimi.ingsw.network.client.listeners.GameListener;

public record PlayersInMatchUpdatedMessage(UUID matchId, List<String> players) implements UpdateMessage {
    
    @Override
    public void update(GameListener listener, int localPlayerId) {
        listener.updatePlayersInMatch(players);
    }
}