package it.polimi.ingsw.network.messages.shipbuilding;

import java.util.Map;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.ResponseMessage;

public class GetPlayerShipResponse implements ResponseMessage {
    private static final long serialVersionUID = 1L;
    private final String playerName;
    private final Map<Position, Component> ship;
    private final boolean success;
    private final String message;

    public GetPlayerShipResponse(String playerName, Map<Position, Component> ship, boolean success, String message) {
        this.playerName = playerName;
        this.ship = ship;
        this.success = success;
        this.message = message;
    }


    @Override
    public void handleClient(GameListener listener) {
        listener.getPlayerShipResponse(playerName, ship);
    }
    
}
