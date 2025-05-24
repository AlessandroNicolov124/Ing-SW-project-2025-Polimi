package it.polimi.ingsw.network.rmi.messages;


import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.client.listeners.GameListener;

public record RemoveComponentMessage(int playerId, Position position, boolean success) implements UpdateMessage {

    @Override
    public void update(GameListener listener, int localPlayerId) {
        if (playerId == localPlayerId) {
            if (success && listener instanceof GameListener gameListener) {
                gameListener.componentRemoved(position);
            }
            listener.displayRefresh();
        }
    }
}