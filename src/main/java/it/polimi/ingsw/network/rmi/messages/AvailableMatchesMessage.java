package it.polimi.ingsw.network.rmi.messages;

import java.util.List;

import it.polimi.ingsw.network.client.listeners.GameListener;

public record AvailableMatchesMessage(List<String> matches) implements UpdateMessage {

    @Override
    public void update(GameListener listener, int localPlayerId) {
        listener.updateAvailableMatches(matches);
    }

}
