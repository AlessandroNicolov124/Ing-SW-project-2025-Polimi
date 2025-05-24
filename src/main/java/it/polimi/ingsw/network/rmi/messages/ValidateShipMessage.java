package it.polimi.ingsw.network.rmi.messages;


import it.polimi.ingsw.network.client.listeners.GameListener;

public record ValidateShipMessage(int playerId, boolean isValid, String validationMessage) implements UpdateMessage {

    @Override
    public void update(GameListener listener, int localPlayerId) {
        if (playerId == localPlayerId) {
            if (listener instanceof GameListener gameListener) {
                gameListener.shipValidated(isValid, validationMessage);
            }

        }
    }
}