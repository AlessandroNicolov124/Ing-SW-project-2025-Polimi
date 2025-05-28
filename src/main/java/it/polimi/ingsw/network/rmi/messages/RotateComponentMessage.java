package it.polimi.ingsw.network.rmi.messages;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.socket.messages.RotationType;

public record RotateComponentMessage(int playerId, Position position, RotationType rotationType, boolean success) implements UpdateMessage {

    @Override
    public void update(GameListener listener, int localPlayerId) {
        if (playerId == localPlayerId) {
            if (success && listener instanceof GameListener gameListener) {
                gameListener.componentRotated(position, rotationType);
            }
            listener.displayRefresh();
        }
    }
}
