package it.polimi.ingsw.network.rmi.messages;

import java.io.Serializable;

import it.polimi.ingsw.network.client.listeners.ShipBuildingListener;

public interface UpdateMessage extends Serializable {

    void update(ShipBuildingListener listener, int localPlayerId);


}
