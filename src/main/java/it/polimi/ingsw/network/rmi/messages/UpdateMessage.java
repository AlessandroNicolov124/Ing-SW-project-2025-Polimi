package it.polimi.ingsw.network.rmi.messages;

import java.io.Serializable;

import it.polimi.ingsw.network.client.listeners.GameListener;

public interface UpdateMessage extends Serializable {

    void update(GameListener listener, int localPlayerId);


}
